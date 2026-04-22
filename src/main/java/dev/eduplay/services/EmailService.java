package dev.eduplay.services;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

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
        // Détection des métadonnées
        String ip = "Non disponible"; // En prod, ceci viendrait d'un service de détection d'IP
        String device = System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")";
        String requestId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        sendPasswordResetEmail(toEmail, resetToken, ip, device, requestId);
    }

    public static void sendPasswordResetEmail(String toEmail, String resetToken, String ip, String device, String requestId) throws MessagingException {
        Session session = Session.getInstance(smtpProps, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        String appLink = "eduplay://reset-password?token=" + resetToken;
        String webLink = "https://eduplay.app/reset-password?token=" + resetToken;

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(fromEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Réinitialisation de votre mot de passe — EduPlay");

        message.setContent(buildEmailHtml(appLink, webLink, ip, device, requestId), "text/html; charset=utf-8");

        Transport.send(message);
    }

    private static String buildEmailHtml(String appLink, String webLink, String ip, String device, String requestId) {
        return """
    <!DOCTYPE html>
    <html>
    <body style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f7f9; margin: 0; padding: 0;">
        <div style="max-width: 600px; margin: 40px auto; background-color: #ffffff; border-radius: 16px; overflow: hidden; box-shadow: 0 10px 25px rgba(0,0,0,0.05);">
            
            <div style="background-color: #4A90E2; padding: 40px; text-align: center;">
                <h1 style="color: #ffffff; margin: 0; font-size: 28px; font-weight: 800;">EduPlay 🎓</h1>
            </div>

            <div style="padding: 40px; color: #334155; line-height: 1.6;">
                <h2 style="color: #1e293b; margin-top: 0;">Bonjour,</h2>
                <p>Vous avez demandé la réinitialisation du mot de passe de votre compte EduPlay.</p>
                
                <p>Cliquez sur le bouton ci-dessous pour choisir un nouveau mot de passe :</p>

                <div style="text-align: center; margin: 40px 0;">
                    <a href="%s" 
                       style="background-color: #4A90E2; color: #ffffff; padding: 16px 32px; border-radius: 12px; text-decoration: none; font-weight: bold; font-size: 16px; display: inline-block; box-shadow: 0 4px 12px rgba(74,144,226,0.3);">
                        Réinitialiser mon mot de passe
                    </a>
                </div>

                <div style="background-color: #f8fafc; border-radius: 12px; padding: 20px; font-size: 13px; color: #64748b;">
                    <p style="margin-top: 0; font-weight: bold; color: #475569;">Détails de la demande :</p>
                    <ul style="margin: 0; padding-left: 20px; list-style-type: none;">
                        <li><strong>ID de requête :</strong> #%s</li>
                        <li><strong>IP :</strong> %s</li>
                        <li><strong>Appareil :</strong> %s</li>
                        <li><strong>Lien web :</strong> <a href="%s" style="color: #4A90E2;">%s</a></li>
                    </ul>
                </div>

                <p style="margin-top: 30px; font-size: 14px; color: #94a3b8;">
                    Ce lien expirera dans <strong>30 minutes</strong>. Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email en toute sécurité.
                </p>
            </div>

            <div style="background-color: #f1f5f9; padding: 20px; text-align: center; font-size: 12px; color: #94a3b8;">
                &copy; 2026 EduPlay. Tous droits réservés.
            </div>
        </div>
    </body>
    </html>
    """.formatted(appLink, requestId, ip, device, webLink, webLink);
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