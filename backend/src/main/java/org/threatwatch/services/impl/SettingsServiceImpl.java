package org.threatwatch.services.impl;

import org.springframework.stereotype.Service;
import org.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.services.SettingsService;

@Service
public class SettingsServiceImpl implements SettingsService {

    @Override
    public SettingsResponseDto getSettings() {
        return new SettingsResponseDto();
    }
}
