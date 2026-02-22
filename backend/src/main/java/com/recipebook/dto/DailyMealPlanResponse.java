package com.recipebook.dto;

import java.time.LocalDate;
import java.util.List;

public class DailyMealPlanResponse {
    public LocalDate date;
    public List<MealPlanResponse> meals;
    public MacroInfo totalMacros;
}
