package org.threatwatch.dtos;

import org.threatwatch.models.NotificationTypes;

public class SettingsRequestDto {

    private Integer batchInterval;
    private String email;
    private NotificationTypes notificationsSelected;
    private String enabled;
    private String productAddition;
    private String severityThreshold;
    private String earlyAlerts;
    private String emailProviderHost;
    private String emailProviderPort;
    private String emailProviderUsername;
    private String emailProviderPassword;
    private String nvdApiKey;
    private String discordWebhookUrl;
    private String slackWebhookUrl;
    private String teamsWebhookUrl;

    public Integer getBatchInterval() { return batchInterval; }

    public String getEmail() { return email; }

    public NotificationTypes getNotificationsSelected() { return notificationsSelected; }

    public String getEnabled() { return this.enabled; }

    public String getProductAddition() { return this.productAddition; }

    public String getSeverityThreshold() { return this.severityThreshold; }

    public String getEarlyAlerts() { return this.earlyAlerts; }

    public String getEmailProviderHost() { return this.emailProviderHost; }

    public String getEmailProviderPort() { return this.emailProviderPort; }

    public String getEmailProviderUsername() { return this.emailProviderUsername; }

    public String getEmailProviderPassword() { return this.emailProviderPassword; }

    public String getNvdApiKey() { return nvdApiKey; }

    public String getDiscordWebhookUrl() { return discordWebhookUrl; }

    public String getSlackWebhookUrl() { return slackWebhookUrl; }

    public String getTeamsWebhookUrl() { return teamsWebhookUrl; }
}
