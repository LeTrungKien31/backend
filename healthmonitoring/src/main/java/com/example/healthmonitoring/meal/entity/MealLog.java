package com.example.healthmonitoring.meal.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "meal_logs")
public class MealLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String userId;

    @ManyToOne(optional=false)
    private Food food;

    @Column(nullable=false)
    private double servings;

    @Column(nullable=false)
    private int totalKcal;

    @Column(nullable=false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Food getFood() { return food; }
    public void setFood(Food food) { this.food = food; }
    public double getServings() { return servings; }
    public void setServings(double servings) { this.servings = servings; }
    public int getTotalKcal() { return totalKcal; }
    public void setTotalKcal(int totalKcal) { this.totalKcal = totalKcal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
