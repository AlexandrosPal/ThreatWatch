package org.threatwatch.schedulers;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.threatwatch.services.BatchJobService;
import org.threatwatch.services.SettingsService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class DynamicScheduler {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final BatchJobService batchJobService;
    private final SettingsService settingsService;

    public DynamicScheduler(BatchJobService batchJobService,
                            SettingsService settingsService) {
        this.batchJobService = batchJobService;
        this.settingsService = settingsService;
    }

    @PostConstruct
    public void start() throws Exception {
        scheduleNextRun();
    }

    private void scheduleNextRun() throws Exception {

        Integer delay = Integer.valueOf(settingsService.retrieveSettings().getBatchInterval());
        Boolean enabled = Boolean.valueOf(settingsService.retrieveSettings().getEnabled());

        batchJobService.executeScheduledRun();

        executor.schedule(() -> {
            try {
                if (enabled) {
                    batchJobService.executeScheduledRun();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    scheduleNextRun();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }, delay, TimeUnit.SECONDS);
    }
}