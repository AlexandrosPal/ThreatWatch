package org.threatwatch.executions;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.threatwatch.cve.model.CveAlertItem;
import org.threatwatch.notifications.NotificationChannel;
import org.threatwatch.settings.SettingsResponseDto;
import org.threatwatch.settings.SettingsService;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PastExecutionService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static String PAST_EXECUTION_TEMPLATE_KEY = "execution:";
    private static final String LAST_EXECUTIONS_KEY = "execution:lastExecutions";

    public PastExecutionService(StringRedisTemplate redisTemplate, SettingsService settingsService, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void savePastExecution(List<CveAlertItem> cvesToSend, SettingsResponseDto settings, PastExecutionStatus status) {
        String timestamp = String.valueOf(Instant.now());
        String pastExecutionRedisKey = PAST_EXECUTION_TEMPLATE_KEY + timestamp;
        Set<NotificationChannel> notificationsSelected = settings.getNotificationsSelected().stream()
                .map(String::toUpperCase)
                .map(NotificationChannel::valueOf)
                .collect(Collectors.toSet());

        HashMap<String, Object> notificationsSent = new HashMap<>();
        for (NotificationChannel channel : notificationsSelected) {
            switch (channel) {
                case EMAIL:
                    HashMap<String, Object> emailInformation = new HashMap<>();
                    emailInformation.put("providerHost", settings.getEmailProviderHost());
                    emailInformation.put("providerPort", settings.getEmailProviderPort());
                    emailInformation.put("providerUsername", settings.getEmailProviderUsername());
                    emailInformation.put("targetEmails", settings.getEmails());

                    notificationsSent.put("email", emailInformation);
                    break;
                case DISCORD:
                    HashMap<String, Object> discordInformation = new HashMap<>();
                    discordInformation.put("webhookUrls", settings.getDiscordWebhookUrls());

                    notificationsSent.put("discord", discordInformation);
                    break;
                case SLACK:
                    HashMap<String, Object> slackInformation = new HashMap<>();
                    slackInformation.put("webhookUrls", settings.getSlackWebhookUrls());

                    notificationsSent.put("slack", slackInformation);
                    break;
                case TEAMS:
                    HashMap<String, Object> teamsInformation = new HashMap<>();
                    teamsInformation.put("webhookUrls", settings.getTeamsWebhookUrls());

                    notificationsSent.put("teams", teamsInformation);
                    break;
            }
        }

        PastExecutionRecord pastExecution = new PastExecutionRecord(
                cvesToSend.size(),
                timestamp,
                cvesToSend,
                notificationsSent,
                status
        );

        String pastExecutionString = objectMapper.writeValueAsString(pastExecution);
        redisTemplate.opsForValue().set(pastExecutionRedisKey, pastExecutionString, 30, TimeUnit.DAYS);
        redisTemplate.opsForList().leftPush(LAST_EXECUTIONS_KEY, pastExecutionRedisKey);
        redisTemplate.opsForList().trim(LAST_EXECUTIONS_KEY, 0, 19);
    }

    private PastExecutionResponseDto retrievePastExecution(String pastExecutionId) {
        try {
            String pastExecution = redisTemplate.opsForValue().get(pastExecutionId);

            return objectMapper.readValue(pastExecution, PastExecutionResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize past execution", e);
        }
    }

    public List<PastExecutionResponseDto> retrievePastExecutions(Integer limit) {
        List<String> lastExecutions = redisTemplate.opsForList().range(LAST_EXECUTIONS_KEY, 0, limit-1);

        return lastExecutions.stream()
                .map(this::retrievePastExecution)
                .toList();
    }

}
