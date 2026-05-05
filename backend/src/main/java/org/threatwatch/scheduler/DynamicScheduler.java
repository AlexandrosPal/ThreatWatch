package org.threatwatch.scheduler;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.LogEvents;
import org.threatwatch.settings.SettingsService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class DynamicScheduler {

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private final BatchJobService batchJobService;
    private final SettingsService settingsService;

    private static final AppLogger appLogger = new AppLogger(LoggerFactory.getLogger(DynamicScheduler.class));

    public DynamicScheduler(BatchJobService batchJobService,
                            SettingsService settingsService) {
        this.batchJobService = batchJobService;
        this.settingsService = settingsService;
    }

    @PostConstruct
    public void start() {
        settingsService.initializeDefaultsIfMissing();
        scheduleNextRun();
    }

    private void scheduleNextRun() {

        int delay = Integer.parseInt(settingsService.retrieveSettings().getBatchInterval());

        executor.schedule(() -> {
            try {
                boolean enabled = Boolean.parseBoolean(settingsService.retrieveSettings().getEnabled());

                if (enabled) {
                    String correlationId = UUID.randomUUID().toString();
                    MDC.put("correlationId", correlationId);

                    appLogger.info(LogEvents.SCHEDULER_RUN,"Started a scheduled run", new HashMap<>());
                    batchJobService.executeScheduledRun();
                }
            } catch (IOException | MessagingException e) {
                appLogger.error(LogEvents.SCHEDULER_RUN,"Error while running scheduled run", new HashMap<>(Map.of("error", e.getMessage())));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                try {
                    scheduleNextRun();
                    MDC.clear();
                } catch (Exception e) {
                    appLogger.error(LogEvents.SCHEDULER_RUN,"Error while scheduling next run", new HashMap<>(Map.of("error", e.getMessage())));
                }
            }
        }, delay, TimeUnit.SECONDS);
    }
}