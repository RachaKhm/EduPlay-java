package dev.eduplay.services;

import dev.eduplay.entities.EventResource;
import dev.eduplay.entities.SchoolEvent;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventResourceServiceTest {

    static EventResourceService service;
    static SchoolEventService eventService;
    static int testEventId;
    static int testResourceId;

    @BeforeAll
    static void setup() throws SQLException {
        service = new EventResourceService();
        eventService = new SchoolEventService();

        // Créer un événement de test
        SchoolEvent event = new SchoolEvent();
        event.setTitle("[TEST] Événement pour ressources");
        event.setDescription("Description test");
        event.setStartDate(LocalDateTime.now().plusDays(1));
        event.setEndDate(LocalDateTime.now().plusDays(2));
        event.setLocation("Lieu test");
        event.setCreatedAt(LocalDateTime.now());

        eventService.ajouter(event);
        testEventId = event.getId();

        System.out.println("=== Démarrage des tests EventResourceService ===");
    }

    @AfterAll
    static void cleanup() throws SQLException {
        // Supprimer l'événement de test
        SchoolEvent event = eventService.recupererParId(testEventId);
        if (event != null) {
            eventService.supprimerAvecRessources(event);
        }
        System.out.println("=== Nettoyage final ===");
    }

    @AfterEach
    void cleanUp() throws SQLException {
        // Nettoyer les ressources de test
        List<EventResource> resources = service.recupererParEventId(testEventId);
        for (EventResource r : resources) {
            if (r.getTitle() != null && r.getTitle().startsWith("[TEST]")) {
                service.supprimer(r);
            }
        }
    }

    @Test
    @Order(1)
    void testAjouterResource() throws SQLException {
        System.out.println("=== TEST: Ajouter une ressource ===");

        SchoolEvent event = new SchoolEvent();
        event.setId(testEventId);

        EventResource resource = new EventResource();
        resource.setType("DOCUMENT");
        resource.setTitle("[TEST] Guide de test");
        resource.setContext("Ceci est un document de test");
        resource.setFilePath("uploads/test.pdf");
        resource.setCreatedAt(LocalDateTime.now());
        resource.setEvent(event);

        service.ajouter(resource);
        testResourceId = resource.getId();

        assertTrue(testResourceId > 0);
        System.out.println("✅ Ressource ajoutée avec ID: " + testResourceId);
    }

    @Test
    @Order(2)
    void testRecupererRessourcesParEventId() throws SQLException {
        System.out.println("=== TEST: Récupérer les ressources par ID événement ===");

        List<EventResource> resources = service.recupererParEventId(testEventId);

        assertNotNull(resources);
        assertFalse(resources.isEmpty());
        System.out.println("✅ Nombre de ressources: " + resources.size());
    }

    @Test
    @Order(3)
    void testRecupererResourceParId() throws SQLException {
        System.out.println("=== TEST: Récupérer une ressource par ID ===");

        EventResource resource = service.recupererParId(testResourceId);

        assertNotNull(resource);
        assertEquals("[TEST] Guide de test", resource.getTitle());
        System.out.println("✅ Ressource trouvée: " + resource.getTitle());
    }

    @Test
    @Order(4)
    void testModifierResource() throws SQLException {
        System.out.println("=== TEST: Modifier une ressource ===");

        EventResource resource = service.recupererParId(testResourceId);
        assertNotNull(resource);

        resource.setTitle("[TEST] Guide modifié");
        resource.setType("VIDEO");
        service.modifier(resource);

        EventResource modifiedResource = service.recupererParId(testResourceId);
        assertEquals("[TEST] Guide modifié", modifiedResource.getTitle());
        assertEquals("VIDEO", modifiedResource.getType());
        System.out.println("✅ Ressource modifiée avec succès");
    }

    @Test
    @Order(5)
    void testSupprimerResource() throws SQLException {
        System.out.println("=== TEST: Supprimer une ressource ===");

        EventResource resource = service.recupererParId(testResourceId);
        assertNotNull(resource);

        service.supprimer(resource);

        EventResource deletedResource = service.recupererParId(testResourceId);
        assertNull(deletedResource);
        System.out.println("✅ Ressource supprimée avec succès");
    }

    @Test
    @Order(6)
    void testAjouterResourceSansTitre() {
        System.out.println("=== TEST: Ajouter une ressource sans titre (doit échouer) ===");

        EventResource resource = new EventResource();
        resource.setType("DOCUMENT");
        resource.setTitle("");
        resource.setEvent(new SchoolEvent());
        resource.getEvent().setId(testEventId);

        assertThrows(SQLException.class, () -> {
            service.ajouter(resource);
        });
        System.out.println("✅ Exception correctement levée");
    }
}