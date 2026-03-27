package org.threatwatch.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.models.ParsedCveModel;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
public class BatchJobService {

    private final EmailService emailService;
    private final SettingsService settingsService;
    private final ProductsService productsService;
    private final NvdRestService nvdRestService;
    private final CveParserService cveParserService;
    private final CveStateService cveStateService;

    @Value("${email.cve.description.product.length.lookup}")
    private int descriptionProductLookupLength;

    public BatchJobService(EmailService emailService, SettingsService settingsService, ProductsService productsService, NvdRestService nvdRestService, CveParserService cveParserService, CveStateService cveStateService) {
        this.emailService = emailService;
        this.settingsService = settingsService;
        this.productsService = productsService;
        this.nvdRestService = nvdRestService;
        this.cveParserService = cveParserService;
        this.cveStateService = cveStateService;
    }

    private boolean descriptionMatchesProduct(String description, String product) {
        String firstPart = description.length() > descriptionProductLookupLength ? description.substring(0, descriptionProductLookupLength) : description;
        return firstPart.toLowerCase().contains(product.toLowerCase());
    }

    public void executeScheduledRun() throws Exception {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .withZone(ZoneOffset.UTC);

        SettingsResponseDto settings = settingsService.retrieveSettings();

        int lookbackWindowInteger = Integer.parseInt(settings.getLookbackWindow());
        Instant rawPublishEndDatetime = Instant.now();
        Instant rawPublishStartDatetime = rawPublishEndDatetime.minusSeconds(lookbackWindowInteger);

        String publishEndDatetime = dateTimeFormatter.format(rawPublishEndDatetime);
        String publishStartDatetime = dateTimeFormatter.format(rawPublishStartDatetime);
        Set<String> products = settingsService.retrieveSettings().getProductsSelected();

        StringBuilder cveListHtml = new StringBuilder();
        String html = emailService.loadHtmlTemplate();
        Set<String> emails = settings.getEmails();
        Set<String> cveIdsToSend = new HashSet<>();

        for (String product : products) {
            Thread.sleep(6000);
            JsonNode response = nvdRestService.getRecentVulnerabilitiesByProduct(product.toLowerCase(), publishStartDatetime, publishEndDatetime);
            JsonNode vulnerabilities = response.path("vulnerabilities");

            for (JsonNode vulnerability : vulnerabilities) {
                JsonNode cve = vulnerability.path("cve");

                ParsedCveModel parsedCve = cveParserService.parseCve(cve);
                String cveId = parsedCve.getCveId();
                String description = parsedCve.getDescription();

                boolean cveAlreadyPresent = cveIdsToSend.contains(cveId);
                boolean isPastCve = !cveStateService.isNewCve(cveId);
                boolean outsideSeverityThreshold = Float.parseFloat(parsedCve.getScore()) < Float.parseFloat(settings.getSeverityThreshold());
                boolean earlyCve = Objects.equals(parsedCve.getScore(), "-1");
                boolean earlyAlertsEnabled = Boolean.parseBoolean(settingsService.retrieveSettings().getEarlyAlerts());

                if (!descriptionMatchesProduct(description, product) || cveAlreadyPresent || isPastCve || (outsideSeverityThreshold && !earlyCve) || (earlyCve && !earlyAlertsEnabled)) {
                    continue;
                }

                cveIdsToSend.add(cveId);

                String cveHtml = emailService.buildCveHtml(product, parsedCve);
                cveListHtml.append(cveHtml);
            }
        }

        if (!cveIdsToSend.isEmpty()) {
            html = html.replace("{{cveList}}", cveListHtml.toString());
            emailService.sendHtmlEmail(emails, "New Vulnerabilities Report", html);

            for (String cveId : cveIdsToSend) {
                cveStateService.markCveAsSeen(cveId);
            }
        }

        System.out.println("Executed scheduled run @" + Instant.now().toString());
    }
}
