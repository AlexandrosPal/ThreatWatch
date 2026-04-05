package org.threatwatch.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.threatwatch.dtos.ApiResponseDto;
import org.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.dtos.SettingsRequestDto;
import org.threatwatch.services.EmailService;
import org.threatwatch.services.NvdRestService;
import org.threatwatch.services.SettingsService;

import java.time.Instant;
import java.util.UUID;

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

        SettingsResponseDto currentSettings = settingsService.retrieveSettings();

        return ResponseEntity.ok(new ApiResponseDto(
                Instant.now(),
                UUID.randomUUID().toString(),
                "ok",
                currentSettings
        ));
    }

    @PatchMapping
    public ResponseEntity<ApiResponseDto> patchSettings(@RequestBody SettingsRequestDto request) {

        settingsService.updateSettings(request);

        return ResponseEntity.accepted().body(new ApiResponseDto(
                Instant.now(),
                UUID.randomUUID().toString(),
                "ok",
                "Settings updated"
        ));
    }

    @GetMapping("/email/connection")
    public ResponseEntity<ApiResponseDto> testEmailProviderConnection() {

        return ResponseEntity.accepted().body(new ApiResponseDto(
                Instant.now(),
                UUID.randomUUID().toString(),
                "ok",
                emailService.validEmailConnection()
        ));
    }

    @GetMapping("/nvd/connection")
    public ResponseEntity<ApiResponseDto> testNvdKeyConnection() {

        return ResponseEntity.accepted().body(new ApiResponseDto(
                Instant.now(),
                UUID.randomUUID().toString(),
                "ok",
                this.nvdRestService.testApiKey()
        ));
    }

}
