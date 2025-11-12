package com.example.healthmonitoring.water.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.healthmonitoring.water.entity.WaterLog;

public interface WaterLogRepository extends JpaRepository<WaterLog, Long> {

    List<WaterLog> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        String userId, LocalDateTime start, LocalDateTime end);

    @Query("""
           select coalesce(sum(w.amount), 0)
           from WaterLog w
           where w.userId = ?1 and w.createdAt between ?2 and ?3
           """)
    Long sumAmountByUserAndRange(String userId, LocalDateTime start, LocalDateTime end);
}
