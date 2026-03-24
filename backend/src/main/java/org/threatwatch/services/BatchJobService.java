package org.threatwatch.services;

import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Set;

@Service
public class BatchJobService {

    private final EmailService emailService;
    private final SettingsService settingsService;
    private final ProductsService productService;
    private final NvdRestService nvdRestService;
    private final CveParserService cveParserService;

    public BatchJobService(EmailService emailService, SettingsService settingsService, ProductsService productService, NvdRestService nvdRestService, CveParserService cveParserService) {
        this.emailService = emailService;
        this.settingsService = settingsService;
        this.productService = productService;
        this.nvdRestService = nvdRestService;
        this.cveParserService = cveParserService;
    }

    public void executeScheduledRun() throws Exception {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .withZone(ZoneOffset.UTC);

        Integer lookbackWindow = Integer.valueOf(settingsService.retrieveSettings().getLookbackWindow());
        Instant rawPublishEndDatetime = Instant.now();
        Instant rawPublishStartDatetime = rawPublishEndDatetime.minusSeconds(lookbackWindow);

        String publishEndDatetime = dateTimeFormatter.format(rawPublishEndDatetime);
        String publishStartDatetime = dateTimeFormatter.format(rawPublishStartDatetime);
        Set<String> products = productService.getProducts().keySet();

        StringBuilder cveListHtml = new StringBuilder();
        String html = emailService.loadHtmlTemplate();
        Set<String> emails = settingsService.retrieveSettings().getEmails();

        for (String product : products) {
            String productLower = product.toLowerCase();
            JsonNode response = nvdRestService.getRecentVulnerabilitiesByProduct(productLower, publishStartDatetime, publishEndDatetime);

            JsonNode vulnerabilities = response.get("vulnerabilities");

            for (JsonNode vulnerabilitie : vulnerabilities) {
                JsonNode cve = vulnerabilitie.get("cve");

                cveParserService.parseCve(cve);
                String cveId = cveParserService.getCveId();
                String description = cveParserService.getDescription();
                String severity = cveParserService.getSeverity();
                String score = cveParserService.getScore();
                String publicedAt = cveParserService.getPublished();

                String color = switch (severity) {
                    case "CRITICAL" -> "#b60205";
                    case "HIGH" -> "#d73a49";
                    case "MEDIUM" -> "#fb8500";
                    case "LOW" -> "#2da44e";
                    default -> "#6c757d";
                };

                cveListHtml.append("""
                        <div style="border:1px solid #e1e4e8; border-radius:6px; padding:12px; margin-bottom:12px;">
                            <div style="font-weight:bold; font-size:14px;">
                                %s  |  %s
                                <span style="background:%s; color:white; padding:2px 6px; border-radius:4px; font-size:11px; margin-left:8px;">%s</span>
                                <span style="padding-left:5px;">%s</span>
                            </div>
                            <div style="font-size:12px; color:#555; margin-top:4px;">
                                Published: %s
                            </div>
                            <div style="font-size:13px; margin-top:8px;">
                                %s
                            </div>
                        </div>
                        """.formatted(product, cveId, color, severity, score, publicedAt, description));
            }
        }
        html = html.replace("{{cveList}}", cveListHtml.toString());
        emailService.sendHtmlEmail(emails, "New Vulnerabilities Report", html);
    }
}
