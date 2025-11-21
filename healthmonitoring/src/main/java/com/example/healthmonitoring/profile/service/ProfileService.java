package com.example.healthmonitoring.profile.service;

import com.example.healthmonitoring.profile.entity.UserProfile;
import com.example.healthmonitoring.profile.repo.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserProfileRepository profileRepo;
    private final HealthCalculationService healthCalc;

    /**
     * Create or update user profile with automatic calculations
     */
    @Transactional
    public UserProfile saveProfile(UserProfile profile) {
        // Calculate age
        int age = healthCalc.calculateAge(profile.getDateOfBirth());

        // Calculate BMI
        double bmi = healthCalc.calculateBMI(profile.getCurrentWeightKg(), profile.getHeightCm());
        profile.setBmi(bmi);

        // Calculate BMR
        double bmr = healthCalc.calculateBMR(
            profile.getCurrentWeightKg(),
            profile.getHeightCm(),
            age,
            profile.getGender()
        );
        profile.setBmr(bmr);

        // Calculate TDEE
        double tdee = healthCalc.calculateTDEE(bmr, profile.getActivityLevel());
        profile.setTdee(tdee);

        // Calculate daily calorie goal
        int calorieGoal = healthCalc.calculateDailyCalorieGoal(tdee, profile.getGoal());
        profile.setDailyCalorieGoal(calorieGoal);

        // Calculate daily water goal
        int waterGoal = healthCalc.calculateDailyWaterGoal(
            profile.getCurrentWeightKg(),
            profile.getActivityLevel()
        );
        profile.setDailyWaterGoalMl(waterGoal);

        return profileRepo.save(profile);
    }

    /**
     * Get user profile by userId
     */
    public UserProfile getProfile(String userId) {
        return profileRepo.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found for user: " + userId));
    }

    /**
     * Check if profile exists
     */
    public boolean hasProfile(String userId) {
        return profileRepo.existsByUserId(userId);
    }

    /**
     * Update weight and recalculate metrics
     */
    @Transactional
    public UserProfile updateWeight(String userId, double newWeightKg) {
        UserProfile profile = getProfile(userId);
        profile.setCurrentWeightKg(newWeightKg);
        return saveProfile(profile);
    }

    /**
     * Get health insights
     */
    public HealthInsights getHealthInsights(String userId) {
        UserProfile profile = getProfile(userId);
        
        String bmiCategory = healthCalc.getBMICategory(profile.getBmi());
        double[] idealWeightRange = healthCalc.calculateIdealWeightRange(profile.getHeightCm());
        
        Integer weeksToGoal = null;
        if (profile.getTargetWeightKg() != null) {
            weeksToGoal = healthCalc.estimateWeeksToGoal(
                profile.getCurrentWeightKg(),
                profile.getTargetWeightKg()
            );
        }

        return HealthInsights.builder()
            .bmi(profile.getBmi())
            .bmiCategory(bmiCategory)
            .bmr(profile.getBmr())
            .tdee(profile.getTdee())
            .dailyCalorieGoal(profile.getDailyCalorieGoal())
            .dailyWaterGoalMl(profile.getDailyWaterGoalMl())
            .idealWeightMin(idealWeightRange[0])
            .idealWeightMax(idealWeightRange[1])
            .weeksToGoal(weeksToGoal)
            .build();
    }

    @lombok.Data
    @lombok.Builder
    public static class HealthInsights {
        private Double bmi;
        private String bmiCategory;
        private Double bmr;
        private Double tdee;
        private Integer dailyCalorieGoal;
        private Integer dailyWaterGoalMl;
        private Double idealWeightMin;
        private Double idealWeightMax;
        private Integer weeksToGoal;
    }
}