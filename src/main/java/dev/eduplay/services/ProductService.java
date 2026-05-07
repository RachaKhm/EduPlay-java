package dev.eduplay.services;

import dev.eduplay.entities.Product;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService {

    Connection cnx = MyDataBase.getInstance().getCnx();

    public int ajouter(Product p) {
        String sql = "INSERT INTO product (name, price, description, availability, picture) VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getName());
            ps.setDouble(2, p.getPrice());
            ps.setString(3, p.getDescription());
            ps.setBoolean(4, p.isAvailability());
            ps.setString(5, p.getPicture());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
            return 0;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Erreur d'insertion produit : " + e.getMessage());
        }
    }

    public void modifier(Product p) {
        String sql = "UPDATE product SET name=?, price=?, description=?, availability=?, picture=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, p.getName());
            ps.setDouble(2, p.getPrice());
            ps.setString(3, p.getDescription());
            ps.setBoolean(4, p.isAvailability());
            ps.setString(5, p.getPicture());
            ps.setInt(6, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException("Erreur modification produit : " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        try {
            PreparedStatement ps = cnx.prepareStatement("DELETE FROM product WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }

    public List<Product> afficher() {
        List<Product> list = new ArrayList<>();
        try {
            ResultSet rs = cnx.createStatement().executeQuery("SELECT * FROM product");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return list;
    }

    public boolean existsByName(String name) {
        boolean exists = false;
        try {
            PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM product WHERE name=?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) exists = rs.getInt(1) > 0;
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return exists;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getString("description"),
                rs.getBoolean("availability"),
                rs.getString("picture")
        );
    }
}

