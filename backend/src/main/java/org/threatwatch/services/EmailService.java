package org.threatwatch.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class EmailService {

    private JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(Set<String> recipients, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no.reply.threatwatch@gmail.com");
        message.setTo(recipients.toArray(new String[0]));
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

}
