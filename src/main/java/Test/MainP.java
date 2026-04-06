package Test;

import Entities.Course;
import Entities.Seance;
import Entities.Subscription;
import Services.CourseService;
import Services.SeanceService;
import Services.SubscriptionService;
import Utils.MyDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class MainP {

    /** FK `course.teacher_id` → `user.id` : use a real user, not a hardcoded 1. */
    private static int firstUserId(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM user ORDER BY id LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Table `user` is empty: insert at least one user before creating a course.");
    }

    /** Second user for parent/kid FKs, or same as first if only one user exists. */
    private static int nthUserId(Connection conn, int offset) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM user ORDER BY id LIMIT 1 OFFSET ?")) {
            ps.setInt(1, offset);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return firstUserId(conn);
    }

    public static void main(String[] args) throws SQLException {

        MyDb db = MyDb.getInstance();
        System.out.println("DB connection: " + (db.getConn() != null));

        CourseService courseService = new CourseService();
        SeanceService seanceService = new SeanceService();
        SubscriptionService subscriptionService = new SubscriptionService();

        /* ===================== Course ===================== */

        /* --- ajout --- */
        /*
        Course c = new Course();
        c.setTitle("Cours JDBC");
        c.setDurationTraining(60);
        c.setDescription("Test");
        c.setLevel("beginner");
        c.setPdfFile("fichier.pdf");
        c.setStatus("active");
        c.setTeacherId(firstUserId(db.getConn()));
        int idC = courseService.ajouter(c);
        System.out.println("Course ajout id=" + idC);
        */
        /* --- modifier --- */
        /*
        Course c = courseService.trouverParId(1).orElse(null);
        if (c != null) {
            c.setTitle("Cours modifié");
            c.setDurationTraining(90);
            c.setDescription("Description mise à jour");
            c.setLevel("intermediate");
            c.setPdfFile("nouveau.pdf");
            c.setStatus("inactive");
            // keep c.getTeacherId() from DB so FK stays valid
            courseService.modifier(c);
        }
        */

        /* --- delete --- */
        /*
        courseService.supprimer(1);
        */

        /* --- affichage --- */
        /*
        System.out.println(courseService.afficherTous());
        */

        /* ===================== Seance ===================== */

        /* --- ajout --- */
        /*
        Seance s = new Seance();
        s.setStartTime(LocalDateTime.of(2026, 5, 1, 9, 0));
        s.setEndTime(LocalDateTime.of(2026, 5, 1, 10, 30));
        s.setCourseId(idC);
        s.setTitle("Séance test");
        s.setDate(LocalDate.of(2026, 5, 1));
        s.setLocation("Salle A");
        s.setStatus("scheduled");
        s.setDescription("Test");
        int idS = seanceService.ajouter(s);
        System.out.println("Seance ajout id=" + idS);
        */

        /* --- modifier --- */
        /*
        Seance s = seanceService.trouverParId(1).orElse(null);
        if (s != null) {
            s.setStartTime(LocalDateTime.of(2026, 6, 1, 10, 0));
            s.setEndTime(LocalDateTime.of(2026, 6, 1, 11, 30));
            s.setCourseId(1);
            s.setTitle("Séance modifiée");
            s.setDate(LocalDate.of(2026, 6, 1));
            s.setLocation("Salle B");
            s.setStatus("done");
            s.setDescription("Contenu mis à jour");
            seanceService.modifier(s);
        }
        */

        /* --- delete --- */
        /*
        seanceService.supprimer(1);
        */

        /* --- affichage --- */
        /*
        System.out.println(seanceService.afficherTous());
        */

        /* ===================== Subscription ===================== */

        /* --- ajout --- */
        /*
        Subscription sub = new Subscription();
        sub.setParentId(nthUserId(db.getConn(), 0));
        sub.setKidId(nthUserId(db.getConn(), 1));
        sub.setCourseId(idC);
        sub.setSubscribedAt(LocalDateTime.now());
        sub.setActive(true);
        int idSub = subscriptionService.ajouter(sub);
        System.out.println("Subscription ajout id=" + idSub);
        */

        /* --- modifier --- */
        /*
        Subscription sub = subscriptionService.trouverParId(1).orElse(null);
        if (sub != null) {
            sub.setParentId(nthUserId(db.getConn(), 0));
            sub.setKidId(nthUserId(db.getConn(), 1));
            sub.setCourseId(idC);
            sub.setSubscribedAt(LocalDateTime.now());
            sub.setActive(false);
            subscriptionService.modifier(sub);
        }
        */

        /* --- delete --- */
        /*
        subscriptionService.supprimer(1);
        */

        /* --- affichage --- */
        /*
        System.out.println(subscriptionService.afficherTous());
        */
    }
}
