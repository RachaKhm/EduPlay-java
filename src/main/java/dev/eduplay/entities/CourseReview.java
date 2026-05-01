package dev.eduplay.entities;

import java.time.LocalDateTime;

public class CourseReview {
    private int id;
    private int courseId;
    private int userId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public CourseReview() {
    }

    public CourseReview(int id, int courseId, int userId, int rating, String comment, LocalDateTime createdAt) {
        this.id = id;
        this.courseId = courseId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
