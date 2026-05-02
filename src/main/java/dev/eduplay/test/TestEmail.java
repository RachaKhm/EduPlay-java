package dev.eduplay.test;

import dev.eduplay.services.EmailServiceEvent;

public class TestEmail {
    public static void main(String[] args) {
        System.out.println("=== TEST ENVOI EMAIL ===\n");

        EmailServiceEvent emailService = new EmailServiceEvent();

        // Test 1: Email simple
        System.out.println("Test 1: Envoi d'un email simple...");
        emailService.sendSimpleEmail(
                "rachakhamassi03@gmail.com",  // Remplacez par votre email
                "Test EduPlay - Email Simple",
                "<h1>Ça fonctionne !</h1><p>Ceci est un test depuis EduPlay.</p>"
        );

        System.out.println("\n✅ Test terminé ! Vérifiez votre boîte mail.");
    }
}