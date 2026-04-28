package dev.eduplay.entities;

import java.time.LocalDateTime;

public class BookRequest {
    private int id;
    private String bookTitle;
    private int enfantId; // ID de l'enfant qui a demandé
    private LocalDateTime requestedAt;
    private boolean isAvailable;
    private boolean isNotified;
    private Integer resourceId; // ID de la ressource une fois liée (nullable)
    
    // UI Fields (populated via JOIN)
    private String enfantName;
    private String enfantRole;

    public BookRequest() {
        this.requestedAt = LocalDateTime.now();
        this.isAvailable = false;
        this.isNotified = false;
    }

    public BookRequest(int id, String bookTitle, int enfantId, LocalDateTime requestedAt, boolean isAvailable, boolean isNotified, Integer resourceId) {
        this.id = id;
        this.bookTitle = bookTitle;
        this.enfantId = enfantId;
        this.requestedAt = requestedAt;
        this.isAvailable = isAvailable;
        this.isNotified = isNotified;
        this.resourceId = resourceId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    
    public int getEnfantId() { return enfantId; }
    public void setEnfantId(int enfantId) { this.enfantId = enfantId; }
    
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    
    public boolean isNotified() { return isNotified; }
    public void setNotified(boolean notified) { isNotified = notified; }
    
    public Integer getResourceId() { return resourceId; }
    public void setResourceId(Integer resourceId) { this.resourceId = resourceId; }

    public String getEnfantName() { return enfantName; }
    public void setEnfantName(String enfantName) { this.enfantName = enfantName; }

    public String getEnfantRole() { return enfantRole; }
    public void setEnfantRole(String enfantRole) { this.enfantRole = enfantRole; }
}
