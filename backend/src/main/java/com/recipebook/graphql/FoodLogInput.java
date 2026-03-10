package com.recipebook.graphql;

import com.recipebook.entity.MealSlot;
import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.NonNull;

import java.time.LocalDate;

@Input("FoodLogInput")
public class FoodLogInput {

    @NonNull
    public LocalDate date;

    @NonNull
    public MealSlot mealSlot;

    public Long recipeId;

    public Long ingredientId;

    public Double ingredientQuantity;

    public String customName;

    public Double customCalories;

    public Double customProtein;

    public Double customCarbs;

    public Double customFat;

    public Long mealPlanId;

    public Double servings;
}
