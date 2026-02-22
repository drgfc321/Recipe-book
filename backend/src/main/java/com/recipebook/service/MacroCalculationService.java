package com.recipebook.service;

import com.recipebook.dto.MacroInfo;
import com.recipebook.dto.RecipeIngredientResponse;
import com.recipebook.dto.RecipeResponse;
import com.recipebook.entity.Recipe;
import com.recipebook.entity.RecipeIngredient;
import com.recipebook.entity.Unit;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Map;

@ApplicationScoped
public class MacroCalculationService {

    private static final Map<Unit, Double> UNIT_TO_GRAMS = Map.of(
            Unit.GRAMS, 1.0,
            Unit.KILOGRAMS, 1000.0,
            Unit.MILLILITERS, 1.0,
            Unit.LITERS, 1000.0,
            Unit.TABLESPOONS, 15.0,
            Unit.TEASPOONS, 5.0,
            Unit.CUPS, 240.0,
            Unit.PIECES, 100.0
    );

    public RecipeResponse toResponse(Recipe recipe) {
        RecipeResponse response = new RecipeResponse();
        response.id = recipe.id;
        response.name = recipe.name;
        response.description = recipe.description;
        response.category = recipe.category;
        response.difficulty = recipe.difficulty;
        response.prepTime = recipe.prepTime;
        response.cookTime = recipe.cookTime;
        response.servings = recipe.servings;
        response.instructions = recipe.instructions;
        response.imageUrl = recipe.imageUrl;
        response.createdAt = recipe.createdAt;

        if (recipe.owner != null) {
            response.ownerId = recipe.owner.id;
            response.ownerUsername = recipe.owner.username;
        }

        double totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFat = 0;

        response.ingredients = new ArrayList<>();
        for (RecipeIngredient ri : recipe.ingredients) {
            RecipeIngredientResponse riResp = new RecipeIngredientResponse();
            riResp.ingredientId = ri.ingredient.id;
            riResp.ingredientName = ri.ingredient.name;
            riResp.ingredientCategory = ri.ingredient.category;
            riResp.quantity = ri.quantity;
            riResp.unit = ri.unit;

            double factor = ri.quantity * UNIT_TO_GRAMS.getOrDefault(ri.unit, 1.0) / 100.0;
            double cal = factor * ri.ingredient.caloriesPer100g;
            double pro = factor * ri.ingredient.proteinPer100g;
            double carb = factor * ri.ingredient.carbsPer100g;
            double fat = factor * ri.ingredient.fatPer100g;

            riResp.macros = new MacroInfo(cal, pro, carb, fat);

            totalCalories += cal;
            totalProtein += pro;
            totalCarbs += carb;
            totalFat += fat;

            response.ingredients.add(riResp);
        }

        response.totalMacros = new MacroInfo(totalCalories, totalProtein, totalCarbs, totalFat);

        int servings = recipe.servings > 0 ? recipe.servings : 1;
        response.perServingMacros = new MacroInfo(
                totalCalories / servings,
                totalProtein / servings,
                totalCarbs / servings,
                totalFat / servings
        );

        return response;
    }
}
