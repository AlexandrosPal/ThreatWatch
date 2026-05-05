package org.threatwatch.settings;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.threatwatch.common.ApiResponseDto;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.CorrelatedResult;
import org.threatwatch.notifications.email.EmailNotificationSender;
import org.threatwatch.cve.ingestion.NvdRestService;

import java.time.Instant;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;
    private final EmailNotificationSender emailService;
    private final NvdRestService nvdRestService;

    public SettingsController(SettingsService settingsService, EmailNotificationSender emailService, NvdRestService nvdRestService) {
        this.settingsService = settingsService;
        this.emailService = emailService;
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
}
