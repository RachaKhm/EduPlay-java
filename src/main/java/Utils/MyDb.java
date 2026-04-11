package Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDb {

    public static final String DEFAULT_URL =
            "jdbc:mysql://127.0.0.1:3306/eduplaydb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
                    + "&zeroDateTimeBehavior=CONVERT_TO_NULL";

    public String PATH = DEFAULT_URL;
    public String user = "root";
    public String pwd = "";
    public Connection conn;
    public static MyDb instance;

    private MyDb() throws SQLException {
        conn = DriverManager.getConnection(PATH, user, pwd);
        System.out.println("cnx etabli !!!!!!");
    }

    public static MyDb getInstance() throws SQLException {
        if (instance == null) {
            instance = new MyDb();
        }
        return instance;
    }

    public Connection getConn() {
        return conn;
    }
}
