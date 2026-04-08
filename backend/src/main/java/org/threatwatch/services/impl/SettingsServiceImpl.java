package org.threatwatch.services.impl;

import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.threatwatch.dtos.SettingsRequestDto;
import org.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.LogEvents;
import org.threatwatch.models.NotificationTypes;
import org.threatwatch.services.ProductsService;
import org.threatwatch.services.SettingsService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class SettingsServiceImpl implements SettingsService {

    private final StringRedisTemplate redisTemplate;
    private final ProductsService productsService;
    private static final AppLogger appLogger = new AppLogger(LoggerFactory.getLogger(SettingsServiceImpl.class));

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

            appLogger.info(LogEvents.SETTINGS_UPDATE, "Updated batch interval", new LinkedHashMap<>(Map.of("batchInterval", batchInterval)));
        }
    }

    private void updateEnabledFlag(String enabled) {
        if (enabled != null) {
            redisTemplate.opsForValue().set(SETTINGS_ENABLED_KEY, enabled);

            appLogger.info(LogEvents.SETTINGS_UPDATE, "Updated scheduler enabled flag", new LinkedHashMap<>(Map.of("enabled", enabled)));
        }
    }

    private void updateEmails(String email) {
        if (email != null) {
            if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(SETTINGS_EMAILS_KEY, email))) {
                redisTemplate.opsForSet().remove(SETTINGS_EMAILS_KEY, email);
                appLogger.info(LogEvents.SETTINGS_UPDATE, "Removed email from list", new LinkedHashMap<>(Map.of("action", "removal", "email", email)));
            } else {
                redisTemplate.opsForSet().add(SETTINGS_EMAILS_KEY, email);
                appLogger.info(LogEvents.SETTINGS_UPDATE, "Added email to list", new LinkedHashMap<>(Map.of("action", "addition", "email", email)));
            }
        }
    }

    private void updateNotificationTypes(NotificationTypes notificationType) {
        if (notificationType != null) {
            if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(SETTINGS_NOTIFICATION_TYPES_KEY, String.valueOf(notificationType)))) {
                redisTemplate.opsForSet().remove(SETTINGS_NOTIFICATION_TYPES_KEY, String.valueOf(notificationType));
                appLogger.info(LogEvents.SETTINGS_UPDATE, "Removed notification type", new LinkedHashMap<>(Map.of("action", "removal", "notificationType", notificationType)));
            } else {
                redisTemplate.opsForSet().add(SETTINGS_NOTIFICATION_TYPES_KEY, String.valueOf(notificationType));
                appLogger.info(LogEvents.SETTINGS_UPDATE, "Added notification type", new LinkedHashMap<>(Map.of("action", "addition", "notificationType", notificationType)));
            }
        }
    }

    private void updateSupportedProducts(String productAddition) {
        if (productAddition != null) {
            Boolean isMember = redisTemplate.opsForSet().isMember(SETTINGS_PRODUCTS_SELECTED_KEY, productAddition);
            if (Boolean.TRUE.equals(isMember)) {
                redisTemplate.opsForSet().remove(SETTINGS_PRODUCTS_SELECTED_KEY, productAddition);
                appLogger.info(LogEvents.SETTINGS_UPDATE, "Removed product from list", new LinkedHashMap<>(Map.of("action", "removal", "product", productAddition)));
            } else {
                redisTemplate.opsForSet().add(SETTINGS_PRODUCTS_SELECTED_KEY, productAddition);
                appLogger.info(LogEvents.SETTINGS_UPDATE, "Added product to list", new LinkedHashMap<>(Map.of("action", "addition", "product", productAddition)));
            }
        }
    }

    private void updateSeverityThreshold(String severityThreshold) {
        Optional.ofNullable(severityThreshold).ifPresent(threshold -> {
            redisTemplate.opsForValue().set(SETTINGS_SEVERITY_THRESHOLD_KEY, severityThreshold);
            appLogger.info(LogEvents.SETTINGS_UPDATE, "Updated severity threshold", new LinkedHashMap<>(Map.of("severityThreshold", severityThreshold)));
        });
    }

    private void updateEarlyAlertsFlag(String earlyAlerts) {
        if (earlyAlerts != null) {
            redisTemplate.opsForValue().set(SETTINGS_EARLY_ALERTS_KEY, earlyAlerts);

            appLogger.info(LogEvents.SETTINGS_UPDATE, "Updated early alerts flag", new LinkedHashMap<>(Map.of("earlyAlerts", earlyAlerts)));
        }
    }

    private void updateEmailProviderHost(String emailProviderHost) {
        if (emailProviderHost != null) {
            redisTemplate.opsForValue().set(SETTINGS_EMAIL_PROVIDER_HOST_KEY, emailProviderHost);

            appLogger.info(LogEvents.SETTINGS_UPDATE, "Updated email provider host", new LinkedHashMap<>(Map.of("emailProviderHost", emailProviderHost)));
        }
    }

    private void updateEmailProviderPort(String emailProviderPort) {
        if (emailProviderPort != null) {
            redisTemplate.opsForValue().set(SETTINGS_EMAIL_PROVIDER_PORT_KEY, emailProviderPort);

            appLogger.info(LogEvents.SETTINGS_UPDATE, "Updated email provider port", new LinkedHashMap<>(Map.of("emailProviderPort", emailProviderPort)));
        }
    }

    private void updateEmailProviderUsername(String emailProviderUsername) {
        if (emailProviderUsername != null) {
            redisTemplate.opsForValue().set(SETTINGS_EMAIL_PROVIDER_USERNAME_KEY, emailProviderUsername);

            appLogger.info(LogEvents.SETTINGS_UPDATE, "Updated email provider username", new LinkedHashMap<>(Map.of("emailProviderUsername", emailProviderUsername)));
        }
    }

    private void updateEmailProviderPassword(String emailProviderPassword) {
        if (emailProviderPassword != null && !emailProviderPassword.isEmpty()) {
            redisTemplate.opsForValue().set(SETTINGS_EMAIL_PROVIDER_PASSWORD_KEY, emailProviderPassword);

            appLogger.info(LogEvents.SETTINGS_UPDATE, "Updated email provider password", new LinkedHashMap<>(Map.of("emailProviderPassword", "*****")));
        }
    }

    private void updateNvdApiKey(String nvdApiKey) {
        if (nvdApiKey != null) {
            redisTemplate.opsForValue().set(SETTINGS_NVD_API_KEY_KEY, nvdApiKey);

            if (nvdApiKey.isBlank()) {
                appLogger.info(LogEvents.SETTINGS_UPDATE, "Removed NVD API key", new LinkedHashMap<>());
            } else {
                appLogger.info(LogEvents.SETTINGS_UPDATE, "Updated NVD API key", new LinkedHashMap<>(Map.of("nvdApiKey", "*****")));
            }
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
