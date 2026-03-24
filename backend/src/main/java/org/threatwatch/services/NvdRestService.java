package org.threatwatch.services;

import tools.jackson.databind.JsonNode;

public interface NvdRestService {

    public JsonNode getRecentVulnerabilitiesByProduct(String product, String publishStartDatetime, String publishEndDatetime);

}
