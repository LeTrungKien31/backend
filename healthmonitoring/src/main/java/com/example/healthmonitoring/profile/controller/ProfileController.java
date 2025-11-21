package com.example.healthmonitoring.profile.controller;

import com.example.healthmonitoring.profile.entity.UserProfile;
import com.example.healthmonitoring.profile.service.ProfileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    private String getUserId(Authentication auth) {
        return auth.getName();
    }

    /**
     * Get user profile
     */
    @GetMapping
    public UserProfile getProfile(Authentication auth) {
        return profileService.getProfile(getUserId(auth));
    }

    /**
     * Create or update profile
     */
    @PostMapping
    public UserProfile saveProfile(@Valid @RequestBody ProfileRequest request, Authentication auth) {
        UserProfile profile = UserProfile.builder()
            .userId(getUserId(auth))
            .gender(request.getGender())
            .dateOfBirth(request.getDateOfBirth())
            .heightCm(request.getHeightCm())
            .currentWeightKg(request.getCurrentWeightKg())
            .targetWeightKg(request.getTargetWeightKg())
            .activityLevel(request.getActivityLevel())
            .goal(request.getGoal())
            .build();

        // If updating existing profile, keep the ID
        if (profileService.hasProfile(getUserId(auth))) {
            UserProfile existing = profileService.getProfile(getUserId(auth));
            profile.setId(existing.getId());
            profile.setCreatedAt(existing.getCreatedAt());
        }

        return profileService.saveProfile(profile);
    }

    /**
     * Update weight only
     */
    @PatchMapping("/weight")
    public UserProfile updateWeight(@Valid @RequestBody UpdateWeightRequest request, Authentication auth) {
        return profileService.updateWeight(getUserId(auth), request.getWeightKg());
    }

    /**
     * Get health insights (BMI, BMR, TDEE, etc.)
     */
    @GetMapping("/insights")
    public ProfileService.HealthInsights getInsights(Authentication auth) {
        return profileService.getHealthInsights(getUserId(auth));