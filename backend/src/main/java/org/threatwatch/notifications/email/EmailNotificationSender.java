package org.threatwatch.notifications.email;

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
import org.threatwatch.cve.model.CveAlertItem;
import org.threatwatch.loggers.AppLogger;
import org.threatwatch.loggers.LogEvents;
import org.threatwatch.notifications.NotificationChannel;
import org.threatwatch.notifications.NotificationRequestDto;
import org.threatwatch.notifications.NotificationSender;
import org.threatwatch.settings.SettingsResponseDto;
import org.threatwatch.settings.SettingsService;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmailNotificationSender implements NotificationSender {

    private SettingsService settingsService;

    @Value("${email.cve.description.length.max}")
    private int maxDescriptionLength;

    private static final AppLogger appLogger = new AppLogger(LoggerFactory.getLogger(EmailNotificationSender.class));

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

    public EmailNotificationSender(SettingsService settingsService) {
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

    private void sendHtmlEmail(Set<String> recipients, String subject, String body) throws MessagingException, UnsupportedEncodingException {
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

    public String buildEmailAlertHtml(List<CveAlertItem> cvesToSend) {
        if (cvesToSend.size() > 10) {
            return buildEmailSummaryHtml(cvesToSend);
        }

        return buildEmailDetailedHtml(cvesToSend);
    }

    private String buildEmailDetailedHtml(List<CveAlertItem> cvesToSend) {
        StringBuilder html = new StringBuilder();

        for (CveAlertItem cve : cvesToSend) {
            html.append(buildCveHtml(cve.getProduct(), cve));
        }

        return html.toString();
    }

    private String buildEmailSummaryHtml(List<CveAlertItem> cvesToSend) {
        Map<String, Long> cvesByProduct = cvesToSend.stream()
                .collect(Collectors.groupingBy(
                        CveAlertItem::getProduct,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        long criticalCount = countSeverity(cvesToSend, "CRITICAL");
        long highCount = countSeverity(cvesToSend, "HIGH");
        long mediumCount = countSeverity(cvesToSend, "MEDIUM");
        long lowCount = countSeverity(cvesToSend, "LOW");

        StringBuilder html = new StringBuilder();

        html.append("<div style=\"border:1px solid #e5e7eb;border-radius:14px;background:#ffffff;overflow:hidden;\">");

        html.append("<div style=\"padding:18px 20px;background:#111827;color:white;\">")
                .append("<div style=\"font-size:13px;color:#cbd5e1;margin-bottom:4px;\">ThreatWatch Report</div>")
                .append("<div style=\"font-size:22px;font-weight:800;line-height:1.2;\">")
                .append(cvesToSend.size())
                .append(" new vulnerabilities detected")
                .append("</div>")
                .append("</div>");

        html.append("<div style=\"padding:18px 20px;\">");

        html.append("<div style=\"display:grid;grid-template-columns:repeat(4,1fr);gap:10px;margin-bottom:20px;\">")
                .append(buildSeverityBox("Critical", criticalCount, "#b42318"))
                .append(buildSeverityBox("High", highCount, "#d92d20"))
                .append(buildSeverityBox("Medium", mediumCount, "#f79009"))
                .append(buildSeverityBox("Low", lowCount, "#12b76a"))
                .append("</div>");

        html.append("<div style=\"font-size:15px;font-weight:800;color:#111827;margin-bottom:10px;\">Affected monitored products</div>");

        html.append("<table style=\"width:100%;border-collapse:collapse;font-size:14px;\">")
                .append("<thead>")
                .append("<tr>")
                .append("<th style=\"text-align:left;padding:10px 0;border-bottom:1px solid #e5e7eb;color:#6b7280;font-size:12px;text-transform:uppercase;\">Product</th>")
                .append("<th style=\"text-align:right;padding:10px 0;border-bottom:1px solid #e5e7eb;color:#6b7280;font-size:12px;text-transform:uppercase;\">CVEs</th>")
                .append("</tr>")
                .append("</thead>")
                .append("<tbody>");

        cvesByProduct.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> html.append("<tr>")
                        .append("<td style=\"padding:11px 0;border-bottom:1px solid #f3f4f6;color:#111827;font-weight:700;\">")
                        .append(entry.getKey())
                        .append("</td>")
                        .append("<td style=\"padding:11px 0;border-bottom:1px solid #f3f4f6;text-align:right;color:#374151;font-weight:700;\">")
                        .append(entry.getValue())
                        .append("</td>")
                        .append("</tr>"));

        html.append("</tbody></table>");

        html.append("<div style=\"margin-top:16px;padding:12px 14px;border-radius:10px;background:#f9fafb;color:#6b7280;font-size:13px;line-height:1.45;\">")
                .append("This is a summary report because numerous CVEs were found. ")
                .append("Open NVD to review the full vulnerability list.")
                .append("</div>");

        html.append("</div></div>");

        return html.toString();
    }

    private long countSeverity(List<CveAlertItem> cves, String severity) {
        return cves.stream()
                .filter(cve -> cve.getSeverity().name().equals(severity))
                .count();
    }

    private String buildSeverityBox(String label, long count, String color) {
        return "<div style=\"border:1px solid #e5e7eb;border-radius:12px;padding:12px;background:#ffffff;\">"
                + "<div style=\"font-size:11px;text-transform:uppercase;color:#6b7280;font-weight:800;margin-bottom:6px;\">"
                + label
                + "</div>"
                + "<div style=\"font-size:22px;font-weight:900;color:" + color + ";\">"
                + count
                + "</div>"
                + "</div>";
    }

    private String buildSeverityPill(String label, long count, String color) {
        return "<div style=\"background:" + color + ";color:white;border-radius:999px;padding:6px 10px;font-size:12px;font-weight:700;\">"
                + label + ": " + count
                + "</div>";
    }

    public String buildCveHtml(String product, CveAlertItem cve) {
        String color = switch (cve.getSeverity()) {
            case CRITICAL -> "#b42318";
            case HIGH -> "#d92d20";
            case MEDIUM -> "#f79009";
            case LOW -> "#12b76a";
            default -> "#98a2b3";
        };

        String desc = cve.getDescription().length() > maxDescriptionLength
                ? cve.getDescription().substring(0, maxDescriptionLength) + "..."
                : cve.getDescription();

        String score = "-1.0".equals(String.valueOf(cve.getScore())) ? "" : String.valueOf(cve.getScore());

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

    @Override
    public NotificationChannel supports() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void sendNotification(NotificationRequestDto request) throws MessagingException, UnsupportedEncodingException {
        sendHtmlEmail(request.getEmails(), request.getTitle(), request.getMessage());
    }
}
