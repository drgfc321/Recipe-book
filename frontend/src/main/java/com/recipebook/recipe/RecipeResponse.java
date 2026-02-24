package com.recipebook.recipe;

import java.util.List;

public record RecipeResponse(
        Long id,
        String name,
        String description,
        String category,
        String difficulty,
        int prepTime,
        int cookTime,
        int servings,
        String instructions,
        String imageUrl,
        Long ownerId,
        String ownerUsername,
        List<RecipeIngredientResponse> ingredients,
        MacroInfo totalMacros,
        MacroInfo perServingMacros,
        String createdAt
) {
}
