package com.recipebook.service;

import com.recipebook.dto.MacroInfo;
import com.recipebook.dto.MissingIngredientResponse;
import com.recipebook.dto.RecipeRecommendationResponse;
import com.recipebook.dto.RecipeResponse;
import com.recipebook.entity.Difficulty;
import com.recipebook.entity.Ingredient;
import com.recipebook.entity.IngredientCategory;
import com.recipebook.entity.Recipe;
import com.recipebook.entity.RecipeCategory;
import com.recipebook.entity.RecipeIngredient;
import com.recipebook.entity.Unit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    MacroCalculationService macroService;

    @InjectMocks
    RecommendationService recommendationService;

    @Test
    void analyzeRecipe_allIngredientsInPantry_100percent() {
        Recipe recipe = createRecipe();
        addIngredient(recipe, 1L, "Chicken", 200, Unit.GRAMS);
        addIngredient(recipe, 2L, "Rice", 150, Unit.GRAMS);

        // Pantry has enough of both (in grams)
        Map<Long, Double> pantry = Map.of(1L, 200.0, 2L, 150.0);

        when(macroService.toResponse(any(Recipe.class))).thenReturn(new RecipeResponse());

        RecipeRecommendationResponse result = recommendationService.analyzeRecipe(recipe, pantry);

        assertEquals(100.0, result.matchPercent, 0.01);
        assertEquals(0, result.missingCount);
        assertEquals(2, result.matchedIngredients);
        assertTrue(result.missingIngredients.isEmpty());
    }

    @Test
    void analyzeRecipe_noIngredientsInPantry_0percent() {
        Recipe recipe = createRecipe();
        addIngredient(recipe, 1L, "Chicken", 200, Unit.GRAMS);
        addIngredient(recipe, 2L, "Rice", 150, Unit.GRAMS);

        Map<Long, Double> pantry = new HashMap<>();

        when(macroService.toResponse(any(Recipe.class))).thenReturn(new RecipeResponse());

        RecipeRecommendationResponse result = recommendationService.analyzeRecipe(recipe, pantry);

        assertEquals(0.0, result.matchPercent, 0.01);
        assertEquals(2, result.missingCount);
        assertEquals(0, result.matchedIngredients);
        assertEquals(2, result.missingIngredients.size());
    }

    @Test
    void analyzeRecipe_partialMatch() {
        Recipe recipe = createRecipe();
        addIngredient(recipe, 1L, "Chicken", 200, Unit.GRAMS);
        addIngredient(recipe, 2L, "Rice", 150, Unit.GRAMS);
        addIngredient(recipe, 3L, "Salt", 5, Unit.GRAMS);
        addIngredient(recipe, 4L, "Pepper", 3, Unit.GRAMS);

        // Pantry has enough chicken and salt, but no rice or pepper
        Map<Long, Double> pantry = Map.of(1L, 200.0, 3L, 5.0);

        when(macroService.toResponse(any(Recipe.class))).thenReturn(new RecipeResponse());

        RecipeRecommendationResponse result = recommendationService.analyzeRecipe(recipe, pantry);

        assertEquals(50.0, result.matchPercent, 0.01);
        assertEquals(2, result.missingCount);
        assertEquals(2, result.matchedIngredients);
    }

    @Test
    void analyzeRecipe_emptyIngredients() {
        Recipe recipe = createRecipe();
        // No ingredients added

        Map<Long, Double> pantry = Map.of(1L, 100.0);

        when(macroService.toResponse(any(Recipe.class))).thenReturn(new RecipeResponse());

        RecipeRecommendationResponse result = recommendationService.analyzeRecipe(recipe, pantry);

        assertEquals(0.0, result.matchPercent, 0.01);
        assertEquals(0, result.missingCount);
        assertEquals(0, result.totalIngredients);
    }

    @Test
    void analyzeRecipe_partialPantryQuantity() {
        Recipe recipe = createRecipe();
        addIngredient(recipe, 1L, "Chicken", 100, Unit.GRAMS);

        // Pantry has only 50g, recipe needs 100g
        Map<Long, Double> pantry = Map.of(1L, 50.0);

        when(macroService.toResponse(any(Recipe.class))).thenReturn(new RecipeResponse());

        RecipeRecommendationResponse result = recommendationService.analyzeRecipe(recipe, pantry);

        assertEquals(0.0, result.matchPercent, 0.01);
        assertEquals(1, result.missingCount);
        MissingIngredientResponse missing = result.missingIngredients.get(0);
        assertEquals(50.0, missing.neededQuantity, 0.01);
        assertEquals(100.0, missing.requiredQuantity, 0.01);
        assertEquals(50.0, missing.pantryQuantity, 0.01);
    }

    @Test
    void analyzeRecipe_missingIngredientDetails() {
        Recipe recipe = createRecipe();
        addIngredient(recipe, 42L, "Tomato", 300, Unit.GRAMS);

        Map<Long, Double> pantry = new HashMap<>();

        when(macroService.toResponse(any(Recipe.class))).thenReturn(new RecipeResponse());

        RecipeRecommendationResponse result = recommendationService.analyzeRecipe(recipe, pantry);

        assertEquals(1, result.missingIngredients.size());
        MissingIngredientResponse missing = result.missingIngredients.get(0);
        assertEquals(42L, missing.ingredientId);
        assertEquals("Tomato", missing.ingredientName);
        assertEquals(IngredientCategory.VEGETABLES, missing.ingredientCategory);
        assertEquals(300.0, missing.requiredQuantity, 0.01);
        assertEquals(0.0, missing.pantryQuantity, 0.01);
        assertEquals(300.0, missing.neededQuantity, 0.01);
        assertEquals(Unit.GRAMS, missing.unit);
    }

    @Test
    void analyzeRecipe_unitConversion() {
        Recipe recipe = createRecipe();
        // Recipe needs 1 cup = 240g
        addIngredient(recipe, 1L, "Flour", 1, Unit.CUPS);

        // Pantry has 240g (exactly enough)
        Map<Long, Double> pantry = Map.of(1L, 240.0);

        when(macroService.toResponse(any(Recipe.class))).thenReturn(new RecipeResponse());

        RecipeRecommendationResponse result = recommendationService.analyzeRecipe(recipe, pantry);

        assertEquals(100.0, result.matchPercent, 0.01);
        assertEquals(0, result.missingCount);
    }

    // --- helpers ---

    private Recipe createRecipe() {
        Recipe recipe = new Recipe();
        recipe.id = 1L;
        recipe.name = "Test Recipe";
        recipe.category = RecipeCategory.DINNER;
        recipe.difficulty = Difficulty.EASY;
        recipe.servings = 2;
        recipe.ingredients = new ArrayList<>();
        return recipe;
    }

    private void addIngredient(Recipe recipe, Long ingredientId, String name, double quantity, Unit unit) {
        Ingredient ingredient = new Ingredient();
        ingredient.id = ingredientId;
        ingredient.name = name;
        ingredient.category = IngredientCategory.VEGETABLES;
        RecipeIngredient ri = new RecipeIngredient();
        ri.ingredient = ingredient;
        ri.quantity = quantity;
        ri.unit = unit;
        recipe.ingredients.add(ri);
    }
}
