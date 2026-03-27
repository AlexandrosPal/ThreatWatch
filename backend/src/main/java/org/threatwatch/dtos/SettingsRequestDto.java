package org.threatwatch.dtos;

import org.threatwatch.models.NotificationTypes;

public class SettingsRequestDto {

    private Integer batchInterval;
    private String lookbackWindow;
    private String deduplicationWindow;
    private String email;
    private NotificationTypes notificationType;
    private String enabled;
    private String productAddition;
    private String severityThreshold;
    private String earlyAlerts;

    public Integer getBatchInterval() { return batchInterval; }

    public String getEmail() { return email; }

    public NotificationTypes getNotificationType() { return notificationType; }

    public String getEnabled() { return this.enabled; }

    public String getProductAddition() { return this.productAddition; }

    public String getSeverityThreshold() {return severityThreshold; }

    public String getEarlyAlerts() { return earlyAlerts; }
}
