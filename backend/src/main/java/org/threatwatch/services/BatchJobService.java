package org.threatwatch.services;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class BatchJobService {

    private final EmailService emailService;
    private final SettingsService settingsService;

    public BatchJobService(EmailService emailService, SettingsService settingsService) {
        this.emailService = emailService;
        this.settingsService = settingsService;
    }

    public void executeScheduledRun() {
        Set<String> emails = settingsService.retrieveSettings().getEmails();

        System.out.println("Started job");
        emailService.sendEmail(emails, "Hello", "hello");
        System.out.println("Sent email");
    }
}
