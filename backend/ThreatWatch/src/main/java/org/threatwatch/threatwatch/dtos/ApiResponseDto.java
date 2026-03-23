package org.threatwatch.threatwatch.dtos;

import java.time.Instant;

public class ApiResponseDto {

    private Instant timestamp;
    private String uuid;
    private String status;
    private Object response;

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
