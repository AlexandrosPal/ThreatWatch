package org.threatwatch.releases.ingestion;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.LogEvents;
import org.threatwatch.releases.ReleasesResponseDto;
import org.threatwatch.releases.VersionService;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;

@Service
public class GitHubRestService {

    private final RestClient githubRestClient;
    private final VersionService versionService;

    private static final AppLogger appLogger = new AppLogger(LoggerFactory.getLogger(GitHubRestService.class));

    public GitHubRestService(@Qualifier("githubRestClient") RestClient githubRestClient, VersionService versionService) {
        this.githubRestClient = githubRestClient;
        this.versionService = versionService;
    }

    public ReleasesResponseDto retrieveLatestRelease() {

        RestClient.RequestHeadersSpec<?> request = githubRestClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/repos/AlexandrosPal/ThreatWatch/releases/latest")
                        .build());

        appLogger.info(LogEvents.GITHUB_REST_CLIENT,"Sent request to GitHub for retrieveing latest release", new HashMap<>());

        JsonNode response = request
                .retrieve()
                .body(JsonNode.class);

        String currentVersion = versionService.getCurrentVersion();
        String latestVersion = response.path("name").asString();

        boolean updateAvailable = versionService.updateAvailable(currentVersion, latestVersion);

        return new ReleasesResponseDto(currentVersion, latestVersion, updateAvailable);
    }
}
