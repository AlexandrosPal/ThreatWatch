package org.threatwatch.notifications.teams;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.threatwatch.cve.model.CveAlertItem;
import org.threatwatch.notifications.NotificationChannel;
import org.threatwatch.notifications.NotificationRequestDto;
import org.threatwatch.notifications.NotificationSender;

import java.util.List;
import java.util.Map;

@Service
public class TeamsNotificationSender implements NotificationSender {

    private final RestClient restClient;

    public TeamsNotificationSender(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public NotificationChannel supports() {
        return NotificationChannel.TEAMS;
    }

    public String buildTeamsAlertMessage(List<CveAlertItem> cvesToSend) {
        StringBuilder message = new StringBuilder();

        message.append(cvesToSend.size())
                .append(" new CVE")
                .append(cvesToSend.size() == 1 ? "" : "s")
                .append(" detected.\n\n");

        cvesToSend.forEach(cve -> {
            message.append(cve.getSeverity().toEmoji())
                    .append(" ")
                    .append(cve.getId())
                    .append("\n")
                    .append("Product: ")
                    .append(cve.getProduct())
                    .append("\n")
                    .append("Severity: ")
                    .append(cve.getSeverity())
                    .append(" (")
                    .append(cve.getScore())
                    .append(")\n")
                    .append("NVD: https://nvd.nist.gov/vuln/detail/")
                    .append(cve.getId())
                    .append("\n\n");
        });

        return message.toString();
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
