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

    private String batchInterval;
    private String lookbackWindow;
    private String deduplicationWindow;
    private Set<String> emails;
    private Set<String> notificationTypes;
    private String enabled;
    private Set<String> productsSelected;
    private String severityThreshold;
    private String earlyAlerts;
    private String emailProviderHost;
    private String emailProviderPort;
    private String emailProviderUsername;
    private String emailProviderPassword;

    public SettingsServiceImpl(StringRedisTemplate redisTemplate, ProductsService productsService) {
        this.redisTemplate = redisTemplate;
        this.productsService = productsService;
    }

    @Override
    public SettingsResponseDto retrieveSettings() {

        this.batchInterval = redisTemplate.opsForValue().get("settings:batchInterval");
        this.lookbackWindow = redisTemplate.opsForValue().get("settings:lookbackWindow");
        this.deduplicationWindow = redisTemplate.opsForValue().get("settings:deduplicationWindow");
        this.enabled = redisTemplate.opsForValue().get("settings:enabled");
        this.severityThreshold = redisTemplate.opsForValue().get("settings:severityThreshold");
        this.earlyAlerts = redisTemplate.opsForValue().get("settings:earlyAlerts");

        this.emails = redisTemplate.opsForSet().members("settings:emails");
        this.notificationTypes = redisTemplate.opsForSet().members("settings:notificationTypes");

        Set<String> supportedProducts = productsService.getProducts().keySet();
        this.productsSelected = redisTemplate.opsForSet().members("settings:productsSelected");

        this.emailProviderHost = redisTemplate.opsForValue().get("settings:emailProviderHost");
        this.emailProviderPort = redisTemplate.opsForValue().get("settings:emailProviderPort");
        this.emailProviderUsername = redisTemplate.opsForValue().get("settings:emailProviderUsername");
        this.emailProviderPassword = redisTemplate.opsForValue().get("settings:emailProviderPassword");

        return new SettingsResponseDto(this.batchInterval, this.lookbackWindow, this.deduplicationWindow, this.emails, this.notificationTypes, this.enabled, supportedProducts, this.productsSelected, this.severityThreshold, this.earlyAlerts, this.emailProviderHost, this.emailProviderPort, this.emailProviderUsername, this.emailProviderPassword);
    }

    @Override
    public void updateSettings(SettingsRequestDto request) {
        Integer batchInterval = request.getBatchInterval();
        String enabled = request.getEnabled();
        String email = request.getEmail();
        NotificationTypes notificationType = request.getNotificationType();
        String productAddition = request.getProductAddition();
        String severityThreshold = request.getSeverityThreshold();
        String earlyAlerts = request.getEarlyAlerts();
        String emailProviderHost = request.getEmailProviderHost();
        String emailProviderPort = request.getEmailProviderPort();
        String emailProviderUsername = request.getEmailProviderUsername();
        String emailProviderPassword = request.getEmailProviderPassword();

        if (batchInterval != null) {
            redisTemplate.opsForValue().set("settings:batchInterval", String.valueOf(batchInterval).replace(".0", ""));
            redisTemplate.opsForValue().set("settings:lookbackWindow", String.valueOf(batchInterval * 2).replace(".0", ""));
            redisTemplate.opsForValue().set("settings:deduplicationWindow", String.valueOf(batchInterval * 2.4).replace(".0", ""));
        }

        if (enabled != null) {
            redisTemplate.opsForValue().set("settings:enabled", enabled);
        }

        if (email != null) {
            if (redisTemplate.opsForSet().isMember("settings:emails", email)) {
                redisTemplate.opsForSet().remove("settings:emails", email);
            } else {
                redisTemplate.opsForSet().add("settings:emails", email);
            }
        }

        if (notificationType != null) {
            if (redisTemplate.opsForSet().isMember("settings:notificationTypes", String.valueOf(notificationType))) {
                redisTemplate.opsForSet().remove("settings:notificationTypes", String.valueOf(notificationType));
            } else {
                redisTemplate.opsForSet().add("settings:notificationTypes", String.valueOf(notificationType));
            }
        }

        if (productAddition != null) {
            String normalizedProduct = productsService.normalizeProduct(
                    request.getProductAddition()
            );

            if (redisTemplate.opsForSet().isMember("settings:productsSelected", productAddition)) {
                redisTemplate.opsForSet().remove("settings:productsSelected", productAddition);
            } else {
                redisTemplate.opsForSet().add("settings:productsSelected", productAddition);
            }
        }

        if (severityThreshold != null) {
            redisTemplate.opsForValue().set("settings:severityThreshold", severityThreshold);
        }

        if (earlyAlerts != null) {
            redisTemplate.opsForValue().set("settings:earlyAlerts", earlyAlerts);
        }

        if (emailProviderHost != null) {
            redisTemplate.opsForValue().set("settings:emailProviderHost", String.valueOf(emailProviderHost));
        }

        if (emailProviderPort != null) {
            redisTemplate.opsForValue().set("settings:emailProviderPort", emailProviderPort);
        }

        if (emailProviderUsername != null) {
            redisTemplate.opsForValue().set("settings:emailProviderUsername", emailProviderUsername);
        }

        if (emailProviderPassword != null) {
            redisTemplate.opsForValue().set("settings:emailProviderPassword", emailProviderPassword);
        }
    }
}
