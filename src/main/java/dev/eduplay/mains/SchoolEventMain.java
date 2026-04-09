package dev.eduplay.mains;

import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.services.SchoolEventService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SchoolEventMain {
    public static void main(String[] args) throws SQLException {
        SchoolEventService ses = new SchoolEventService();
        SchoolEvent event1 = new SchoolEvent(1, "Atelier Petits Mosaïstes", "Viens découvrir l'art de la mosaïque",
                LocalDateTime.of(2026, 5, 15, 10, 0), LocalDateTime.of(2026, 5, 15, 12, 0),
                "Musée National du Bardo, Tunis", "uploads/events/mosaique.jpg",
                null, "36.809876", "10.134552", null, null);

        SchoolEvent event2 = new SchoolEvent(2, "Safari des Oasis", "Découverte des animaux du désert",
                LocalDateTime.of(2026, 6, 10, 9, 30), LocalDateTime.of(2026, 6, 10, 16, 0),
                "Zoo d'Enfidha, Enfidha", "uploads/events/safari.jpg",
                null, "36.135722", "10.374585", null, null);

        SchoolEvent event3 = new SchoolEvent(3, "Pâtissier en Herbe", "Prépare des zlabia et baklawa",
                LocalDateTime.of(2026, 7, 5, 14, 0), LocalDateTime.of(2026, 7, 5, 17, 0),
                "Maison de la Culture, Sidi Bou Saïd", "uploads/events/patisserie.jpg",
                null, "36.869650", "10.342430", null, null);

//        try {
//            ses.ajouter(event1);
//            System.out.println("event ajouté!");
//        }
//        catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }



//        try {
//            List<SchoolEvent> events = ses.recuperer();
//
//            System.out.println("Events in the database:");
//            for (SchoolEvent s : events) {
//                System.out.println(s);
//            }
//
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }


        SchoolEvent event4 = new SchoolEvent(25, "Atelier Petits Mosaïstes", "Viens découvrir l'art de la mosaïque",
                LocalDateTime.of(2026, 5, 15, 10, 0), LocalDateTime.of(2026, 5, 15, 12, 0),
                "Musée National du Bardo, Tunis", "uploads/events/mosaique.jpg",
                null, "36.809876", "10.134552", null, null);
//        try{
//            ses.chercher(event4);
//        } catch (SQLException e){
//            System.out.println(e.getMessage());
//        }
//
//        event4.setTitle("test");
//        event4.setDescription("test");
//        try {
//            ses.modifier(event4);
//            System.out.println("event modifié!");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }

        try {
            ses.supprimer(event4);
            System.out.println("event supprimé!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

}
