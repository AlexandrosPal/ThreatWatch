package org.threatwatch.notifications;

import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface NotificationSender {

    NotificationChannel supports();

    void sendNotification(NotificationRequestDto request) throws MessagingException, UnsupportedEncodingException;
}
