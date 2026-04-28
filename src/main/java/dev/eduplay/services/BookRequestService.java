package dev.eduplay.services;

import dev.eduplay.entities.BookRequest;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookRequestService {

    Connection cnx = MyDataBase.getInstance().getCnx();

    public void ajouter(BookRequest br) {
        String sql = "INSERT INTO book_request (book_title, enfant_id, requested_at, is_available, is_notified, resource_id) VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, br.getBookTitle());
            ps.setInt(2, br.getEnfantId());
            ps.setTimestamp(3, Timestamp.valueOf(br.getRequestedAt()));
            ps.setBoolean(4, br.isAvailable());
            ps.setBoolean(5, br.isNotified());
            if (br.getResourceId() != null) {
                ps.setInt(6, br.getResourceId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }

    public List<BookRequest> afficherTous() {
        List<BookRequest> list = new ArrayList<>();
        String sql = "SELECT br.*, u.first_name, u.last_name, u.type " +
                     "FROM book_request br " +
                     "JOIN user u ON br.enfant_id = u.id " +
                     "ORDER BY br.requested_at DESC";
        try {
            ResultSet rs = cnx.createStatement().executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return list;
    }

    public List<BookRequest> afficherEnAttente() {
        List<BookRequest> list = new ArrayList<>();
        String sql = "SELECT br.*, u.first_name, u.last_name, u.type " +
                     "FROM book_request br " +
                     "JOIN user u ON br.enfant_id = u.id " +
                     "WHERE br.is_available = 0 " +
                     "ORDER BY br.requested_at DESC";
        try {
            ResultSet rs = cnx.createStatement().executeQuery(sql);
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return list;
    }

    public void marquerDisponible(int id, int resourceId) {
        String sql = "UPDATE book_request SET is_available=1, resource_id=? WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, resourceId);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }

    public List<BookRequest> findPendingByTitle(String title) {
        List<BookRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM book_request WHERE is_available = 0 AND LOWER(book_title) = LOWER(?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return list;
    }

    public void marquerDisponibleParTitre(String title, int resourceId) {
        String sql = "UPDATE book_request SET is_available=1, resource_id=? WHERE LOWER(book_title) = LOWER(?) AND is_available=0";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, resourceId);
            ps.setString(2, title);
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }

    public List<BookRequest> getNotificationsNonLues(int enfantId) {
        List<BookRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM book_request WHERE enfant_id = ? AND is_available = 1 AND is_notified = 0";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, enfantId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { System.out.println(e.getMessage()); }
        return list;
    }

    public void marquerCommeNotifie(int id) {
        String sql = "UPDATE book_request SET is_notified=1 WHERE id=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) { System.out.println(e.getMessage()); }
    }

    private BookRequest mapRow(ResultSet rs) throws SQLException {
        BookRequest br = new BookRequest();
        br.setId(rs.getInt("id"));
        br.setBookTitle(rs.getString("book_title"));
        br.setEnfantId(rs.getInt("enfant_id"));
        br.setRequestedAt(rs.getTimestamp("requested_at").toLocalDateTime());
        br.setAvailable(rs.getBoolean("is_available"));
        br.setNotified(rs.getBoolean("is_notified"));
        int resId = rs.getInt("resource_id");
        if (!rs.wasNull()) {
            br.setResourceId(resId);
        }
        
        try {
            String fname = rs.getString("first_name");
            String lname = rs.getString("last_name");
            if (fname != null && lname != null) {
                br.setEnfantName(fname + " " + lname);
            }
            br.setEnfantRole(rs.getString("type"));
        } catch (SQLException ignored) {}
        
        return br;
    }
}
