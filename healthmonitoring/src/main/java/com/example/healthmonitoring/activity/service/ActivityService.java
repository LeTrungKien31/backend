package com.example.healthmonitoring.activity.service;

import org.springframework.stereotype.Service;

@Service
public class ActivityService {
    // kcal per minute ≈ MET * 3.5 * weight(kg) / 200
    public int calcKcal(double met, double weightKg, int minutes){
        double perMin = met * 3.5 * weightKg / 200.0;
        return (int)Math.round(perMin * minutes);
    }
}
