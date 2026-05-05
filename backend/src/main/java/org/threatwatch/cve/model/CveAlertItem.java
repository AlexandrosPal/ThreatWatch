package org.threatwatch.cve.model;

public class CveAlertItem {

    private final String product;
    private final String id;
    private final String description;
    private final Severity severity;
    private final Float score;
    private final String published;

    public CveAlertItem(String product, String id, String description, Severity severity, Float score, String published) {
        this.product = product;
        this.id = id;
        this.description = description;
        this.severity = severity;
        this.score = score;
        this.published = published;
    }

    public String getProduct() { return product; }

    public String getId() { return id; }

    public String getDescription() { return description; }

    public Severity getSeverity() { return severity; }

    public Float getScore() { return score; }

    public String getPublished() { return published; }
}
