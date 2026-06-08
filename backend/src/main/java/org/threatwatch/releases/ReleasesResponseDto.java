package org.threatwatch.releases;

public class ReleasesResponseDto {

    private final String currentVersion;
    private final String latestVersion;
    private final boolean updateAvailable;

    public ReleasesResponseDto(String currentVersion, String latestVersion, boolean updateAvailable) {
        this.currentVersion = currentVersion;
        this.latestVersion = latestVersion;
        this.updateAvailable = updateAvailable;
    }

    public String getCurrentVersion() { return currentVersion; }

    public String getLatestVersion() { return latestVersion; }

    public boolean getUpdateAvailable() { return updateAvailable; }
}
