package org.threatwatch.loggers;

public record CorrelatedResult<T>(
        String correlationId,
        T result
) {}