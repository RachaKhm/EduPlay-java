package dev.eduplay.mains;

import dev.eduplay.entities.Library;
import dev.eduplay.services.LibraryService;

public class MainLibrary {
    public static void main(String[] args) {

        LibraryService service = new LibraryService();

        // Ajouter
        service.ajouter(new Library(
                "Math Book",
                "Livre pour apprendre les maths",
                "image.jpg",
                6,
                12,
                "Beginner",
                "Education"
        ));

        // Afficher
        service.afficher().forEach(l ->
                System.out.println(l.getId() + " " + l.getName())
        );

        // Modifier
        service.modifier(new Library(
                12,
                "Math Advanced",
                "Description modifiée",
                "new.jpg",
                7,
                14,
                "Intermediate",
                "Science"
        ));

        // Supprimer
        //service.supprimer(1);
    }
}