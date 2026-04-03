package org.threatwatch.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.threatwatch.dtos.ApiResponseDto;
import org.threatwatch.dtos.SettingsRequestDto;
import org.threatwatch.services.BatchJobService;

import java.time.Instant;
import java.util.UUID;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/batch")
public class BatchController {

    private final BatchJobService batchJobService;

    public BatchController(BatchJobService batchJobService) { this.batchJobService = batchJobService; }

    @PostMapping("/run")
    public ResponseEntity<ApiResponseDto> runScheduler() throws Exception {

        this.batchJobService.executeScheduledRun();

        return ResponseEntity.accepted().body(new ApiResponseDto(
                Instant.now(),
                UUID.randomUUID().toString(),
                "ok",
                "Scan initialized."
        ));
    }

}
