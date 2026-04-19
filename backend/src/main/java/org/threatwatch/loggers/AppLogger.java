package org.threatwatch.loggers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class AppLogger {

    private static final String CORRELATION_ID = "correlationId";

    private final Logger log;
    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    public AppLogger(Logger log) {
        this.log = log;
    }

    public static <T> CorrelatedResult<T> withCorrelationIdRun(Runnable action) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID, correlationId);

        try {
            action.run();
            return new CorrelatedResult<>(
                    correlationId,
                    null
            );
        } finally {
            MDC.remove(CORRELATION_ID);
        }
    }

    public static <T> CorrelatedResult<T> withCorrelationIdCall(Supplier<T> action) {
        String correlationId = UUID.randomUUID().toString();
        MDC.put(CORRELATION_ID, correlationId);

        try {
            return new CorrelatedResult<>(
                    correlationId,
                    action.get()
            );
        } finally {
            MDC.remove(CORRELATION_ID);
        }
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
            json.put(CORRELATION_ID, MDC.get(CORRELATION_ID));
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
