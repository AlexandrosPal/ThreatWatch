package org.threatwatch.models;

import tools.jackson.databind.JsonNode;

public class ParsedCveModel {
    private String cveId;
    private String description;
    private JsonNode metrics;
    private JsonNode cvssData;
    private String severity;
    private String score;
    private String published;

    public ParsedCveModel(String cveId, String description, JsonNode metrics, JsonNode cvssData, String severity, String score, String published) {
        this.cveId = cveId;
        this.description = description;
        this.metrics = metrics;
        this.cvssData = cvssData;
        this.severity = severity;
        this.score = score;
        this.published = published;
    }

    public String getCveId() { return cveId; }

    public String getDescription() { return description; }

    public String getSeverity() { return severity; }

    public String getScore() { return score; }

    public String getPublished() { return published; }
}
