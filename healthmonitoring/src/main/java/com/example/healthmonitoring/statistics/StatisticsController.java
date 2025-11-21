package com.example.healthmonitoring.statistics;

import com.example.healthmonitoring.activity.repo.ActivityLogRepository;
import com.example.healthmonitoring.meal.repo.MealLogRepository;
import com.example.healthmonitoring.water.repo.WaterLogRepository;
import com.example.healthmonitoring.profile.repo.UserProfileRepository;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final WaterLogRepository waterRepo;
    private final MealLogRepository mealRepo;
    private final ActivityLogRepository activityRepo;
    private final UserProfileRepository profileRepo;

    private String getUserId(Authentication auth) {
        return auth.getName();
    }

    /**
     * Get summary statistics for a date range
     */
    @GetMapping("/summary")
    public SummaryStats getSummary(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        String userId = getUserId(auth);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay().minusNanos(1);

        long totalWater = waterRepo.sumAmountByUserAndRange(userId, start, end);
        int totalCaloriesIn = mealRepo.sumKcalByUserAndRange(userId, start, end);
        int totalCaloriesOut = activityRepo.sumKcalByUserAndRange(userId, start, end);
        int netCalories = totalCaloriesIn - totalCaloriesOut;

        long days = ChronoUnit.DAYS.between(from, to) + 1;
        int avgWaterPerDay = days > 0 ? (int) (totalWater / days) : 0;
        int avgCaloriesInPerDay = days > 0 ? totalCaloriesIn / (int) days : 0;
        int avgCaloriesOutPerDay = days > 0 ? totalCaloriesOut / (int) days : 0;

        return SummaryStats.builder()
            .totalWaterMl((int) totalWater)
            .totalCaloriesIn(totalCaloriesIn)
            .totalCaloriesOut(totalCaloriesOut)
            .netCalories(netCalories)
            .avgWaterPerDay(avgWaterPerDay)
            .avgCaloriesInPerDay(avgCaloriesInPerDay)
            .avgCaloriesOutPerDay(avgCaloriesOutPerDay)
            .daysCount((int) days)
            .build();
    }

    /**
     * Get daily breakdown for charts
     */
    @GetMapping("/daily")
    public List<DailyStats> getDailyStats(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        String userId = getUserId(auth);
        List<DailyStats> result = new ArrayList<>();

        LocalDate current = from;
        while (!current.isAfter(to)) {
            LocalDateTime start = current.atStartOfDay();
            LocalDateTime end = current.plusDays(1).atStartOfDay().minusNanos(1);

            long water = waterRepo.sumAmountByUserAndRange(userId, start, end);
            int caloriesIn = mealRepo.sumKcalByUserAndRange(userId, start, end);
            int caloriesOut = activityRepo.sumKcalByUserAndRange(userId, start, end);

            result.add(DailyStats.builder()
                .date(current)
                .waterMl((int) water)
                .caloriesIn(caloriesIn)
                .caloriesOut(caloriesOut)
                .netCalories(caloriesIn - caloriesOut)
                .build());

            current = current.plusDays(1);
        }

        return result;
    }

    /**
     * Get weekly statistics
     */
    @GetMapping("/weekly")
    public WeeklyStats getWeeklyStats(Authentication auth) {
        String userId = getUserId(auth);
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6); // Last 7 days

        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        long totalWater = waterRepo.sumAmountByUserAndRange(userId, start, end);
        int totalCaloriesIn = mealRepo.sumKcalByUserAndRange(userId, start, end);
        int totalCaloriesOut = activityRepo.sumKcalByUserAndRange(userId, start, end);

        // Get daily breakdown
        List<DailyStats> dailyData = new ArrayList<>();
        LocalDate current = weekStart;
        while (!current.isAfter(today)) {
            LocalDateTime dayStart = current.atStartOfDay();
            LocalDateTime dayEnd = current.plusDays(1).atStartOfDay().minusNanos(1);

            long water = waterRepo.sumAmountByUserAndRange(userId, dayStart, dayEnd);
            int caloriesIn = mealRepo.sumKcalByUserAndRange(userId, dayStart, dayEnd);
            int caloriesOut = activityRepo.sumKcalByUserAndRange(userId, dayStart, dayEnd);

            dailyData.add(DailyStats.builder()
                .date(current)
                .waterMl((int) water)
                .caloriesIn(caloriesIn)
                .caloriesOut(caloriesOut)
                .netCalories(caloriesIn - caloriesOut)
                .build());

            current = current.plusDays(1);
        }

        return WeeklyStats.builder()
            .weekStart(weekStart)
            .weekEnd(today)
            .totalWaterMl((int) totalWater)
            .totalCaloriesIn(totalCaloriesIn)
            .totalCaloriesOut(totalCaloriesOut)
            .avgWaterPerDay((int) totalWater / 7)
            .avgCaloriesInPerDay(totalCaloriesIn / 7)
            .avgCaloriesOutPerDay(totalCaloriesOut / 7)
            .dailyBreakdown(dailyData)
            .build();
    }

    /**
     * Get monthly statistics
     */
    @GetMapping("/monthly")
    public MonthlyStats getMonthlyStats(
            Authentication auth,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        
        String userId = getUserId(auth);
        LocalDate now = LocalDate.now();
        int targetYear = year != null ? year : now.getYear();
        int targetMonth = month != null ? month : now.getMonthValue();

        LocalDate monthStart = LocalDate.of(targetYear, targetMonth, 1);
        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

        LocalDateTime start = monthStart.atStartOfDay();
        LocalDateTime end = monthEnd.plusDays(1).atStartOfDay().minusNanos(1);

        long totalWater = waterRepo.sumAmountByUserAndRange(userId, start, end);
        int totalCaloriesIn = mealRepo.sumKcalByUserAndRange(userId, start, end);
        int totalCaloriesOut = activityRepo.sumKcalByUserAndRange(userId, start, end);

        int days = monthEnd.getDayOfMonth();

        return MonthlyStats.builder()
            .year(targetYear)
            .month(targetMonth)
            .monthStart(monthStart)
            .monthEnd(monthEnd)
            .totalWaterMl((int) totalWater)
            .totalCaloriesIn(totalCaloriesIn)
            .totalCaloriesOut(totalCaloriesOut)
            .avgWaterPerDay((int) totalWater / days)
            .avgCaloriesInPerDay(totalCaloriesIn / days)
            .avgCaloriesOutPerDay(totalCaloriesOut / days)
            .daysInMonth(days)
            .build();
    }

    // DTOs
    @Data
    @Builder
    public static class SummaryStats {
        private int totalWaterMl;
        private int totalCaloriesIn;
        private int totalCaloriesOut;
        private int netCalories;
        private int avgWaterPerDay;
        private int avgCaloriesInPerDay;
        private int avgCaloriesOutPerDay;
        private int daysCount;
    }

    @Data
    @Builder
    public static class DailyStats {
        private LocalDate date;
        private int waterMl;
        private int caloriesIn;
        private int caloriesOut;
        private int netCalories;
    }

    @Data
    @Builder
    public static class WeeklyStats {
        private LocalDate weekStart;
        private LocalDate weekEnd;
        private int totalWaterMl;
        private int totalCaloriesIn;
        private int totalCaloriesOut;
        private int avgWaterPerDay;
        private int avgCaloriesInPerDay;
        private int avgCaloriesOutPerDay;
        private List<DailyStats> dailyBreakdown;
    }

    @Data
    @Builder
    public static class MonthlyStats {
        private int year;
        private int month;
        private LocalDate monthStart;
        private LocalDate monthEnd;
        private int totalWaterMl;
        private int totalCaloriesIn;
        private int totalCaloriesOut;
        private int avgWaterPerDay;
        private int avgCaloriesInPerDay;
        private int avgCaloriesOutPerDay;
        private int daysInMonth;
    }
}