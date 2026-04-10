package dev.eduplay.entities;

public class Library {
    private int id;
    private String name;
    private String description;
    private String coverImage;
    private int minAge;
    private int maxAge;
    private String level;
    private String theme;

    public Library() {}

    public Library(int id, String name, String description, String coverImage,
                   int minAge, int maxAge, String level, String theme) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.coverImage = coverImage;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.level = level;
        this.theme = theme;
    }

    public Library(String name, String description, String coverImage,
                   int minAge, int maxAge, String level, String theme) {
        this.name = name;
        this.description = description;
        this.coverImage = coverImage;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.level = level;
        this.theme = theme;
    }

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public int getMinAge() { return minAge; }
    public void setMinAge(int minAge) { this.minAge = minAge; }

    public int getMaxAge() { return maxAge; }
    public void setMaxAge(int maxAge) { this.maxAge = maxAge; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
}