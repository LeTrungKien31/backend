package com.example.healthmonitoring.activity.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.healthmonitoring.activity.entity.ActivityLog;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        String userId, LocalDateTime start, LocalDateTime end);

    @Query("""
           select coalesce(sum(a.totalKcal), 0)
           from ActivityLog a
           where a.userId = ?1 and a.createdAt between ?2 and ?3
           """)
    int sumKcalByUserAndRange(String userId, LocalDateTime start, LocalDateTime end);
}
