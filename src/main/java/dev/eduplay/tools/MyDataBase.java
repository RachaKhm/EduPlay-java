package dev.eduplay.tools;

import org.flywaydb.core.Flyway;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class MyDataBase {
    String url="jdbc:mysql://localhost:3306/eduplaydb";
    String user ="root";
    String mdp ="";
    private Connection cnx;
    private String lastErrorMessage = null;
    static MyDataBase myDb;
    private MyDataBase(){
        try {
            // ✅ Initialiser Flyway
            Flyway flyway = Flyway.configure()
                    .dataSource(url, user, mdp)
                    .baselineOnMigrate(true)
                    .load();

            System.out.println("🚀 Réparation et exécution des migrations Flyway...");
            flyway.repair(); // ✅ Nettoie les migrations échouées
            flyway.migrate();

        } catch (Exception e) {
            lastErrorMessage = "Flyway error: " + e.getMessage();
            System.err.println("❌ Erreur Flyway (non-bloquante): " + e.getMessage());
        }

        try {
            cnx = DriverManager.getConnection(url,user,mdp);
            System.out.println("cnx etablie !!");
            // Ensure default admin user exists for dev/testing
            try {
                String check = "SELECT id FROM user WHERE email = ? OR username = ? LIMIT 1";
                var ps = cnx.prepareStatement(check);
                ps.setString(1, "admin@gmail.com");
                ps.setString(2, "admin");
                var rs = ps.executeQuery();
                if (!rs.next()) {
                    System.out.println("Admin not found — creating default admin account (admin@eduplay.com / admin123)");
                    String hashed = BCrypt.hashpw("admin123", BCrypt.gensalt());
                    String insert = "INSERT INTO user (first_name, last_name, email, type, telephone, adresse, active, created_at, password, username, birth_date, specialite, niveau, roles, parent_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement ins = cnx.prepareStatement(insert)) {
                        ins.setString(1, "Admin");
                        ins.setString(2, "EduPlay");
                        ins.setString(3, "admin@eduplay.com");
                        ins.setString(4, "admin");
                        ins.setString(5, null);
                        ins.setString(6, null);
                        ins.setBoolean(7, true);
                        ins.setString(8, hashed);
                        ins.setString(9, "admin");
                        ins.setDate(10, null);
                        ins.setString(11, null);
                        ins.setString(12, null);
                        ins.setString(13, "[\"ROLE_ADMIN\"]");
                        ins.setNull(14, java.sql.Types.INTEGER);
                        ins.executeUpdate();
                        System.out.println("Default admin created (admin@eduplay.com).");
                    } catch (SQLException ex) {
                        System.err.println("Failed to insert default admin: " + ex.getMessage());
                    }
                } else {
                    System.out.println("Admin user already exists.");
                }
            } catch (SQLException e) {
                System.err.println("Error checking/creating default admin: " + e.getMessage());
            }
        } catch (SQLException e) {
            lastErrorMessage = "DB connection error: " + e.getMessage();
            System.err.println("❌ Erreur Connexion DB: " + e.getMessage());
        }
    }

    public static MyDataBase getInstance(){
        if(myDb ==null){
            myDb=new MyDataBase();
        }
        return myDb;
    }

    public Connection getCnx() {
        return cnx;
    }

    /** Retourne le message d'erreur capturé lors de la tentative de connexion ou d'exécution Flyway. */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /** Essaye de ré-initialiser la connexion (utile pour UI "réessayer"). */
    public boolean reconnect() {
        try {
            cnx = DriverManager.getConnection(url, user, mdp);
            lastErrorMessage = null;
            return true;
        } catch (SQLException e) {
            lastErrorMessage = "DB reconnect error: " + e.getMessage();
            System.err.println("Reconnect failed: " + e.getMessage());
            return false;
        }
    }
}
