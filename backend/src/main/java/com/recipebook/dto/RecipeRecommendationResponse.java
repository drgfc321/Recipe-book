package com.recipebook.dto;

import java.util.List;

public class RecipeRecommendationResponse {
    public RecipeResponse recipe;
    public double matchPercent;
    public int totalIngredients;
    public int matchedIngredients;
    public int missingCount;
    public List<MissingIngredientResponse> missingIngredients;
}
