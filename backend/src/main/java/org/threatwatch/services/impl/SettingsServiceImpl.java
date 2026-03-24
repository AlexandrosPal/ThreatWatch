package org.threatwatch.services.impl;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.threatwatch.dtos.SettingsRequestDto;
import org.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.models.NotificationTypes;
import org.threatwatch.services.SettingsService;

import java.util.Set;

@Service
public class SettingsServiceImpl implements SettingsService {

    private final StringRedisTemplate redisTemplate;

    private String batchInterval;
    private String lookbackWindow;
    private String deduplicationWindow;
    private Set<String> emails;
    private Set<String> notificationTypes;
    private String enabled;

    public SettingsServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public SettingsResponseDto retrieveSettings() {

        this.batchInterval = redisTemplate.opsForValue().get("settings:batchInterval");
        this.lookbackWindow = redisTemplate.opsForValue().get("settings:lookbackWindow");
        this.deduplicationWindow = redisTemplate.opsForValue().get("settings:deduplicationWindow");
        this.enabled = redisTemplate.opsForValue().get("settings:enabled");

        this.emails = redisTemplate.opsForSet().members("settings:emails");
        this.notificationTypes = redisTemplate.opsForSet().members("settings:notificationTypes");

        return new SettingsResponseDto(this.batchInterval, this.lookbackWindow, this.deduplicationWindow, this.emails, this.notificationTypes, this.enabled);
    }

    @Override
    public void updateSettings(SettingsRequestDto request) {
        Integer batchInterval = request.getBatchInterval();
        String enabled = request.getEnabled();
        String email = request.getEmail();
        NotificationTypes notificationType = request.getNotificationType();

        if (batchInterval != null) {
            redisTemplate.opsForValue().set("settings:batchInterval", String.valueOf(batchInterval));
            redisTemplate.opsForValue().set("settings:lookbackWindow", String.valueOf(batchInterval * 2));
            redisTemplate.opsForValue().set("settings:deduplicationWindow", String.valueOf(batchInterval * 2.4));
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
    }
}
