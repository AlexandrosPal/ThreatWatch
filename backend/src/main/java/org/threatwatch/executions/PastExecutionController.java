package org.threatwatch.executions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.threatwatch.common.ApiResponseDto;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.CorrelatedResult;

import java.time.Instant;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/past-executions")
public class PastExecutionController {

    private final PastExecutionService pastExecutionService;

    public PastExecutionController(PastExecutionService pastExecutionService) {
        this.pastExecutionService = pastExecutionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto> getPastExecutions(@RequestParam(required = false, defaultValue = "5") Integer limit) {

        CorrelatedResult<List<PastExecutionResponseDto>> settingsResult = AppLogger.withCorrelationIdCall(() -> pastExecutionService.retrievePastExecutions(limit));

        return ResponseEntity.ok(new ApiResponseDto(
                Instant.now(),
                settingsResult.correlationId(),
                "ok",
                pastExecutionService.retrievePastExecutions(limit)
        ));
    }
}
