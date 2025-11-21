package com.example.healthmonitoring.bmi.service;

import com.example.healthmonitoring.bmi.entity.WeightLog;
import com.example.healthmonitoring.bmi.repo.WeightLogRepository;
import com.example.healthmonitoring.profile.entity.UserProfile;
import com.example.healthmonitoring.profile.repo.UserProfileRepository;
import com.example.healthmonitoring.profile.service.HealthCalculationService;
import com.example.healthmonitoring.profile.service.ProfileService;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BmiService {

    private final WeightLogRepository weightRepo;
    private final UserProfileRepository profileRepo;
    private final HealthCalculationService healthCalc;
    private final ProfileService profileService;

    /**
     * Log weight and update profile
     */
    @Transactional
    public WeightLog logWeight(String userId, double weightKg, String note) {
        // Get user profile for height
        UserProfile profile = profileRepo.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Profile not found. Please create profile first."));

        // Calculate BMI
        double bmi = healthCalc.calculateBMI(weightKg, profile.getHeightCm());

        // Create weight log
        WeightLog log = WeightLog.builder()
            .userId(userId)
            .weightKg(weightKg)
            .bmi(bmi)
            .note(note)
            .build();

        WeightLog saved = weightRepo.save(log);

        // Update current weight in profile
        profileService.updateWeight(userId, weightKg);

        return saved;
    }

    /**
     * Get weight history
     */
    public List<WeightLog> getWeightHistory(String userId) {
        return weightRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get weight progress chart data
     */
    public WeightProgressResponse getWeightProgress(String userId, LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay().minusNanos(1);

        List<WeightLog> logs = weightRepo.findByUserIdAndCreatedAtBetweenOrderByCreatedAtAsc(
            userId, start, end
        );

        if (logs.isEmpty()) {
            return WeightProgressResponse.builder()
                .logs(new ArrayList<>())
                .weightChange(0.0)
                .averageBmi(0.0)
                .build();
        }

        double weightChange = logs.get(logs.size() - 1).getWeightKg() - logs.get(0).getWeightKg();
        double avgBmi = logs.stream().mapToDouble(WeightLog::getBmi).average().orElse(0.0);

        return WeightProgressResponse.builder()
            .logs(logs)
            .weightChange(weightChange)
            .averageBmi(avgBmi)
            .startWeight(logs.get(0).getWeightKg())
            .currentWeight(logs.get(logs.size() - 1).getWeightKg())
            .build();
    }

    /**
     * Get latest weight
     */
    public WeightLog getLatestWeight(String userId) {
        return weightRepo.findFirstByUserIdOrderByCreatedAtDesc(userId)
            .orElseThrow(() -> new RuntimeException("No weight logs found"));
    }

    /**
     * Delete weight log
     */
    @Transactional
    public void deleteWeightLog(String userId, Long logId) {
        WeightLog log = weightRepo.findById(logId)
            .orElseThrow(() -> new RuntimeException("Weight log not found"));

        if (!log.getUserId().equals(userId)) {
            throw new RuntimeException("Forbidden");
        }

        weightRepo.deleteById(logId);

        // Update profile with latest weight if available
        weightRepo.findFirstByUserIdOrderByCreatedAtDesc(userId)
            .ifPresent(latest -> profileService.updateWeight(userId, latest.getWeightKg()));
    }

    @Data
    @Builder
    public static class WeightProgressResponse {
        private List<WeightLog> logs;
        private double weightChange;
        private double averageBmi;
        private Double startWeight;
        private Double currentWeight;
    }
}