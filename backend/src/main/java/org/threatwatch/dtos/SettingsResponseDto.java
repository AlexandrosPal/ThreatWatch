package org.threatwatch.dtos;

import java.util.Set;

public class SettingsResponseDto {

    private String batchInterval;
    private String lookbackWindow;
    private String deduplicationWindow;
    private Set<String> emails;
    private Set<String> notificationTypes;
    private String enabled;

    public SettingsResponseDto(String batchInterval, String lookbackWindow, String deduplicationWindow, Set<String> emails, Set<String> notificationTypes, String enabled) {
        this.batchInterval = batchInterval;
        this.lookbackWindow = lookbackWindow;
        this.deduplicationWindow = deduplicationWindow;
        this.emails = emails;
        this.notificationTypes = notificationTypes;
        this.enabled = enabled;
    }

    public String getBatchInterval() { return this.batchInterval; }

    public String getLookbackWindow() { return this.lookbackWindow; }

    public String getDeduplicationWindow() { return this.deduplicationWindow; }

    public Set<String> getEmails() { return this.emails; }

    public Set<String> getNotificationTypes() { return this.notificationTypes; }

    public String getEnabled() { return this.enabled; }
}
