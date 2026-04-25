package dev.eduplay.services;

import java.time.format.DateTimeFormatter;
import java.util.Map;

public class EmailTemplateService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Génère le HTML pour l'email de confirmation
     */
    public static String generateConfirmationHtml(Map<String, String> data) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Confirmation d'inscription</title></head>
            <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto; background: #fff; border-radius: 16px; overflow: hidden;">
                    <div style="background: linear-gradient(135deg, #10b981, #059669); color: white; padding: 30px; text-align: center;">
                        <h1 style="margin: 0;">🎫 Confirmation</h1>
                    </div>
                    <div style="padding: 30px;">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>Votre inscription pour <strong>%s</strong> a été confirmée.</p>
                        <div style="background: #f3f4f6; padding: 20px; border-radius: 12px; margin: 20px 0;">
                            <p><strong>👶 Enfant :</strong> %s</p>
                            <p><strong>📅 Date :</strong> %s</p>
                            <p><strong>📍 Lieu :</strong> %s</p>
                        </div>
                        <p style="text-align: center;">Votre QR code est en pièce jointe.</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                data.get("parentName"),
                data.get("eventTitle"),
                data.get("childName"),
                data.get("eventDate"),
                data.get("eventLocation")
        );
    }

    /**
     * Génère le HTML pour l'email de modification
     */
    public static String generateModificationHtml(Map<String, String> data) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Modification d'événement</title></head>
            <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto; background: #fff; border-radius: 16px; overflow: hidden;">
                    <div style="background: linear-gradient(135deg, #f59e0b, #d97706); color: white; padding: 30px; text-align: center;">
                        <h1 style="margin: 0;">📢 Modification</h1>
                    </div>
                    <div style="padding: 30px;">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <p>L'événement <strong>%s</strong> a été modifié.</p>
                        <div style="background: #fef3c7; padding: 20px; border-radius: 12px; margin: 20px 0;">
                            %s
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """,
                data.get("parentName"),
                data.get("eventTitle"),
                data.get("changes")
        );
    }

    /**
     * Génère le HTML pour l'email de rappel
     */
    public static String generateReminderHtml(Map<String, String> data) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"><title>Rappel événement</title></head>
            <body style="font-family: Arial, sans-serif;">
                <div style="max-width: 600px; margin: 0 auto; background: #fff; border-radius: 16px; overflow: hidden;">
                    <div style="background: linear-gradient(135deg, #3b82f6, #2563eb); color: white; padding: 30px; text-align: center;">
                        <h1 style="margin: 0;">⏰ Rappel</h1>
                        <p>L'événement a lieu demain !</p>
                    </div>
                    <div style="padding: 30px;">
                        <p>Bonjour <strong>%s</strong>,</p>
                        <div style="background: #dbeafe; padding: 20px; border-radius: 12px; margin: 20px 0;">
                            <p><strong>🎉 Événement :</strong> %s</p>
                            <p><strong>👶 Enfant :</strong> %s</p>
                            <p><strong>📅 Date :</strong> %s</p>
                            <p><strong>📍 Lieu :</strong> %s</p>
                        </div>
                        <p>N'oubliez pas votre QR code !</p>
                    </div>
                </div>
            </body>
            </html>
            """,
                data.get("parentName"),
                data.get("eventTitle"),
                data.get("childName"),
                data.get("eventDate"),
                data.get("eventLocation")
        );
    }
}