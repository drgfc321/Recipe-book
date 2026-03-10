package com.recipebook.dto;

import com.recipebook.entity.MealSlot;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FoodLogResponse {
    public Long id;
    public LocalDate date;
    public MealSlot mealSlot;
    public double servings;
    public LocalDateTime loggedAt;
    public FoodSourceType sourceType;
    public RecipeResponse recipe;
    public String ingredientName;
    public Double ingredientQuantity;
    public String customName;
    public MacroInfo actualMacros;
}
