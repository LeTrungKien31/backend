package com.example.healthmonitoring.profile.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId; // email from JWT

    @Column(nullable = false)
    private String gender; // MALE, FEMALE, OTHER

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private double heightCm;

    @Column(nullable = false)
    private double currentWeightKg;

    private Double targetWeightKg;

    @Column(nullable = false)
    private String activityLevel; // SEDENTARY, LIGHTLY_ACTIVE, MODERATELY_ACTIVE, VERY_ACTIVE, EXTRA_ACTIVE

    @Column(nullable = false)
    private String goal; // LOSE_WEIGHT, MAINTAIN, GAIN_WEIGHT

    // Calculated fields
    private Double bmi;
    private Double bmr;
    private Double tdee;
    private Integer dailyCalorieGoal;
    private Integer dailyWaterGoalMl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}