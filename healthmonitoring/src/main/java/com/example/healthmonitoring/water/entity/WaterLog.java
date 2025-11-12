package com.example.healthmonitoring.water.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity @Table(name = "water_logs")
public class WaterLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;      // lấy từ JWT
    private int amount;         // ml
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // getters/setters
    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}