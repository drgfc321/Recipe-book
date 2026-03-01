package com.recipebook.foodlog;

import com.recipebook.recipe.RecipeResponse;

public record PlannedMealStatus(
        Long mealPlanId,
        String mealSlot,
        RecipeResponse recipe,
        boolean logged,
        Long foodLogId
) {
}
