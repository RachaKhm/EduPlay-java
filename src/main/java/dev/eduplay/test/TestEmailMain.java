package dev.eduplay.test;

import dev.eduplay.services.EmailService;

public class TestEmailMain {
    public static void main(String[] args) {
        System.out.println("=== TEST ENVOI EMAIL ===\n");

        EmailService emailService = new EmailService();

        // Test d'envoi simple
        emailService.sendSimpleEmail(
                "votre.email.personnel@gmail.com",  // ← REMPLACEZ PAR VOTRE EMAIL
                "Test EduPlay - Configuration Email",
                """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8"></head>
                <body>
                    <h1>✅ Test réussi !</h1>
                    <p>La configuration email d'EduPlay fonctionne correctement.</p>
                    <hr>
                    <p><strong>Détails de la configuration :</strong></p>
                    <ul>
                        <li>Serveur SMTP configuré</li>
                        <li>Authentification OK</li>
                        <li>Envoi d'email fonctionnel</li>
                    </ul>
                    <p>Date et heure : %s</p>
                </body>
                </html>
                """.formatted(java.time.LocalDateTime.now())
        );

        System.out.println("\n➡️ Vérifiez votre boîte mail (et les spams) !");
    }
}