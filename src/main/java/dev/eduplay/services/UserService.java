package dev.eduplay.services;

import dev.eduplay.entities.User;
import dev.eduplay.interfaces.IGeneralService;
import dev.eduplay.tools.MyDataBase;
import dev.eduplay.utils.PasswordUtils;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;
import java.time.LocalDateTime;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements IGeneralService<User> {

    private Connection cnx;

    public UserService() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    /**
     * Vérifie rapidement si la connexion à la base de données est initialisée.
     * Utilisé par les contrôleurs pour afficher un message lisible si la DB n'est pas disponible.
     */
    public boolean isConnected() {
        return cnx != null;
    }

    private void ensureConnected() {
        if (cnx == null) {
            String msg = "Base de données non connectée. Vérifiez MyDataBase (url/user/mdp) et que MySQL tourne.";
            throw new IllegalStateException(msg);
        }
    }

    @Override
    public void ajouter(User user) {
        ensureConnected();
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
            // parentId is Integer (nullable). Guard against null to avoid NPE on unboxing.
            if (user.getParentId() != null && user.getParentId() > 0) {
                ps.setInt(14, user.getParentId());
            } else {
                ps.setNull(14, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();
            System.out.println("User added successfully: " + user.getFullName());
        } catch (SQLException e) {
            // Rethrow as runtime so controllers can handle and show a helpful message
            throw new RuntimeException("Erreur base de données lors de l'ajout de l'utilisateur: " + e.getMessage(), e);
        }
    }

    @Override
    public void modifier(User user) {
        String query = "UPDATE user SET first_name = ?, last_name = ?, email = ?, " +
                "telephone = ?, adresse = ?, active = ?, password = ?, username = ?, " +
                "birth_date = ?, specialite = ?, niveau = ? WHERE id = ?";

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
            System.out.println("User updated successfully: " + user.getFullName());
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
    }

    @Override
    public void supprimer(User user) {
        String query = "DELETE FROM user WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, user.getId());
            ps.executeUpdate();
            System.out.println("User deleted successfully: " + user.getFullName());
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
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
            System.out.println("Retrieved " + users.size() + " users");
        } catch (SQLException e) {
            System.err.println("Error retrieving users: " + e.getMessage());
        }
        return users;
    }

    public User getById(int id) {
        String query = "SELECT * FROM user WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractUserFromResultSet(rs);
        } catch (SQLException e) {
            System.err.println("Error retrieving user by ID: " + e.getMessage());
        }
        return null;
    }

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
            System.err.println("Error retrieving users by type: " + e.getMessage());
        }
        return users;
    }

    public User findByLogin(String login) {
        ensureConnected();
        String query = "SELECT * FROM user WHERE email = ? OR username = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, login.trim());
            ps.setString(2, login.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractUserFromResultSet(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur base de données lors de la recherche de connexion: " + e.getMessage(), e);
        }
        return null;
    }

    public User findByEmail(String email) {
        ensureConnected();
        String query = "SELECT * FROM user WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, email.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractUserFromResultSet(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur base de données lors de la recherche d'email: " + e.getMessage(), e);
        }
        return null;
    }
    public User authenticate(String identifier, String password) {
        User user = findByLogin(identifier);
        if (user == null) return null;
        if (!PasswordUtils.checkPassword(password, user.getPassword())) return null;
        return user;
    }

    // ─── RESET MOT DE PASSE ───────────────────────────────────────────────────

    public boolean generateResetToken(String email) {
        String sql = "SELECT id FROM user WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false; // email inexistant

            String token = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(30);

            String update = "UPDATE user SET reset_token = ?, reset_token_expiry = ? WHERE email = ?";
            try (PreparedStatement ups = cnx.prepareStatement(update)) {
                ups.setString(1, token);
                ups.setObject(2, expiry);
                ups.setString(3, email);
                ups.executeUpdate();
            }

            // Essayer d'envoyer l'email
            try {
                EmailService emailService = new EmailService();
                emailService.sendPasswordResetEmail(email, token);
                System.out.println("[Reset] Email envoyé à " + email);
            } catch (Exception e) {
                // Email non configuré → fallback : stocker le token en mémoire
                // pour que le ResetPasswordController puisse le pré-remplir
                System.err.println("[Reset] Email non envoyé, fallback TokenHolder : " + e.getMessage());
                dev.eduplay.core.TokenHolder.set(token);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean resetPassword(String token, String newPassword) {
        String sql = "SELECT id, reset_token_expiry FROM user WHERE reset_token = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, token);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;

            LocalDateTime expiry = rs.getObject("reset_token_expiry", LocalDateTime.class);
            if (expiry == null || LocalDateTime.now().isAfter(expiry)) return false;

            String hashed = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            String update = "UPDATE user SET password = ?, reset_token = NULL, reset_token_expiry = NULL WHERE reset_token = ?";
            try (PreparedStatement ups = cnx.prepareStatement(update)) {
                ups.setString(1, hashed);
                ups.setString(2, token);
                ups.executeUpdate();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── BRUTE FORCE PROTECTION ───────────────────────────────────────────────

    public boolean isAccountLocked(String identifier) {
        String sql = "SELECT locked_until FROM user WHERE email = ? OR username = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                LocalDateTime lockedUntil = rs.getObject("locked_until", LocalDateTime.class);
                if (lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil)) return true;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public void recordFailedAttempt(String identifier) {
        String sql = """
            UPDATE user SET login_attempts = login_attempts + 1,
            locked_until = CASE WHEN login_attempts + 1 >= 5
                THEN DATE_ADD(NOW(), INTERVAL 15 MINUTE) ELSE locked_until END
            WHERE email = ? OR username = ?
        """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, identifier);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void resetFailedAttempts(int userId) {
        String sql = "UPDATE user SET login_attempts = 0, locked_until = NULL WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── SESSION TOKEN ────────────────────────────────────────────────────────

    public String createSession(int userId) {
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusHours(8);
        String sql = "UPDATE user SET session_token = ?, session_expiry = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setObject(2, expiry);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
        return token;
    }

    public void invalidateSession(int userId) {
        String sql = "UPDATE user SET session_token = NULL, session_expiry = NULL WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── 2FA OTP ──────────────────────────────────────────────────────────────

    public boolean sendOtp(User user) {
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);
        String sql = "UPDATE user SET otp_code = ?, otp_expiry = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, otp);
            ps.setObject(2, expiry);
            ps.setInt(3, user.getId());
            ps.executeUpdate();
            EmailService emailService = new EmailService();
            emailService.sendOtpEmail(user.getEmail(), otp);
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean verifyOtp(int userId, String inputOtp) {
        String sql = "SELECT otp_code, otp_expiry FROM user WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;

            String storedOtp = rs.getString("otp_code");
            LocalDateTime expiry = rs.getObject("otp_expiry", LocalDateTime.class);

            if (storedOtp == null || !storedOtp.equals(inputOtp)) return false;
            if (expiry == null || LocalDateTime.now().isAfter(expiry)) return false;

            String clear = "UPDATE user SET otp_code = NULL, otp_expiry = NULL WHERE id = ?";
            try (PreparedStatement ps2 = cnx.prepareStatement(clear)) {
                ps2.setInt(1, userId);
                ps2.executeUpdate();
            }
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ─── PHOTO DE PROFIL (CLOUDINARY) ─────────────────────────────────────────

    public boolean updateProfilePicture(int userId, String imageUrl) {
        String sql = "UPDATE user SET profile_picture = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, imageUrl);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public String getProfilePicture(int userId) {
        String sql = "SELECT profile_picture FROM user WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("profile_picture");
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ─── EXTRACTION RESULT SET ────────────────────────────────────────────────

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

        // Lire profile_picture si la colonne existe
        try {
            user.setProfilePicture(rs.getString("profile_picture"));
        } catch (SQLException ignored) {}

        java.sql.Date birthDate = rs.getDate("birth_date");
        if (birthDate != null) user.setBirthDate(birthDate.toLocalDate());

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) user.setCreatedAt(createdAt.toLocalDateTime());

        return user;
    }
    // Récupérer l'embedding par ID (utilisé dans FaceLoginController)
    public String getFacialEmbedding(int userId) {
        String sql = "SELECT facial_embedding FROM user WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("facial_embedding");
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // Sauvegarder l'embedding lors de l'enrollment
    public boolean saveFacialEmbedding(int userId, String embeddingJson) {
        String sql = "UPDATE user SET facial_embedding = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, embeddingJson);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}