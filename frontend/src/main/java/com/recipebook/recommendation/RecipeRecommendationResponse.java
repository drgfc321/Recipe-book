package com.recipebook.recommendation;

import com.recipebook.recipe.RecipeResponse;

import java.util.List;

public record RecipeRecommendationResponse(
        RecipeResponse recipe,
        double matchPercent,
        int totalIngredients,
        int matchedIngredients,
        int missingCount,
        List<MissingIngredientResponse> missingIngredients
) {
}
