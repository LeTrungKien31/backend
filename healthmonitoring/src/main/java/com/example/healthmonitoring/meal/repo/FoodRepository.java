package com.example.healthmonitoring.meal.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.healthmonitoring.meal.entity.Food;

public interface FoodRepository extends JpaRepository<Food, Long> {
    Optional<Food> findByNameIgnoreCase(String name);
}
