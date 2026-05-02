package dev.eduplay.services;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {

    private static String fromEmail;
    private static String password;
    private static Properties smtpProps;
    private static boolean configured = false;

    static {
        try (InputStream is = EmailService.class.getResourceAsStream("/email.properties")) {
            if (is != null) {
                Properties config = new Properties();
                config.load(is);

                fromEmail = config.getProperty("mail.from");
                password  = config.getProperty("mail.password");

                smtpProps = new Properties();
                smtpProps.put("mail.smtp.host",            config.getProperty("mail.smtp.host"));
                smtpProps.put("mail.smtp.port",            config.getProperty("mail.smtp.port"));
                smtpProps.put("mail.smtp.auth",            config.getProperty("mail.smtp.auth"));
                smtpProps.put("mail.smtp.starttls.enable", config.getProperty("mail.smtp.starttls.enable"));

                configured = fromEmail != null && !fromEmail.isBlank()
                        && password  != null && !password.isBlank();
            }
        } catch (Exception e) {
            System.err.println("[EmailService] email.properties manquant — emails désactivés.");
        }
    }

    // ─── Reset mot de passe ───────────────────────────────────────────────

    public static void sendPasswordResetEmail(String toEmail, String resetToken)
            throws MessagingException {
        if (!configured) throw new MessagingException("EmailService non configuré.");

        Session session = buildSession();
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("EduPlay — Réinitialisation de votre mot de passe");
        message.setContent(buildResetHtml(resetToken), "text/html; charset=utf-8");
        Transport.send(message);

        System.out.println("[EmailService] Email reset envoyé à " + toEmail);
    }

    private static String buildResetHtml(String token) {
        String localLink = "http://localhost:8765/reset?token=" + token;

        return """
            <div style="font-family: Arial, sans-serif; max-width: 520px;
                        margin: auto; padding: 32px; background: #F8F9FA;
                        border-radius: 12px;">

                <div style="text-align: center; margin-bottom: 24px;">
                    <h1 style="color: #E94560; margin: 0; font-size: 28px;">EduPlay</h1>
                    <p style="color: #9999BB; margin: 4px 0 0;">Plateforme éducative</p>
                </div>

                <div style="background: white; border-radius: 8px; padding: 24px;
                            border: 1px solid #EBEBEB;">
                    <p style="color: #22223A; font-size: 15px; margin-top: 0;">
                        Vous avez demandé la réinitialisation de votre mot de passe.
                    </p>

                    <div style="text-align: center; margin: 24px 0;">
                        <a href="%s"
                           style="display: inline-block;
                                  background-color: #E94560;
                                  color: white;
                                  padding: 14px 32px;
                                  text-decoration: none;
                                  border-radius: 8px;
                                  font-weight: bold;
                                  font-size: 15px;">
                            Réinitialiser mon mot de passe
                        </a>
                    </div>

                    <hr style="border: none; border-top: 1px solid #EBEBEB; margin: 20px 0;"/>

                    <p style="color: #555577; font-size: 13px; margin: 0 0 8px;">
                        Si le bouton ne fonctionne pas, copiez ce code dans l'application :
                    </p>
                    <div style="background: #F0F4FF; border: 1px solid #C0C8F0;
                                border-radius: 6px; padding: 12px 16px;
                                font-family: monospace; font-size: 13px;
                                word-break: break-all; color: #1A1A2E;">
                        %s
                    </div>
                </div>

                <p style="color: #BBBBCC; font-size: 11px; text-align: center; margin-top: 16px;">
                    Ce code expire dans 30 minutes. Si vous n'avez pas fait cette demande, ignorez cet email.
                </p>
            </div>
        """.formatted(localLink, token);
    }

    // ─── OTP 2FA ──────────────────────────────────────────────────────────

    public static void sendOtpEmail(String toEmail, String otp)
            throws MessagingException {
        if (!configured) throw new MessagingException("EmailService non configuré.");

        Session session = buildSession();
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("EduPlay — Votre code de vérification");
        message.setContent(buildOtpHtml(otp), "text/html; charset=utf-8");
        Transport.send(message);
    }

    private static String buildOtpHtml(String otp) {
        return """
            <div style="font-family: Arial, sans-serif; max-width: 500px;
                        margin: auto; padding: 32px; background: #F8F9FA; border-radius: 12px;">
                <div style="text-align: center; margin-bottom: 20px;">
                    <h1 style="color: #4A90E2; margin: 0;">EduPlay</h1>
                </div>
                <div style="background: white; border-radius: 8px; padding: 24px;
                            border: 1px solid #EBEBEB; text-align: center;">
                    <p style="color: #22223A; font-size: 15px;">
                        Votre code de vérification :
                    </p>
                    <div style="font-size: 40px; font-weight: bold; letter-spacing: 12px;
                                color: #4A90E2; padding: 20px;
                                background: #EEF4FF; border-radius: 8px; margin: 16px 0;">
                        %s
                    </div>
                    <p style="color: #888; font-size: 12px; margin: 0;">
                        Expire dans <strong>5 minutes</strong>. Ne le partagez jamais.
                    </p>
                </div>
            </div>
        """.formatted(otp);
    }

    // ─── Utilitaires ──────────────────────────────────────────────────────

    private static Session buildSession() {
        return Session.getInstance(smtpProps, new javax.mail.Authenticator() {
            @Override
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(fromEmail, password);
            }
        });
    }

    public static boolean isConfigured() {
        return configured;
    }
}