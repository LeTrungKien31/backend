package com.example.healthmonitoring.meal.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.healthmonitoring.meal.entity.MealLog;

public interface MealLogRepository extends JpaRepository<MealLog, Long> {

    List<MealLog> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        String userId, LocalDateTime start, LocalDateTime end);

    @Query("""
           select coalesce(sum(m.totalKcal), 0)
           from MealLog m
           where m.userId = ?1 and m.createdAt between ?2 and ?3
           """)
    int sumKcalByUserAndRange(String userId, LocalDateTime start, LocalDateTime end);
}
