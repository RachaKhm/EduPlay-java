package dev.eduplay.mains;

import dev.eduplay.entities.Book;
import dev.eduplay.services.BookService;

public class MainBook {
    public static void main(String[] args) {

        BookService service = new BookService();

        // Ajouter
        service.ajouter(new Book(
                1, // ⚠️ doit exister dans table library
                "Java Book",
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
        service.modifier(new Book(
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