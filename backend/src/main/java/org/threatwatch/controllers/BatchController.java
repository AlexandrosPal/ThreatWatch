package org.threatwatch.controllers;

import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.threatwatch.dtos.ApiResponseDto;
import org.threatwatch.services.BatchJobService;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private final BatchJobService batchJobService;

    public BatchController(BatchJobService batchJobService) { this.batchJobService = batchJobService; }

    @PostMapping("/run")
    public ResponseEntity<ApiResponseDto> runScheduler() throws IOException, InterruptedException, MessagingException {

        this.batchJobService.executeScheduledRun();

        return ResponseEntity.accepted().body(new ApiResponseDto(
                Instant.now(),
                UUID.randomUUID().toString(),
                "ok",
                "Scan initialized."
        ));
    }

}
