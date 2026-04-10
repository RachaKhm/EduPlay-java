package dev.eduplay.mains;

import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.entities.User;
import dev.eduplay.services.EventRegistrationService;
import dev.eduplay.services.SchoolEventService;
import dev.eduplay.services.UserService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class EventRegistrationMain {
    public static void main(String[] args) throws SQLException {
        EventRegistrationService ers = new EventRegistrationService();
        SchoolEventService schoolEventService = new SchoolEventService();
        UserService userService = new UserService();


        SchoolEvent event1 = new SchoolEvent(27, "Atelier Mosaïstes", "Description",
                LocalDateTime.of(2026, 5, 15, 10, 0), LocalDateTime.of(2026, 5, 15, 12, 0),
                "Tunis", null, null, null, null, null, null);

        SchoolEvent event2 = new SchoolEvent(2, "Safari Oasis", "Description",
                LocalDateTime.of(2026, 6, 10, 9, 30), LocalDateTime.of(2026, 6, 10, 16, 0),
                "Enfidha", null, null, null, null, null, null);

        SchoolEvent event3 = new SchoolEvent(3, "Pâtissier en Herbe", "Description",
                LocalDateTime.of(2026, 7, 5, 14, 0), LocalDateTime.of(2026, 7, 5, 17, 0),
                "Sidi Bou Saïd", null, null, null, null, null, null);

        // ========== CRÉER LES PARENTS ==========
        User parent1 = new User("Fatma", "Ben Ali", "fatma.benali@email.com", "PARENT");
        parent1.setId(1);

        User parent2 = new User("Karim", "Mansouri", "karim.mansouri@email.com", "PARENT");
        parent2.setId(2);

        User parent3 = new User("Sami", "Khemiri", "sami.khemiri@email.com", "PARENT");
        parent3.setId(3);

        EventRegistration registration1 = new EventRegistration(
                24,
                EventRegistration.STATUS_PENDING,
                LocalDateTime.now(),
                "Lina Ben Ali",
                "22123456",
                "3A",
                "Allergie aux arachides",
                "Ahmed Ben Ali",
                "55123456",
                "Autorisation parentale fournie",
                "qr_code_1",
                "uploads/qrcodes/ticket_1.png",
                null,
                false,
                null
        );
        registration1.setEvent(event1);
        registration1.setParent(parent1);

//        EventRegistration registration2 = new EventRegistration(
//                2,
//                EventRegistration.STATUS_APPROVED,
//                LocalDateTime.now(),
//                "Youssef Mansouri",
//                "33234567",
//                "CE2",
//                null,
//                "Sonia Mansouri",
//                "66234567",
//                null,
//                "qr_code_2",
//                "uploads/qrcodes/ticket_2.png",
//                LocalDateTime.now(),
//                true,
//                LocalDateTime.now()
//        );
//        registration2.setEvent(event2);
//        registration2.setParent(parent2);
//
//        EventRegistration registration3 = new EventRegistration(
//                3,
//                EventRegistration.STATUS_REJECTED,
//                LocalDateTime.now(),
//                "Maya Khemiri",
//                "55456789",
//                "6ème",
//                "Asthme",
//                "Karim Khemiri",
//                "77456789",
//                "Placement en liste d'attente",
//                "qr_code_3",
//                "uploads/qrcodes/ticket_3.png",
//                null,
//                false,
//                null
//        );
//        registration3.setEvent(event3);
//        registration3.setParent(parent3);

//        //ajout
//        try {
//            ers.ajouter(registration1);
//            System.out.println("registration ajoutée!");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }

//                //suppression
        try {
//            EventRegistration registration4 = new EventRegistration(
//                    4,
//                    EventRegistration.STATUS_REJECTED,
//                    LocalDateTime.now(),
//                    "Maya Khemiri",
//                    "55456789",
//                    "6ème",
//                    "Asthme",
//                    "Karim Khemiri",
//                    "77456789",
//                    "Placement en liste d'attente",
//                    "qr_code_3",
//                    "uploads/qrcodes/ticket_3.png",
//                    null,
//                    false,
//                    null);
            ers.supprimer(registration1);
            System.out.println("registration supprimé!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        //recuperation
        try {
            List<EventRegistration> registrations = ers.recuperer();

            System.out.println("Registrations in the database:");
            for (EventRegistration q : registrations) {
                System.out.println(q);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

//        //        //modif
//        EventRegistration registration5 = new EventRegistration(
//                5,
//                EventRegistration.STATUS_REJECTED,
//                LocalDateTime.now(),
//                "Maya Khemiri",
//                "55456789",
//                "6ème",
//                "Asthme",
//                "Karim Khemiri",
//                "77456789",
//                "Placement en liste d'attente",
//                "qr_code_3",
//                "uploads/qrcodes/ticket_3.png",
//                null,
//                false,
//                null);
//        registration1.setChildFullName("Racha");
//        registration1.setParentPhone("50000000");
//        try {
//            ers.modifier(registration1);
//            System.out.println("registration modifié!");
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }

//        //chercher
//        EventRegistration registration6 = new EventRegistration(
//                6,
//                EventRegistration.STATUS_REJECTED,
//                LocalDateTime.now(),
//                "Maya Khemiri",
//                "55456789",
//                "6ème",
//                "Asthme",
//                "Karim Khemiri",
//                "77456789",
//                "Placement en liste d'attente",
//                "qr_code_3",
//                "uploads/qrcodes/ticket_3.png",
//                null,
//                false,
//                null);
//        try{
//            ers.chercher(registration6);
//        } catch (SQLException e){
//            System.out.println(e.getMessage());
//        }

    }




}
