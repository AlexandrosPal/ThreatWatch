package org.threatwatch.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class AppLogger {

    private final Logger log;
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    public AppLogger(Logger log) {
        this.log = log;
    }

    public void info(String event, String message, Object data) {
        log.info(toJson("INFO", event, message, data));
    }

    public void error(String event, String message, Object data) {
        log.error(toJson("ERROR", event, message, data));
    }

    private String toJson(String level, String event, String message, Object data) {
        try {
            Map<String, Object> json = new LinkedHashMap<>();
            json.put("event", event);
            json.put("level", level);
            json.put("timestamp", dateTimeFormatter.format(Instant.now()));
            json.put("message", message);

            if (Objects.equals(level, "INFO")) {
                json.put("status", "ok");
            } else if (Objects.equals(level, "ERROR")) {
                json.put("status", "error");
            }

            json.put("data", data);

            return objectMapper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            return "{\"event\":\"log_serialization_failed\"}";
        }
    }

}
