package dev.eduplay.test;

import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.services.EmailService;
import dev.eduplay.services.EventRegistrationService;
import dev.eduplay.services.SchoolEventService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmailTest {

    public static void main(String[] args) throws SQLException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        EmailService emailService = new EmailService();
        EventRegistrationService regService = new EventRegistrationService();
        SchoolEventService eventService = new SchoolEventService();

        System.out.println("=== TEST DES EMAILS EDUPLAY ===\n");

        // 1. Tester le rappel 24h - Prendre une inscription existante
        System.out.println("1. TEST RAPPEL 24h");
        System.out.println("   Récupération des inscriptions...");
        List<EventRegistration> registrations = regService.recuperer();

        if (!registrations.isEmpty()) {
            EventRegistration reg = registrations.get(0);
            SchoolEvent event = reg.getEvent();

            if (event != null) {
                String eventDate = event.getStartDate() != null ?
                        event.getStartDate().format(formatter) : "Date non spécifiée";
                String eventLocation = event.getLocation() != null ?
                        event.getLocation() : "Lieu non spécifié";

                System.out.println("   Envoi d'un test de rappel pour: " + reg.getChildFullName());
                emailService.sendReminderEmail(
                        reg.getParent().getEmail(),
                        reg.getParent().getFullName(),
                        reg.getChildFullName(),
                        event.getTitle(),
                        eventDate,
                        eventLocation,
                        reg.getQrCodePath(),
                        reg.getId()
                );
                System.out.println("   ✅ Rappel envoyé à: " + reg.getParent().getEmail());
            }
        } else {
            System.out.println("   ⚠️ Aucune inscription trouvée pour tester");
        }

        System.out.println("\n2. TEST MODIFICATION D'ÉVÉNEMENT");
        System.out.println("   Récupération des événements...");
        List<SchoolEvent> events = eventService.recuperer();

        if (!events.isEmpty()) {
            SchoolEvent event = events.get(0);
            List<EventRegistration> eventRegistrations = regService.recupererParEventId(event.getId());

            if (!eventRegistrations.isEmpty()) {
                String oldDate = event.getStartDate().format(formatter);
                String newDate = event.getStartDate().plusDays(3).format(formatter);
                String oldLocation = event.getLocation();
                String newLocation = oldLocation + " (Nouvelle salle)";

                System.out.println("   Événement: " + event.getTitle());
                System.out.println("   Ancienne date: " + oldDate);
                System.out.println("   Nouvelle date: " + newDate);
                System.out.println("   Nombre d'inscrits: " + eventRegistrations.size());

                for (EventRegistration reg : eventRegistrations) {
                    if (reg.getParent() != null && reg.getParent().getEmail() != null) {
                        emailService.sendEventModificationNotification(
                                reg.getParent().getEmail(),
                                reg.getParent().getFullName(),
                                reg.getChildFullName(),
                                event.getTitle(),
                                oldDate, newDate,
                                oldLocation, newLocation
                        );
                        System.out.println("   ✅ Notification envoyée à: " + reg.getParent().getEmail());
                    }
                }
            } else {
                System.out.println("   ⚠️ Aucune inscription pour cet événement");
            }
        } else {
            System.out.println("   ⚠️ Aucun événement trouvé");
        }

        System.out.println("\n=== FIN DES TESTS ===");
        System.out.println("Vérifiez les emails reçus !");
    }
}