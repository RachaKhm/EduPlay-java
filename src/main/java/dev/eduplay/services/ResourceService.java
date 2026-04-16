package dev.eduplay.services;

import dev.eduplay.entities.Resource;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResourceService {

    Connection cnx = MyDataBase.getInstance().getCnx();

    public void ajouter(Resource b) {
        String sql = "INSERT INTO resource (library_id_id, title, author, summary, cover_image, pdf_file, type, min_age, max_age, language) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, b.getLibraryId());
            ps.setString(2, b.getTitle());
            ps.setString(3, b.getAuthor());
            ps.setString(4, b.getSummary());
            ps.setString(5, b.getCoverImage());
            ps.setString(6, b.getPdfFile());
            ps.setString(7, b.getType());
            ps.setInt(8, b.getMinAge());
            ps.setInt(9, b.getMaxAge());
            ps.setString(10, b.getLanguage());
            ps.executeUpdate();
        } catch (SQLException e) { 
            System.out.println(e.getMessage()); 
            throw new RuntimeException("Erreur d'insertion en BDD : " + e.getMessage()); 
        }
    }

    public void modifier(Resource b) {
        String sql = "UPDATE resource SET library_id_id=?, title=?, author=?, summary=?, cover_image=?, pdf_file=?, type=?, min_age=?, max_age=?, language=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, b.getLibraryId());
            ps.setString(2, b.getTitle());
            ps.setString(3, b.getAuthor());
            ps.setString(4, b.getSummary());
            ps.setString(5, b.getCoverImage());
            ps.setString(6, b.getPdfFile());
            ps.setString(7, b.getType());
            ps.setInt(8, b.getMinAge());
            ps.setInt(9, b.getMaxAge());
            ps.setString(10, b.getLanguage());
            ps.setInt(11, b.getId());
            ps.executeUpdate();
        } catch (SQLException e) { 
            System.out.println(e.getMessage()); 
            throw new RuntimeException("Erreur de modification en BDD : " + e.getMessage()); 
        }
    }

    public void supprimer(int id) {
        try {
            PreparedStatement ps = cnx.prepareStatement("DELETE FROM resource WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }

    public List<Resource> afficher() {
        List<Resource> list = new ArrayList<>();
        try {
            ResultSet rs = cnx.createStatement().executeQuery("SELECT * FROM resource");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return list;
    }

    public List<Resource> afficherParLibrairie(int libraryId) {
        List<Resource> list = new ArrayList<>();
        try {
            PreparedStatement ps = cnx.prepareStatement("SELECT * FROM resource WHERE library_id_id=?");
            ps.setInt(1, libraryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return list;
    }

    public boolean existsByTitle(String title) {
        boolean exists = false;
        try {
            PreparedStatement ps = cnx.prepareStatement("SELECT COUNT(*) FROM resource WHERE title=?");
            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                exists = rs.getInt(1) > 0;
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return exists;
    }

    private Resource mapRow(ResultSet rs) throws SQLException {
        return new Resource(
                rs.getInt("id"), rs.getInt("library_id_id"),
                rs.getString("title"), rs.getString("author"),
                rs.getString("summary"), rs.getString("cover_image"),
                rs.getString("pdf_file"), rs.getString("type"),
                rs.getInt("min_age"), rs.getInt("max_age"),
                rs.getString("language")
        );
    }
}
