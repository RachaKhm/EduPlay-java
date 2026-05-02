package dev.eduplay.services;

import dev.eduplay.config.EmailConfig;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public class EmailServiceEvent {

    private javax.mail.Session session;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public EmailServiceEvent() {
        Properties props = EmailConfig.getSmtpProperties();
        session = javax.mail.Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                return new javax.mail.PasswordAuthentication(EmailConfig.getUsername(), EmailConfig.getPassword());
            }
        });
    }

    /**
     * Crée un InternetAddress avec gestion de l'exception
     */
    private InternetAddress createInternetAddress(String email, String name) {
        try {
            return new InternetAddress(email, name, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.err.println("❌ Erreur encodage email: " + e.getMessage());
            try {
                return new InternetAddress(email);
            } catch (AddressException ex) {
                System.err.println("❌ Email invalide: " + email);
                return null;
            }
        }
    }

    /**
     * Envoi d'email simple (texte HTML)
     */
    public void sendSimpleEmail(String to, String subject, String bodyHtml) {
        try {
            MimeMessage message = new MimeMessage(session);
            InternetAddress fromAddress = createInternetAddress(EmailConfig.getFromEmail(), EmailConfig.getFromName());
            if (fromAddress == null) {
                System.err.println("❌ Email expéditeur invalide");
                return;
            }
            message.setFrom(fromAddress);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject, "UTF-8");

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(bodyHtml, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            message.setContent(multipart);

            Transport.send(message);
            System.out.println("✅ Email envoyé à: " + to);
        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
        }
    }

    /**
     * Envoi d'email avec pièce jointe (QR code)
     */
    public void sendEmailWithAttachment(String to, String subject, String bodyHtml, String attachmentPath) {
        try {
            MimeMessage message = new MimeMessage(session);
            InternetAddress fromAddress = createInternetAddress(EmailConfig.getFromEmail(), EmailConfig.getFromName());
            if (fromAddress == null) {
                System.err.println("❌ Email expéditeur invalide");
                return;
            }
            message.setFrom(fromAddress);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject, "UTF-8");

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(bodyHtml, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);

            if (attachmentPath != null && !attachmentPath.isEmpty()) {
                File attachmentFile = new File(attachmentPath);
                if (attachmentFile.exists()) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(attachmentFile);
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(attachmentFile.getName());
                    multipart.addBodyPart(attachmentPart);
                    System.out.println("📎 QR code attaché: " + attachmentFile.getName());
                } else {
                    System.err.println("⚠️ Fichier QR code non trouvé: " + attachmentPath);
                }
            }

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("✅ Email avec QR code envoyé à: " + to);
        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
        }
    }

    /**
     * Email de confirmation d'inscription
     */
    public void sendRegistrationConfirmation(String toEmail, String parentName, String childName,
                                             String eventTitle, String eventDate, String eventLocation,
                                             String qrCodePath, int registrationId) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            System.err.println("❌ Email parent invalide (null ou vide)");
            return;
        }

        String subject = "🎫 Confirmation d'inscription - " + eventTitle;

        String body = String.format("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Confirmation d'inscription</title></head>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden;">
                    <div style="background: linear-gradient(135deg, #10b981, #059669); color: white; padding: 30px; text-align: center;">
                        <h1 style="margin: 0;">🎫 Confirmation d'inscription</h1>
                        <p style="margin: 10px 0 0;">EduPlay - Plateforme Éducative</p>
                    </div>
                    <div style="padding: 30px;">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Votre inscription a bien été enregistrée pour l'événement :</p>
                        <div style="background-color: #f3f4f6; border-radius: 12px; padding: 20px; margin: 20px 0;">
                            <p><strong>📅 Événement :</strong> %s</p>
                            <p><strong>👶 Enfant :</strong> %s</p>
                            <p><strong>📅 Date :</strong> %s</p>
                            <p><strong>📍 Lieu :</strong> %s</p>
                        </div>
                        <div style="text-align: center; margin: 25px 0; padding: 20px; background-color: #f9fafb; border-radius: 12px;">
                            <p><strong>🎫 Votre ticket d'entrée</strong></p>
                            <p>Présentez ce QR code à l'entrée (imprimé ou sur mobile)</p>
                            <p><strong>ID inscription : #%d</strong></p>
                        </div>
                    </div>
                    <div style="background-color: #f3f4f6; padding: 20px; text-align: center; font-size: 12px; color: #6c757d;">
                        <p>© 2026 EduPlay - Tous droits réservés</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """, parentName, eventTitle, childName, eventDate, eventLocation, registrationId);

        sendEmailWithAttachment(toEmail, subject, body, qrCodePath);
    }

    /**
     * Email de notification de modification d'événement
     */
    public void sendEventModificationNotification(String toEmail, String parentName, String childName,
                                                  String eventTitle, String oldDate, String newDate,
                                                  String oldLocation, String newLocation) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            System.err.println("❌ Email parent invalide (null ou vide)");
            return;
        }

        String subject = "📢 Modification d'événement - " + eventTitle;

        StringBuilder changesHtml = new StringBuilder();
        if (oldDate != null && newDate != null && !oldDate.equals(newDate)) {
            changesHtml.append(String.format("""
                <tr>
                    <td style="padding: 8px;">📅 Date</td>
                    <td style="padding: 8px; text-decoration: line-through; color: #dc2626;">%s</td>
                    <td style="padding: 8px; color: #10b981;">%s</td>
                </tr>
                """, oldDate, newDate));
        }
        if (oldLocation != null && newLocation != null && !oldLocation.equals(newLocation)) {
            changesHtml.append(String.format("""
                <tr>
                    <td style="padding: 8px;">📍 Lieu</td>
                    <td style="padding: 8px; text-decoration: line-through; color: #dc2626;">%s</td>
                    <td style="padding: 8px; color: #10b981;">%s</td>
                </tr>
                """, oldLocation, newLocation));
        }

        String body = String.format("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Modification d'événement</title></head>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden;">
                    <div style="background: linear-gradient(135deg, #f59e0b, #d97706); color: white; padding: 30px; text-align: center;">
                        <h1 style="margin: 0;">📢 Modification d'événement</h1>
                    </div>
                    <div style="padding: 30px;">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>L'événement <strong>%s</strong> auquel vous avez inscrit <strong>%s</strong> a été modifié.</p>
                        <div style="background-color: #fef3c7; border-radius: 12px; padding: 20px; margin: 20px 0;">
                            <h3>📋 Modifications :</h3>
                            <table style="width: 100%%; border-collapse: collapse;">
                                %s
                            <table>
                        </div>
                        <p>Merci de prendre note de ces changements.</p>
                    </div>
                    <div style="background-color: #f3f4f6; padding: 20px; text-align: center; font-size: 12px; color: #6c757d;">
                        <p>© 2026 EduPlay - Tous droits réservés</p>
                    </div>
                </div>
            </body>
            </html>
            """, parentName, eventTitle, childName, changesHtml.toString());

        sendSimpleEmail(toEmail, subject, body);
    }

    /**
     * Email de rappel 24h avant l'événement
     */
    public void sendReminderEmail(String toEmail, String parentName, String childName,
                                  String eventTitle, String eventDate, String eventLocation,
                                  String qrCodePath, int registrationId) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            System.err.println("❌ Email parent invalide (null ou vide)");
            return;
        }

        String subject = "⏰ Rappel : Événement demain - " + eventTitle;

        String body = String.format("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Rappel événement</title></head>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 16px; overflow: hidden;">
                    <div style="background: linear-gradient(135deg, #3b82f6, #2563eb); color: white; padding: 30px; text-align: center;">
                        <h1 style="margin: 0;">⏰ Rappel</h1>
                        <p>L'événement a lieu demain !</p>
                    </div>
                    <div style="padding: 30px;">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <div style="background-color: #dbeafe; border-radius: 12px; padding: 20px; margin: 20px 0;">
                            <h3>📅 Détails :</h3>
                            <p><strong>🎉 Événement :</strong> %s</p>
                            <p><strong>👶 Enfant :</strong> %s</p>
                            <p><strong>📅 Date :</strong> %s</p>
                            <p><strong>📍 Lieu :</strong> %s</p>
                            <p><strong>🎫 ID inscription :</strong> #%d</p>
                        </div>
                        <p>⚠️ <strong>N'oubliez pas d'apporter votre QR code !</strong></p>
                    </div>
                    <div style="background-color: #f3f4f6; padding: 20px; text-align: center; font-size: 12px; color: #6c757d;">
                        <p>© 2026 EduPlay - Tous droits réservés</p>
                    </div>
                </div>
            </body>
            </html>
            """, parentName, eventTitle, childName, eventDate, eventLocation, registrationId);

        sendEmailWithAttachment(toEmail, subject, body, qrCodePath);
    }
}