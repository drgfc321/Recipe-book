package com.recipebook.mealplan;

import com.recipebook.recipe.MacroInfo;

import java.util.List;

public record WeeklyMealPlanResponse(
        String weekStart,
        String weekEnd,
        List<DailyMealPlanResponse> days,
        MacroInfo totalMacros,
        MacroInfo averageDailyMacros
) {
}
