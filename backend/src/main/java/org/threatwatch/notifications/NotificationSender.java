package org.threatwatch.notifications;

import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;

public interface NotificationSender {

    NotificationChannel supports();

    Boolean testNotification(NotificationRequestDto request) throws MessagingException, UnsupportedEncodingException;

    void sendNotification(NotificationRequestDto request) throws MessagingException, UnsupportedEncodingException;
}
