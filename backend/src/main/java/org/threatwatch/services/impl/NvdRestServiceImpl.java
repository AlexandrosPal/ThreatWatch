package org.threatwatch.services.impl;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.LogEvents;
import org.threatwatch.services.NvdRestService;
import org.threatwatch.services.SettingsService;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

@Service
public class NvdRestServiceImpl implements NvdRestService {

    private final SettingsService settingsService;
    private final RestClient nvdRestClient;

    private static final AppLogger appLogger = new AppLogger(LoggerFactory.getLogger(NvdRestServiceImpl.class));

    public NvdRestServiceImpl(SettingsService settingsService, RestClient nvdRestClient) {
        this.settingsService = settingsService;
        this.nvdRestClient = nvdRestClient;
    }

    public Boolean testApiKey() {

        try {
            SettingsResponseDto settings = settingsService.retrieveSettings();
            String nvdApiKey = settings.getNvdApiKey();

            nvdRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cves/2.0")
                        .queryParam("keywordSearch", "apiKeyTest")
                        .build())
                .header("apiKey", nvdApiKey)
                .retrieve()
                .toBodilessEntity();

            appLogger.info(LogEvents.NVD_REST_CLIENT,"Validated NVD API key", new HashMap<>());

            return true;

        } catch (RestClientResponseException e) {
            appLogger.error(LogEvents.NVD_REST_CLIENT,"Error while sending NVD test call", new HashMap<>(Map.of("error", e.getMessage())));
            return false;
        }
    }

    @Override
    public JsonNode getRecentVulnerabilitiesByProduct(String product, String publishStartDatetime, String publishEndDatetime) {

        SettingsResponseDto settings = settingsService.retrieveSettings();
        String nvdApiKey = settings.getNvdApiKey();

        RestClient.RequestHeadersSpec<?> request = nvdRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cves/2.0")
                        .queryParam("keywordSearch", product)
                        .queryParam("pubStartDate", publishStartDatetime)
                        .queryParam("pubEndDate", publishEndDatetime)
                        .build());

        if (nvdApiKey != null && !nvdApiKey.isBlank()) {
            request = request.header("apiKey", nvdApiKey);
        }

        return request
                .retrieve()
                .body(JsonNode.class);
    }

}
