package org.threatwatch.notifications;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationChannel {
    EMAIL,
    DISCORD,
    SLACK,
    TEAMS;


    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static NotificationChannel fromJson(String value) {
        return NotificationChannel.valueOf(value.toUpperCase());
    }
}
