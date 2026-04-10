package dev.eduplay.services;

import dev.eduplay.entities.Library;
import dev.eduplay.interfaces.ICrud;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibraryService implements ICrud<Library> {

    Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public void ajouter(Library l) {
        String sql = "INSERT INTO library (name, description, cover_image, min_age, max_age, level, theme) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, l.getName());
            ps.setString(2, l.getDescription());
            ps.setString(3, l.getCoverImage());
            ps.setInt(4, l.getMinAge());
            ps.setInt(5, l.getMaxAge());
            ps.setString(6, l.getLevel());
            ps.setString(7, l.getTheme());
            ps.executeUpdate();
            System.out.println("Livre ajouté !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Library l) {
        String sql = "UPDATE library SET name=?, description=?, cover_image=?, min_age=?, max_age=?, level=?, theme=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, l.getName());
            ps.setString(2, l.getDescription());
            ps.setString(3, l.getCoverImage());
            ps.setInt(4, l.getMinAge());
            ps.setInt(5, l.getMaxAge());
            ps.setString(6, l.getLevel());
            ps.setString(7, l.getTheme());
            ps.setInt(8, l.getId());
            ps.executeUpdate();
            System.out.println("Modifié !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM library WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Supprimé !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<Library> afficher() {
        List<Library> list = new ArrayList<>();
        String sql = "SELECT * FROM library";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Library l = new Library(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("cover_image"),
                        rs.getInt("min_age"),
                        rs.getInt("max_age"),
                        rs.getString("level"),
                        rs.getString("theme")
                );
                list.add(l);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }
}