package org.threatwatch.releases.ingestion;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.LogEvents;
import org.threatwatch.releases.ReleasesResponseDto;
import org.threatwatch.releases.VersionService;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
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

        try {
            RestClient.RequestHeadersSpec<?> request = githubRestClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/AlexandrosPal/ThreatWatch/main/backend/pom.xml")
                            .build());

            appLogger.info(LogEvents.GITHUB_REST_CLIENT, "Sent request to GitHub for retrieveing latest release", new HashMap<>());

            String response = request
                    .retrieve()
                    .body(String.class);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document pom = builder.parse(
                    new ByteArrayInputStream(response.getBytes()));

            XPath xpath = XPathFactory.newInstance().newXPath();

            String latestVersion = xpath.evaluate(
                    "/project/version",
                    pom);

            String currentVersion = versionService.getCurrentVersion();

            boolean updateAvailable = versionService.updateAvailable(currentVersion, latestVersion);

            return new ReleasesResponseDto(currentVersion, latestVersion, updateAvailable);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve latest version", e);
        }
    }
}
