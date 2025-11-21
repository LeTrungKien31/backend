package com.example.healthmonitoring.profile.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Period;

@Service
public class HealthCalculationService {

    /**
     * Calculate BMI (Body Mass Index)
     * BMI = weight(kg) / (height(m))^2
     */
    public double calculateBMI(double weightKg, double heightCm) {
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }

    /**
     * Get BMI category
     */
    public String getBMICategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }

    /**
     * Calculate BMR (Basal Metabolic Rate) using Mifflin-St Jeor Equation
     * Men: BMR = 10 × weight(kg) + 6.25 × height(cm) - 5 × age + 5
     * Women: BMR = 10 × weight(kg) + 6.25 × height(cm) - 5 × age - 161
     */
    public double calculateBMR(double weightKg, double heightCm, int age, String gender) {
        double bmr = 10 * weightKg + 6.25 * heightCm - 5 * age;
        
        if ("MALE".equalsIgnoreCase(gender)) {
            bmr += 5;
        } else if ("FEMALE".equalsIgnoreCase(gender)) {
            bmr -= 161;
        } else {
            // For OTHER, use average
            bmr -= 78;
        }
        
        return bmr;
    }

    /**
     * Calculate TDEE (Total Daily Energy Expenditure)
     * TDEE = BMR × Activity Factor
     */
    public double calculateTDEE(double bmr, String activityLevel) {
        double activityFactor = switch (activityLevel.toUpperCase()) {
            case "SEDENTARY" -> 1.2;           // Little or no exercise
            case "LIGHTLY_ACTIVE" -> 1.375;    // Exercise 1-3 days/week
            case "MODERATELY_ACTIVE" -> 1.55;  // Exercise 3-5 days/week
            case "VERY_ACTIVE" -> 1.725;       // Exercise 6-7 days/week
            case "EXTRA_ACTIVE" -> 1.9;        // Very hard exercise & physical job
            default -> 1.2;
        };
        
        return bmr * activityFactor;
    }

    /**
     * Calculate daily calorie goal based on TDEE and user goal
     */
    public int calculateDailyCalorieGoal(double tdee, String goal) {
        return switch (goal.toUpperCase()) {
            case "LOSE_WEIGHT" -> (int) (tdee - 500);      // 0.5kg loss per week
            case "GAIN_WEIGHT" -> (int) (tdee + 500);      // 0.5kg gain per week
            case "MAINTAIN" -> (int) tdee;
            default -> (int) tdee;
        };
    }

    /**
     * Calculate daily water intake goal (ml)
     * Basic formula: weight(kg) × 30-35 ml
     * Adjusted for activity level
     */
    public int calculateDailyWaterGoal(double weightKg, String activityLevel) {
        double baseWater = weightKg * 33; // ml
        
        double activityBonus = switch (activityLevel.toUpperCase()) {
            case "SEDENTARY" -> 0;
            case "LIGHTLY_ACTIVE" -> 250;
            case "MODERATELY_ACTIVE" -> 500;
            case "VERY_ACTIVE" -> 750;
            case "EXTRA_ACTIVE" -> 1000;
            default -> 0;
        };
        
        return (int) (baseWater + activityBonus);
    }

    /**
     * Calculate age from date of birth
     */
    public int calculateAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    /**
     * Calculate ideal weight range (kg) based on BMI 18.5-24.9
     */
    public double[] calculateIdealWeightRange(double heightCm) {
        double heightM = heightCm / 100.0;
        double minWeight = 18.5 * heightM * heightM;
        double maxWeight = 24.9 * heightM * heightM;
        return new double[]{minWeight, maxWeight};
    }

    /**
     * Estimate time to reach target weight (weeks)
     * Assumes safe weight change of 0.5kg per week
     */
    public int estimateWeeksToGoal(double currentWeight, double targetWeight) {
        double difference = Math.abs(targetWeight - currentWeight);
        return (int) Math.ceil(difference / 0.5);
    }
}