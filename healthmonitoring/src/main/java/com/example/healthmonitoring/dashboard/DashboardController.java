package com.example.healthmonitoring.dashboard;

import com.example.healthmonitoring.activity.repo.ActivityLogRepository;
import com.example.healthmonitoring.meal.repo.MealLogRepository;
import com.example.healthmonitoring.water.repo.WaterLogRepository;
import com.example.healthmonitoring.profile.repo.UserProfileRepository;
import com.example.healthmonitoring.profile.entity.UserProfile;
import lombok.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final WaterLogRepository waterRepo;
    private final MealLogRepository mealRepo;
    private final ActivityLogRepository activityRepo;
    private final UserProfileRepository profileRepo;

    private String getUserId(Authentication auth) {
        return auth.getName();
    }

    /**
     * Get today's complete dashboard
     */
    @GetMapping("/today")
    public TodayDashboard getToday(Authentication auth) {
        String userId = getUserId(auth);
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        // Get actual values
        long waterMl = waterRepo.sumAmountByUserAndRange(userId, start, end);
        int caloriesIn = mealRepo.sumKcalByUserAndRange(userId, start, end);
        int caloriesOut = activityRepo.sumKcalByUserAndRange(userId, start, end);
        int netCalories = caloriesIn - caloriesOut;

        // Get goals from profile
        UserProfile profile = profileRepo.findByUserId(userId).orElse(null);
        
        int waterGoal = profile != null && profile.getDailyWaterGoalMl() != null 
            ? profile.getDailyWaterGoalMl() : 2000;
        int calorieGoal = profile != null && profile.getDailyCalorieGoal() != null 
            ? profile.getDailyCalorieGoal() : 2000;

        // Calculate percentages
        double waterPercentage = (double) waterMl / waterGoal * 100;
        double caloriePercentage = (double) caloriesIn / calorieGoal * 100;

        return TodayDashboard.builder()
            .date(today)
            .water(WaterData.builder()
                .current((int) waterMl)
                .goal(waterGoal)
                .percentage(Math.min(waterPercentage, 100.0))
                .remaining(Math.max(0, waterGoal - (int) waterMl))
                .build())
            .calories(CalorieData.builder()
                .intake(caloriesIn)
                .burned(caloriesOut)
                .net(netCalories)
                .goal(calorieGoal)
                .remaining(calorieGoal - caloriesIn)
                .percentage(Math.min(caloriePercentage, 100.0))
                .build())
            .hasProfile(profile != null)
            .build();
    }

    /**
     * Get week overview (last 7 days)
     */
    @GetMapping("/week")
    public WeekOverview getWeekOverview(Authentication auth) {
        String userId = getUserId(auth);
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(6);

        LocalDateTime start = weekStart.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        long totalWater = waterRepo.sumAmountByUserAndRange(userId, start, end);
        int totalCaloriesIn = mealRepo.sumKcalByUserAndRange(userId, start, end);
        int totalCaloriesOut = activityRepo.sumKcalByUserAndRange(userId, start, end);

        return WeekOverview.builder()
            .weekStart(weekStart)
            .weekEnd(today)
            .avgWaterPerDay((int) totalWater / 7)
            .avgCaloriesInPerDay(totalCaloriesIn / 7)
            .avgCaloriesOutPerDay(totalCaloriesOut / 7)
            .totalCaloriesIn(totalCaloriesIn)
            .totalCaloriesOut(totalCaloriesOut)
            .build();
    }

    // DTOs
    @Data
    @Builder
    public static class TodayDashboard {
        private LocalDate date;
        private WaterData water;
        private CalorieData calories;
        private boolean hasProfile;
    }

    @Data
    @Builder
    public static class WaterData {
        private int current;
        private int goal;
        private double percentage;
        private int remaining;
    }

    @Data
    @Builder
    public static class CalorieData {
        private int intake;
        private int burned;
        private int net;
        private int goal;
        private int remaining;
        private double percentage;
    }

    @Data
    @Builder
    public static class WeekOverview {
        private LocalDate weekStart;
        private LocalDate weekEnd;
        private int avgWaterPerDay;
        private int avgCaloriesInPerDay;
        private int avgCaloriesOutPerDay;
        private int totalCaloriesIn;
        private int totalCaloriesOut;
    }
}