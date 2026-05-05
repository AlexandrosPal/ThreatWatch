package org.threatwatch.settings;

public interface SettingsService {

    void initializeDefaultsIfMissing();

    public SettingsResponseDto retrieveSettings();

    public void updateSettings(SettingsRequestDto request);
}
