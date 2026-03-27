package org.threatwatch.dtos;

import java.util.Set;

public class SettingsResponseDto {

    private String batchInterval;
    private String lookbackWindow;
    private String deduplicationWindow;
    private Set<String> emails;
    private Set<String> notificationTypes;
    private String enabled;
    private Set<String> supportedProducts;
    private Set<String> productsSelected;
    private String severityThreshold;
    private String earlyAlerts;

    public SettingsResponseDto(String batchInterval, String lookbackWindow, String deduplicationWindow, Set<String> emails, Set<String> notificationTypes, String enabled, Set<String> supportedProducts, Set<String> productsSelected, String severityThreshold, String earlyAlerts) {
        this.batchInterval = batchInterval;
        this.lookbackWindow = lookbackWindow;
        this.deduplicationWindow = deduplicationWindow;
        this.emails = emails;
        this.notificationTypes = notificationTypes;
        this.enabled = enabled;
        this.supportedProducts = supportedProducts;
        this.productsSelected = productsSelected;
        this.severityThreshold = severityThreshold;
        this.earlyAlerts = earlyAlerts;
    }

    public String getBatchInterval() { return this.batchInterval; }

    public String getLookbackWindow() { return this.lookbackWindow; }

    public String getDeduplicationWindow() { return this.deduplicationWindow; }

    public Set<String> getEmails() { return this.emails; }

    public Set<String> getNotificationTypes() { return this.notificationTypes; }

    public String getEnabled() { return this.enabled; }

    public Set<String> getSupportedProducts() { return this.supportedProducts; }

    public Set<String> getProductsSelected() { return this.productsSelected; }

    public String getSeverityThreshold() { return this.severityThreshold; }

    public String getEarlyAlerts() { return earlyAlerts; }
}
