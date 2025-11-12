package com.example.healthmonitoring.meal.dto;

import java.time.LocalDate;

public class MealDtos {
    public static class CreateReq {
        public Long foodId;     // hoặc name
        public double servings; // >0
    }
    public static class RangeQuery {
        public LocalDate from;
        public LocalDate to;
    }
    public static class TodayKcalRes {
        public int totalKcal;
        public TodayKcalRes(int v){ totalKcal = v; }
    }
}