package dev.eduplay.entities;

/**
 * Entité Product pour la gestion Marketplace.
 * Champs : id, name, price, description, availability, picture
 */
public class Product {
    private int id;
    private String name;
    private double price;
    private String description;
    private boolean availability;
    private String picture; // URL ou chemin

    public Product() {
        // valeur par défaut
        this.availability = true;
    }

    public Product(int id, String name, double price, String description, boolean availability, String picture) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.availability = availability;
        this.picture = picture;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", availability=" + availability +
                '}';
    }
}

