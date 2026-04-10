package org.threatwatch.controllers;

import jakarta.mail.MessagingException;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.threatwatch.dtos.ApiResponseDto;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.LogEvents;
import org.threatwatch.services.BatchJobService;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private String correlationId = UUID.randomUUID().toString();

    private final BatchJobService batchJobService;

    private static final AppLogger appLogger = new AppLogger(LoggerFactory.getLogger(BatchController.class));

    public BatchController(BatchJobService batchJobService) { this.batchJobService = batchJobService; }

    @PostMapping("/run")
    public ResponseEntity<ApiResponseDto> runScheduler() throws IOException, InterruptedException, MessagingException {

        appLogger.info(LogEvents.BATCH_RUN, "Manually initiated scheduler run", new LinkedHashMap<>());
        MDC.put("correlationId", correlationId);
        this.batchJobService.executeScheduledRun();

        return ResponseEntity.accepted().body(new ApiResponseDto(
                Instant.now(),
                UUID.randomUUID().toString(),
                "ok",
                "Scan initialized."
        ));
    }

}
