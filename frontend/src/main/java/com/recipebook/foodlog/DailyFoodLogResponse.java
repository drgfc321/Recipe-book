package com.recipebook.foodlog;

import com.recipebook.recipe.MacroInfo;

import java.util.List;

public record DailyFoodLogResponse(
        String date,
        List<FoodLogResponse> entries,
        MacroInfo totalActualMacros,
        UserNutritionTargetResponse targets,
        List<PlannedMealStatus> plannedMeals
) {
}
