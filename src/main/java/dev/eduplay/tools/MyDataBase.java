package dev.eduplay.tools;

import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    String url="jdbc:mysql://localhost:3306/eduplaydb";
    String user ="root";
    String mdp ="";
    private Connection cnx;
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
            System.err.println("❌ Erreur Flyway (non-bloquante): " + e.getMessage());
        }

        try {
            cnx = DriverManager.getConnection(url,user,mdp);
            System.out.println("cnx etablie !!");
        } catch (SQLException e) {
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
}
