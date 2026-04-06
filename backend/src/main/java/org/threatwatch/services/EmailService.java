package org.threatwatch.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.threatwatch.dtos.SettingsResponseDto;
import org.threatwatch.logger.AppLogger;
import org.threatwatch.logger.LogEvents;
import org.threatwatch.models.CveAlertItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Service
public class EmailService {

    private SettingsService settingsService;

    @Value("${email.cve.description.length.max}")
    private int maxDescriptionLength;

    private static final AppLogger appLogger = new AppLogger(LoggerFactory.getLogger(EmailService.class));

    private JavaMailSenderImpl createMailSender(String host, String port, String username, String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setPort(Integer.parseInt(port));
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        return mailSender;
    }

    public EmailService(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    public JavaMailSenderImpl buildMailSender() {
        SettingsResponseDto settings = this.settingsService.retrieveSettings();

        return createMailSender(
                settings.getEmailProviderHost(),
                settings.getEmailProviderPort(),
                settings.getEmailProviderUsername(),
                settings.getEmailProviderPassword()
        );
    }

    public boolean validEmailConnection() {
        JavaMailSenderImpl dynamicMailSender = buildMailSender();
        boolean validEmailConnection = true;

        try {
            dynamicMailSender.testConnection();
            appLogger.info(LogEvents.EMAIL_CONNECTION,"Validated email connection", new HashMap<>());
        } catch (MessagingException e) {
            appLogger.error(LogEvents.EMAIL_CONNECTION,"Error while sending email test call", new HashMap<>(Map.of("error", e.getMessage())));
            validEmailConnection = false;
        }

        return validEmailConnection;
    }

    public void sendEmail(Set<String> recipients, String subject, String body) throws UnsupportedEncodingException {
        JavaMailSenderImpl dynamicMailSender = buildMailSender();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(String.valueOf(new InternetAddress(
                "no.reply.threatwatch@gmail.com",
                "ThreatWatch Alerts"
        )));
        message.setTo(recipients.toArray(new String[0]));
        message.setSubject(subject);
        message.setText(body);

        dynamicMailSender.send(message);
    }

    public String loadHtmlTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/emailTemplate.html");
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public void sendHtmlEmail(Set<String> recipients, String subject, String body) throws MessagingException, UnsupportedEncodingException {
        JavaMailSender dynamicMailSender = buildMailSender();

        MimeMessage message = dynamicMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        message.setFrom(new InternetAddress(
                "no.reply.threatwatch@gmail.com",
                "ThreatWatch Alerts"
        ));
        helper.setTo(recipients.toArray(new String[0]));
        helper.setSubject(subject);

        helper.setText(body, true);
        message.setContent(body, "text/html; charset=utf-8");

        dynamicMailSender.send(message);
    }

    public String buildCveHtml(String product, CveAlertItem cve) {
        String color = switch (cve.getSeverity()) {
            case "CRITICAL" -> "#b42318";
            case "HIGH" -> "#d92d20";
            case "MEDIUM" -> "#f79009";
            case "LOW" -> "#12b76a";
            default -> "#98a2b3";
        };

        String desc = cve.getDescription().length() > maxDescriptionLength
                ? cve.getDescription().substring(0, maxDescriptionLength) + "..."
                : cve.getDescription();

        String score = "-1".equals(String.valueOf(cve.getScore())) ? "" : String.valueOf(cve.getScore());

        return "<div class=\"card\" style=\"border:1px solid #e5e7eb;padding:12px;margin-bottom:8px;\">"
                + "<div style=\"font-weight:bold;\">" + product + " | " + cve.getId() + "</div>"
                + "<div style=\"font-size:12px;color:#6b7280;\">" + cve.getPublished() + "</div>"
                + "<div style=\"margin-top:6px;\">"
                + "<span class=\"badge\" style=\"background:" + color + ";color:white;padding:3px 6px;font-size:11px;\">" + cve.getSeverity() + "</span>"
                + (score.isEmpty() ? "" : "<span style=\"margin-left:6px;\">" + score + "</span>")
                + "</div>"
                + "<div style=\"margin-top:8px;font-size:13px;\">" + desc + "</div>"
                + "<a href=\"https://nvd.nist.gov/vuln/detail/" + cve.getId() + "\" style=\"color:#2563eb;font-size:12px;text-decoration: none;\">View →</a>"
                + "</div>";
    }
}
