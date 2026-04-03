package org.threatwatch.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.threatwatch.dtos.ApiResponseDto;
import org.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.dtos.SettingsRequestDto;
import org.threatwatch.services.EmailService;
import org.threatwatch.services.SettingsService;

import java.time.Instant;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    SettingsService settingsService;
    EmailService emailService;

    public SettingsController(SettingsService settingsService, EmailService emailService) {
        this.settingsService = settingsService;
        this.emailService = emailService;
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

}
