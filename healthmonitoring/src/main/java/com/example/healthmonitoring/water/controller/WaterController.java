package com.example.healthmonitoring.water.controller;

import com.example.healthmonitoring.water.entity.WaterLog;
import com.example.healthmonitoring.water.repo.WaterLogRepository;
import com.example.healthmonitoring.profile.repo.UserProfileRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/water")
@RequiredArgsConstructor
public class WaterController {

    private final WaterLogRepository waterRepo;
    private final UserProfileRepository profileRepo;

    private String getUserId(Authentication auth) {
        return auth.getName();
    }

    /**
     * Add water log
     */
    @PostMapping
    public WaterLog addWater(@Valid @RequestBody AddWaterRequest request, Authentication auth) {
        WaterLog log = new WaterLog();
        log.setUserId(getUserId(auth));
        log.setAmount(request.getAmount());
        return waterRepo.save(log);
    }

    /**
     * Get today's total water intake
     */
    @GetMapping("/today/total")
    public TodayWaterResponse getTodayTotal(Authentication auth) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        long totalMl = waterRepo.sumAmountByUserAndRange(getUserId(auth), start, end);
        
        // Get goal from profile
        int goalMl = profileRepo.findByUserId(getUserId(auth))
            .map(p -> p.getDailyWaterGoalMl() != null ? p.getDailyWaterGoalMl() : 2000)
            .orElse(2000);

        double percentage = (double) totalMl / goalMl * 100;

        return new TodayWaterResponse(
            Math.toIntExact(totalMl),
            goalMl,
            Math.min(percentage, 100.0),
            totalMl >= goalMl
        );
    }

    /**
     * Get water logs history
     */
    @GetMapping("/history")
    public List<WaterLog> getHistory(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay().minusNanos(1);
        
        return waterRepo.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            getUserId(auth), start, end
        );
    }

    /**
     * Delete water log
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWater(@PathVariable Long id, Authentication auth) {
        WaterLog log = waterRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Water log not found"));
        
        if (!log.getUserId().equals(getUserId(auth))) {
            throw new RuntimeException("Forbidden");
        }
        
        waterRepo.deleteById(id);
    }

    /**
     * Get water statistics for a date range
     */
    @GetMapping("/stats")
    public WaterStatsResponse getStats(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay().minusNanos(1);
        
        long totalMl = waterRepo.sumAmountByUserAndRange(getUserId(auth), start, end);
        List<WaterLog> logs = waterRepo.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            getUserId(auth), start, end
        );

        int days = (int) java.time.temporal.ChronoUnit.DAYS.between(from, to) + 1;
        int averagePerDay = days > 0 ? Math.toIntExact(totalMl / days) : 0;

        return new WaterStatsResponse(
            Math.toIntExact(totalMl),
            averagePerDay,
            logs.size(),
            days
        );
    }

    // DTO Classes
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddWaterRequest {
        @Positive(message = "Amount must be positive")
        private int amount; // ml
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class TodayWaterResponse {
        private int totalMl;
        private int goalMl;
        private double percentage;
        private boolean goalReached;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class WaterStatsResponse {
        private int totalMl;
        private int averagePerDay;
        private int logsCount;
        private int daysCount;
    }
}