package dev.eduplay.tools;

import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Lance ce main pour insérer les 4 comptes de test en DB.
 * Run → DataSeeder.main()
 */
public class DataSeeder {

    public static void main(String[] args) {
        UserService service = new UserService();
        String hashedPwd = BCrypt.hashpw("123456", BCrypt.gensalt());

        // ── Admin ─────────────────────────────────────────────────────────
        User admin = new User();
        admin.setFirstName("Admin");
        admin.setLastName("EduPlay");
        admin.setEmail("admin@gmail.com");
        admin.setUsername("admin");
        admin.setPassword(hashedPwd);
        admin.setType("admin");
        admin.setActive(true);
        insertIfNotExists(service, admin, "admin@gmail.com");

        // ── Parent ────────────────────────────────────────────────────────
        User parent = new User();
        parent.setFirstName("Parent");
        parent.setLastName("Test");
        parent.setEmail("parent@gmail.com");
        parent.setUsername("parent");
        parent.setPassword(hashedPwd);
        parent.setType("parent");
        parent.setActive(true);
        insertIfNotExists(service, parent, "parent@gmail.com");

        // ── Enseignant ────────────────────────────────────────────────────
        User teacher = new User();
        teacher.setFirstName("Teacher");
        teacher.setLastName("Test");
        teacher.setEmail("teacher@gmail.com");
        teacher.setUsername("teacher");
        teacher.setPassword(hashedPwd);
        teacher.setType("enseignant");
        teacher.setSpecialite("Informatique");
        teacher.setActive(true);
        insertIfNotExists(service, teacher, "teacher@gmail.com");

        // ── Enfant ────────────────────────────────────────────────────────
        User child = new User();
        child.setFirstName("Child");
        child.setLastName("Test");
        child.setUsername("child");
        child.setPassword(hashedPwd);
        child.setType("enfant");
        child.setNiveau("3ème année");
        child.setActive(true);
        insertIfNotExistsByUsername(service, child, "child");

        System.out.println("=== Seed terminé ===");
        System.out.println("admin@gmail.com  / 123456  → dashboard admin");
        System.out.println("parent@gmail.com / 123456  → dashboard parent");
        System.out.println("teacher@gmail.com/ 123456  → dashboard enseignant");
        System.out.println("child (username) / 123456  → dashboard enfant");
    }

    private static void insertIfNotExists(UserService service, User user, String email) {
        if (service.findByLogin(email) != null) {
            System.out.println("[SKIP] " + email + " existe déjà.");
        } else {
            service.ajouter(user);
            System.out.println("[OK]   " + email + " créé.");
        }
    }

    private static void insertIfNotExistsByUsername(UserService service, User user, String username) {
        if (service.findByLogin(username) != null) {
            System.out.println("[SKIP] username '" + username + "' existe déjà.");
        } else {
            service.ajouter(user);
            System.out.println("[OK]   username '" + username + "' créé.");
        }
    }
}