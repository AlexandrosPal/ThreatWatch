package org.threatwatch.services;

import tools.jackson.databind.JsonNode;

public interface NvdRestService {

    public Boolean testApiKey();

    public JsonNode getRecentVulnerabilitiesByProduct(String product, String publishStartDatetime, String publishEndDatetime);

}
