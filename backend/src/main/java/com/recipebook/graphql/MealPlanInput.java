package com.recipebook.graphql;

import com.recipebook.entity.MealSlot;
import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.NonNull;

import java.time.LocalDate;

@Input("MealPlanInput")
public class MealPlanInput {

    @NonNull
    public LocalDate date;

    @NonNull
    public MealSlot mealSlot;

    @NonNull
    public Long recipeId;
}
