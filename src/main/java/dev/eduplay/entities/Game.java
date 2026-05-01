package dev.eduplay.entities;

public class Game {
    private int id;
    private Level level;
    private String name;
    private String type;
    private String description;
    private String image;

    public Game(int id, Level level, String name, String type, String description, String image) {
        this.id = id;
        this.level = level;
        this.name = name;
        this.type = type;
        this.description = description;
        this.image = image;
    }

    public Game(Level level, String name, String type, String description, String image) {
        this.level = level;
        this.name = name;
        this.type = type;
        this.description = description;
        this.image = image;
    }

    public Game() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Level getId_level() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;

    }


    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", id_level=" + level +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}

