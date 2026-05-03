//package dev.eduplay.services;
//
//import dev.eduplay.entities.EventRegistration;
//import dev.eduplay.entities.SchoolEvent;
//import dev.eduplay.entities.User;
//import org.junit.jupiter.api.*;
//
//import java.sql.SQLException;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//class EventRegistrationServiceTest {
//
//    static EventRegistrationService service;
//    static SchoolEventService eventService;
//    static UserService userService;
//    static int testEventId;
//    static int testUserId;
//    static int testRegistrationId;
//
//    @BeforeAll
//    static void setup() throws SQLException {
//        service = new EventRegistrationService();
//        eventService = new SchoolEventService();
//        userService = new UserService();
//
//        // Créer un événement de test
//        SchoolEvent event = new SchoolEvent();
//        event.setTitle("[TEST] Événement pour inscriptions");
//        event.setDescription("Description test");
//        event.setStartDate(LocalDateTime.now().plusDays(1));
//        event.setEndDate(LocalDateTime.now().plusDays(2));
//        event.setLocation("Lieu test");
//        event.setCreatedAt(LocalDateTime.now());
//        eventService.ajouter(event);
//        testEventId = event.getId();
//
//        // Créer un utilisateur parent de test
//        User user = new User();
//        user.setFirstName("Test");
//        user.setLastName("Parent");
//        user.setEmail("test.parent@email.com");
//        user.setPassword("password123");
//        user.setType("parent");
//        user.setActive(true);
//        userService.ajouter(user);
//
//        // Récupérer l'ID après insertion
//        List<User> users = userService.recuperer();
//        for (User u : users) {
//            if (u.getEmail() != null && u.getEmail().equals("test.parent@email.com")) {
//                testUserId = u.getId();
//                break;
//            }
//        }
//
//        System.out.println("=== Démarrage des tests ===");
//        System.out.println("Event ID: " + testEventId);
//        System.out.println("User ID: " + testUserId);
//    }
//
//    @AfterAll
//    static void cleanup() throws SQLException {
//        // Nettoyer l'événement de test
//        List<SchoolEvent> events = eventService.recuperer();
//        for (SchoolEvent e : events) {
//            if (e.getId() == testEventId) {
//                eventService.supprimerAvecRessources(e);
//                break;
//            }
//        }
//
//        // Nettoyer l'utilisateur de test
//        List<User> users = userService.recuperer();
//        for (User u : users) {
//            if (u.getId() == testUserId) {
//                userService.supprimer(u);
//                break;
//            }
//        }
//        System.out.println("=== Nettoyage final ===");
//    }
//
//    @AfterEach
//    void cleanUp() throws SQLException {
//        // Nettoyer les inscriptions de test
//        List<EventRegistration> registrations = service.recuperer();
//        for (EventRegistration r : registrations) {
//            if (r.getChildFullName() != null && r.getChildFullName().startsWith("[TEST]")) {
//                service.supprimer(r);
//            }
//        }
//    }
//
//    @Test
//    @Order(1)
//    void testAjouterInscription() throws SQLException {
//        System.out.println("=== TEST: Ajouter une inscription ===");
//
//        // Récupérer l'événement
//        SchoolEvent event = null;
//        for (SchoolEvent e : eventService.recuperer()) {
//            if (e.getId() == testEventId) {
//                event = e;
//                break;
//            }
//        }
//
//        // Récupérer le parent
//        User parent = null;
//        for (User u : userService.recuperer()) {
//            if (u.getId() == testUserId) {
//                parent = u;
//                break;
//            }
//        }
//
//        assertNotNull(event, "L'événement ne doit pas être null");
//        assertNotNull(parent, "Le parent ne doit pas être null");
//
//        EventRegistration registration = new EventRegistration();
//        registration.setEvent(event);
//        registration.setParent(parent);
//        registration.setRegisteredAt(LocalDateTime.now());
//        registration.setChildFullName("[TEST] Enfant Test");
//        registration.setParentPhone("12345678");
//        registration.setChildClassLevel("CE2");
//        registration.setReminderSent(false);
//
//        service.ajouter(registration);
//        testRegistrationId = registration.getId();
//
//        assertTrue(testRegistrationId > 0);
//        System.out.println("✅ Inscription ajoutée avec ID: " + testRegistrationId);
//    }
//
//    @Test
//    @Order(2)
//    void testRecupererInscriptions() throws SQLException {
//        System.out.println("=== TEST: Récupérer les inscriptions ===");
//
//        List<EventRegistration> registrations = service.recuperer();
//
//        assertNotNull(registrations);
//        System.out.println("✅ Nombre d'inscriptions: " + registrations.size());
//    }
//
//    @Test
//    @Order(3)
//    void testModifierInscription() throws SQLException {
//        System.out.println("=== TEST: Modifier une inscription ===");
//
//        // Trouver l'inscription de test
//        EventRegistration registration = null;
//        for (EventRegistration r : service.recuperer()) {
//            if (r.getId() == testRegistrationId) {
//                registration = r;
//                break;
//            }
//        }
//
//        assertNotNull(registration);
//
//        registration.setNotes("Test notes");
//        service.modifierBack(registration);
//
//        // Vérifier la modification
//        EventRegistration modifiedRegistration = null;
//        for (EventRegistration r : service.recuperer()) {
//            if (r.getId() == testRegistrationId) {
//                modifiedRegistration = r;
//                break;
//            }
//        }
//
//        assertNotNull(modifiedRegistration);
//        assertEquals("Test notes", modifiedRegistration.getNotes());
//        System.out.println("✅ Inscription modifiée avec succès");
//    }
//
//    @Test
//    @Order(4)
//    void testSupprimerInscription() throws SQLException {
//        System.out.println("=== TEST: Supprimer une inscription ===");
//
//        // Trouver l'inscription de test
//        EventRegistration registration = null;
//        for (EventRegistration r : service.recuperer()) {
//            if (r.getId() == testRegistrationId) {
//                registration = r;
//                break;
//            }
//        }
//
//        assertNotNull(registration);
//
//        service.supprimer(registration);
//
//        // Vérifier la suppression
//        boolean exists = false;
//        for (EventRegistration r : service.recuperer()) {
//            if (r.getId() == testRegistrationId) {
//                exists = true;
//                break;
//            }
//        }
//
//        assertFalse(exists);
//        System.out.println("✅ Inscription supprimée avec succès");
//    }
//}