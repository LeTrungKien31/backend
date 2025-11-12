package com.example.healthmonitoring.meal.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.healthmonitoring.meal.entity.Food;
import com.example.healthmonitoring.meal.repo.FoodRepository;

@RestController
@RequestMapping("/api/v1/foods")
public class FoodController {
    private final FoodRepository repo;
    public FoodController(FoodRepository repo){ this.repo = repo; }

    @GetMapping
    public List<Food> list(@RequestParam(required=false) String q){
        if (q == null || q.isBlank()) return repo.findAll();
        final var lower = q.toLowerCase();
        return repo.findAll().stream()
                .filter(f -> f.getName().toLowerCase().contains(lower))
                .toList();
    }
}
