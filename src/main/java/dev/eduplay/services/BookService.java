package dev.eduplay.services;

import dev.eduplay.entities.Book;
import dev.eduplay.interfaces.ICrud;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookService implements ICrud<Book> {

    Connection cnx = MyDataBase.getInstance().getCnx();

    @Override
    public void ajouter(Book b) {
        String sql = "INSERT INTO resource (library_id_id, title, author, summary, cover_image, pdf_file, type, min_age, max_age, language) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
            System.out.println("Ajouté !");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(Book b) {
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

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Modifié !");
            } else {
                System.out.println("⚠️ ID introuvable !");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(int id) {
        String sql = "DELETE FROM resource WHERE id=?";

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
    public List<Book> afficher() {
        List<Book> list = new ArrayList<>();
        String sql = "SELECT * FROM resource";

        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Book b = new Book(
                        rs.getInt("id"),
                        rs.getInt("library_id_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("summary"),
                        rs.getString("cover_image"),
                        rs.getString("pdf_file"),
                        rs.getString("type"),
                        rs.getInt("min_age"),
                        rs.getInt("max_age"),
                        rs.getString("language")
                );
                list.add(b);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return list;
    }
}