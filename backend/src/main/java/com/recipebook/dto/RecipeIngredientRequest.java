package com.recipebook.dto;

import com.recipebook.entity.Unit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RecipeIngredientRequest {
    @NotNull
    public Long ingredientId;

    @NotNull
    @Positive
    public Double quantity;

    @NotNull
    public Unit unit;
}
