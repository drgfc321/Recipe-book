package com.recipebook.foodlog;

public record UserNutritionTargetResponse(
        double calories,
        double protein,
        double carbs,
        double fat,
        String dayType
) {
}
