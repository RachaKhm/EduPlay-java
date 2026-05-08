package dev.eduplay.entities;

import java.time.LocalDateTime;

public class Commande {
    private int id;
    private int productId;
    private int parentId;
    private int quantity;
    private double totalPrice;
    private String status; // ex: pending, confirmed, shipped
    private LocalDateTime createdAt;

    public Commande() {
        this.status = "pending";
        this.createdAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // shipping address removed from Commande entity (handled elsewhere if needed)

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

