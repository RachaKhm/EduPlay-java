package dev.eduplay.services;

import dev.eduplay.tools.MyDataBase;
import entities.Level;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class LevelService {

    private Connection cnx;

    public LevelService() throws SQLException {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public void add(Level level) throws SQLException {
        String sql ="insert into level (name, description, difficulty, min_age, max_age, pedag_goal, created_at, updated_at) values (?, ?, ?, ?, ?, ?, NOW(), NOW())";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, level.getName());
        ps.setString(2, level.getDescription());
        ps.setInt(3, level.getDifficulty());
        ps.setInt(4, level.getMinAge());
        ps.setInt(5, level.getMaxAge());
        ps.setString(6,level.getPedagGoal());

        ps.executeUpdate();
        System.out.println("ajout avec succées");

    }
    public void delete(int id) throws SQLException {
        String sql ="delete from level where id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("suppression avec succées");


    }
    public void update(Level level) throws SQLException {
        String sql ="update level set name=?, description=?, difficulty=?, min_age=?, max_age=?, pedag_goal=?,updated_at=NOW() where id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, level.getName());
        ps.setString(2, level.getDescription());
        ps.setInt(3, level.getDifficulty());
        ps.setInt(4, level.getMinAge());
        ps.setInt(5, level.getMaxAge());
        ps.setString(6,level.getPedagGoal());
        ps.setInt(7, level.getId());
        ps.executeUpdate();
        System.out.println("update avec succées");
    }
public List<Level> getAll(){
            List<Level> levels= new ArrayList<Level>();

            String sql="select * from level";
    Statement st = null;
    try {
        st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next())
        {
            Level l = new Level(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getInt("difficulty"),
                    rs.getInt("min_age"),
                    rs.getInt("max_age"),
                    rs.getString("pedag_goal"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getTimestamp("updated_at").toLocalDateTime()
            );
            levels.add(l);

        }
    } catch (SQLException e) {
        throw new RuntimeException(e);
    }
return levels;
}

}
