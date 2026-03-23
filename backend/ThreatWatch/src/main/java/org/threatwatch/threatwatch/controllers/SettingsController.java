package org.threatwatch.threatwatch.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.threatwatch.threatwatch.dtos.ApiResponseDto;
import org.threatwatch.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.threatwatch.services.SettingsService;

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
    public ApiResponseDto retrieveSettings() {

        SettingsResponseDto currentSettings = settingsService.getSettings();

        return new ApiResponseDto(
                Instant.now(),
                UUID.randomUUID().toString(),
                "ok",
                currentSettings
        );
    }

    @PatchMapping
    public ApiResponseDto updateSettings() {
        return new ApiResponseDto();
    }

}
