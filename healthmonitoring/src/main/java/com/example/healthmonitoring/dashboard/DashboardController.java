package com.example.healthmonitoring.dashboard;

import java.time.LocalDate;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.healthmonitoring.water.repo.WaterLogRepository;
import com.example.healthmonitoring.meal.repo.MealLogRepository;
import com.example.healthmonitoring.activity.repo.ActivityLogRepository;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    public static class TodayRes {
        public int waterMl;
        public int kcalIn;
        public int kcalOut;
        public int kcalNet;
        public TodayRes(int waterMl, int in, int out) {
            this.waterMl = waterMl;
            this.kcalIn = in;
            this.kcalOut = out;
            this.kcalNet = in - out;
        }
    }

    private final WaterLogRepository waterRepo;
    private final MealLogRepository mealRepo;
    private final ActivityLogRepository actRepo;

    public DashboardController(WaterLogRepository w, MealLogRepository m, ActivityLogRepository a) {
        this.waterRepo = w;
        this.mealRepo = m;
        this.actRepo = a;
    }

    private String uid(Authentication a) { return a.getName(); }

    @GetMapping("/today")
    public TodayRes today(Authentication a) {
        var d = LocalDate.now();
        var s = d.atStartOfDay();
        var e = d.plusDays(1).atStartOfDay().minusNanos(1);

        var u = uid(a);
        int water = Math.toIntExact(waterRepo.sumAmountByUserAndRange(u, s, e));
        int inKcal = mealRepo.sumKcalByUserAndRange(u, s, e);
        int outKcal = actRepo.sumKcalByUserAndRange(u, s, e);

        return new TodayRes(water, inKcal, outKcal);
    }
}
