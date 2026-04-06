package Services;

import Entities.Subscription;
import Interfaces.IService;
import Utils.MyDb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SubscriptionService implements IService<Subscription> {
    private final Connection cnx;

    public SubscriptionService() throws SQLException {
        cnx = Objects.requireNonNull(
                MyDb.getInstance().getConn(),
                "No JDBC connection — MyDb failed to open (see earlier SQLException).");
    }

    @Override
    public void add(Subscription s) throws SQLException {
        ajouter(s);
    }

    @Override
    public void delete(Subscription s) throws SQLException {
        supprimer(s.getId());
    }

    @Override
    public void update(Subscription s) throws SQLException {
        modifier(s);
    }

    @Override
    public List<Subscription> display() throws SQLException {
        return afficherTous();
    }

    public int ajouter(Subscription s) throws SQLException {
        String sql = """
                INSERT INTO subscription (parent_id, kid_id, course_id, subscribed_at, active)
                VALUES (?,?,?,?,?)
                """;
        try (PreparedStatement st = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            st.setInt(1, s.getParentId());
            st.setInt(2, s.getKidId());
            st.setInt(3, s.getCourseId());
            st.setTimestamp(4, Timestamp.valueOf(s.getSubscribedAt()));
            st.setBoolean(5, s.isActive());
            st.executeUpdate();
            try (ResultSet keys = st.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Insert subscription failed: no generated key");
    }

    public void modifier(Subscription s) throws SQLException {
        String sql = """
                UPDATE subscription SET parent_id=?, kid_id=?, course_id=?, subscribed_at=?, active=?
                WHERE id=?
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, s.getParentId());
            ps.setInt(2, s.getKidId());
            ps.setInt(3, s.getCourseId());
            ps.setTimestamp(4, Timestamp.valueOf(s.getSubscribedAt()));
            ps.setBoolean(5, s.isActive());
            ps.setInt(6, s.getId());
            ps.executeUpdate();
        }
    }

    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM subscription WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Optional<Subscription> trouverParId(int id) throws SQLException {
        String sql = "SELECT * FROM subscription WHERE id=?";
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

    public List<Subscription> afficherTous() throws SQLException {
        List<Subscription> list = new ArrayList<>();
        String sql = "SELECT * FROM subscription ORDER BY id";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    private static Subscription mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("subscribed_at");
        LocalDateTime subscribed = ts != null ? ts.toLocalDateTime() : null;
        return new Subscription(
                rs.getInt("id"),
                rs.getInt("parent_id"),
                rs.getInt("kid_id"),
                rs.getInt("course_id"),
                subscribed,
                rs.getBoolean("active")
        );
    }
}
