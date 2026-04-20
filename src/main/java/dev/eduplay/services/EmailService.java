package dev.eduplay.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {

    private static String fromEmail;
    private static String password;
    private static Properties smtpProps;

    static {
        try (InputStream is = EmailService.class
                .getResourceAsStream("/email.properties")) {
            Properties config = new Properties();
            config.load(is);

            fromEmail = config.getProperty("mail.from");
            password   = config.getProperty("mail.password");

            smtpProps = new Properties();
            smtpProps.put("mail.smtp.host",           config.getProperty("mail.smtp.host"));
            smtpProps.put("mail.smtp.port",           config.getProperty("mail.smtp.port"));
            smtpProps.put("mail.smtp.auth",           config.getProperty("mail.smtp.auth"));
            smtpProps.put("mail.smtp.starttls.enable",config.getProperty("mail.smtp.starttls.enable"));
        } catch (Exception e) {
            throw new RuntimeException("Impossible de charger email.properties", e);
        }
    }

    public static void sendPasswordResetEmail(String toEmail, String resetToken) throws MessagingException {
        Session session = Session.getInstance(smtpProps, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        String resetLink = "eduplay://reset-password?token=" + resetToken;

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("EduPlay — Réinitialisation de votre mot de passe");
        message.setContent(buildEmailHtml(resetLink), "text/html; charset=utf-8");

        Transport.send(message);
    }

    private static String buildEmailHtml(String resetLink) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto;">
                <h2 style="color: #4A90D9;">EduPlay 🎓</h2>
                <p>Vous avez demandé la réinitialisation de votre mot de passe.</p>
                <p>Votre code de réinitialisation est :</p>
                <div style="font-size: 28px; font-weight: bold; letter-spacing: 6px;
                            color: #4A90D9; text-align: center; padding: 16px;
                            background: #f0f4ff; border-radius: 8px;">
                    %s
                </div>
                <p style="color: #888; font-size: 12px;">
                    Ce lien expire dans <strong>30 minutes</strong>.<br>
                    Si vous n'avez pas fait cette demande, ignorez cet email.
                </p>
            </div>
            """.formatted(resetLink);
    }
    public static void sendOtpEmail(String toEmail, String otp) throws MessagingException {
        Session session = Session.getInstance(smtpProps, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("EduPlay — Votre code de vérification");
        message.setContent("""
        <div style="font-family: Arial, sans-serif; max-width: 500px; margin: auto;">
            <h2 style="color: #4A90D9;">EduPlay 🎓</h2>
            <p>Votre code de vérification est :</p>
            <div style="font-size: 36px; font-weight: bold; letter-spacing: 8px;
                        color: #4A90D9; text-align: center; padding: 20px;
                        background: #f0f4ff; border-radius: 8px;">
                %s
            </div>
            <p style="color: #888; font-size: 12px;">
                Ce code expire dans <strong>5 minutes</strong>.
            </p>
        </div>
    """.formatted(otp), "text/html; charset=utf-8");

        Transport.send(message);
    }
}