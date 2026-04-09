package dev.eduplay.mains;

import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class UserMain {
    public static void main(String[] args) {

        UserService us = new UserService();

        // 🔹 Création d’un utilisateur
        User u1 = new User("Ali", "Ben Salah", "ali@gmail.com", "parent");
        u1.setUsername("ali123");
        u1.setPassword("123456");
        u1.setBirthDate(LocalDate.of(1990, 5, 12));
        u1.setTelephone("12345678");
        u1.setAdresse("Tunis");
        u1.setActive(true);

        User u2 = new User("Sarra", "Trabelsi", "sarra@gmail.com", "enseignant");
        u2.setUsername("sarraT");
        u2.setPassword("abcdef");
        u2.setSpecialite("Math");
        u2.setTelephone("98765432");

        // ================== AJOUT ==================
        /*
        try {
            us.ajouter(u1);
            us.ajouter(u2);
            System.out.println("Users ajoutés !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        */

        // ================== AFFICHAGE ==================
        /*
        try {
            List<User> users = us.getAll();

            System.out.println("Liste des utilisateurs :");
            for (User u : users) {
                System.out.println(u);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        */

        // ================== MODIFICATION ==================
        /*
        u1.setEmail("ali_new@gmail.com");
        u1.setAdresse("Sousse");

        try {
            us.modifier(u1);
            System.out.println("User modifié !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        */

        // ================== SUPPRESSION ==================

        us.supprimer(u1);
        System.out.println("User supprimé !");

    }
}