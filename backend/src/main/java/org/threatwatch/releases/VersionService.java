package org.threatwatch.releases;

import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

@Service
public class VersionService {

    private final BuildProperties buildProperties;

    public VersionService(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    public String getCurrentVersion() {
        return buildProperties.getVersion();
    }

    public boolean updateAvailable(String currentVersion, String latestVersion) {

        String[] current = currentVersion.split("\\.");
        String[] latest = latestVersion.replaceFirst("^v", "").split("\\.");

        int currentMajor = Integer.parseInt(current[0]);
        int currentMinor = Integer.parseInt(current[1]);

        int latestMajor = Integer.parseInt(latest[0]);
        int latestMinor = Integer.parseInt(latest[1]);

        return latestMajor > currentMajor
                || (latestMajor == currentMajor && latestMinor > currentMinor);
    }
}
