package org.threatwatch.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.threatwatch.dtos.ApiResponseDto;
import org.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.services.SettingsService;

import java.time.Instant;
import java.util.UUID;

@Controller
@RequestMapping("/api/settings")
public class SettingsController {

    SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto> retrieveSettings() {

        SettingsResponseDto currentSettings = settingsService.getSettings();

        return ResponseEntity.ok(new ApiResponseDto(
                Instant.now(),
                UUID.randomUUID().toString(),
                "ok",
                currentSettings
        ));
    }

    @PatchMapping
    public ResponseEntity<ApiResponseDto> updateSettings() {

        return ResponseEntity.ok(new ApiResponseDto(
                Instant.now(),
                UUID.randomUUID().toString(),
                "ok",
                "Settings updated"
        ));
    }

}
