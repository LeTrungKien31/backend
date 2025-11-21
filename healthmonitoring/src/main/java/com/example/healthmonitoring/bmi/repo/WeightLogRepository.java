package com.example.healthmonitoring.bmi.repo;

import com.example.healthmonitoring.bmi.entity.WeightLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WeightLogRepository extends JpaRepository<WeightLog, Long> {
    
    List<WeightLog> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<WeightLog> findByUserIdAndCreatedAtBetweenOrderByCreatedAtAsc(
        String userId, LocalDateTime start, LocalDateTime end
    );
    
    Optional<WeightLog> findFirstByUserIdOrderByCreatedAtDesc(String userId);
    
    @Query("SELECT w FROM WeightLog w WHERE w.userId = ?1 ORDER BY w.createdAt DESC")
    List<WeightLog> findLatestByUserId(String userId);
}