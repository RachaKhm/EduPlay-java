package dev.eduplay.services;

import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.interfaces.IGeneralService;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.sql.SQLSyntaxErrorException;
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
        try {
            // Try with latitude/longitude
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
        } catch (SQLSyntaxErrorException e) {
            // Fallback without latitude/longitude
            String sql = "INSERT INTO school_event(title, description, start_date, end_date, location, image_path, created_at) VALUES(?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = cn.prepareStatement(sql);
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(event.getStartDate()));
            ps.setTimestamp(4, Timestamp.valueOf(event.getEndDate()));
            ps.setString(5, event.getLocation());
            ps.setString(6, event.getImagePath());
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        }
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
            try {
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
            } catch (SQLSyntaxErrorException e) {
                // Fallback without latitude/longitude
                String sql = "UPDATE school_event SET title = ?, description = ?, start_date = ?, end_date = ?, location = ?, image_path = ? WHERE id = ?";
                PreparedStatement pst = cn.prepareStatement(sql);
                pst.setString(1, event.getTitle());
                pst.setString(2, event.getDescription());
                pst.setTimestamp(3, Timestamp.valueOf(event.getStartDate()));
                pst.setTimestamp(4, Timestamp.valueOf(event.getEndDate()));
                pst.setString(5, event.getLocation());
                pst.setString(6, event.getImagePath());
                pst.setInt(7, event.getId());
                pst.executeUpdate();
            }
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
            SchoolEvent event = new SchoolEvent();
            event.setId(rs.getInt("id"));
            event.setTitle(rs.getString("title"));
            event.setDescription(rs.getString("description"));
            event.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
            event.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
            event.setLocation(rs.getString("location"));
            event.setImagePath(rs.getString("image_path"));
            if (rs.getTimestamp("created_at") != null)
                event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            try { event.setLatitude(rs.getString("latitude")); } catch (Exception ignored) {}
            try { event.setLongitude(rs.getString("longitude")); } catch (Exception ignored) {}
            events.add(event);
        }
        return events;
    }

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


    /**
     * Supprime un événement et toutes ses ressources associées
     */
    public void supprimerAvecRessources(SchoolEvent event) throws SQLException {
        cn.setAutoCommit(false);

        try {
            // 1. Supprimer les ressources
            String sqlResources = "DELETE FROM event_resource WHERE event_id = ?";
            PreparedStatement psResources = cn.prepareStatement(sqlResources);
            psResources.setInt(1, event.getId());
            int resourcesDeleted = psResources.executeUpdate();
            System.out.println("📄 " + resourcesDeleted + " ressource(s) supprimée(s)");

            // 2. Supprimer l'événement
            String sqlEvent = "DELETE FROM school_event WHERE id = ?";
            PreparedStatement psEvent = cn.prepareStatement(sqlEvent);
            psEvent.setInt(1, event.getId());
            int eventDeleted = psEvent.executeUpdate();
            System.out.println("🗑️ Événement supprimé: " + event.getTitle());

            cn.commit();

        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.setAutoCommit(true);
        }
    }

}