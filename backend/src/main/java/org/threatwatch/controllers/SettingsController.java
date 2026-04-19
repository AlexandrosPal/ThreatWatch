package org.threatwatch.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.threatwatch.dtos.ApiResponseDto;
import org.threatwatch.dtos.SettingsRequestDto;
import org.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.CorrelatedResult;
import org.threatwatch.services.EmailService;
import org.threatwatch.services.NvdRestService;
import org.threatwatch.services.SettingsService;

import java.time.Instant;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;
    private final EmailService emailService;
    private final NvdRestService nvdRestService;

    public SettingsController(SettingsService settingsService, EmailService emailService, NvdRestService nvdRestService) {
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
