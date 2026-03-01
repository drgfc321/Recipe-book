package com.recipebook.dto;

import com.recipebook.entity.MealSlot;

public class PlannedMealStatus {
    public Long mealPlanId;
    public MealSlot mealSlot;
    public RecipeResponse recipe;
    public boolean logged;
    public Long foodLogId;
}
