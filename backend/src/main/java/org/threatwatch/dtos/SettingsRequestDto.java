package org.threatwatch.dtos;

import org.threatwatch.models.NotificationTypes;

public class SettingsRequestDto {

    private Integer batchInterval;
    private String email;
    private NotificationTypes notificationType;
    private String enabled;
    private String productAddition;
    private String severityThreshold;
    private String earlyAlerts;
    private String emailProviderHost;
    private String emailProviderPort;
    private String emailProviderUsername;
    private String emailProviderPassword;
    private String nvdApiKey;

    public Integer getBatchInterval() { return batchInterval; }

    public String getEmail() { return email; }

    public NotificationTypes getNotificationType() { return notificationType; }

    public String getEnabled() { return this.enabled; }

    public String getProductAddition() { return this.productAddition; }

    public String getSeverityThreshold() { return this.severityThreshold; }

    public String getEarlyAlerts() { return this.earlyAlerts; }

    public String getEmailProviderHost() { return this.emailProviderHost; }

    public String getEmailProviderPort() { return this.emailProviderPort; }

    public String getEmailProviderUsername() { return this.emailProviderUsername; }

    public String getEmailProviderPassword() { return this.emailProviderPassword; }

    public String getNvdApiKey() { return nvdApiKey; }
}
