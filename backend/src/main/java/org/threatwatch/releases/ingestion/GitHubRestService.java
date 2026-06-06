package org.threatwatch.releases.ingestion;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.LogEvents;
import org.threatwatch.releases.ReleasesResponseDto;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;

@Service
public class GitHubRestService {

    private final RestClient githubRestClient;

    private static final AppLogger appLogger = new AppLogger(LoggerFactory.getLogger(GitHubRestService.class));

    public GitHubRestService(@Qualifier("githubRestClient") RestClient githubRestClient) {
        this.githubRestClient = githubRestClient;
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

        String latestVersion = response.path("name").asString();

        return new ReleasesResponseDto(latestVersion);
    }
}
