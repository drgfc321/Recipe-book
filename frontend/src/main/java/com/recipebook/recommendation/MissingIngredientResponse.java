package com.recipebook.recommendation;

public record MissingIngredientResponse(
        Long ingredientId,
        String ingredientName,
        String ingredientCategory,
        double requiredQuantity,
        double pantryQuantity,
        double neededQuantity,
        String unit
) {
}
