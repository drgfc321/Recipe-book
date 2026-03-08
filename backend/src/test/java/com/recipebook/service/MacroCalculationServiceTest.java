package com.recipebook.service;

import com.recipebook.dto.MacroInfo;
import com.recipebook.dto.RecipeResponse;
import com.recipebook.entity.Difficulty;
import com.recipebook.entity.Ingredient;
import com.recipebook.entity.IngredientCategory;
import com.recipebook.entity.Recipe;
import com.recipebook.entity.RecipeCategory;
import com.recipebook.entity.RecipeIngredient;
import com.recipebook.entity.Unit;
import com.recipebook.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MacroCalculationServiceTest {

    private MacroCalculationService service;

    @BeforeEach
    void setUp() {
        service = new MacroCalculationService();
    }

    // --- toGrams tests ---

    @Test
    void toGrams_grams() {
        assertEquals(200.0, MacroCalculationService.toGrams(200, Unit.GRAMS));
    }

    @Test
    void toGrams_kilograms() {
        assertEquals(2000.0, MacroCalculationService.toGrams(2, Unit.KILOGRAMS));
    }

    @Test
    void toGrams_liters() {
        assertEquals(500.0, MacroCalculationService.toGrams(0.5, Unit.LITERS));
    }

    @Test
    void toGrams_milliliters() {
        assertEquals(100.0, MacroCalculationService.toGrams(100, Unit.MILLILITERS));
    }

    @Test
    void toGrams_tablespoons() {
        assertEquals(45.0, MacroCalculationService.toGrams(3, Unit.TABLESPOONS));
    }

    @Test
    void toGrams_teaspoons() {
        assertEquals(10.0, MacroCalculationService.toGrams(2, Unit.TEASPOONS));
    }

    @Test
    void toGrams_cups() {
        assertEquals(240.0, MacroCalculationService.toGrams(1, Unit.CUPS));
    }

    @Test
    void toGrams_pieces() {
        assertEquals(200.0, MacroCalculationService.toGrams(2, Unit.PIECES));
    }

    @Test
    void toGrams_zeroQuantity() {
        assertEquals(0.0, MacroCalculationService.toGrams(0, Unit.GRAMS));
        assertEquals(0.0, MacroCalculationService.toGrams(0, Unit.CUPS));
    }

    // --- toResponse tests ---

    @Test
    void toResponse_singleIngredient() {
        Recipe recipe = createRecipe(4);
        Ingredient chicken = createIngredient(1L, "Chicken", IngredientCategory.MEAT, 165, 31, 0, 3.6);
        recipe.ingredients.add(createRecipeIngredient(chicken, 200, Unit.GRAMS));

        RecipeResponse response = service.toResponse(recipe);

        // factor = 200 * 1.0 / 100 = 2.0
        assertEquals(330.0, response.totalMacros.calories, 0.01);
        assertEquals(62.0, response.totalMacros.protein, 0.01);
        assertEquals(0.0, response.totalMacros.carbs, 0.01);
        assertEquals(7.2, response.totalMacros.fat, 0.01);
        assertEquals(1, response.ingredients.size());
    }

    @Test
    void toResponse_multipleIngredients() {
        Recipe recipe = createRecipe(2);
        Ingredient chicken = createIngredient(1L, "Chicken", IngredientCategory.MEAT, 165, 31, 0, 3.6);
        Ingredient rice = createIngredient(2L, "Rice", IngredientCategory.GRAINS, 130, 2.7, 28, 0.3);
        recipe.ingredients.add(createRecipeIngredient(chicken, 200, Unit.GRAMS));
        recipe.ingredients.add(createRecipeIngredient(rice, 150, Unit.GRAMS));

        RecipeResponse response = service.toResponse(recipe);

        // chicken: factor=2.0 → cal=330, pro=62, carb=0, fat=7.2
        // rice: factor=1.5 → cal=195, pro=4.05, carb=42, fat=0.45
        assertEquals(525.0, response.totalMacros.calories, 0.01);
        assertEquals(66.05, response.totalMacros.protein, 0.01);
        assertEquals(42.0, response.totalMacros.carbs, 0.01);
        assertEquals(7.65, response.totalMacros.fat, 0.01);
    }

    @Test
    void toResponse_perServingDivision() {
        Recipe recipe = createRecipe(4);
        Ingredient chicken = createIngredient(1L, "Chicken", IngredientCategory.MEAT, 200, 40, 0, 4);
        recipe.ingredients.add(createRecipeIngredient(chicken, 400, Unit.GRAMS));

        RecipeResponse response = service.toResponse(recipe);

        // total: factor=4.0 → cal=800, pro=160, carb=0, fat=16
        // per serving (/4): cal=200, pro=40, carb=0, fat=4
        assertEquals(200.0, response.perServingMacros.calories, 0.01);
        assertEquals(40.0, response.perServingMacros.protein, 0.01);
        assertEquals(0.0, response.perServingMacros.carbs, 0.01);
        assertEquals(4.0, response.perServingMacros.fat, 0.01);
    }

    @Test
    void toResponse_zeroServings() {
        Recipe recipe = createRecipe(0);
        Ingredient chicken = createIngredient(1L, "Chicken", IngredientCategory.MEAT, 100, 20, 0, 2);
        recipe.ingredients.add(createRecipeIngredient(chicken, 100, Unit.GRAMS));

        RecipeResponse response = service.toResponse(recipe);

        // servings=0 → falls back to dividing by 1
        assertEquals(response.totalMacros.calories, response.perServingMacros.calories, 0.01);
        assertEquals(response.totalMacros.protein, response.perServingMacros.protein, 0.01);
    }

    @Test
    void toResponse_emptyIngredients() {
        Recipe recipe = createRecipe(2);

        RecipeResponse response = service.toResponse(recipe);

        assertEquals(0.0, response.totalMacros.calories, 0.01);
        assertEquals(0.0, response.totalMacros.protein, 0.01);
        assertEquals(0.0, response.totalMacros.carbs, 0.01);
        assertEquals(0.0, response.totalMacros.fat, 0.01);
        assertTrue(response.ingredients.isEmpty());
    }

    @Test
    void toResponse_nullOwner() {
        Recipe recipe = createRecipe(1);
        recipe.owner = null;

        RecipeResponse response = service.toResponse(recipe);

        assertNull(response.ownerId);
        assertNull(response.ownerUsername);
    }

    // --- helpers ---

    private Recipe createRecipe(int servings) {
        Recipe recipe = new Recipe();
        recipe.id = 1L;
        recipe.name = "Test Recipe";
        recipe.description = "A test recipe";
        recipe.category = RecipeCategory.DINNER;
        recipe.difficulty = Difficulty.EASY;
        recipe.prepTime = 10;
        recipe.cookTime = 20;
        recipe.servings = servings;
        recipe.instructions = "Cook it";
        recipe.ingredients = new ArrayList<>();
        User owner = new User();
        owner.id = 100L;
        owner.username = "testuser";
        recipe.owner = owner;
        return recipe;
    }

    private Ingredient createIngredient(Long id, String name, IngredientCategory category,
                                        double cal, double protein, double carbs, double fat) {
        Ingredient ingredient = new Ingredient();
        ingredient.id = id;
        ingredient.name = name;
        ingredient.category = category;
        ingredient.caloriesPer100g = cal;
        ingredient.proteinPer100g = protein;
        ingredient.carbsPer100g = carbs;
        ingredient.fatPer100g = fat;
        return ingredient;
    }

    private RecipeIngredient createRecipeIngredient(Ingredient ingredient, double quantity, Unit unit) {
        RecipeIngredient ri = new RecipeIngredient();
        ri.ingredient = ingredient;
        ri.quantity = quantity;
        ri.unit = unit;
        return ri;
    }
}
