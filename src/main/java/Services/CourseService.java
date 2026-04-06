package Services;

import Entities.Course;
import Utils.MyDb;

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
                MyDb.getInstance().getConn(),
                "No JDBC connection — MyDb failed to open (see earlier SQLException).");
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
                    return keys.getInt(1);
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
