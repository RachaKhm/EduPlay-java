package dev.eduplay.services;

import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.SchoolEvent;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EmailSchedulerService {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final EventRegistrationService registrationService;
    private final SchoolEventService eventService;
    private final EmailService emailService;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private boolean running = false;

    public EmailSchedulerService() {
        this.registrationService = new EventRegistrationService();
        this.eventService = new SchoolEventService();
        this.emailService = new EmailService();
    }

    public void startReminderScheduler() {
        if (running) return;
        running = true;

        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkAndSendReminders();
            } catch (SQLException e) {
                System.err.println("❌ Erreur lors de l'envoi des rappels: " + e.getMessage());
            }
        }, 0, 30, TimeUnit.MINUTES);

        System.out.println("✅ Scheduler de rappels démarré (vérification toutes les 30 min)");
    }

    private void checkAndSendReminders() throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24Hours = now.plusHours(24);

        List<SchoolEvent> upcomingEvents = eventService.getEventsStartingBetween(now, in24Hours);

        if (upcomingEvents.isEmpty()) {
            return;
        }

        System.out.println("📧 Vérification des rappels - " + upcomingEvents.size() + " événement(s) dans les 24h");

        for (SchoolEvent event : upcomingEvents) {
            List<EventRegistration> registrations = registrationService.recupererParEventId(event.getId());

            for (EventRegistration registration : registrations) {
                if (!registration.isReminderSent()) {
                    sendReminderForRegistration(registration, event);

                    registration.setReminderSent(true);
                    registration.setReminderSentAt(LocalDateTime.now());
                    registrationService.modifier(registration);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    private void sendReminderForRegistration(EventRegistration registration, SchoolEvent event) {
        if (registration.getParent() == null || registration.getParent().getEmail() == null) {
            return;
        }

        String eventDate = event.getStartDate() != null ?
                event.getStartDate().format(formatter) : "Date non spécifiée";
        String eventLocation = event.getLocation() != null ?
                event.getLocation() : "Lieu non spécifié";

        emailService.sendReminderEmail(
                registration.getParent().getEmail(),
                registration.getParent().getFullName(),
                registration.getChildFullName(),
                event.getTitle(),
                eventDate,
                eventLocation,
                registration.getQrCodePath(),
                registration.getId()
        );
    }

    /**
     * ✅ TEST MANUEL - Envoie les rappels pour TOUS les événements dans les 24h
     * (ignore le flag reminder_sent pour le test)
     */
    public void sendManualRemindersForTest() throws SQLException {
        System.out.println("\n🔔 ===== TEST MANUEL : ENVOI DES RAPPELS =====");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime in24Hours = now.plusHours(24);

        List<SchoolEvent> upcomingEvents = eventService.getEventsStartingBetween(now, in24Hours);

        if (upcomingEvents.isEmpty()) {
            System.out.println("📭 Aucun événement dans les 24 prochaines heures");
            return;
        }

        System.out.println("📧 " + upcomingEvents.size() + " événement(s) trouvé(s) dans les 24h");

        int totalEmailsSent = 0;

        for (SchoolEvent event : upcomingEvents) {
            System.out.println("\n   📅 Événement: " + event.getTitle());
            System.out.println("      Date: " + (event.getStartDate() != null ? event.getStartDate().format(formatter) : "N/A"));

            List<EventRegistration> registrations = registrationService.recupererParEventId(event.getId());

            if (registrations.isEmpty()) {
                System.out.println("      ⚠️ Aucune inscription pour cet événement");
                continue;
            }

            System.out.println("      📝 " + registrations.size() + " inscription(s)");

            for (EventRegistration registration : registrations) {
                if (registration.getParent() != null && registration.getParent().getEmail() != null) {
                    sendReminderForRegistration(registration, event);
                    totalEmailsSent++;
                    System.out.println("      ✅ Rappel envoyé à: " + registration.getParent().getEmail() + " (pour " + registration.getChildFullName() + ")");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    System.out.println("      ⚠️ Pas d'email pour: " + registration.getChildFullName());
                }
            }
        }

        System.out.println("\n📊 === RÉSUMÉ ===");
        System.out.println("   Total emails envoyés: " + totalEmailsSent);
        System.out.println("   Vérifiez vos boîtes mail !\n");
    }

    /**
     * ✅ TEST MANUEL - Envoie un rappel pour une inscription spécifique par son ID
     */
    public void sendManualReminderForRegistration(int registrationId) throws SQLException {
        EventRegistration registration = registrationService.recupererParId(registrationId);
        if (registration == null) {
            System.out.println("❌ Inscription non trouvée avec l'ID: " + registrationId);
            return;
        }

        if (registration.getEvent() == null) {
            System.out.println("❌ Événement non trouvé pour cette inscription");
            return;
        }

        System.out.println("\n🔔 TEST MANUEL - Envoi d'un rappel pour l'inscription ID: " + registrationId);
        System.out.println("   Enfant: " + registration.getChildFullName());
        System.out.println("   Événement: " + registration.getEvent().getTitle());
        System.out.println("   Parent: " + (registration.getParent() != null ? registration.getParent().getEmail() : "N/A"));

        sendReminderForRegistration(registration, registration.getEvent());

        System.out.println("✅ Rappel envoyé !\n");
    }

    public void stopReminderScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            running = false;
            System.out.println("⏹ Scheduler de rappels arrêté");
        }
    }
}