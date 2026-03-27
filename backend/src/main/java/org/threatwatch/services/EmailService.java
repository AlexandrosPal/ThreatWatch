package org.threatwatch.services;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.threatwatch.models.ParsedCveModel;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;

@Service
public class EmailService {

    private JavaMailSender mailSender;

    @Value("${email.cve.description.length.max}")
    private int maxDescriptionLength;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(Set<String> recipients, String subject, String body) throws Exception {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(String.valueOf(new InternetAddress(
                "no.reply.threatwatch@gmail.com",
                "ThreatWatch Alerts"
        )));
        message.setTo(recipients.toArray(new String[0]));
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public String loadHtmlTemplate() throws Exception {
        ClassPathResource resource = new ClassPathResource("templates/emailTemplate.html");
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public void sendHtmlEmail(Set<String> recipients, String subject, String body) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        message.setFrom(new InternetAddress(
                "no.reply.threatwatch@gmail.com",
                "ThreatWatch Alerts"
        ));
        helper.setTo(recipients.toArray(new String[0]));
        helper.setSubject(subject);

        helper.setText(body, true);
        message.setContent(body, "text/html; charset=utf-8");

        mailSender.send(message);
    }

    public String buildCveHtml(String product, ParsedCveModel cve) {
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

        String score = "-1".equals(cve.getScore()) ? "" : cve.getScore();
        String scoreHtml = score.isEmpty() ? "" : "<span class=\"score\">" + score + "</span>";

        return "<div class=\"card\" style=\"border:1px solid #e5e7eb;padding:12px;margin-bottom:8px;\">"
                + "<div style=\"font-weight:bold;\">" + product + " | " + cve.getCveId() + "</div>"
                + "<div style=\"font-size:12px;color:#6b7280;\">" + cve.getPublished() + "</div>"
                + "<div style=\"margin-top:6px;\">"
                + "<span class=\"badge\" style=\"background:" + color + ";color:white;padding:3px 6px;font-size:11px;\">" + cve.getSeverity() + "</span>"
                + (score.isEmpty() ? "" : "<span style=\"margin-left:6px;\">" + score + "</span>")
                + "</div>"
                + "<div style=\"margin-top:8px;font-size:13px;\">" + desc + "</div>"
                + "<a href=\"https://nvd.nist.gov/vuln/detail/" + cve.getCveId() + "\" style=\"color:#2563eb;font-size:12px;text-decoration: none;\">View →</a>"
                + "</div>";
    }
}
