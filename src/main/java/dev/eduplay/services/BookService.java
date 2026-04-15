package dev.eduplay.services;

import dev.eduplay.entities.Book;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookService {

    Connection cnx = MyDataBase.getInstance().getCnx();

    public void ajouter(Book b) {
        String sql = "INSERT INTO book (library_id, title, author, summary, cover_image, pdf_file, type, min_age, max_age, language) VALUES (?,?,?,?,?,?,?,?,?,?)";
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
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }

    public void modifier(Book b) {
        String sql = "UPDATE book SET library_id=?, title=?, author=?, summary=?, cover_image=?, pdf_file=?, type=?, min_age=?, max_age=?, language=? WHERE id=?";
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
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }

    public void supprimer(int id) {
        try {
            PreparedStatement ps = cnx.prepareStatement("DELETE FROM book WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }

    public List<Book> afficher() {
        List<Book> list = new ArrayList<>();
        try {
            ResultSet rs = cnx.createStatement().executeQuery("SELECT * FROM book");
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return list;
    }

    public List<Book> afficherParLibrairie(int libraryId) {
        List<Book> list = new ArrayList<>();
        try {
            PreparedStatement ps = cnx.prepareStatement("SELECT * FROM book WHERE library_id=?");
            ps.setInt(1, libraryId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return list;
    }

    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
                rs.getInt("id"), rs.getInt("library_id"),
                rs.getString("title"), rs.getString("author"),
                rs.getString("summary"), rs.getString("cover_image"),
                rs.getString("pdf_file"), rs.getString("type"),
                rs.getInt("min_age"), rs.getInt("max_age"),
                rs.getString("language")
        );
    }
}
