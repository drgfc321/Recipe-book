package com.recipebook.foodlog;

import com.recipebook.recipe.MacroInfo;
import com.recipebook.recipe.RecipeResponse;

public record FoodLogResponse(
        Long id,
        String date,
        String mealSlot,
        double servings,
        String loggedAt,
        FoodSourceType sourceType,
        RecipeResponse recipe,
        String ingredientName,
        Double ingredientQuantity,
        String customName,
        MacroInfo actualMacros
) {
}
