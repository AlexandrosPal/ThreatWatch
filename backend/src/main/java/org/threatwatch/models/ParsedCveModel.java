package org.threatwatch.models;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;

public class ParsedCveModel {
    private String cveId;
    private String description;
    private String severity;
    private String score;
    private String published;
    private ArrayList<String> references;

    public ParsedCveModel(String cveId, String description, String severity, String score, String published, ArrayList<String> references) {
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

    public ArrayList<String> getReferences() { return this.references; }
}
