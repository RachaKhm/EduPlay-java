package dev.eduplay.entities;

import java.time.LocalDateTime;
import java.util.List;

public class SchoolEvent {

    private int id;
    private String title;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String location;
    private String imagePath;
    private LocalDateTime createdAt;
    private String latitude;
    private String longitude;
    private List<EventResource> resources;
    private List<EventRegistration> registrations;

    private int maxCapacity = 50;
    private int currentRegistrations = 0;

    // Constructeurs
    public SchoolEvent() {}

    public SchoolEvent(int id, String title, String description, LocalDateTime startDate,
                       LocalDateTime endDate, String location, String imagePath,
                       LocalDateTime createdAt, String latitude, String longitude,
                       List<EventResource> resources, List<EventRegistration> registrations) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
        this.latitude = latitude;
        this.longitude = longitude;
        this.resources = resources;
        this.registrations = registrations;
    }

    public SchoolEvent(int id, String title, String description, LocalDateTime startDate,
                       LocalDateTime endDate, String location, String imagePath,
                       LocalDateTime createdAt, String latitude, String longitude) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.location = location;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    public List<EventResource> getResources() { return resources; }
    public void setResources(List<EventResource> resources) { this.resources = resources; }

    public List<EventRegistration> getRegistrations() { return registrations; }
    public void setRegistrations(List<EventRegistration> registrations) { this.registrations = registrations; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getCurrentRegistrations() { return currentRegistrations; }
    public void setCurrentRegistrations(int currentRegistrations) {
        this.currentRegistrations = currentRegistrations;
    }

    public boolean hasAvailableSpaces() {
        boolean available = currentRegistrations < maxCapacity;
        System.out.println("🔍 Vérification capacité - Actuel: " + currentRegistrations + " / Max: " + maxCapacity + " - Places dispo: " + available);
        return available;
    }

    public int getRemainingSpaces() {
        return maxCapacity - currentRegistrations;
    }

    @Override
    public String toString() {
        return "SchoolEvent{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", maxCapacity=" + maxCapacity +
                ", currentRegistrations=" + currentRegistrations +
                '}';
    }
}