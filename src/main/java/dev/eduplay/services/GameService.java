package dev.eduplay.services;

import dev.eduplay.entities.Game;

import dev.eduplay.entities.Level;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameService {
    private Connection cnx;
    private String tableName = "game";
    private String levelColumn = null; // actual FK column name in game table

    public GameService() throws SQLException {
        cnx = MyDataBase.getInstance().getCnx();
        // detect actual table name (game or games)
        try {
            DatabaseMetaData md = cnx.getMetaData();
            try (ResultSet t = md.getTables(null, null, "game", null)) {
                if (!t.next()) {
                    try (ResultSet t2 = md.getTables(null, null, "games", null)) {
                        if (t2.next()) tableName = "games";
                    }
                }
            }
            // detect level FK column name
            try (ResultSet cols = md.getColumns(null, null, tableName, null)) {
                while (cols.next()) {
                    String c = cols.getString("COLUMN_NAME");
                    if (c == null) continue;
                    String lc = c.toLowerCase();
                    if (lc.contains("level") || lc.contains("id_level")) {
                        levelColumn = c;
                        break;
                    }
                }
            }
            if (levelColumn == null) levelColumn = "id_level_id"; // fallback
        } catch (SQLException ex) {
            // ignore, we'll use defaults
        }
    }

    public void add(Game game) {
        String sql = "insert into " + tableName + " (" + levelColumn + ", name, type, description, image) values (?,?,?,?,?)";
        PreparedStatement ps = null;
        try {
            ps = cnx.prepareStatement(sql);
            Level lvl = game.getId_level();
            ps.setInt(1, lvl != null ? lvl.getId() : 0);
            ps.setString(2, game.getName());
            ps.setString(3, game.getType());
            ps.setString(4, game.getDescription());
            ps.setString(5, game.getImage());
            ps.executeUpdate();
            System.out.println("ajout avec succées");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void delete(int id) throws SQLException {
        String sql = "delete from game where id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("suppression avec succées");
    }

    public void update(Game game) throws SQLException {
        String sql = "update " + tableName + " set name=?, type=?, description=?, image=?, " + levelColumn + "=? where id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, game.getName());
        ps.setString(2, game.getType());
        ps.setString(3, game.getDescription());
        ps.setString(4, game.getImage());
        Level lvl = game.getId_level();
        ps.setInt(5, lvl != null ? lvl.getId() : 0);
        ps.setInt(6, game.getId());

        ps.executeUpdate();
        System.out.println("update avec succées");
    }

    // Dans GameService.java
    public List<Game> getAll() throws SQLException {
        List<Game> games = new ArrayList<>();
        String sql = "SELECT g.*, l.id as level_id, l.name as level_name, " +
                "l.description as level_desc, l.difficulty, " +
                "l.min_age, l.max_age, l.pedag_goal " +
                "FROM " + tableName + " g " +
                "LEFT JOIN level l ON g." + levelColumn + " = l.id";

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