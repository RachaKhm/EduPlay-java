package dev.eduplay.services;

import dev.eduplay.entities.EventResource;
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
        String sql = "INSERT INTO school_event(title, description, start_date, end_date, location, image_path, created_at, latitude, longitude, max_capacity, current_registrations) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, event.getTitle());
        ps.setString(2, event.getDescription());
        ps.setTimestamp(3, Timestamp.valueOf(event.getStartDate()));
        ps.setTimestamp(4, Timestamp.valueOf(event.getEndDate()));
        ps.setString(5, event.getLocation());
        ps.setString(6, event.getImagePath());
        ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
        ps.setString(8, event.getLatitude());
        ps.setString(9, event.getLongitude());
        ps.setInt(10, event.getMaxCapacity());  // ✅ Ajout de la capacité max
        ps.setInt(11, 0);  // current_registrations initialisé à 0

        System.out.println("Executing insert school_event with max_capacity: " + event.getMaxCapacity());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            event.setId(rs.getInt(1));
        }
    }

    @Override
    public void supprimer(SchoolEvent event) throws SQLException {
        String sqlq = "DELETE FROM school_event WHERE id = ?";
        PreparedStatement ps = cn.prepareStatement(sqlq);
        ps.setInt(1, event.getId());
        ps.executeUpdate();
    }

    public void supprimerAvecRessources(SchoolEvent event) throws SQLException {
        cn.setAutoCommit(false);
        try {
            String sqlResources = "DELETE FROM event_resource WHERE event_id = ?";
            PreparedStatement psResources = cn.prepareStatement(sqlResources);
            psResources.setInt(1, event.getId());
            psResources.executeUpdate();

            String sqlEvent = "DELETE FROM school_event WHERE id = ?";
            PreparedStatement psEvent = cn.prepareStatement(sqlEvent);
            psEvent.setInt(1, event.getId());
            psEvent.executeUpdate();

            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.setAutoCommit(true);
        }
    }

    @Override
    public int chercher(SchoolEvent event) throws SQLException {
        String sql = "SELECT 1 FROM school_event WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, event.getId());
        ResultSet rs = pst.executeQuery();
        return rs.next() ? event.getId() : -1;
    }

    @Override
    public void modifier(SchoolEvent event) throws SQLException {
        String sql = "UPDATE school_event SET title = ?, description = ?, start_date = ?, end_date = ?, location = ?, image_path = ?, latitude = ?, longitude = ?, max_capacity = ? WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setString(1, event.getTitle());
        pst.setString(2, event.getDescription());
        pst.setTimestamp(3, Timestamp.valueOf(event.getStartDate()));
        pst.setTimestamp(4, Timestamp.valueOf(event.getEndDate()));
        pst.setString(5, event.getLocation());
        pst.setString(6, event.getImagePath());
        pst.setString(7, event.getLatitude());
        pst.setString(8, event.getLongitude());
        pst.setInt(9, event.getMaxCapacity());  // ✅ Modification de la capacité
        pst.setInt(10, event.getId());

        System.out.println("Updating event ID: " + event.getId() + " with max_capacity: " + event.getMaxCapacity());
        pst.executeUpdate();
    }

    @Override
    public List<SchoolEvent> recuperer() throws SQLException {
        String sql = "SELECT * FROM school_event ORDER BY start_date DESC";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<SchoolEvent> events = new ArrayList<>();
        while(rs.next()){
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
            event.setMaxCapacity(rs.getInt("max_capacity"));  // ✅ Récupération capacité
            event.setCurrentRegistrations(rs.getInt("current_registrations"));  // ✅ Récupération inscriptions
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
            event.setMaxCapacity(rs.getInt("max_capacity"));  // ✅ Récupération capacité
            event.setCurrentRegistrations(rs.getInt("current_registrations"));  // ✅ Récupération inscriptions
            return event;
        }
        return null;
    }
}