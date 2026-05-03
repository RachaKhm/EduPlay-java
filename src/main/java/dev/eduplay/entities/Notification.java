package dev.eduplay.entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {

    private int id;
    private int userId;
    private String title;
    private String message;
    private String type; // EVENT_NEW, EVENT_MODIFIED, REMINDER, REGISTRATION_CONFIRMED
    private int referenceId;
    private boolean read;
    private LocalDateTime createdAt;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public Notification() {}

    public Notification(int userId, String title, String message, String type, int referenceId) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.read = false;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public int getReferenceId() { return referenceId; }
    public boolean isRead() { return read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getFormattedDate() { return createdAt != null ? createdAt.format(formatter) : ""; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setUserId(int userId) { this.userId = userId; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setType(String type) { this.type = type; }
    public void setReferenceId(int referenceId) { this.referenceId = referenceId; }
    public void setRead(boolean read) { this.read = read; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", read=" + read +
                '}';
    }
}