package entities;

public class Game   {
    private int id;
    private int id_level;
    private String name;
    private String type;
    private String description;
    private String image;

    public Game(int id, int id_level, String name, String type, String description, String image) {
        this.id = id;
        this.id_level = id_level;
        this.name = name;
        this.type = type;
        this.description = description;
        this.image = image;
    }

    public Game(int id_level, String name, String type, String description, String image) {
        this.id_level = id_level;
        this.name = name;
        this.type = type;
        this.description = description;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_level() {
        return id_level;
    }

    public void setId_level(int id_level) {
        this.id_level = id_level;
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
                ", id_level=" + id_level +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}

