package org.threatwatch.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

import java.util.Set;

@Builder
public class SettingsResponseDto {

    private final String batchInterval;
    private final String lookbackWindow;
    private final String deduplicationWindow;
    private final Set<String> emails;
    private final Set<String> notificationTypes;
    private final String enabled;
    private final Set<String> supportedProducts;
    private final Set<String> productsSelected;
    private final String severityThreshold;
    private final String earlyAlerts;
    private final String emailProviderHost;
    private final String emailProviderPort;
    private final String emailProviderUsername;
    private final String nvdApiKeyProvided;

    @JsonIgnore
    private final String emailProviderPassword;
    @JsonIgnore
    private final String nvdApiKey;

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

    public String getEmailProviderHost() { return this.emailProviderHost; }

    public String getEmailProviderPort() { return this.emailProviderPort; }

    public String getEmailProviderUsername() { return this.emailProviderUsername; }

    public String getEmailProviderPassword() { return this.emailProviderPassword; }

    public String getNvdApiKeyProvided() { return this.nvdApiKeyProvided; }

    public String getNvdApiKey() { return this.nvdApiKey; }
}
