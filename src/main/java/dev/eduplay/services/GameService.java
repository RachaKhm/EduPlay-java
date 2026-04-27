package dev.eduplay.services;

import dev.eduplay.entities.Game;

import dev.eduplay.entities.Level;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameService {
    private Connection cnx;


    public GameService() throws SQLException {
        cnx=MyDataBase.getInstance().getCnx();
    }

    public void add(Game game)  {
        String sql= "insert into game (id_level_id,name,type,description,image) values (?,?,?,?,?)";
        PreparedStatement ps = null;
        try {
            ps = cnx.prepareStatement(sql);
            ps.setInt(1,game.getId_level().getId());
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
        ps.setInt(5, game.getId_level().getId());
        ps.setInt(6, game.getId());

        ps.executeUpdate();
        IO.println("update avec succées");
    }

    // Dans GameService.java
    public List<Game> getAll() throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT g.*, l.id as level_id, l.name as level_name, " +
                "l.description as level_desc, l.difficulty, " +
                "l.min_age, l.max_age, l.pedag_goal " +
                "FROM game g " +
                "INNER JOIN level l ON g.id_level_id = l.id";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                // Créer l'objet Level complet
                Level level = new Level();
                level.setId(rs.getInt("level_id"));
                level.setName(rs.getString("level_name"));
                level.setDescription(rs.getString("level_desc"));
                level.setDifficulty(rs.getInt("difficulty"));
                level.setMinAge(rs.getInt("min_age"));
                level.setMaxAge(rs.getInt("max_age"));
                level.setPedagGoal(rs.getString("pedag_goal"));

                // Créer le Game avec l'objet Level
                Game game = new Game();
                game.setId(rs.getInt("id"));
                game.setLevel(level);
                game.setName(rs.getString("name"));
                game.setType(rs.getString("type"));
                game.setDescription(rs.getString("description"));
                game.setImage(rs.getString("image"));

                games.add(game);
            }
        }

        return games;
    }
}
