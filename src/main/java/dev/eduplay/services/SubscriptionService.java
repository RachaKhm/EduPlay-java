package dev.eduplay.services;


import dev.eduplay.entities.Subscription;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SubscriptionService {
    private final Connection cnx;

    public SubscriptionService() throws SQLException {
        cnx = Objects.requireNonNull(
                MyDataBase.getInstance().getCnx(),
                "No JDBC connection — MyDataBase failed to open (see earlier SQLException).");
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

    public List<Subscription> listParParentEtCours(int parentId, int courseId) throws SQLException {
        List<Subscription> list = new ArrayList<>();
        String sql = "SELECT * FROM subscription WHERE parent_id = ? AND course_id = ? ORDER BY id";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Optional<Subscription> trouverParParentEnfantEtCours(int parentId, int kidId, int courseId) throws SQLException {
        String sql = "SELECT * FROM subscription WHERE parent_id = ? AND kid_id = ? AND course_id = ? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, parentId);
            ps.setInt(2, kidId);
            ps.setInt(3, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    /** Abonnement actif enfant + cours (pour vérifier l'accès à la fiche cours). */
    public boolean estAbonneActif(int kidId, int courseId) throws SQLException {
        String sql = """
                SELECT 1 FROM subscription
                WHERE kid_id = ? AND course_id = ? AND IFNULL(active, 0) = 1
                LIMIT 1
                """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, kidId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
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
