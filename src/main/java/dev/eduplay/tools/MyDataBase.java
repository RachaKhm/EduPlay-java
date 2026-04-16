package dev.eduplay.tools;

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
            cnx = DriverManager.getConnection(url,user,mdp);
            System.out.println("cnx etablie !!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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
