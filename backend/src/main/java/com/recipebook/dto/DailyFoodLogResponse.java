package com.recipebook.dto;

import java.time.LocalDate;
import java.util.List;

public class DailyFoodLogResponse {
    public LocalDate date;
    public List<FoodLogResponse> entries;
    public MacroInfo totalActualMacros;
    public UserNutritionTargetResponse targets;
    public List<PlannedMealStatus> plannedMeals;
}
