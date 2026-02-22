package com.recipebook.dto;

import com.recipebook.entity.IngredientCategory;
import com.recipebook.entity.Unit;

public class MissingIngredientResponse {
    public Long ingredientId;
    public String ingredientName;
    public IngredientCategory ingredientCategory;
    public double requiredQuantity;
    public double pantryQuantity;
    public double neededQuantity;
    public Unit unit;
}
