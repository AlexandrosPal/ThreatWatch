package org.threatwatch.common;

import java.time.Instant;

public class ApiResponseDto {

    private final Instant timestamp;
    private final String uuid;
    private final String status;
    private final Object response;

    public ApiResponseDto(Instant timestamp, String uuid, String status, Object response) {
        this.timestamp = timestamp;
        this.uuid = uuid;
        this.status = status;
        this.response = response;
    }

    public Instant getTimestamp() { return timestamp; }

    public String getUuid() { return uuid; }

    public String getStatus() { return status; }

    public Object getResponse() { return response; }
}
