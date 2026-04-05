package org.threatwatch.models;

import java.util.List;

public class ParsedCveModel {
    private final String cveId;
    private final String description;
    private final String severity;
    private final String score;
    private final String published;
    private final List<String> references;

    public ParsedCveModel(String cveId, String description, String severity, String score, String published, List<String> references) {
        this.cveId = cveId;
        this.description = description;
        this.severity = severity;
        this.score = score;
        this.published = published;
        this.references = references;
    }

    public String getCveId() { return this.cveId; }

    public String getDescription() { return this.description; }

    public String getSeverity() { return this.severity; }

    public String getScore() { return this.score; }

    public String getPublished() { return this.published; }

    public List<String> getReferences() { return this.references; }
}
