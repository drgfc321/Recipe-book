package com.recipebook.dto;

import com.recipebook.entity.Difficulty;
import com.recipebook.entity.RecipeCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class RecipeRequest {
    @NotBlank
    public String name;

    public String description;

    @NotNull
    public RecipeCategory category;

    @NotNull
    public Difficulty difficulty;

    @Positive
    public int prepTime;

    @Positive
    public int cookTime;

    @Positive
    public int servings;

    public String instructions;

    public String imageUrl;

    @Valid
    public List<RecipeIngredientRequest> ingredients;
}
