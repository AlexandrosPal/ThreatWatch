package org.threatwatch.services.impl;

import org.springframework.web.client.RestClientResponseException;
import org.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.services.SettingsService;
import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.threatwatch.services.NvdRestService;

@Service
public class NvdRestServiceImpl implements NvdRestService {

    private final SettingsService settingsService;
    private final RestClient nvdRestClient;

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

            return true;

        } catch (RestClientResponseException e) {
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
