package com.recipebook.dto;

import java.time.LocalDate;
import java.util.List;

public class WeeklyMealPlanResponse {
    public LocalDate weekStart;
    public LocalDate weekEnd;
    public List<DailyMealPlanResponse> days;
    public MacroInfo totalMacros;
    public MacroInfo averageDailyMacros;
}
