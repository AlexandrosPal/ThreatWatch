package org.threatwatch.services;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.threatwatch.models.ParsedCveModel;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

@Service
public class EmailService {

    private JavaMailSender mailSender;

    @Value("${email.cve.description.length.max}")
    private int maxDescriptionLength;

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

    public String loadHtmlTemplate() throws Exception {
        ClassPathResource resource = new ClassPathResource("templates/emailTemplate.html");
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public void sendHtmlEmail(Set<String> recipients, String subject, String body) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(recipients.toArray(new String[0]));
        helper.setSubject(subject);

        helper.setText(body, true);
        message.setContent(body, "text/html; charset=utf-8");

        mailSender.send(message);
    }

    public String buildCveHtml(String product, ParsedCveModel parsedCve) {
        String color = switch (parsedCve.getSeverity()) {
            case "CRITICAL" -> "#b60205";
            case "HIGH" -> "#d73a49";
            case "MEDIUM" -> "#fb8500";
            case "LOW" -> "#2da44e";
            default -> "#6c757d";
        };

        String shortDescription = parsedCve.getDescription().length() > maxDescriptionLength
                ? parsedCve.getDescription().substring(0, maxDescriptionLength) + "..."
                : parsedCve.getDescription();

        return """
            <div style="border:1px solid #e1e4e8; border-radius:6px; padding:12px; margin-bottom:12px;">
                <div style="font-weight:bold; font-size:14px;">
                    %s  |  %s
                    <span style="background:%s; color:white; padding:2px 6px; border-radius:4px; font-size:11px; margin-left:8px;">%s</span>
                    <span style="padding-left:5px;">%s</span>
                </div>
                <div style="font-size:12px; color:#555; margin-top:4px;">
                    Published: %s
                </div>
                <div style="font-size:13px; margin-top:8px;">
                    %s
                    <a href="https://nvd.nist.gov/vuln/detail/%s">View details</a>
                </div>
            </div>
            """.formatted(product, parsedCve.getCveId(), color, parsedCve.getSeverity(), parsedCve.getScore(), parsedCve.getPublished(), shortDescription, parsedCve.getCveId());
    }

}
