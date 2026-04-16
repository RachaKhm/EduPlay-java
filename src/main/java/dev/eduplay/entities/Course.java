package dev.eduplay.entities;

import java.time.LocalDateTime;

public class Course {
    private int id;
    private String title;
    private Integer durationTraining;
    private String description;
    private String level;
    private String pdfFile;
    private String status;
    private int teacherId;
    private LocalDateTime createdAt;

    public Course() {
    }

    public Course(int id, String title, Integer durationTraining, String description,
                  String level, String pdfFile, String status, int teacherId, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.durationTraining = durationTraining;
        this.description = description;
        this.level = level;
        this.pdfFile = pdfFile;
        this.status = status;
        this.teacherId = teacherId;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getDurationTraining() {
        return durationTraining;
    }

    public void setDurationTraining(Integer durationTraining) {
        this.durationTraining = durationTraining;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(String pdfFile) {
        this.pdfFile = pdfFile;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", durationTraining=" + durationTraining +
                ", level='" + level + '\'' +
                ", status='" + status + '\'' +
                ", teacherId=" + teacherId +
                '}';
    }
}
