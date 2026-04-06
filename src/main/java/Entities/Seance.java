package Entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Seance {
    private int id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int courseId;
    private String title;
    private LocalDate date;
    private String location;
    private String status;
    private String description;

    public Seance() {
    }

    public Seance(int id, LocalDateTime startTime, LocalDateTime endTime, int courseId,
                  String title, LocalDate date, String location, String status, String description) {
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.courseId = courseId;
        this.title = title;
        this.date = date;
        this.location = location;
        this.status = status;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Seance{" +
                "id=" + id +
                ", courseId=" + courseId +
                ", title='" + title + '\'' +
                ", date=" + date +
                ", status='" + status + '\'' +
                '}';
    }
}
