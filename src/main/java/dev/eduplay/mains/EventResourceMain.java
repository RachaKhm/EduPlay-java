package dev.eduplay.mains;

import dev.eduplay.entities.EventResource;
import dev.eduplay.services.EventResourceService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class EventResourceMain {
    public static void main(String[] args) throws SQLException {
        EventResourceService qs = new EventResourceService();
        EventResource r1 = new EventResource(1, "video", "Tuto Java", "Description", "/path/video.mp4", "https://url.com", LocalDateTime.now(), null);
        EventResource r2 = new EventResource(2, "pdf", "Guide MySQL", "Description PDF", "/path/guide.pdf", "", LocalDateTime.now(), null);
        EventResource r3 = new EventResource(3, "link", "Documentation", "Liens utiles", "", "https://docs.com", LocalDateTime.now(), null);
        //ajout
        try {
            qs.ajouter(r1);
          //qs.ajouter(q5);
            System.out.println("ressource ajoutée!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

//        //suppression
        try {
            EventResource r4 = new EventResource(4, "link", "Documentation", "Liens utiles", "", "https://docs.com", LocalDateTime.now(), null);
            qs.supprimer(r4);
            System.out.println("ressource supprimée!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        //recuperation
        try {
            List<EventResource> ressources = qs.recuperer();

            System.out.println("Resource in the database:");
            for (EventResource r : ressources) {
                System.out.println(r);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

//        //modif
        EventResource r5 = new EventResource(5, "link", "Documentation", "Liens utiles", "", "https://docs.com", LocalDateTime.now(), null);
        r5.setType("pdf");
        r5.setTitle("test");
        try {
            qs.modifier(r5);
            System.out.println("Ressource modifiée!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        //chercher
        EventResource r6 = new EventResource(6, "link", "Documentation", "Liens utiles", "", "https://docs.com", LocalDateTime.now(), null);
        try{
            qs.chercher(r6);
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }

    }
}
