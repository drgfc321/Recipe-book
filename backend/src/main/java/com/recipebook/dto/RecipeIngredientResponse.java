package com.recipebook.dto;

import com.recipebook.entity.IngredientCategory;
import com.recipebook.entity.Unit;

public class RecipeIngredientResponse {
    public Long ingredientId;
    public String ingredientName;
    public IngredientCategory ingredientCategory;
    public double quantity;
    public Unit unit;
    public MacroInfo macros;
}
