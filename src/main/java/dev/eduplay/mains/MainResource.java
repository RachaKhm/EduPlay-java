package dev.eduplay.mains;

import dev.eduplay.entities.Resource;
import dev.eduplay.services.ResourceService;

public class MainResource {
    public static void main(String[] args) {

        ResourceService service = new ResourceService();

        // Ajouter
        service.ajouter(new Resource(
                1, // ⚠️ doit exister dans table library
                "Java Resource",
                "Ali",
                "Livre de programmation",
                "img.jpg",
                "file.pdf",
                "Education",
                10,
                18,
                "FR"
        ));

        // Afficher
        service.afficher().forEach(b ->
                System.out.println(b.getId() + " | " + b.getTitle())
        );

        // Modifier
        service.modifier(new Resource(
                1, // ⚠️ ID existant
                1,
                "UPDATED TITLE",
                "NEW AUTHOR",
                "NEW DESC",
                "new.jpg",
                "new.pdf",
                "Science",
                12,
                20,
                "EN"
        ));

        // Supprimer
        service.supprimer(1);
    }
}