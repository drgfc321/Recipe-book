package com.recipebook.dto;

public class UserNutritionTargetResponse {
    public double calories;
    public double protein;
    public double carbs;
    public double fat;
    public String dayType;

    public UserNutritionTargetResponse() {}

    public UserNutritionTargetResponse(double calories, double protein, double carbs, double fat) {
        this(calories, protein, carbs, fat, "DEFAULT");
    }

    public UserNutritionTargetResponse(double calories, double protein, double carbs, double fat, String dayType) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
        this.dayType = dayType;
    }
}
