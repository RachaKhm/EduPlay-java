package dev.eduplay.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;

import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class EmailService {

    private static Session session;
    private static String fromEmail;
    private static String fromName;
    private static String password;
    private static Properties smtpProps;
    private static boolean configured = false;

    static {
        Properties config = new Properties();
        try (InputStream is = EmailService.class.getResourceAsStream("/email.properties")) {
            if (is != null) {
                config.load(is);
                fromEmail = config.getProperty("mail.from");
                fromName  = config.getProperty("mail.from.name", "EduPlay");
                password  = config.getProperty("mail.password");

                smtpProps = new Properties();
                smtpProps.put("mail.smtp.host",            config.getProperty("mail.smtp.host"));
                smtpProps.put("mail.smtp.port",            config.getProperty("mail.smtp.port"));
                smtpProps.put("mail.smtp.auth",            config.getProperty("mail.smtp.auth"));
                smtpProps.put("mail.smtp.starttls.enable", config.getProperty("mail.smtp.starttls.enable"));

                session = Session.getInstance(smtpProps, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(fromEmail, password);
                    }
                });
                configured = fromEmail != null && !fromEmail.isBlank() && password != null && !password.isBlank();
            }
        } catch (Exception e) {
            System.err.println("[EmailService] email.properties manquant — emails désactivés.");
        }
    }

    public EmailService() {
        // Kept for backward compatibility if needed, though session is now static
    }

    private static Session buildSession() {
        return session;
    }

    // ─── CORE SEND METHODS ──────────────────────────────────────────────

    private InternetAddress createAddress(String email, String name) throws Exception {
        try {
            return new InternetAddress(email, name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new InternetAddress(email);
        }
    }

    private void send(String to, String subject, String html, String attachmentPath) throws MessagingException {
        Message message = new MimeMessage(session);

        try {
            message.setFrom(createAddress(fromEmail, fromName));
        } catch (Exception e) {
            throw new MessagingException("Invalid sender", e);
        }

        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(html, "text/html; charset=utf-8");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(htmlPart);

        // Attachment (optional)
        if (attachmentPath != null && !attachmentPath.isBlank()) {
            File file = new File(attachmentPath);
            if (file.exists()) {
                MimeBodyPart attachPart = new MimeBodyPart();
                DataSource source = new FileDataSource(file);
                attachPart.setDataHandler(new DataHandler(source));
                attachPart.setFileName(file.getName());
                multipart.addBodyPart(attachPart);
            }
        }

        message.setContent(multipart);
        Transport.send(message);
    }

    public void sendSimpleEmail(String to, String subject, String html) {
        try {
            send(to, subject, html, null);
        } catch (MessagingException e) {
            System.err.println("Erreur d'envoi d'email simple: " + e.getMessage());
        }
    }

    // ─── PASSWORD RESET ─────────────────────────────────────────────────

    public void sendPasswordResetEmail(String to, String token) throws MessagingException {
        String link = "http://localhost:8765/reset?token=" + token;

        String html = """
            <h2>Réinitialisation du mot de passe</h2>
            <p>Cliquez sur le bouton :</p>
            <a href="%s">Réinitialiser</a>
            <p>Code : %s</p>
        """.formatted(link, token);

        send(to, "Reset Password", html, null);
    }

    // ─── OTP ───────────────────────────────────────────────────────────

    public void sendOtpEmail(String to, String otp) throws MessagingException {
        String html = """
            <h2>Code OTP</h2>
            <h1>%s</h1>
        """.formatted(otp);

        send(to, "OTP Verification", html, null);
    }

    // ─── EVENT EMAILS ──────────────────────────────────────────────────

    public void sendRegistrationConfirmation(String to, String parent, String child,
                                             String event, String date, String location,
                                             String qrPath, int id) throws MessagingException {

        String html = """
            <h2>Confirmation inscription</h2>
            <p>Parent: %s</p>
            <p>Enfant: %s</p>
            <p>Event: %s</p>
            <p>Date: %s</p>
            <p>Lieu: %s</p>
            <p>ID: #%d</p>
        """.formatted(parent, child, event, date, location, id);

        send(to, "Confirmation - " + event, html, qrPath);
    }

    public void sendReminderEmail(String to, String parent, String child,
                                  String event, String date, String location,
                                  String qrPath, int id) throws MessagingException {

        String html = """
            <h2>Rappel événement</h2>
            <p>%s, n'oubliez pas :</p>
            <p>%s - %s</p>
        """.formatted(parent, event, date);

        send(to, "Rappel - " + event, html, qrPath);
    }

    public void sendEventModificationNotification(String to, String parent, String child, String event,
                                                  String oldDate, String newDate, String oldLocation, String newLocation) {
        try {
            String html = """
                <h2>Modification événement</h2>
                <p>Bonjour %s,</p>
                <p>L'événement <strong>%s</strong> pour <strong>%s</strong> a été modifié.</p>
                <h3>Anciennes informations:</h3>
                <ul>
                    <li>Date: %s</li>
                    <li>Lieu: %s</li>
                </ul>
                <h3>Nouvelles informations:</h3>
                <ul>
                    <li>Date: %s</li>
                    <li>Lieu: %s</li>
                </ul>
            """.formatted(parent, event, child, oldDate, oldLocation, newDate, newLocation);

            send(to, "Modification - " + event, html, null);
        } catch (MessagingException e) {
            System.err.println("Erreur d'envoi d'email de modification: " + e.getMessage());
        }
    }

    // ─── Notification de création de cours ────────────────────────────────

    public static void sendCourseCreationNotification(String teacherName, String courseTitle) {
        if (!configured) return;
        try {
            Session session = buildSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("nadinezairi60@gmail.com"));
            message.setSubject("EduPlay — Nouveau cours créé");

            String html = """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                    <h2 style="color: #3B82F6;">Nouveau cours disponible !</h2>
                    <p>L'enseignant <strong>%s</strong> vient de créer un nouveau cours intitulé :</p>
                    <p style="font-size: 18px; font-weight: bold; background: #f0f7ff; padding: 10px; border-radius: 5px; color: #1E40AF;">
                        %s
                    </p>
                    <p>Vous pouvez dès maintenant le consulter sur la plateforme EduPlay.</p>
                </div>
            """.formatted(teacherName, courseTitle);

            message.setContent(html, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("[EmailService] Notification de cours envoyée à nadinezairi60@gmail.com");
        } catch (Exception e) {
            System.err.println("[EmailService] Échec envoi notification cours: " + e.getMessage());
        }
    }
}