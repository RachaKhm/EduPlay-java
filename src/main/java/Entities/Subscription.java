package Entities;

import java.time.LocalDateTime;

public class Subscription {
    private int id;
    private int parentId;
    private int kidId;
    private int courseId;
    private LocalDateTime subscribedAt;
    private boolean active;

    public Subscription() {
    }

    public Subscription(int id, int parentId, int kidId, int courseId,
                        LocalDateTime subscribedAt, boolean active) {
        this.id = id;
        this.parentId = parentId;
        this.kidId = kidId;
        this.courseId = courseId;
        this.subscribedAt = subscribedAt;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getKidId() {
        return kidId;
    }

    public void setKidId(int kidId) {
        this.kidId = kidId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public LocalDateTime getSubscribedAt() {
        return subscribedAt;
    }

    public void setSubscribedAt(LocalDateTime subscribedAt) {
        this.subscribedAt = subscribedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", kidId=" + kidId +
                ", courseId=" + courseId +
                ", active=" + active +
                '}';
    }
}
