package org.threatwatch.services;

import org.threatwatch.dtos.SettingsRequestDto;
import org.threatwatch.dtos.SettingsResponseDto;

public interface SettingsService {

    void initializeDefaultsIfMissing();

    public SettingsResponseDto retrieveSettings();

    public void updateSettings(SettingsRequestDto request);
}
