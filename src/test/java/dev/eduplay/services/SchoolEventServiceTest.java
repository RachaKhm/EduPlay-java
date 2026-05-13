package dev.eduplay.services;

import dev.eduplay.entities.SchoolEvent;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SchoolEventServiceTest {

    static SchoolEventService service;
    static int testEventId;

    @BeforeAll
    static void setup() {
        service = new SchoolEventService();
        System.out.println("=== Démarrage des tests SchoolEventService ===");
    }

    @BeforeEach
    void init() {
        System.out.println("--- Préparation avant test ---");
    }

    @AfterEach
    void cleanUp() throws SQLException {
        // Nettoyer les données de test après chaque test
        List<SchoolEvent> events = service.recuperer();
        for (SchoolEvent e : events) {
            if (e.getTitle() != null && e.getTitle().startsWith("[TEST]")) {
                service.supprimerAvecRessources(e);
            }
        }
        System.out.println("--- Nettoyage après test ---");
    }

    @Test
    @Order(1)
    void testAjouterEvent() throws SQLException {
        System.out.println("=== TEST: Ajouter un événement ===");

        SchoolEvent event = new SchoolEvent();
        event.setTitle("[TEST] Conférence Java");
        event.setDescription("Description de test pour la conférence");
        event.setStartDate(LocalDateTime.of(2025, 12, 15, 10, 0));
        event.setEndDate(LocalDateTime.of(2025, 12, 15, 18, 0));
        event.setLocation("Salle A - Université");
        event.setCreatedAt(LocalDateTime.now());

        service.ajouter(event);
        testEventId = event.getId();

        assertTrue(testEventId > 0, "L'ID de l'événement devrait être > 0");
        System.out.println("✅ Événement ajouté avec ID: " + testEventId);
    }

    @Test
    @Order(2)
    void testRecupererTousEvents() throws SQLException {
        System.out.println("=== TEST: Récupérer tous les événements ===");

        List<SchoolEvent> events = service.recuperer();

        assertNotNull(events, "La liste ne devrait pas être null");
        assertFalse(events.isEmpty(), "La liste ne devrait pas être vide");
        System.out.println("✅ Nombre d'événements: " + events.size());
    }

    @Test
    @Order(3)
    void testRecupererEventParId() throws SQLException {
        System.out.println("=== TEST: Récupérer un événement par ID ===");

        SchoolEvent event = service.recupererParId(testEventId);

        assertNotNull(event, "L'événement devrait exister");
        assertEquals("[TEST] Conférence Java", event.getTitle());
        System.out.println("✅ Événement trouvé: " + event.getTitle());
    }

    @Test
    @Order(4)
    void testModifierEvent() throws SQLException {
        System.out.println("=== TEST: Modifier un événement ===");

        SchoolEvent event = service.recupererParId(testEventId);
        assertNotNull(event);

        event.setTitle("[TEST] Conférence Java Modifiée");
        event.setLocation("Salle B - Université");
        service.modifier(event);

        SchoolEvent modifiedEvent = service.recupererParId(testEventId);
        assertEquals("[TEST] Conférence Java Modifiée", modifiedEvent.getTitle());
        assertEquals("Salle B - Université", modifiedEvent.getLocation());
        System.out.println("✅ Événement modifié avec succès");
    }

    @Test
    @Order(5)
    void testChercherEvent() throws SQLException {
        System.out.println("=== TEST: Chercher un événement ===");

        SchoolEvent event = new SchoolEvent();
        event.setId(testEventId);
        int result = service.chercher(event);

        assertEquals(testEventId, result);
        System.out.println("✅ Événement trouvé avec ID: " + result);
    }

    @Test
    @Order(6)
    void testSupprimerEvent() throws SQLException {
        System.out.println("=== TEST: Supprimer un événement ===");

        SchoolEvent event = service.recupererParId(testEventId);
        assertNotNull(event);

        service.supprimerAvecRessources(event);

        SchoolEvent deletedEvent = service.recupererParId(testEventId);
        assertNull(deletedEvent, "L'événement devrait être supprimé");
        System.out.println("✅ Événement supprimé avec succès");
    }

    @Test
    @Order(7)
    void testAjouterEventSansTitre() {
        System.out.println("=== TEST: Ajouter un événement sans titre (doit échouer) ===");

        SchoolEvent event = new SchoolEvent();
        event.setTitle("");
        event.setDescription("Test sans titre");

        assertThrows(SQLException.class, () -> {
            service.ajouter(event);
        });
        System.out.println("✅ Exception correctement levée");
    }
}