package org.threatwatch.scheduler;

import jakarta.mail.MessagingException;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.threatwatch.common.ApiResponseDto;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.LogEvents;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private final BatchJobService batchJobService;

    private static final AppLogger appLogger = new AppLogger(LoggerFactory.getLogger(BatchController.class));

    public BatchController(BatchJobService batchJobService) { this.batchJobService = batchJobService; }

    @PostMapping("/run")
    public ResponseEntity<ApiResponseDto> runScheduler() throws IOException, InterruptedException, MessagingException {

        String correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        appLogger.info(LogEvents.BATCH_RUN, "Manually initiated scheduler run", new LinkedHashMap<>());
        this.batchJobService.executeScheduledRun();
        MDC.clear();

        return ResponseEntity.accepted().body(new ApiResponseDto(
                Instant.now(),
                UUID.randomUUID().toString(),
                "ok",
                "Scan initialized."
        ));
    }

}
