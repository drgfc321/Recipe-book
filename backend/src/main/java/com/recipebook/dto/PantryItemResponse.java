package com.recipebook.dto;

import com.recipebook.entity.IngredientCategory;
import com.recipebook.entity.Unit;

import java.time.LocalDate;

public class PantryItemResponse {
    public Long id;
    public Long ingredientId;
    public String ingredientName;
    public IngredientCategory ingredientCategory;
    public double quantity;
    public Unit unit;
    public LocalDate expirationDate;
    public boolean expiringSoon;
}
