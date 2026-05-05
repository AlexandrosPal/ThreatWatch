package org.threatwatch.notifications.discord;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.threatwatch.cve.model.CveAlertItem;
import org.threatwatch.notifications.NotificationChannel;
import org.threatwatch.notifications.NotificationRequestDto;
import org.threatwatch.notifications.NotificationSender;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DiscordNotificationSender implements NotificationSender {

    private final RestClient restClient;

    public DiscordNotificationSender(RestClient.Builder restClientBuilder) {
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
                    .append(cve.getSeverity().toEmoji())
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

    @Override
    public NotificationChannel supports() {
        return NotificationChannel.DISCORD;
    }

    @Override
    public void sendNotification(NotificationRequestDto request) {
        for (String url : request.getWebhookUrls()) {
            restClient
                    .post()
                    .uri(url)
                    .body(Map.of("content", request.getMessage()))
                    .retrieve()
                    .toBodilessEntity();
        }
    }
}
