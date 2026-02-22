package com.recipebook.dto;

import com.recipebook.entity.MealSlot;

import java.time.LocalDate;

public class MealPlanResponse {
    public Long id;
    public LocalDate date;
    public MealSlot mealSlot;
    public RecipeResponse recipe;
}
