package org.threatwatch.threatwatch.services.impl;

import org.threatwatch.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.threatwatch.services.SettingsService;

public class SettingsServiceImpl implements SettingsService {

    @Override
    public SettingsResponseDto getSettings() {
        return new SettingsResponseDto();
    }
}
