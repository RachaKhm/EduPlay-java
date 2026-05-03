package dev.eduplay.mains;

import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.entities.User;
import dev.eduplay.services.EventRegistrationService;
import dev.eduplay.services.SchoolEventService;
import dev.eduplay.services.UserService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class EventRegistrationMain {
    public static void main(String[] args) throws SQLException {
        EventRegistrationService ers = new EventRegistrationService();
        SchoolEventService schoolEventService = new SchoolEventService();
        UserService userService = new UserService();

        // ========== CRÉER LES ÉVÉNEMENTS ==========
        SchoolEvent event1 = new SchoolEvent(27, "Atelier Mosaïstes", "Description",
                LocalDateTime.of(2026, 5, 15, 10, 0), LocalDateTime.of(2026, 5, 15, 12, 0),
                "Tunis", null, null, null, null, null, null);
        event1.setMaxCapacity(50);
        event1.setCurrentRegistrations(0);

        SchoolEvent event2 = new SchoolEvent(2, "Safari Oasis", "Description",
                LocalDateTime.of(2026, 6, 10, 9, 30), LocalDateTime.of(2026, 6, 10, 16, 0),
                "Enfidha", null, null, null, null, null, null);
        event2.setMaxCapacity(30);
        event2.setCurrentRegistrations(0);

        SchoolEvent event3 = new SchoolEvent(3, "Pâtissier en Herbe", "Description",
                LocalDateTime.of(2026, 7, 5, 14, 0), LocalDateTime.of(2026, 7, 5, 17, 0),
                "Sidi Bou Saïd", null, null, null, null, null, null);
        event3.setMaxCapacity(20);
        event3.setCurrentRegistrations(0);

        // ========== CRÉER LES PARENTS ==========
        User parent1 = new User("Fatma", "Ben Ali", "fatma.benali@email.com", "PARENT");
        parent1.setId(1);

        User parent2 = new User("Karim", "Mansouri", "karim.mansouri@email.com", "PARENT");
        parent2.setId(2);

        User parent3 = new User("Sami", "Khemiri", "sami.khemiri@email.com", "PARENT");
        parent3.setId(3);

        // ========== CRÉER UNE INSCRIPTION (SANS STATUS) ==========
        EventRegistration registration1 = new EventRegistration();
        registration1.setChildFullName("Lina Ben Ali");
        registration1.setParentPhone("22123456");
        registration1.setChildClassLevel("3A");
        registration1.setMedicalNotes("Allergie aux arachides");
        registration1.setEmergencyContactName("Ahmed Ben Ali");
        registration1.setEmergencyContactPhone("55123456");
        registration1.setNotes("Autorisation parentale fournie");
        registration1.setTicketQrCode("qr_code_1");
        registration1.setQrCodePath("uploads/qrcodes/ticket_1.png");
        registration1.setRegisteredAt(LocalDateTime.now());
        registration1.setEvent(event1);
        registration1.setParent(parent1);
        registration1.setReminderSent(false);
        registration1.setScannedAt(null);
        registration1.setReminderSentAt(null);

        // ========== AJOUTER UNE INSCRIPTION ==========
        try {
            ers.ajouter(registration1);
            System.out.println("✅ Inscription ajoutée avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }

        // ========== SUPPRIMER UNE INSCRIPTION ==========
        try {
            ers.supprimer(registration1);
            System.out.println("✅ Inscription supprimée avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }

        // ========== RÉCUPÉRER TOUTES LES INSCRIPTIONS ==========
        try {
            List<EventRegistration> registrations = ers.recuperer();

            System.out.println("\n📋 Liste des inscriptions dans la base de données:");
            if (registrations.isEmpty()) {
                System.out.println("   Aucune inscription trouvée.");
            } else {
                for (EventRegistration q : registrations) {
                    System.out.println("   - ID: " + q.getId() +
                            " | Enfant: " + q.getChildFullName() +
                            " | Événement: " + (q.getEvent() != null ? q.getEvent().getTitle() : "N/A"));
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération: " + e.getMessage());
        }

        // ========== MODIFIER UNE INSCRIPTION ==========
        EventRegistration registrationToModify = new EventRegistration();
        registrationToModify.setId(1); // ID existant
        registrationToModify.setChildFullName("Lina Ben Ali Modifiée");
        registrationToModify.setParentPhone("50000000");
        registrationToModify.setChildClassLevel("4A");
        registrationToModify.setMedicalNotes("Plus d'allergies");
        registrationToModify.setEmergencyContactName("Ahmed Ben Ali");
        registrationToModify.setEmergencyContactPhone("55123456");
        registrationToModify.setNotes("Nouvelle autorisation");
        registrationToModify.setTicketQrCode("qr_code_1_updated");
        registrationToModify.setQrCodePath("uploads/qrcodes/ticket_1_updated.png");
        registrationToModify.setScannedAt(null);
        registrationToModify.setReminderSent(false);
        registrationToModify.setReminderSentAt(null);

        try {
            ers.modifier(registrationToModify);
            System.out.println("✅ Inscription modifiée avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la modification: " + e.getMessage());
        }

        // ========== CHERCHER UNE INSCRIPTION ==========
        EventRegistration searchRegistration = new EventRegistration();
        searchRegistration.setId(1);
        try {
            int result = ers.chercher(searchRegistration);
            if (result != -1) {
                System.out.println("✅ Inscription trouvée avec l'ID: " + result);
            } else {
                System.out.println("❌ Inscription non trouvée");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la recherche: " + e.getMessage());
        }

        // ========== RÉCUPÉRER LES INSCRIPTIONS PAR ID ÉVÉNEMENT ==========
        try {
            List<EventRegistration> registrationsByEvent = ers.recupererParEventId(27);
            System.out.println("\n📋 Inscriptions pour l'événement ID 27:");
            for (EventRegistration q : registrationsByEvent) {
                System.out.println("   - Enfant: " + q.getChildFullName() + " | Tél: " + q.getParentPhone());
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }

        // ========== RÉCUPÉRER LES INSCRIPTIONS PAR ID PARENT ==========
        try {
            List<EventRegistration> registrationsByParent = ers.recupererParParentId(1);
            System.out.println("\n📋 Inscriptions pour le parent ID 1:");
            for (EventRegistration q : registrationsByParent) {
                System.out.println("   - Enfant: " + q.getChildFullName() + " | Événement: " +
                        (q.getEvent() != null ? q.getEvent().getTitle() : "N/A"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur: " + e.getMessage());
        }
    }
}