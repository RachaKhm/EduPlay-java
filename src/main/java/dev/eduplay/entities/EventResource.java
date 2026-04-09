package dev.eduplay.entities;

import java.time.LocalDateTime;

public class EventResource {
    private int id;
    private String type;
    private String title;
    private String context;
    private String filePath;
    private String url;
    private LocalDateTime createdAt;
    private SchoolEvent event;

    public EventResource() {}

    public EventResource(int id, String type, String title, String context, String filePath, String url, LocalDateTime createdAt, SchoolEvent event) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.context = context;
        this.filePath = filePath;
        this.url = url;
        this.createdAt = createdAt;
        this.event = event;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public SchoolEvent getEvent() {
        return event;
    }

    public void setEvent(SchoolEvent event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "EventResource{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", context='" + context + '\'' +
                ", filePath='" + filePath + '\'' +
                ", url='" + url + '\'' +
                ", createdAt=" + createdAt +
                ", event=" + event +
                '}';
    }
}
