package com.recipebook.dto;

public class UserNutritionTargetResponse {
    public double calories;
    public double protein;
    public double carbs;
    public double fat;

    public UserNutritionTargetResponse() {}

    public UserNutritionTargetResponse(double calories, double protein, double carbs, double fat) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }
}
