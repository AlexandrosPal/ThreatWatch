package org.threatwatch.executions;

import org.threatwatch.cve.model.CveAlertItem;

import java.util.HashMap;
import java.util.List;

public class PastExecutionResponseDto {

    private final Integer totalCves;
    private final String timestamp;
    private final List<CveAlertItem> cves;
    private final HashMap<String, HashMap> notificationsSent;

    public PastExecutionResponseDto(Integer totalCves, String timestamp, List<CveAlertItem> cves, HashMap<String, HashMap> notificationsSent) {
        this.totalCves = totalCves;
        this.timestamp = timestamp;
        this.cves = cves;
        this.notificationsSent = notificationsSent;
    }

    public Integer getTotalCves() { return totalCves; }

    public String getTimestamp() { return timestamp; }

    public List<CveAlertItem> getCves() { return cves; }

    public HashMap<String, HashMap> getNotificationsSent() { return notificationsSent; }

}
