package dev.eduplay.test;

import dev.eduplay.services.EmailSchedulerService;
import dev.eduplay.services.EventRegistrationService;

import java.sql.SQLException;
import java.util.List;

public class TestManuelRappel {

    public static void main(String[] args) throws SQLException {
        System.out.println("╔══════════════════════════════════════════════════════╗");
        System.out.println("║           TEST EMAILS EDUPLAY - MANUEL              ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");

        EmailSchedulerService scheduler = new EmailSchedulerService();
        EventRegistrationService regService = new EventRegistrationService();

        // Afficher les inscriptions disponibles
        System.out.println("📋 LISTE DES INSCRIPTIONS DISPONIBLES :");
        System.out.println("----------------------------------------");

        List<dev.eduplay.entities.EventRegistration> registrations = regService.recuperer();

        if (registrations.isEmpty()) {
            System.out.println("❌ Aucune inscription trouvée !");
            System.out.println("   Créez d'abord une inscription via l'interface parent.");
            return;
        }

        for (dev.eduplay.entities.EventRegistration reg : registrations) {
            String eventTitle = reg.getEvent() != null ? reg.getEvent().getTitle() : "N/A";
            String parentEmail = reg.getParent() != null ? reg.getParent().getEmail() : "N/A";
            String parentName = reg.getParent() != null ? reg.getParent().getFullName() : "N/A";
            System.out.println("   ID: " + reg.getId() + " | Enfant: " + reg.getChildFullName() +
                    " | Événement: " + eventTitle + " | Parent: " + parentName + " (" + parentEmail + ")");
        }

        // Demander l'ID à tester (remplacez par un ID de la liste qui a un parent avec email)
        int testId = 37; // ← METTEZ L'ID DE VOTRE INSCRIPTION ICI (doit avoir un parent avec email)

        System.out.println("\n🔧 TEST AVEC L'INSCRIPTION ID: " + testId);
        System.out.println("----------------------------------------");

        try {
            // Test 1: Envoyer tous les rappels pour les événements dans les 24h
            System.out.println("\n1️⃣ Envoi de tous les rappels (événements dans les 24h)...");
            scheduler.sendManualRemindersForTest();

            // Test 2: Envoyer un rappel pour une inscription spécifique
            System.out.println("\n2️⃣ Envoi d'un rappel pour l'inscription ID: " + testId);
            scheduler.sendManualReminderForRegistration(testId);

            // Test 3: Envoyer un email de modification (changement de date) - Utilise testModificationNow
            System.out.println("\n3️⃣ Envoi d'un email de modification (date)");
            scheduler.testModificationNow(testId, "date");

            // Test 4: Envoyer un email de modification (changement de lieu)
            System.out.println("\n4️⃣ Envoi d'un email de modification (lieu)");
            scheduler.testModificationNow(testId, "lieu");

            // Test 5: Envoyer un email de modification (date + lieu)
            System.out.println("\n5️⃣ Envoi d'un email de modification (date + lieu)");
            scheduler.testModificationNow(testId, "both");

        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║                    TEST TERMINÉ                      ║");
        System.out.println("╚══════════════════════════════════════════════════════╝");
        System.out.println("\n📧 Vérifiez vos boîtes mail !");
        System.out.println("   (Pensez à regarder dans les spams)");
    }
}