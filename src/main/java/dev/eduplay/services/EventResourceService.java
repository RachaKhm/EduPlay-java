package dev.eduplay.services;

import dev.eduplay.entities.EventResource;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventResourceService implements IGeneralService<EventResource>  {
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

        System.out.println("Executing insert event_resource...");
        ps.executeUpdate();
    }

    @Override
    public void supprimer(EventResource resource) throws SQLException {
        if(chercher(resource) ==resource.getId()){
            String sql = "DELETE FROM event_resource WHERE id = ?";
            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setInt(1, resource.getId());
            pst.executeUpdate();
        }
        else{
            System.out.println("resource n'esxiste pas");
        }
    }

    @Override
    public int chercher(EventResource resource) throws SQLException {
        String sql = "SELECT 1 FROM event_resource WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, resource.getId());
        ResultSet rs = pst.executeQuery();
        if (rs.next()){
            System.out.println("cette ressource existe avec l'id "+resource.getId());
        }else{
            System.out.println("cette ressource n'existe pas");
        }
        return resource.getId();    }

    @Override
    public void modifier(EventResource resource) throws SQLException {
        if(chercher(resource)== resource.getId()){
            String sql = "UPDATE event_resource SET type = ?, title = ?, context = ?, file_path = ?, url = ? WHERE id = ?";            PreparedStatement pst = cn.prepareStatement(sql);
            pst.setString(1, resource.getType());
            pst.setString(2, resource.getTitle());
            pst.setString(3, resource.getContext());
            pst.setString(4, resource.getFilePath());
            pst.setString(5, resource.getUrl());
            pst.setInt(6, resource.getId());
            pst.executeUpdate();
        }
        else {
            System.out.println("cette resssouce n'existe pas");
        }
    }

    @Override
    public List<EventResource> recuperer() throws SQLException {
        String sql = "select * from event_resource";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<EventResource> ressources = new ArrayList<>();
        while(rs.next()){
            EventResource er = new EventResource(
                    rs.getInt("id"),
                    rs.getString("type"),
                    rs.getString("title"),
                    rs.getString("context"),
                    rs.getString("file_path"),
                    rs.getString("url"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                null
            );
            ressources.add(er);
        }

        return ressources;    }
}
