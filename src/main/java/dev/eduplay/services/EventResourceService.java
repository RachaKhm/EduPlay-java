package dev.eduplay.services;

import dev.eduplay.entities.EventResource;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventResourceService implements IGeneralService<EventResource> {

    Connection cn;

    public EventResourceService() {
        cn = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(EventResource resource) throws SQLException {
        String sql = "INSERT INTO event_resource(type, title, context, file_path, url, created_at, event_id) VALUES(?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, resource.getType());
        ps.setString(2, resource.getTitle());
        ps.setString(3, resource.getContext());
        ps.setString(4, resource.getFilePath());
        ps.setString(5, resource.getUrl());
        ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(7, resource.getEvent().getId());
        ps.executeUpdate();
    }

    // ✅ MÉTHODE AJOUTER AVEC eventId DIRECTEMENT (pour AddResourceController)
    public void ajouter(EventResource resource, int eventId) throws SQLException {
        String sql = "INSERT INTO event_resource(type, title, context, file_path, url, created_at, event_id) VALUES(?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, resource.getType());
        ps.setString(2, resource.getTitle());
        ps.setString(3, resource.getContext());
        ps.setString(4, resource.getFilePath());
        ps.setString(5, resource.getUrl());
        ps.setTimestamp(6, Timestamp.valueOf(resource.getCreatedAt()));
        ps.setInt(7, eventId);
        ps.executeUpdate();
    }

    @Override
    public void supprimer(EventResource resource) throws SQLException {
        String sql = "DELETE FROM event_resource WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, resource.getId());
        pst.executeUpdate();
    }

    @Override
    public int chercher(EventResource resource) throws SQLException {
        String sql = "SELECT 1 FROM event_resource WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, resource.getId());
        ResultSet rs = pst.executeQuery();
        return rs.next() ? resource.getId() : -1;
    }

    @Override
    public void modifier(EventResource resource) throws SQLException {
        String sql = "UPDATE event_resource SET type = ?, title = ?, context = ?, file_path = ?, url = ? WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setString(1, resource.getType());
        pst.setString(2, resource.getTitle());
        pst.setString(3, resource.getContext());
        pst.setString(4, resource.getFilePath());
        pst.setString(5, resource.getUrl());
        pst.setInt(6, resource.getId());
        pst.executeUpdate();
    }

    @Override
    public List<EventResource> recuperer() throws SQLException {
        String sql = "SELECT * FROM event_resource ORDER BY created_at DESC";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<EventResource> resources = new ArrayList<>();
        while (rs.next()) {
            EventResource er = new EventResource();
            er.setId(rs.getInt("id"));
            er.setType(rs.getString("type"));
            er.setTitle(rs.getString("title"));
            er.setContext(rs.getString("context"));
            er.setFilePath(rs.getString("file_path"));
            er.setUrl(rs.getString("url"));
            er.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

            // Récupérer l'événement associé
            SchoolEvent event = new SchoolEvent();
            event.setId(rs.getInt("event_id"));
            er.setEvent(event);

            resources.add(er);
        }
        return resources;
    }

    // ✅ Récupérer les ressources par ID d'événement
    public List<EventResource> recupererParEventId(int eventId) throws SQLException {
        String sql = "SELECT * FROM event_resource WHERE event_id = ? ORDER BY created_at DESC";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, eventId);
        ResultSet rs = pst.executeQuery();
        List<EventResource> resources = new ArrayList<>();
        while (rs.next()) {
            EventResource er = new EventResource();
            er.setId(rs.getInt("id"));
            er.setType(rs.getString("type"));
            er.setTitle(rs.getString("title"));
            er.setContext(rs.getString("context"));
            er.setFilePath(rs.getString("file_path"));
            er.setUrl(rs.getString("url"));
            er.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

            // Créer l'événement associé avec son ID
            SchoolEvent event = new SchoolEvent();
            event.setId(rs.getInt("event_id"));
            er.setEvent(event);

            resources.add(er);
        }
        return resources;
    }

    // ✅ Récupérer une ressource par son ID
    public EventResource recupererParId(int id) throws SQLException {
        String sql = "SELECT * FROM event_resource WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            EventResource er = new EventResource();
            er.setId(rs.getInt("id"));
            er.setType(rs.getString("type"));
            er.setTitle(rs.getString("title"));
            er.setContext(rs.getString("context"));
            er.setFilePath(rs.getString("file_path"));
            er.setUrl(rs.getString("url"));
            er.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

            SchoolEvent event = new SchoolEvent();
            event.setId(rs.getInt("event_id"));
            er.setEvent(event);

            return er;
        }
        return null;
    }
}