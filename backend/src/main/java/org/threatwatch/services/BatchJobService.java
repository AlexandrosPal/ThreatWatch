package org.threatwatch.services;

import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class BatchJobService {

    private final EmailService emailService;
    private final SettingsService settingsService;
    private final ProductsService productService;
    private final NvdRestService nvdRestService;

    public BatchJobService(EmailService emailService, SettingsService settingsService, ProductsService productService, NvdRestService nvdRestService) {
        this.emailService = emailService;
        this.settingsService = settingsService;
        this.productService = productService;
        this.nvdRestService = nvdRestService;
    }

    public void executeScheduledRun() {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .withZone(ZoneOffset.UTC);

        Integer lookbackWindow = Integer.valueOf(settingsService.retrieveSettings().getLookbackWindow());
        Instant rawPublishEndDatetime = Instant.now();
        Instant rawPublishStartDatetime = rawPublishEndDatetime.minusSeconds(lookbackWindow);

        String publishEndDatetime = dateTimeFormatter.format(rawPublishEndDatetime);
        String publishStartDatetime = dateTimeFormatter.format(rawPublishStartDatetime);

        String product = "nginx";

        JsonNode response = nvdRestService.getRecentVulnerabilitiesByProduct(product, publishStartDatetime, publishEndDatetime);

        JsonNode vulnerabilities = response.get("vulnerabilities");

        for (JsonNode vuln : vulnerabilities) {
            JsonNode cve = vuln.get("cve");

            String id = cve.get("id").asString();

            System.out.println(id);
        }

//        Set<String> emails = settingsService.retrieveSettings().getEmails();
//        emailService.sendEmail(emails, "Hello", "hello");
    }
}
