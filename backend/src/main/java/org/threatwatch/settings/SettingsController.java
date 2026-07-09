package org.threatwatch.settings;

import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.threatwatch.common.ApiResponseDto;
import org.threatwatch.cve.ingestion.NvdRestService;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.CorrelatedResult;
import org.threatwatch.notifications.NotificationChannel;
import org.threatwatch.notifications.NotificationRequestDto;
import org.threatwatch.notifications.discord.DiscordNotificationSender;
import org.threatwatch.notifications.email.EmailNotificationSender;
import org.threatwatch.notifications.slack.SlackNotificationSender;
import org.threatwatch.notifications.teams.TeamsNotificationSender;

import java.io.UnsupportedEncodingException;
import java.time.Instant;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;
    private final EmailNotificationSender emailService;
    private final DiscordNotificationSender discordService;
    private final SlackNotificationSender slackService;
    private final TeamsNotificationSender teamsService;
    private final NvdRestService nvdRestService;

    public SettingsController(SettingsService settingsService, EmailNotificationSender emailService, NvdRestService nvdRestService, DiscordNotificationSender discordService, SlackNotificationSender slackService, TeamsNotificationSender teamsService) {
        this.settingsService = settingsService;
        this.emailService = emailService;
        this.discordService = discordService;
        this.slackService = slackService;
        this.teamsService = teamsService;
        this.nvdRestService = nvdRestService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto> getSettings() {

        CorrelatedResult<SettingsResponseDto> settingsResult = AppLogger.withCorrelationIdCall(settingsService::retrieveSettings);

        return ResponseEntity.ok(new ApiResponseDto(
                Instant.now(),
                settingsResult.correlationId(),
                "ok",
                settingsResult.result()
        ));
    }

    @PatchMapping
    public ResponseEntity<ApiResponseDto> patchSettings(@RequestBody SettingsRequestDto request) {

        CorrelatedResult<Void> result = AppLogger.withCorrelationIdRun(() -> settingsService.updateSettings(request));

        return ResponseEntity.accepted().body(new ApiResponseDto(
                Instant.now(),
                result.correlationId(),
                "ok",
                "Settings updated"
        ));
    }

    @GetMapping("/email/connection")
    public ResponseEntity<ApiResponseDto> testEmailProviderConnection() {

        CorrelatedResult<Boolean> testEmailResult = AppLogger.withCorrelationIdCall(emailService::validEmailConnection);

        return ResponseEntity.accepted().body(new ApiResponseDto(
                Instant.now(),
                testEmailResult.correlationId(),
                "ok",
                testEmailResult.result()
        ));
    }

    @GetMapping("/nvd/connection")
    public ResponseEntity<ApiResponseDto> testNvdKeyConnection() {

        CorrelatedResult<Boolean> testNvdKeyResult = AppLogger.withCorrelationIdCall(this.nvdRestService::testApiKey);

        return ResponseEntity.accepted().body(new ApiResponseDto(
                Instant.now(),
                testNvdKeyResult.correlationId(),
                "ok",
                testNvdKeyResult.result()
        ));
    }

    @GetMapping("/notification/test")
    public ResponseEntity<ApiResponseDto> testNotificationConnection(@RequestParam NotificationChannel channel, @RequestParam(required = false) String webhookUrl) {

        CorrelatedResult<Boolean> testNotificationResult;
        NotificationRequestDto request = new NotificationRequestDto();
        SettingsResponseDto settings = this.settingsService.retrieveSettings();

        switch (channel) {
            case EMAIL:
                request.setEmails(settings.getEmails());
                request.setTitle("Test Email from ThreatWatch");
                request.setMessage("This is a test email from ThreatWatch.\nIf you see this then the test worked and the email reached your inbox.");
                testNotificationResult = AppLogger.withCorrelationIdCall(() -> {
                    try {
                        return emailService.testNotification(request);
                    } catch (MessagingException | UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                });
                break;
            case DISCORD:
                request.setTestWebhookUrl(webhookUrl);
                request.setMessage("This is a test email from ThreatWatch.\nIf you see this then the test worked and the email reached your inbox.");
                testNotificationResult = AppLogger.withCorrelationIdCall(() -> discordService.testNotification(request));
                break;
            case SLACK:
                request.setTestWebhookUrl(webhookUrl);
                request.setMessage("This is a test email from ThreatWatch.\nIf you see this then the test worked and the email reached your inbox.");
                testNotificationResult = AppLogger.withCorrelationIdCall(() -> slackService.testNotification(request));
                break;
            case TEAMS:
                request.setTestWebhookUrl(webhookUrl);
                request.setMessage("This is a test email from ThreatWatch.\nIf you see this then the test worked and the email reached your inbox.");
                testNotificationResult = AppLogger.withCorrelationIdCall(() -> teamsService.testNotification(request));
                break;
            default:
                testNotificationResult = AppLogger.withCorrelationIdCall(() -> false);
        }

        return ResponseEntity.accepted().body(new ApiResponseDto(
                Instant.now(),
                testNotificationResult.correlationId(),
                "ok",
                testNotificationResult.result()
        ));
    }
}
