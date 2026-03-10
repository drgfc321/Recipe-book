package com.recipebook.mealplan;

import com.recipebook.recipe.MacroInfo;

import java.util.List;

public record DailyMealPlanResponse(
        String date,
        List<MealPlanResponse> meals,
        MacroInfo totalMacros,
        String dayType
) {
}
