package com.recipebook.nutritionhistory;

public record DailyNutritionSummary(
        String date,
        double totalCalories, double totalProtein, double totalCarbs, double totalFat,
        double targetCalories, double targetProtein, double targetCarbs, double targetFat,
        String dayType
) {}
