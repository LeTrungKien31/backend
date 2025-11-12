package com.example.healthmonitoring.activity.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "activity_logs")
public class ActivityLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false) private String userId;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private double met;
    @Column(nullable=false) private int minutes;
    @Column(nullable=false) private double weightKgAtTime;
    @Column(nullable=false) private int totalKcal;
    @Column(nullable=false) private LocalDateTime createdAt;

    @PrePersist void onCreate(){ if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getMet() { return met; }
    public void setMet(double met) { this.met = met; }
    public int getMinutes() { return minutes; }
    public void setMinutes(int minutes) { this.minutes = minutes; }
    public double getWeightKgAtTime() { return weightKgAtTime; }
    public void setWeightKgAtTime(double weightKgAtTime) { this.weightKgAtTime = weightKgAtTime; }
    public int getTotalKcal() { return totalKcal; }
    public void setTotalKcal(int totalKcal) { this.totalKcal = totalKcal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
