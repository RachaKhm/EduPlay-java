package dev.eduplay.services;

import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SchoolEventService implements IGeneralService<SchoolEvent> {

    Connection cn;

    public SchoolEventService() {
        cn = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(SchoolEvent event) throws SQLException {
        String sql = "INSERT INTO school_event(title, description, start_date, end_date, location, image_path, created_at, latitude, longitude) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, event.getTitle());
        ps.setString(2, event.getDescription());
        ps.setTimestamp(3, Timestamp.valueOf(event.getStartDate()));
        ps.setTimestamp(4, Timestamp.valueOf(event.getEndDate()));
        ps.setString(5, event.getLocation());
        ps.setString(6, event.getImagePath());
        ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
        ps.setString(8, event.getLatitude());
        ps.setString(9, event.getLongitude());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(SchoolEvent event) throws SQLException {
        String sql = "DELETE FROM school_event WHERE id = ?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setInt(1, event.getId());
        ps.executeUpdate();
    }

    @Override
    public int chercher(SchoolEvent event) throws SQLException {
        String sql = "SELECT 1 FROM school_event WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, event.getId());
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            System.out.println("cet event existe avec l'id " + event.getId());
        } else {
            System.out.println("cet event n'existe pas");
        }
        return event.getId();
    }

    @Override
    public void modifier(SchoolEvent event) throws SQLException {
        if (chercher(event) == event.getId()) {
            String sql = "UPDATE school_event SET title = ?, description = ?, start_date = ?, end_date = ?, location = ?, image_path = ?, latitude = ?, longitude = ? WHERE id = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, event.getTitle());
            pst.setString(2, event.getDescription());
            pst.setTimestamp(3, Timestamp.valueOf(event.getStartDate()));
            pst.setTimestamp(4, Timestamp.valueOf(event.getEndDate()));
            pst.setString(5, event.getLocation());
            pst.setString(6, event.getImagePath());
            pst.setString(7, event.getLatitude());
            pst.setString(8, event.getLongitude());
            pst.setInt(9, event.getId());
            pst.executeUpdate();
        } else {
            System.out.println("cet event n'existe pas");
        }
    }

    @Override
    public List<SchoolEvent> recuperer() throws SQLException {
        String sql = "SELECT * FROM school_event ORDER BY start_date DESC";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<SchoolEvent> events = new ArrayList<>();
        while (rs.next()) {
            SchoolEvent event = new SchoolEvent(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getTimestamp("start_date").toLocalDateTime(),
                    rs.getTimestamp("end_date").toLocalDateTime(),
                    rs.getString("location"),
                    rs.getString("image_path"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getString("latitude"),
                    rs.getString("longitude"));
            events.add(event);
        }
        return events;
    }

    // ==================== MÉTHODE SPÉCIFIQUE (SANS @Override) ====================
    // Cette méthode n'est PAS dans l'interface IGeneralService
    public SchoolEvent recupererParId(int id) throws SQLException {
        String sql = "SELECT * FROM school_event WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            SchoolEvent event = new SchoolEvent();
            event.setId(rs.getInt("id"));
            event.setTitle(rs.getString("title"));
            event.setDescription(rs.getString("description"));
            event.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
            event.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
            event.setLocation(rs.getString("location"));
            event.setImagePath(rs.getString("image_path"));
            event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            event.setLatitude(rs.getString("latitude"));
            event.setLongitude(rs.getString("longitude"));
            return event;
        }
        return null;
    }
}