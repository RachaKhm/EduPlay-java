package dev.eduplay.services;

import dev.eduplay.entities.User;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IGeneralService<User> {

    private Connection cnx;

    public UserService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(User user) {
        String query = "INSERT INTO user (first_name, last_name, email, type, telephone, adresse, active, created_at, password, username, birth_date, specialite, niveau, roles, parent_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getType());
            ps.setString(5, user.getTelephone());
            ps.setString(6, user.getAdresse());
            ps.setBoolean(7, user.isActive());
            ps.setString(8, user.getPassword() != null ? user.getPassword() : "");
            ps.setString(9, user.getUsername());
            ps.setDate(10, user.getBirthDate() != null ? java.sql.Date.valueOf(user.getBirthDate()) : null);
            ps.setString(11, user.getSpecialite());
            ps.setString(12, user.getNiveau());
            ps.setString(13, "[\"ROLE_" + (user.getType() != null ? user.getType().toUpperCase() : "USER") + "\"]");
            if (user.getParentId() > 0) {
                ps.setInt(14, user.getParentId());
            } else {
                ps.setNull(14, java.sql.Types.INTEGER);
            }

            ps.executeUpdate();
            System.out.println(" User added successfully: " + user.getFullName());

        } catch (SQLException e) {
            System.err.println(" Error adding user: " + e.getMessage());
        }
    }

    @Override
    public void modifier(User user) {
        String query = "UPDATE user SET first_name = ?, last_name = ?, email = ?, " +
                "telephone = ?, adresse = ?, active = ?, password = ?, username = ?, birth_date = ?, specialite = ?, niveau = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getTelephone());
            ps.setString(5, user.getAdresse());
            ps.setBoolean(6, user.isActive());
            ps.setString(7, user.getPassword());
            ps.setString(8, user.getUsername());
            ps.setDate(9, user.getBirthDate() != null ? java.sql.Date.valueOf(user.getBirthDate()) : null);
            ps.setString(10, user.getSpecialite());
            ps.setString(11, user.getNiveau());
            ps.setInt(12, user.getId());

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
    public int chercher(User user) throws SQLException {
        return 0;
    }

    @Override
    public List<User> recuperer() {
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
        user.setUsername(rs.getString("username"));
        user.setSpecialite(rs.getString("specialite"));
        user.setNiveau(rs.getString("niveau"));
        user.setParentId(rs.getInt("parent_id"));
        
        java.sql.Date birthDate = rs.getDate("birth_date");
        if (birthDate != null) {
            user.setBirthDate(birthDate.toLocalDate());
        }

        // Gérer les dates nullables
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        return user;
    }

    public User findByLogin(String login) {
        String query = "SELECT * FROM user WHERE email = ? OR username = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, login.trim());
            ps.setString(2, login.trim());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
