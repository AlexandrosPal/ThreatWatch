package org.threatwatch.schedulers;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Component;
import org.threatwatch.services.BatchJobService;
import org.threatwatch.services.SettingsService;

import java.io.IOException;
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
    public void start() {
        scheduleNextRun();
    }

    private void scheduleNextRun() {
        int delay = Integer.parseInt(settingsService.retrieveSettings().getBatchInterval());

        executor.schedule(() -> {
            try {
                boolean enabled = Boolean.parseBoolean(settingsService.retrieveSettings().getEnabled());

                if (enabled) {
                    batchJobService.executeScheduledRun();
                }
            } catch (IOException | MessagingException e) {
                System.err.println("Error while running scheduled run");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                try {
                    scheduleNextRun();
                } catch (Exception e) {
                    System.err.println("Error in scheduling next run");
                }
            }
        }, delay, TimeUnit.SECONDS);
    }
}