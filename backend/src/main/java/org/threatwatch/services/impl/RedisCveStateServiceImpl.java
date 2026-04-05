package org.threatwatch.services.impl;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.threatwatch.services.CveStateService;
import org.threatwatch.services.SettingsService;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCveStateServiceImpl implements CveStateService {

    private final StringRedisTemplate redisTemplate;
    private final SettingsService settingsService;


    public RedisCveStateServiceImpl(StringRedisTemplate redisTemplate, SettingsService settingsService) {
        this.redisTemplate = redisTemplate;
        this.settingsService = settingsService;
    }

    @Override
    public boolean isNewCve(String cveId) {
        return !redisTemplate.hasKey(cveId);
    }

    @Override
    public void markCveAsSeen(String cveId) {
        redisTemplate.opsForValue().set(cveId, Instant.now().toString(), Long.parseLong(settingsService.retrieveSettings().getDeduplicationWindow()), TimeUnit.SECONDS);
    }

}
