package org.threatwatch.releases;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.threatwatch.common.ApiResponseDto;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.CorrelatedResult;
import org.threatwatch.releases.ingestion.GitHubRestService;

import java.time.Instant;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/version")
public class ReleasesController {

    private final GitHubRestService githubRestService;

    public ReleasesController(GitHubRestService githubRestService) {
        this.githubRestService = githubRestService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto> getLatestVersion() {

        CorrelatedResult<ReleasesResponseDto> settingsResult = AppLogger.withCorrelationIdCall(githubRestService::retrieveLatestRelease);

        return ResponseEntity.ok(new ApiResponseDto(
                Instant.now(),
                settingsResult.correlationId(),
                "ok",
                settingsResult.result()
        ));
    }
}
