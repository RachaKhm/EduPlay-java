package dev.eduplay.services;

import dev.eduplay.entities.Course;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CourseService {
    private final Connection cnx;

    public CourseService() throws SQLException {
        cnx = Objects.requireNonNull(
                MyDataBase.getInstance().getCnx(),
                "No JDBC connection — MyDataBase failed to open (see earlier SQLException).");
    }

    public int ajouter(Course c) throws SQLException {
        String sql = """
                INSERT INTO course (title, duration_training, description, level, pdf_file, status, teacher_id)
                VALUES (?,?,?,?,?,?,?)
                """;
        try (PreparedStatement st = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setString(1, c.getTitle());
            if (c.getDurationTraining() != null) {
                st.setInt(2, c.getDurationTraining());
            } else {
                st.setNull(2, Types.INTEGER);
            }
            st.setString(3, c.getDescription());
            st.setString(4, c.getLevel());
            st.setString(5, c.getPdfFile());
            st.setString(6, c.getStatus());
            st.setInt(7, c.getTeacherId());
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    int courseId = keys.getInt(1);
                    // Notification email
                    try {
                        String teacherName = dev.eduplay.core.AppContext.getFullName();
                        EmailService.sendCourseCreationNotification(teacherName, c.getTitle());
                    } catch (Exception e) {
                        System.err.println("Failed to send course notification: " + e.getMessage());
                    }
                    return courseId;
                }
            }
        }
        throw new SQLException("Insert course failed: no generated key");
    }

    public void modifier(Course c) throws SQLException {
        String sql = """
                UPDATE course SET title=?, duration_training=?, description=?, level=?, pdf_file=?, status=?, teacher_id=?
                WHERE id=?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getTitle());
            if (c.getDurationTraining() != null) {
                ps.setInt(2, c.getDurationTraining());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, c.getDescription());
            ps.setString(4, c.getLevel());
            ps.setString(5, c.getPdfFile());
            ps.setString(6, c.getStatus());
            ps.setInt(7, c.getTeacherId());
            ps.setInt(8, c.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Mise à jour du contenu par l'enseignant : le statut n'est pas modifié (reste géré par l'admin).
     */
    public void modifierContenuSansStatut(Course c) throws SQLException {
        String sql = """
                UPDATE course SET title=?, duration_training=?, description=?, level=?, pdf_file=?, teacher_id=?
                WHERE id=?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getTitle());
            if (c.getDurationTraining() != null) {
                ps.setInt(2, c.getDurationTraining());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, c.getDescription());
            ps.setString(4, c.getLevel());
            ps.setString(5, c.getPdfFile());
            ps.setInt(6, c.getTeacherId());
            ps.setInt(7, c.getId());
            ps.executeUpdate();
        }
    }

    public void mettreAJourStatut(int id, String status) throws SQLException {
        String sql = "UPDATE course SET status=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM course WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Optional<Course> trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM course WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Course> afficherTous() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM course ORDER BY id";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Cours visibles dans le catalogue parent (publiés uniquement). */
    public List<Course> afficherPublies() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE LOWER(TRIM(status)) = 'published' ORDER BY created_at DESC, id DESC";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Cours auxquels l'enfant est inscrit (abonnement actif).
     * Ne filtre plus sur le statut du cours : un enfant doit voir tout cours où il est abonné
     * (brouillon, publié, etc.). Les cours archivés sont exclus.
     */
    public List<Course> afficherPourEnfantAbonne(int kidId) throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = """
                SELECT DISTINCT c.* FROM course c
                INNER JOIN subscription s ON s.course_id = c.id AND s.kid_id = ?
                WHERE IFNULL(s.active, 0) = 1
                  AND LOWER(TRIM(COALESCE(c.status, ''))) <> 'archived'
                ORDER BY c.created_at DESC, c.id DESC
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, kidId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Course> afficherParTeacher(int teacherId) throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM course WHERE teacher_id = ? ORDER BY id";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, teacherId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private static Course mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime created = ts != null ? ts.toLocalDateTime() : null;
        int dur = rs.getInt("duration_training");
        boolean durationNull = rs.wasNull();
        return new Course(
                rs.getInt("id"),
                rs.getString("title"),
                durationNull ? null : dur,
                rs.getString("description"),
                rs.getString("level"),
                rs.getString("pdf_file"),
                rs.getString("status"),
                rs.getInt("teacher_id"),
                created
        );
    }
}
