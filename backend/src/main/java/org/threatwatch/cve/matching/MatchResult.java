package org.threatwatch.cve.matching;

public class MatchResult {

    private final MatchConfidence confidence;
    private final String reason;

    public MatchResult(MatchConfidence confidence, String reason) {
        this.confidence = confidence;
        this.reason = reason;
    }

    public MatchConfidence getConfidence() { return confidence; }

    public String getReason() { return reason; }
}
