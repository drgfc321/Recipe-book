package com.recipebook.ingredient;

public record IngredientResponse(
        Long id,
        String name,
        String category,
        double caloriesPer100g,
        double proteinPer100g,
        double carbsPer100g,
        double fatPer100g
) {
}
