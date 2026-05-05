package org.threatwatch.settings;

import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.LogEvents;
import org.threatwatch.notifications.NotificationChannel;
import org.threatwatch.products.ProductsService;

import java.util.*;
import java.util.stream.Collectors;

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
    private static final String SETTINGS_NOTIFICATION_TYPES_KEY = "settings:notificationsSelected";
    private static final String SETTINGS_PRODUCTS_SELECTED_KEY = "settings:productsSelected";
    private static final String SETTINGS_EMAIL_PROVIDER_HOST_KEY = "settings:emailProviderHost";
    private static final String SETTINGS_EMAIL_PROVIDER_PORT_KEY = "settings:emailProviderPort";
    private static final String SETTINGS_EMAIL_PROVIDER_USERNAME_KEY = "settings:emailProviderUsername";
    private static final String SETTINGS_EMAIL_PROVIDER_PASSWORD_KEY = "settings:emailProviderPassword";
    private static final String SETTINGS_NVD_API_KEY_KEY = "settings:nvdApiKey";
    private static final String SETTINGS_DISCORD_WEBHOOK_URL = "settings:discordWebhookUrls";
    private static final String SETTINGS_SLACK_WEBHOOK_URL = "settings:slackWebhookUrls";
    private static final String SETTINGS_TEAMS_WEBHOOK_URL = "settings:teamsWebhookUrls";

    public SettingsServiceImpl(StringRedisTemplate redisTemplate, ProductsService productsService) {
        this.redisTemplate = redisTemplate;
        this.productsService = productsService;
    }

    @Override
    public void initializeDefaultsIfMissing() {
        setDefaultIfMissing(SETTINGS_BATCH_INTERVAL_KEY, "1800");
        setDefaultIfMissing(SETTINGS_LOOKBACK_WINDOW_KEY, "3600");
        setDefaultIfMissing(SETTINGS_DEDUPLICATION_WINDOW_KEY, "4320");
        setDefaultIfMissing(SETTINGS_ENABLED_KEY, "false");
        setDefaultIfMissing(SETTINGS_SEVERITY_THRESHOLD_KEY, "7.0");
        setDefaultIfMissing(SETTINGS_EARLY_ALERTS_KEY, "false");
    }

    private void setDefaultIfMissing(String key, String value) {
        String existing = redisTemplate.opsForValue().get(key);
        if (existing == null || existing.isBlank()) {
            redisTemplate.opsForValue().set(key, value);
        }
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

    private void updateSelectedNotifications(NotificationChannel notificationType) {
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

    private void updateDiscordWebhookUrls(String discordWebhookUrl) {
        if (discordWebhookUrl != null) {
            Boolean isMember = redisTemplate.opsForSet().isMember(SETTINGS_PRODUCTS_SELECTED_KEY, discordWebhookUrl);
            if (Boolean.TRUE.equals(isMember)) {
                redisTemplate.opsForSet().remove(SETTINGS_DISCORD_WEBHOOK_URL, discordWebhookUrl);
                appLogger.info(LogEvents.SETTINGS_UPDATE, "Removed Discord webhook URL from list", new LinkedHashMap<>(Map.of("discordWebhookUrl", discordWebhookUrl)));
            } else {
                redisTemplate.opsForSet().add(SETTINGS_DISCORD_WEBHOOK_URL, discordWebhookUrl);
                appLogger.info(LogEvents.SETTINGS_UPDATE, "Added Discord webhook URL to list", new LinkedHashMap<>(Map.of("discordWebhookUrl", discordWebhookUrl)));
            }
        }
    }

    private void updateSlackWebhookUrls(String slackWebhookUrl) {
        if (slackWebhookUrl != null) {
            Boolean isMember = redisTemplate.opsForSet().isMember(SETTINGS_PRODUCTS_SELECTED_KEY, slackWebhookUrl);
            if (Boolean.TRUE.equals(isMember)) {
                redisTemplate.opsForSet().remove(SETTINGS_SLACK_WEBHOOK_URL, slackWebhookUrl);
                appLogger.info(LogEvents.SETTINGS_UPDATE, "Removed Slack webhook URL from list", new LinkedHashMap<>(Map.of("slackWebhookUrl", slackWebhookUrl)));
            } else {
                redisTemplate.opsForSet().add(SETTINGS_SLACK_WEBHOOK_URL, slackWebhookUrl);
                appLogger.info(LogEvents.SETTINGS_UPDATE, "Added Slack webhook URL to list", new LinkedHashMap<>(Map.of("slackWebhookUrl", slackWebhookUrl)));
            }
        }
    }

    private void updateTeamsWebhookUrls(String teamsWebhookUrl) {
        if (teamsWebhookUrl != null) {
            Boolean isMember = redisTemplate.opsForSet().isMember(SETTINGS_PRODUCTS_SELECTED_KEY, teamsWebhookUrl);
            if (Boolean.TRUE.equals(isMember)) {
                redisTemplate.opsForSet().remove(SETTINGS_TEAMS_WEBHOOK_URL, teamsWebhookUrl);
                appLogger.info(LogEvents.SETTINGS_UPDATE, "Removed MS Teams webhook URL from list", new LinkedHashMap<>(Map.of("teamsWebhookUrl", teamsWebhookUrl)));
            } else {
                redisTemplate.opsForSet().add(SETTINGS_TEAMS_WEBHOOK_URL, teamsWebhookUrl);
                appLogger.info(LogEvents.SETTINGS_UPDATE, "Added MS Teams webhook URL to list", new LinkedHashMap<>(Map.of("teamsWebhookUrl", teamsWebhookUrl)));
            }
        }
    }

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;

        return Arrays.stream(input.toLowerCase().split(" "))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
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
        Set<String> supportedNotifications = Arrays.stream(NotificationChannel.values())
                .map(String::valueOf)
                .map(SettingsServiceImpl::toTitleCase)
                .collect(Collectors.toSet());
        Set<String> notificationsSelected = redisTemplate.opsForSet().members(SETTINGS_NOTIFICATION_TYPES_KEY).stream()
                .map(String::valueOf)
                .map(SettingsServiceImpl::toTitleCase)
                .collect(Collectors.toSet());

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

        Set<String> discordWebhookUrls = redisTemplate.opsForSet().members(SETTINGS_DISCORD_WEBHOOK_URL);
        Set<String> slackWebhookUrls = redisTemplate.opsForSet().members(SETTINGS_SLACK_WEBHOOK_URL);
        Set<String> teamsWebhookUrls = redisTemplate.opsForSet().members(SETTINGS_TEAMS_WEBHOOK_URL);

        return SettingsResponseDto.builder()
                .batchInterval(batchInterval)
                .lookbackWindow(lookbackWindow)
                .deduplicationWindow(deduplicationWindow)
                .emails(emails)
                .supportedNotifications(supportedNotifications)
                .notificationsSelected(notificationsSelected)
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
                .discordWebhookUrls(discordWebhookUrls)
                .slackWebhookUrls(slackWebhookUrls)
                .teamsWebhookUrls(teamsWebhookUrls)
                .build();
    }

    @Override
    public void updateSettings(SettingsRequestDto request) {
        updateBatchInterval(request.getBatchInterval());
        updateEnabledFlag(request.getEnabled());
        updateEmails(request.getEmail());
        updateSelectedNotifications(request.getNotificationsSelected());
        updateSupportedProducts(request.getProductAddition());
        updateSeverityThreshold(request.getSeverityThreshold());
        updateEarlyAlertsFlag(request.getEarlyAlerts());
        updateEmailProviderHost(request.getEmailProviderHost());
        updateEmailProviderPort(request.getEmailProviderPort());
        updateEmailProviderUsername(request.getEmailProviderUsername());
        updateEmailProviderPassword(request.getEmailProviderPassword());
        updateNvdApiKey(request.getNvdApiKey());
        updateDiscordWebhookUrls(request.getDiscordWebhookUrl());
        updateSlackWebhookUrls(request.getSlackWebhookUrl());
        updateTeamsWebhookUrls(request.getTeamsWebhookUrl());
    }
}
