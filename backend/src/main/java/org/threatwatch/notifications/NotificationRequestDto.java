package org.threatwatch.notifications;

import java.util.Set;

public class NotificationRequestDto {

    private String title;
    private String message;
    private Set<String> emails;
    private Set<String> webhookUrls;

    public String getTitle() { return title; }

    public String getMessage() { return message; }

    public Set<String> getEmails() { return emails; }

    public Set<String> getWebhookUrls() { return webhookUrls; }

    public void setTitle(String title) { this.title = title; }

    public void setMessage(String message) { this.message = message; }

    public void setEmails(Set<String> emails) { this.emails = emails; }

    public void setWebhookUrls(Set<String> webhookUrls) { this.webhookUrls = webhookUrls; }
}
