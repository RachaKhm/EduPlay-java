//package dev.eduplay.test;
//
//import dev.eduplay.services.EmailSchedulerService;
//import dev.eduplay.services.EventRegistrationService;
//import dev.eduplay.services.SchoolEventService;
//
//import java.sql.SQLException;
//import java.util.List;
//
//public class EmailTest {
//
//    public static void main(String[] args) throws SQLException {
//        System.out.println("╔══════════════════════════════════════════════════════╗");
//        System.out.println("║           TEST EMAILS EDUPLAY - IMMÉDIAT            ║");
//        System.out.println("╚══════════════════════════════════════════════════════╝\n");
//
//        EmailSchedulerService scheduler = new EmailSchedulerService();
//        EventRegistrationService regService = new EventRegistrationService();
//        SchoolEventService eventService = new SchoolEventService();
//
//        // ============================================================
//        // ÉTAPE 1 : Lister les inscriptions disponibles
//        // ============================================================
//        System.out.println("📋 LISTE DES INSCRIPTIONS DISPONIBLES :");
//        System.out.println("----------------------------------------");
//
//        List<dev.eduplay.entities.EventRegistration> registrations = regService.recuperer();
//
//        if (registrations.isEmpty()) {
//            System.out.println("❌ Aucune inscription trouvée !");
//            System.out.println("   Créez d'abord une inscription via l'interface parent.");
//            return;
//        }
//
//        for (dev.eduplay.entities.EventRegistration reg : registrations) {
//            String eventTitle = reg.getEvent() != null ? reg.getEvent().getTitle() : "N/A";
//            String parentEmail = reg.getParent() != null ? reg.getParent().getEmail() : "N/A";
//            System.out.println("   ID: " + reg.getId() + " | Enfant: " + reg.getChildFullName() +
//                    " | Événement: " + eventTitle + " | Parent: " + parentEmail);
//        }
//
//        // ============================================================
//        // ÉTAPE 2 : Choisir une inscription pour le test
//        // ============================================================
//        System.out.println("\n🔧 CHOIX DU TEST :");
//        System.out.println("----------------------------------------");
//
//        // Remplacer 1 par l'ID de l'inscription que vous voulez tester
//        int testRegistrationId = 1; // ← METTEZ L'ID DE VOTRE INSCRIPTION ICI
//
//        dev.eduplay.entities.EventRegistration testReg = regService.recupererParId(testRegistrationId);
//
//        if (testReg == null) {
//            System.out.println("❌ Aucune inscription avec l'ID: " + testRegistrationId);
//            System.out.println("   Utilisez un ID de la liste ci-dessus.");
//            return;
//        }
//
//        System.out.println("   Test avec l'inscription ID: " + testRegistrationId);
//        System.out.println("   Enfant: " + testReg.getChildFullName());
//        System.out.println("   Parent: " + (testReg.getParent() != null ? testReg.getParent().getEmail() : "N/A"));
//
//        // ============================================================
//        // ÉTAPE 3 : Lancer les tests
//        // ============================================================
//        System.out.println("\n📧 ENVOI DES EMAILS DE TEST :");
//        System.out.println("----------------------------------------");
//
//        // Test 1: Email de rappel
//        System.out.println("\n1️⃣ TEST RAPPEL 24h");
//        scheduler.testReminderNow(testRegistrationId);
//
//        // Pause entre les envois
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        // Test 2: Email de modification (changement de date)
//        System.out.println("2️⃣ TEST MODIFICATION (changement de date)");
//        scheduler.testModificationNow(testRegistrationId, "date");
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        // Test 3: Email de modification (changement de lieu)
//        System.out.println("3️⃣ TEST MODIFICATION (changement de lieu)");
//        scheduler.testModificationNow(testRegistrationId, "lieu");
//
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//
//        // Test 4: Email de modification (date + lieu)
//        System.out.println("4️⃣ TEST MODIFICATION (date ET lieu)");
//        scheduler.testModificationNow(testRegistrationId, "both");
//
//        // ============================================================
//        // RÉSULTAT
//        // ============================================================
//        System.out.println("\n╔══════════════════════════════════════════════════════╗");
//        System.out.println("║                    TEST TERMINÉ                      ║");
//        System.out.println("╚══════════════════════════════════════════════════════╝");
//        System.out.println("\n📧 Vérifiez vos boîtes mail !");
//        System.out.println("   (Pensez à regarder dans les spams)");
//    }
//}