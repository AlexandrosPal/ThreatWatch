package org.threatwatch.services;

import jakarta.mail.MessagingException;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.LogEvents;
import org.threatwatch.models.CveAlertItem;
import org.threatwatch.models.ParsedCveModel;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BatchJobService {

    private final EmailService emailService;
    private final SettingsService settingsService;
    private final NvdRestService nvdRestService;
    private final CveParserService cveParserService;
    private final CveStateService cveStateService;

    private static final AppLogger appLogger = new AppLogger(LoggerFactory.getLogger(BatchJobService.class));

    @Value("${email.cve.description.product.length.lookup}")
    private int descriptionProductLookupLength;

    @Value("${backend.nvd.requests.interval}")
    private int nvdReqeustsInterval;

    public BatchJobService(EmailService emailService, SettingsService settingsService, NvdRestService nvdRestService, CveParserService cveParserService, CveStateService cveStateService) {
        this.emailService = emailService;
        this.settingsService = settingsService;
        this.nvdRestService = nvdRestService;
        this.cveParserService = cveParserService;
        this.cveStateService = cveStateService;
    }

    private boolean descriptionMatchesProduct(String description, String product) {
        String firstPart = description.length() > descriptionProductLookupLength ? description.substring(0, descriptionProductLookupLength) : description;
        return firstPart.toLowerCase().contains(product.toLowerCase());
    }

    private boolean referencesMatchProduct(List<String> references, String product) {
        return references.stream()
                .anyMatch(url -> url.toLowerCase().contains(product.toLowerCase()));
    }

    public void executeScheduledRun() throws IOException, InterruptedException, MessagingException {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .withZone(ZoneOffset.UTC);

        SettingsResponseDto settings = settingsService.retrieveSettings();

        int lookbackWindowInteger = Integer.parseInt(settings.getLookbackWindow());
        Instant rawPublishEndDatetime = Instant.now();
        Instant rawPublishStartDatetime = rawPublishEndDatetime.minusSeconds(lookbackWindowInteger);

        String publishEndDatetime = dateTimeFormatter.format(rawPublishEndDatetime);
        String publishStartDatetime = dateTimeFormatter.format(rawPublishStartDatetime);
        Set<String> products = settings.getProductsSelected();

        StringBuilder cveListHtml = new StringBuilder();
        String html = emailService.loadHtmlTemplate();
        Set<String> emails = settings.getEmails();
        Set<String> cveIdsToSend = new HashSet<>();
        List<CveAlertItem> cvesToSend = new ArrayList<>();

        for (String product : products) {
            Thread.sleep(nvdReqeustsInterval);
            JsonNode response = nvdRestService.getRecentVulnerabilitiesByProduct(product.toLowerCase(), publishStartDatetime, publishEndDatetime);
            JsonNode vulnerabilities = response.path("vulnerabilities");

            int validCveCounter = 0;

            for (JsonNode vulnerability : vulnerabilities) {
                JsonNode cve = vulnerability.path("cve");

                ParsedCveModel parsedCve = cveParserService.parseCve(cve);
                String cveId = parsedCve.getCveId();
                String description = parsedCve.getDescription();
                List<String> references = parsedCve.getReferences();

                boolean cveAlreadyPresent = cveIdsToSend.contains(cveId);
                boolean isPastCve = !cveStateService.isNewCve(cveId);
                boolean earlyCve = Objects.equals(String.valueOf(parsedCve.getScore()), "-1");
                boolean outsideSeverityThreshold = !earlyCve && Float.parseFloat(parsedCve.getScore()) < Float.parseFloat(settings.getSeverityThreshold());
                boolean earlyAlertsEnabled = Boolean.parseBoolean(settings.getEarlyAlerts());

                if ((!descriptionMatchesProduct(description, product) || !referencesMatchProduct(references, product)) || cveAlreadyPresent || isPastCve || (outsideSeverityThreshold && !earlyCve) || (earlyCve && !earlyAlertsEnabled)) {
                    continue;
                }

                validCveCounter += 1;
                cveIdsToSend.add(cveId);
                cvesToSend.add(new CveAlertItem(
                        product,
                        parsedCve.getCveId(),
                        parsedCve.getDescription(),
                        parsedCve.getSeverity(),
                        Float.valueOf(parsedCve.getScore()),
                        parsedCve.getPublished()
                ));
            }
            appLogger.info(LogEvents.BATCH_RUN, "Searched vulnerabilities for product '%s'".formatted(product), new LinkedHashMap<>(Map.of("vulnerabilities", validCveCounter, "product", product)));
        }

        cvesToSend.sort(
                Comparator.comparing(
                        CveAlertItem::getScore,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        for (CveAlertItem cve : cvesToSend) {
            String cveHtml = emailService.buildCveHtml(cve.getProduct(), cve);
            cveListHtml.append(cveHtml);
        }

        if (!cveIdsToSend.isEmpty()) {
            html = html.replace("{{cveList}}", cveListHtml.toString());
            emailService.sendHtmlEmail(emails, "New Vulnerabilities Report", html);

            for (String cveId : cveIdsToSend) {
                cveStateService.markCveAsSeen(cveId);
            }
        }

        appLogger.info(LogEvents.EMAIL_SENT, "Finished scheduled run and sent alert email", new LinkedHashMap<>(Map.of("vulnerabilities", cvesToSend.size())));
    }
}
