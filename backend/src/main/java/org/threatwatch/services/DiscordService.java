package org.threatwatch.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.threatwatch.models.CveAlertItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DiscordService {

    private final RestClient restClient;

    public DiscordService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String buildDiscordAlertMessage(List<CveAlertItem> cvesToSend) {
        if (cvesToSend.size() > 10) {
            return buildDiscordSummaryMessage(cvesToSend);
        }

        return buildDiscordDetailedMessage(cvesToSend);
    }

    private String buildDiscordSummaryMessage(List<CveAlertItem> cvesToSend) {
        Map<String, Long> cvesByProduct = cvesToSend.stream()
                .collect(Collectors.groupingBy(
                        CveAlertItem::getProduct,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        StringBuilder message = new StringBuilder();

        message.append("## 🚨 ThreatWatch Vulnerability Report\n\n");
        message.append("**")
                .append(cvesToSend.size())
                .append(" new CVEs detected** across your monitored technologies.\n\n");

        message.append("### Summary by product\n");

        cvesByProduct.forEach((product, count) ->
                message.append("> **")
                        .append(product)
                        .append("** — ")
                        .append(count)
                        .append(" CVE")
                        .append(count == 1 ? "" : "s")
                        .append("\n")
        );

        message.append("\n_ThreatWatch_");

        return message.toString();
    }

    private String buildDiscordDetailedMessage(List<CveAlertItem> cvesToSend) {
        StringBuilder message = new StringBuilder();

        message.append("## 🚨 ThreatWatch Vulnerability Report\n\n");
        message.append("**")
                .append(cvesToSend.size())
                .append(" new CVE")
                .append(cvesToSend.size() == 1 ? "" : "s")
                .append(" detected** for your monitored technologies.\n\n");

        message.append("### Findings\n");

        cvesToSend.forEach(cve -> {
            message.append("\n")
                    .append(getSeverityEmoji(cve.getScore()))
                    .append(" **")
                    .append(cve.getId())
                    .append("**\n")
                    .append("> **Product:** ")
                    .append(cve.getProduct())
                    .append("\n")
                    .append("> **Severity:** ")
                    .append(cve.getSeverity())
                    .append(" (")
                    .append(cve.getScore())
                    .append(")\n")
                    .append("> **Published:** ")
                    .append(cve.getPublished())
                    .append("\n")
                    .append("> **NVD:** https://nvd.nist.gov/vuln/detail/")
                    .append(cve.getId())
                    .append("\n");
        });

        message.append("\n_ThreatWatch_");

        return message.toString();
    }

    private String getSeverityEmoji(Float score) {
        if (score == null || score < 0) return "⚪";
        if (score >= 9.0) return "🔥";
        if (score >= 7.0) return "🔴";
        if (score >= 4.0) return "🟠";
        return "🟢";
    }

    public void sendAlert(String webhookUrl, String message) {
        restClient
                .post()
                .uri(webhookUrl)
                .body(Map.of("content", message))
                .retrieve()
                .toBodilessEntity();
    }
}
