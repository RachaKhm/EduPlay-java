package entities;

import java.time.LocalDateTime;

public class Level {

    private int id;
    private String name;
    private String description;
    private int difficulty;
    private int minAge;
    private int maxAge;
    private String pedagGoal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Level(String name, String description, int difficulty, int minAge, int maxAge, String pedagGoal) {
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.pedagGoal = pedagGoal;

    }

    public Level(int id, String name, String description, int difficulty, int minAge, int maxAge, String pedagGoal) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.pedagGoal = pedagGoal;

    }

    public Level(int id, String name, String description, int difficulty, int minAge, int maxAge, String pedagGoal, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.difficulty = difficulty;
        this.minAge = minAge;
        this.maxAge = maxAge;
        this.pedagGoal = pedagGoal;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getMinAge() {
        return minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public String getPedagGoal() {
        return pedagGoal;
    }




    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public void setPedagGoal(String pedagGoal) {
        this.pedagGoal = pedagGoal;
    }



    @Override
    public String toString() {
        return "Level{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", difficulty=" + difficulty +
                ", minAge=" + minAge +
                ", maxAge=" + maxAge +
                ", pedagGoal='" + pedagGoal + '\'' +

                '}';
    }
}
