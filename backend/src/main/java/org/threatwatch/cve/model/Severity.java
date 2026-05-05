package org.threatwatch.cve.model;

public enum Severity {

    CRITICAL, HIGH, MEDIUM, LOW, UNKNOWN;

    public String toEmoji() {
        return switch (this) {
            case CRITICAL -> "🔥";
            case HIGH -> "🔴";
            case MEDIUM -> "🟠";
            case LOW -> "🟢";
            default -> "⚪";
        };
    }
}
