package com.recipebook.mealplan;

import com.recipebook.recipe.RecipeResponse;

public record MealPlanResponse(
        Long id,
        String date,
        String mealSlot,
        RecipeResponse recipe
) {
}
