package org.threatwatch.releases;

public class ReleasesResponseDto {

    private final String version;

    public ReleasesResponseDto(String version) {
        this.version = version;
    }

    public String getVersion() { return version; }
}
