package org.threatwatch.services;

public interface CveStateService {

    boolean isNewCve(String cveId);

    void markCveAsSeen(String cveId);

}
