package com.recipebook.dto;

import com.recipebook.entity.Difficulty;
import com.recipebook.entity.RecipeCategory;

import java.time.LocalDateTime;
import java.util.List;

public class RecipeResponse {
    public Long id;
    public String name;
    public String description;
    public RecipeCategory category;
    public Difficulty difficulty;
    public int prepTime;
    public int cookTime;
    public int servings;
    public String instructions;
    public String imageUrl;
    public Long ownerId;
    public String ownerUsername;
    public List<RecipeIngredientResponse> ingredients;
    public MacroInfo totalMacros;
    public MacroInfo perServingMacros;
    public LocalDateTime createdAt;
}
