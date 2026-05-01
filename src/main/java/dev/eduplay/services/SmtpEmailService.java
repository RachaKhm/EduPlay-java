package dev.eduplay.services;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class SmtpEmailService {

    private final String username;
    private final String appPassword;
    private final String host;
    private final int port;

    public SmtpEmailService(File configFile) throws IOException {
        Objects.requireNonNull(configFile, "configFile is required");
        Properties cfg = new Properties();
        try (FileInputStream in = new FileInputStream(configFile)) {
            cfg.load(in);
        }

        this.username = require(cfg, "smtp.username");
        this.appPassword = normalizeAppPassword(require(cfg, "smtp.appPassword"));
        this.host = cfg.getProperty("smtp.host", "smtp.gmail.com").trim();
        this.port = Integer.parseInt(cfg.getProperty("smtp.port", "587").trim());
    }

    public void sendCourseCreatedEmail(List<String> recipientEmails, String courseTitle, String teacherName)
            throws MessagingException {
        if (recipientEmails == null || recipientEmails.isEmpty()) return;

        String safeTitle = (courseTitle == null || courseTitle.isBlank()) ? "Nouveau cours" : courseTitle.trim();
        String safeTeacher = (teacherName == null || teacherName.isBlank()) ? "Un enseignant" : teacherName.trim();

        String body = """
                Bonjour,

                Un nouvel enseignant a créé un cours sur EduPlay.

                Titre du cours : %s
                Enseignant : %s

                Connectez-vous à votre espace parent pour consulter les détails.

                Cordialement,
                Équipe EduPlay
                """.formatted(safeTitle, safeTeacher);

        MessagingException firstError = null;
        try {
            sendWithStartTls587(recipientEmails, safeTitle, body);
            return;
        } catch (MessagingException e) {
            firstError = e;
        }

        try {
            sendWithSsl465(recipientEmails, safeTitle, body);
        } catch (MessagingException secondError) {
            secondError.setNextException(firstError);
            throw secondError;
        }
    }

    private void sendWithStartTls587(List<String> recipients, String safeTitle, String body) throws MessagingException {
        Session session = Session.getInstance(mailPropsStartTls(), authenticator());
        Message message = buildMessage(session, recipients, safeTitle, body);
        Transport.send(message);
    }

    private void sendWithSsl465(List<String> recipients, String safeTitle, String body) throws MessagingException {
        Session session = Session.getInstance(mailPropsSsl(), authenticator());
        Message message = buildMessage(session, recipients, safeTitle, body);
        Transport.send(message);
    }

    private Message buildMessage(Session session, List<String> recipients, String safeTitle, String body)
            throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(String.join(",", recipients)));
        message.setSubject("Nouveau cours disponible sur EduPlay: " + safeTitle);
        message.setText(body);
        return message;
    }

    private Authenticator authenticator() {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, appPassword);
            }
        };
    }

    private Properties mailPropsStartTls() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.auth.mechanisms", "LOGIN PLAIN");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "12000");
        props.put("mail.smtp.timeout", "12000");
        props.put("mail.smtp.writetimeout", "12000");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "587");
        return props;
    }

    private Properties mailPropsSsl() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.auth.mechanisms", "LOGIN PLAIN");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.trust", host);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", "12000");
        props.put("mail.smtp.timeout", "12000");
        props.put("mail.smtp.writetimeout", "12000");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", "465");
        return props;
    }

    private static String require(Properties cfg, String key) {
        String value = cfg.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing SMTP setting: " + key);
        }
        return value.trim();
    }

    /**
     * Gmail app passwords are displayed with spaces (e.g. "abcd efgh ijkl mnop"),
     * but SMTP expects a continuous token.
     */
    private static String normalizeAppPassword(String raw) {
        if (raw == null) return null;
        return raw.replaceAll("\\s+", "");
    }
}
