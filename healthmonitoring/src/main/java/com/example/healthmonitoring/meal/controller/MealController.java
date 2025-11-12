package com.example.healthmonitoring.meal.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.healthmonitoring.meal.entity.MealLog;
import com.example.healthmonitoring.meal.repo.FoodRepository;
import com.example.healthmonitoring.meal.repo.MealLogRepository;

@RestController
@RequestMapping("/api/v1/meal")
public class MealController {

    public static class CreateReq { public Long foodId; public double servings; }
    public static class TodayKcalRes { public int totalKcal; public TodayKcalRes(int v){ totalKcal=v; } }

    private final MealLogRepository mealRepo;
    private final FoodRepository foodRepo;

    public MealController(MealLogRepository mealRepo, FoodRepository foodRepo) {
        this.mealRepo = mealRepo; this.foodRepo = foodRepo;
    }
    private String uid(Authentication a){ return a.getName(); }

    @PostMapping
    public MealLog add(@RequestBody CreateReq req, Authentication a){
        if (req == null || req.servings <= 0) throw new IllegalArgumentException("servings must be > 0");
        var food = foodRepo.findById(req.foodId).orElseThrow();
        var log = new MealLog();
        log.setUserId(uid(a));
        log.setFood(food);
        log.setServings(req.servings);
        log.setTotalKcal((int)Math.round(food.getKcalPerServing() * req.servings));
        return mealRepo.save(log);
    }

    @GetMapping("/today/total")
    public TodayKcalRes todayTotal(Authentication a){
        var d = LocalDate.now();
        var s = d.atStartOfDay();
        var e = d.plusDays(1).atStartOfDay().minusNanos(1);
        int total = mealRepo.sumKcalByUserAndRange(uid(a), s, e);
        return new TodayKcalRes(total);
    }

    @GetMapping("/history")
    public List<MealLog> history(
            Authentication a,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to){
        var s = from.atStartOfDay();
        var e = to.plusDays(1).atStartOfDay().minusNanos(1);
        return mealRepo.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(uid(a), s, e);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication a){
        var m = mealRepo.findById(id).orElseThrow();
        if (!m.getUserId().equals(uid(a))) throw new RuntimeException("forbidden");
        mealRepo.deleteById(id);
    }
}
