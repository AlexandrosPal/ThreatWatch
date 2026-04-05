package org.threatwatch.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationTypes {
    EMAIL;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static NotificationTypes fromJson(String value) {
        return NotificationTypes.valueOf(value.toUpperCase());
    }
}
