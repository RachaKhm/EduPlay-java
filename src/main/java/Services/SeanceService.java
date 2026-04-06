package Services;

import Entities.Seance;
import Interfaces.IService;
import Utils.MyDb;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SeanceService implements IService<Seance> {
    private final Connection cnx;

    public SeanceService() throws SQLException {
        cnx = Objects.requireNonNull(
                MyDb.getInstance().getConn(),
                "No JDBC connection — MyDb failed to open (see earlier SQLException).");
    }

    @Override
    public void add(Seance s) throws SQLException {
        ajouter(s);
    }

    @Override
    public void delete(Seance s) throws SQLException {
        supprimer(s.getId());
    }

    @Override
    public void update(Seance s) throws SQLException {
        modifier(s);
    }

    @Override
    public List<Seance> display() throws SQLException {
        return afficherTous();
    }

    public int ajouter(Seance s) throws SQLException {
        String sql = """
                INSERT INTO seance (start_time, end_time, course_id, title, date, location, status, description)
                VALUES (?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement st = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setTimestamp(1, Timestamp.valueOf(s.getStartTime()));
            st.setTimestamp(2, Timestamp.valueOf(s.getEndTime()));
            st.setInt(3, s.getCourseId());
            st.setString(4, s.getTitle());
            if (s.getDate() != null) {
                st.setDate(5, Date.valueOf(s.getDate()));
            } else {
                st.setNull(5, Types.DATE);
            }
            st.setString(6, s.getLocation());
            st.setString(7, s.getStatus() != null ? s.getStatus() : "scheduled");
            st.setString(8, s.getDescription());
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Insert seance failed: no generated key");
    }

    public void modifier(Seance s) throws SQLException {
        String sql = """
                UPDATE seance SET start_time=?, end_time=?, course_id=?, title=?, date=?, location=?, status=?, description=?
                WHERE id=?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(s.getStartTime()));
            ps.setTimestamp(2, Timestamp.valueOf(s.getEndTime()));
            ps.setInt(3, s.getCourseId());
            ps.setString(4, s.getTitle());
            if (s.getDate() != null) {
                ps.setDate(5, Date.valueOf(s.getDate()));
            } else {
                ps.setNull(5, Types.DATE);
            }
            ps.setString(6, s.getLocation());
            ps.setString(7, s.getStatus());
            ps.setString(8, s.getDescription());
            ps.setInt(9, s.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM seance WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Optional<Seance> trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM seance WHERE id=?";
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

    public List<Seance> afficherTous() throws SQLException {
        List<Seance> list = new ArrayList<>();
        String sql = "SELECT * FROM seance ORDER BY id";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Seance> parCourseId(int courseId) throws SQLException {
        List<Seance> list = new ArrayList<>();
        String sql = "SELECT * FROM seance WHERE course_id=? ORDER BY id";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private static Seance mapRow(ResultSet rs) throws SQLException {
        Timestamp st = rs.getTimestamp("start_time");
        Timestamp et = rs.getTimestamp("end_time");
        LocalDateTime start = st != null ? st.toLocalDateTime() : null;
        LocalDateTime end = et != null ? et.toLocalDateTime() : null;
        Date d = rs.getDate("date");
        LocalDate date = d != null ? d.toLocalDate() : null;
        return new Seance(
                rs.getInt("id"),
                start,
                end,
                rs.getInt("course_id"),
                rs.getString("title"),
                date,
                rs.getString("location"),
                rs.getString("status"),
                rs.getString("description")
        );
    }
}
