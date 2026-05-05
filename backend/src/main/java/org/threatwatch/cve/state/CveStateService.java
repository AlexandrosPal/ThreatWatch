package org.threatwatch.cve.state;

public interface CveStateService {

    boolean isNewCve(String cveId);

    void markCveAsSeen(String cveId);

}
