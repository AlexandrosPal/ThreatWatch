package org.threatwatch.services.impl;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.threatwatch.dtos.SettingsRequestDto;
import org.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.models.NotificationTypes;
import org.threatwatch.services.ProductsService;
import org.threatwatch.services.SettingsService;

import java.util.Set;

@Service
public class SettingsServiceImpl implements SettingsService {

    private final StringRedisTemplate redisTemplate;
    private final ProductsService productsService;

    private static final String SETTINGS_BATCH_INTERVAL_KEY = "settings:batchInterval";
    private static final String SETTINGS_LOOKBACK_WINDOW_KEY = "settings:lookbackWindow";
    private static final String SETTINGS_DEDUPLICATION_WINDOW_KEY = "settings:deduplicationWindow";
    private static final String SETTINGS_ENABLED_KEY = "settings:enabled";
    private static final String SETTINGS_SEVERITY_THRESHOLD_KEY = "settings:severityThreshold";
    private static final String SETTINGS_EARLY_ALERTS_KEY = "settings:earlyAlerts";
    private static final String SETTINGS_EMAILS_KEY = "settings:emails";
    private static final String SETTINGS_NOTIFICATION_TYPES_KEY = "settings:notificationTypes";
    private static final String SETTINGS_PRODUCTS_SELECTED_KEY = "settings:productsSelected";
    private static final String SETTINGS_EMAIL_PROVIDER_HOST_KEY = "settings:emailProviderHost";
    private static final String SETTINGS_EMAIL_PROVIDER_PORT_KEY = "settings:emailProviderPort";
    private static final String SETTINGS_EMAIL_PROVIDER_USERNAME_KEY = "settings:emailProviderUsername";
    private static final String SETTINGS_EMAIL_PROVIDER_PASSWORD_KEY = "settings:emailProviderPassword";
    private static final String SETTINGS_NVD_API_KEY_KEY = "settings:nvdApiKey";

    public SettingsServiceImpl(StringRedisTemplate redisTemplate, ProductsService productsService) {
        this.redisTemplate = redisTemplate;
        this.productsService = productsService;
    }

    private void updateBatchInterval(Integer batchInterval) {
        if (batchInterval != null) {
            redisTemplate.opsForValue().set(SETTINGS_BATCH_INTERVAL_KEY, String.valueOf(batchInterval).replace(".0", ""));
            redisTemplate.opsForValue().set(SETTINGS_LOOKBACK_WINDOW_KEY, String.valueOf(batchInterval * 2).replace(".0", ""));
            redisTemplate.opsForValue().set(SETTINGS_DEDUPLICATION_WINDOW_KEY, String.valueOf(batchInterval * 2.4).replace(".0", ""));
        }
    }

    private void updateEnabledFlag(String enabled) {
        if (enabled != null) {
            redisTemplate.opsForValue().set(SETTINGS_ENABLED_KEY, enabled);
        }
    }

    private void updateEmails(String email) {
        if (email != null) {
            if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(SETTINGS_EMAILS_KEY, email))) {
                redisTemplate.opsForSet().remove(SETTINGS_EMAILS_KEY, email);
            } else {
                redisTemplate.opsForSet().add(SETTINGS_EMAILS_KEY, email);
            }
        }
    }

    private void updateNotificationTypes(NotificationTypes notificationType) {
        if (notificationType != null) {
            if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(SETTINGS_NOTIFICATION_TYPES_KEY, String.valueOf(notificationType)))) {
                redisTemplate.opsForSet().remove(SETTINGS_NOTIFICATION_TYPES_KEY, String.valueOf(notificationType));
            } else {
                redisTemplate.opsForSet().add(SETTINGS_NOTIFICATION_TYPES_KEY, String.valueOf(notificationType));
            }
        }
    }

    private void updateSupportedProducts(String productAddition) {
        if (productAddition != null) {
            Boolean isMember = redisTemplate.opsForSet().isMember(SETTINGS_PRODUCTS_SELECTED_KEY, productAddition);
            if (Boolean.TRUE.equals(isMember)) {
                redisTemplate.opsForSet().remove(SETTINGS_PRODUCTS_SELECTED_KEY, productAddition);
            } else {
                redisTemplate.opsForSet().add(SETTINGS_PRODUCTS_SELECTED_KEY, productAddition);
            }
        }
    }

    private void updateSeverityThreshold(String severityThreshold) {
        if (severityThreshold != null) {
            redisTemplate.opsForValue().set(SETTINGS_SEVERITY_THRESHOLD_KEY, severityThreshold);
        }
    }

    private void updateEarlyAlertsFlag(String earlyAlerts) {
        if (earlyAlerts != null) {
            redisTemplate.opsForValue().set(SETTINGS_EARLY_ALERTS_KEY, earlyAlerts);
        }
    }

    private void updateEmailProviderHost(String emailProviderHost) {
        if (emailProviderHost != null) {
            redisTemplate.opsForValue().set(SETTINGS_EMAIL_PROVIDER_HOST_KEY, emailProviderHost);
        }
    }

    private void updateEmailProviderPort(String emailProviderPort) {
        if (emailProviderPort != null) {
            redisTemplate.opsForValue().set(SETTINGS_EMAIL_PROVIDER_PORT_KEY, emailProviderPort);
        }
    }

    private void updateEmailProviderUsername(String emailProviderUsername) {
        if (emailProviderUsername != null) {
            redisTemplate.opsForValue().set(SETTINGS_EMAIL_PROVIDER_USERNAME_KEY, emailProviderUsername);
        }
    }

    private void updateEmailProviderPassword(String emailProviderPassword) {
        if (emailProviderPassword != null) {
            redisTemplate.opsForValue().set(SETTINGS_EMAIL_PROVIDER_PASSWORD_KEY, emailProviderPassword);
        }
    }

    private void updateNvdApiKey(String nvdApiKey) {
        if (nvdApiKey != null) {
            redisTemplate.opsForValue().set(SETTINGS_NVD_API_KEY_KEY, nvdApiKey);
        }
    }

    @Override
    public SettingsResponseDto retrieveSettings() {

        String batchInterval = redisTemplate.opsForValue().get(SETTINGS_BATCH_INTERVAL_KEY);
        String lookbackWindow = redisTemplate.opsForValue().get(SETTINGS_LOOKBACK_WINDOW_KEY);
        String deduplicationWindow = redisTemplate.opsForValue().get(SETTINGS_DEDUPLICATION_WINDOW_KEY);
        String enabled = redisTemplate.opsForValue().get(SETTINGS_ENABLED_KEY);
        String severityThreshold = redisTemplate.opsForValue().get(SETTINGS_SEVERITY_THRESHOLD_KEY);
        String earlyAlerts = redisTemplate.opsForValue().get(SETTINGS_EARLY_ALERTS_KEY);

        Set<String> emails = redisTemplate.opsForSet().members(SETTINGS_EMAILS_KEY);
        Set<String> notificationTypes = redisTemplate.opsForSet().members(SETTINGS_NOTIFICATION_TYPES_KEY);

        Set<String> supportedProducts = productsService.getProducts().keySet();
        Set<String> productsSelected = redisTemplate.opsForSet().members(SETTINGS_PRODUCTS_SELECTED_KEY);

        String emailProviderHost = redisTemplate.opsForValue().get(SETTINGS_EMAIL_PROVIDER_HOST_KEY);
        String emailProviderPort = redisTemplate.opsForValue().get(SETTINGS_EMAIL_PROVIDER_PORT_KEY);
        String emailProviderUsername = redisTemplate.opsForValue().get(SETTINGS_EMAIL_PROVIDER_USERNAME_KEY);
        String emailProviderPassword = redisTemplate.opsForValue().get(SETTINGS_EMAIL_PROVIDER_PASSWORD_KEY);

        String nvdApiKey = redisTemplate.opsForValue().get(SETTINGS_NVD_API_KEY_KEY);
        String nvdApiKeyProvided;
        if (nvdApiKey != null && !nvdApiKey.isEmpty()) {
            nvdApiKeyProvided = "true";
        } else {
            nvdApiKeyProvided = "false";
        }

        return SettingsResponseDto.builder()
                .batchInterval(batchInterval)
                .lookbackWindow(lookbackWindow)
                .deduplicationWindow(deduplicationWindow)
                .emails(emails)
                .notificationTypes(notificationTypes)
                .enabled(enabled)
                .supportedProducts(supportedProducts)
                .productsSelected(productsSelected)
                .severityThreshold(severityThreshold)
                .earlyAlerts(earlyAlerts)
                .emailProviderHost(emailProviderHost)
                .emailProviderPort(emailProviderPort)
                .emailProviderUsername(emailProviderUsername)
                .emailProviderPassword(emailProviderPassword)
                .nvdApiKey(nvdApiKey)
                .nvdApiKeyProvided(nvdApiKeyProvided)
                .build();
    }

    @Override
    public void updateSettings(SettingsRequestDto request) {
        updateBatchInterval(request.getBatchInterval());
        updateEnabledFlag(request.getEnabled());
        updateEmails(request.getEmail());
        updateNotificationTypes(request.getNotificationType());
        updateSupportedProducts(request.getProductAddition());
        updateSeverityThreshold(request.getSeverityThreshold());
        updateEarlyAlertsFlag(request.getEarlyAlerts());
        updateEmailProviderHost(request.getEmailProviderHost());
        updateEmailProviderPort(request.getEmailProviderPort());
        updateEmailProviderUsername(request.getEmailProviderUsername());
        updateEmailProviderPassword(request.getEmailProviderPassword());
        updateNvdApiKey(request.getNvdApiKey());
    }
}
