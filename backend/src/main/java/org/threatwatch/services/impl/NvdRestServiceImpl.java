package org.threatwatch.services.impl;

import tools.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.threatwatch.services.NvdRestService;

@Service
public class NvdRestServiceImpl implements NvdRestService {

    private final RestClient nvdRestClient;

    public NvdRestServiceImpl(RestClient nvdRestClient) {
        this.nvdRestClient = nvdRestClient;
    }

    @Override
    public JsonNode getRecentVulnerabilitiesByProduct(String product, String publishStartDatetime, String publishEndDatetime) {

        JsonNode response = nvdRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/cves/2.0")
                        .queryParam("keywordSearch", product)
                        .queryParam("pubStartDate", publishStartDatetime)
                        .queryParam("pubEndDate", publishEndDatetime)
                        .build())
                .retrieve()
                .body(JsonNode.class);

        return response;
    }

}
