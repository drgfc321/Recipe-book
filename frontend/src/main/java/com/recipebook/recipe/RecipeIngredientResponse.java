package com.recipebook.recipe;

public record RecipeIngredientResponse(
        Long ingredientId,
        String ingredientName,
        String ingredientCategory,
        double quantity,
        String unit,
        MacroInfo macros
) {
}
