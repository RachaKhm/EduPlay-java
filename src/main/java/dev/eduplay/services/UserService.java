package dev.eduplay.services;

import dev.eduplay.entities.User;
import dev.eduplay.interfaces.IUser;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IUser<User> {

    private Connection cnx;

    public UserService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(User user) {
        String query = "INSERT INTO user (first_name, last_name, email, type, telephone, adresse, active, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getType());
            ps.setString(5, user.getTelephone());
            ps.setString(6, user.getAdresse());
            ps.setBoolean(7, user.isActive());

            ps.executeUpdate();
            System.out.println(" User added successfully: " + user.getFullName());

        } catch (SQLException e) {
            System.err.println(" Error adding user: " + e.getMessage());
        }
    }

    @Override
    public void modifier(User user) {
        String query = "UPDATE user SET first_name = ?, last_name = ?, email = ?, " +
                "telephone = ?, adresse = ?, active = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getTelephone());
            ps.setString(5, user.getAdresse());
            ps.setBoolean(6, user.isActive());
            ps.setInt(7, user.getId());

            ps.executeUpdate();
            System.out.println(" User updated successfully: " + user.getFullName());

        } catch (SQLException e) {
            System.err.println(" Error updating user: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(User user) {
        String query = "DELETE FROM user WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, user.getId());
            ps.executeUpdate();
            System.out.println(" User deleted successfully: " + user.getFullName());

        } catch (SQLException e) {
            System.err.println(" Error deleting user: " + e.getMessage());
        }
    }

    @Override
    public List<User> getAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user ORDER BY created_at DESC";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

            System.out.println(" Retrieved " + users.size() + " users");

        } catch (SQLException e) {
            System.err.println(" Error retrieving users: " + e.getMessage());
        }

        return users;
    }

    // Méthodes supplémentaires utiles
    public User getById(int id) {
        String query = "SELECT * FROM user WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            System.err.println(" Error retrieving user by ID: " + e.getMessage());
        }

        return null;
    }

    public List<User> getByType(String type) {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE type = ? ORDER BY created_at DESC";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, type);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }

        } catch (SQLException e) {
            System.err.println(" Error retrieving users by type: " + e.getMessage());
        }

        return users;
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setType(rs.getString("type"));
        user.setTelephone(rs.getString("telephone"));
        user.setAdresse(rs.getString("adresse"));
        user.setActive(rs.getBoolean("active"));

        if (hasColumn(rs, "parent_id")) {
            int pid = rs.getInt("parent_id");
            if (!rs.wasNull()) {
                user.setParentId(pid);
            }
        }

        // Handle invalid MySQL zero dates (0000-00-00...) without failing row mapping.
        String createdAtRaw = rs.getString("created_at");
        if (createdAtRaw != null
                && !createdAtRaw.isBlank()
                && !createdAtRaw.startsWith("0000-00-00")) {
            Timestamp createdAt = rs.getTimestamp("created_at");
            if (createdAt != null) {
                user.setCreatedAt(createdAt.toLocalDateTime());
            }
        }

        return user;
    }

    private static boolean hasColumn(ResultSet rs, String columnLabel) throws SQLException {
        var md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            if (columnLabel.equalsIgnoreCase(md.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Enfants liés au parent via {@code user.parent_id}.
     * Ne filtre plus sur {@code type = 'enfant'} strictement : en base le type peut varier
     * (casse, espaces, libellé différent). On exclut seulement les rôles adultes connus.
     */
    public List<User> getChildrenByParentId(int parentId) {
        List<User> users = new ArrayList<>();
        String query = """
                SELECT * FROM `user`
                WHERE parent_id = ?
                  AND LOWER(TRIM(COALESCE(type, ''))) NOT IN ('admin', 'enseignant', 'parent')
                ORDER BY first_name, last_name
                """;
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, parentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println(" Error retrieving children for parent " + parentId + ": " + e.getMessage());
        }
        return users;
    }
}
