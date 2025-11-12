package com.example.healthmonitoring.activity.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.healthmonitoring.activity.entity.ActivityLog;
import com.example.healthmonitoring.activity.repo.ActivityLogRepository;
import com.example.healthmonitoring.activity.service.ActivityService;

@RestController
@RequestMapping("/api/v1/activity")
public class ActivityController {

    public static class CreateReq {
        public String name; public double met; public int minutes; public double weightKg;
    }
    public static class TodayOutRes { public int totalKcal; public TodayOutRes(int v){ totalKcal=v; } }

    private final ActivityLogRepository repo;
    private final ActivityService svc;

    public ActivityController(ActivityLogRepository repo, ActivityService svc){
        this.repo = repo; this.svc = svc;
    }
    private String uid(Authentication a){ return a.getName(); }

    @PostMapping
    public ActivityLog add(@RequestBody CreateReq req, Authentication a){
        if (req.met <= 0 || req.minutes <= 0 || req.weightKg <= 0) throw new IllegalArgumentException("invalid input");
        var log = new ActivityLog();
        log.setUserId(uid(a));
        log.setName(req.name);
        log.setMet(req.met);
        log.setMinutes(req.minutes);
        log.setWeightKgAtTime(req.weightKg);
        log.setTotalKcal(svc.calcKcal(req.met, req.weightKg, req.minutes));
        return repo.save(log);
    }

    @GetMapping("/today/total")
    public TodayOutRes todayTotal(Authentication a){
        var d = LocalDate.now();
        var s = d.atStartOfDay();
        var e = d.plusDays(1).atStartOfDay().minusNanos(1);
        return new TodayOutRes(repo.sumKcalByUserAndRange(uid(a), s, e));
    }

    @GetMapping("/history")
    public List<ActivityLog> history(
        Authentication a,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to){
        var s = from.atStartOfDay();
        var e = to.plusDays(1).atStartOfDay().minusNanos(1);
        return repo.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(uid(a), s, e);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication a){
        var m = repo.findById(id).orElseThrow();
        if (!m.getUserId().equals(uid(a))) throw new RuntimeException("forbidden");
        repo.deleteById(id);
    }
}
