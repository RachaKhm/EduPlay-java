package services;
import java.sql.*;
import java.util.*;

import entities.Game;
import tools.MyDatabase;

public class GameService {
    private Connection cnx;


    public GameService() throws SQLException {
        cnx=MyDatabase.getInstance().getConnection();
    }

    public void add(Game game)  {
        String sql= "insert into game (id_level_id,name,type,description,image) values (?,?,?,?,?)";
        PreparedStatement ps = null;
        try {
            ps = cnx.prepareStatement(sql);
            ps.setInt(1,game.getId_level());
            ps.setString(2,game.getName());
            ps.setString(3,game.getType());
            ps.setString(4,game.getDescription());
            ps.setString(5,game.getImage());
            ps.executeUpdate();
            IO.println("ajout avec succées");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void delete(int id) throws SQLException {
        String sql= "delete from game where id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1,id);
        ps.executeUpdate();
        IO.println("suppression avec succées");
    }

    public void update(Game game) throws SQLException {
        String sql ="update game set name=?,type=?,description=?,image=? ,id_level_id=? where id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, game.getName());
        ps.setString(2, game.getType());
        ps.setString(3, game.getDescription());
        ps.setString(4, game.getImage());
        ps.setInt(5, game.getId_level());
        ps.setInt(6, game.getId());

        ps.executeUpdate();
        IO.println("update avec succées");
    }

    public List<Game> getAll() throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql ="select * from game ";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            Game g= new Game(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5) );
            games.add(g);
        }

        return games;
    }
}
