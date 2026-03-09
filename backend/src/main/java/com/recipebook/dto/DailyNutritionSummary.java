package com.recipebook.dto;

import java.time.LocalDate;

public class DailyNutritionSummary {
    public LocalDate date;
    public double totalCalories;
    public double totalProtein;
    public double totalCarbs;
    public double totalFat;
    public double targetCalories;
    public double targetProtein;
    public double targetCarbs;
    public double targetFat;
    public String dayType;

    public DailyNutritionSummary() {}

    public DailyNutritionSummary(LocalDate date,
                                  double totalCalories, double totalProtein, double totalCarbs, double totalFat,
                                  double targetCalories, double targetProtein, double targetCarbs, double targetFat,
                                  String dayType) {
        this.date = date;
        this.totalCalories = totalCalories;
        this.totalProtein = totalProtein;
        this.totalCarbs = totalCarbs;
        this.totalFat = totalFat;
        this.targetCalories = targetCalories;
        this.targetProtein = targetProtein;
        this.targetCarbs = targetCarbs;
        this.targetFat = targetFat;
        this.dayType = dayType;
    }
}
