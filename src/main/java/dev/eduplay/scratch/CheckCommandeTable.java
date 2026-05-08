package dev.eduplay.scratch;

import dev.eduplay.tools.MyDataBase;
import java.sql.*;

public class CheckCommandeTable {
    public static void main(String[] args) {
        try {
            Connection cnx = MyDataBase.getInstance().getCnx();
            DatabaseMetaData meta = cnx.getMetaData();
            ResultSet rs = meta.getColumns(null, null, "commande", null);
            System.out.println("Columns in table 'commande':");
            while (rs.next()) {
                String name = rs.getString("COLUMN_NAME");
                String type = rs.getString("TYPE_NAME");
                int nullable = rs.getInt("NULLABLE");
                String def = rs.getString("COLUMN_DEF");
                System.out.println("- " + name + " (" + type + "), Nullable: " + (nullable == DatabaseMetaData.columnNullable ? "YES" : "NO") + ", Default: " + def);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
