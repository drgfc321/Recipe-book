package com.recipebook.service;

import com.recipebook.entity.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class RecipeImportService {

    private static final Logger LOG = Logger.getLogger(RecipeImportService.class);

    public record ImportResult(int imported, int failed, List<String> errors) {}

    @Transactional
    public ImportResult importFromJson(Path filePath, User owner) {
        List<String> errors = new ArrayList<>();
        int imported = 0;
        int failed = 0;

        String jsonText;
        try {
            jsonText = Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new ImportResult(0, 0, List.of("Cannot read file: " + e.getMessage()));
        }

        JsonArray recipes;
        try (JsonReader reader = Json.createReader(new StringReader(jsonText))) {
            recipes = reader.readArray();
        } catch (Exception e) {
            return new ImportResult(0, 0, List.of("Invalid JSON: " + e.getMessage()));
        }

        LOG.infof("Found %d recipes in JSON", recipes.size());

        for (int i = 0; i < recipes.size(); i++) {
            JsonObject recipeJson = recipes.getJsonObject(i);
            String name = recipeJson.getString("name", "Unknown");
            try {
                createRecipeFromJson(recipeJson, owner);
                imported++;
                LOG.infof("Imported: %s", name);
            } catch (Exception e) {
                failed++;
                String err = name + ": " + e.getMessage();
                errors.add(err);
                LOG.warnf("Failed to import %s: %s", name, e.getMessage());
            }
        }

        return new ImportResult(imported, failed, errors);
    }

    private void createRecipeFromJson(JsonObject json, User owner) {
        String name = json.getString("name");

        // Check for duplicate
        long existing = Recipe.count("lower(name) = ?1 and owner = ?2", name.toLowerCase(), owner);
        if (existing > 0) {
            LOG.infof("Skipping duplicate: %s", name);
            return;
        }

        Recipe recipe = new Recipe();
        recipe.name = name;
        recipe.category = RecipeCategory.valueOf(json.getString("category", "OTHER"));
        recipe.difficulty = Difficulty.valueOf(json.getString("difficulty", "EASY"));
        recipe.servings = json.getInt("servings", 1);
        recipe.instructions = json.getString("instructions", "");
        recipe.owner = owner;
        recipe.createdAt = LocalDateTime.now();

        // Store total macros from PDF in description for reference
        int totalCal = json.getInt("totalCalories", 0);
        int protein = json.getInt("protein", 0);
        int carbs = json.getInt("carbs", 0);
        int fat = json.getInt("fat", 0);
        recipe.description = String.format("%d kcal | P: %dg | C: %dg | F: %dg (total reteta)",
                totalCal, protein, carbs, fat);

        recipe.persist();

        // Process ingredients
        JsonArray ingredients = json.getJsonArray("ingredients");
        if (ingredients != null) {
            for (int i = 0; i < ingredients.size(); i++) {
                JsonObject ingJson = ingredients.getJsonObject(i);
                String ingName = ingJson.getString("name");
                double quantity = ingJson.getJsonNumber("quantity").doubleValue();
                Unit unit = Unit.valueOf(ingJson.getString("unit", "GRAMS"));

                Ingredient ingredient = findOrCreateIngredient(ingName);
                if (ingredient != null) {
                    RecipeIngredient ri = new RecipeIngredient();
                    ri.recipe = recipe;
                    ri.ingredient = ingredient;
                    ri.quantity = quantity;
                    ri.unit = unit;
                    ri.persist();
                    recipe.ingredients.add(ri);
                }
            }
        }
    }

    private Ingredient findOrCreateIngredient(String name) {
        String normalized = name.toLowerCase().trim();

        // Try exact match in DB
        Ingredient existing = Ingredient.find("lower(name) = ?1", normalized).firstResult();
        if (existing != null) return existing;

        // Try partial match
        existing = Ingredient.find("lower(name) like ?1", "%" + normalized + "%").firstResult();
        if (existing != null) return existing;

        // Look up nutrition data from our hardcoded map
        RomanianNutritionData.NutrientInfo info = RomanianNutritionData.lookup(normalized);
        if (info == null) {
            LOG.warnf("No nutrition data for: %s — creating with zero values", name);
            info = new RomanianNutritionData.NutrientInfo(0, 0, 0, 0, IngredientCategory.OTHER);
        }

        Ingredient ingredient = new Ingredient();
        ingredient.name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        ingredient.category = info.category();
        ingredient.caloriesPer100g = info.calories();
        ingredient.proteinPer100g = info.protein();
        ingredient.carbsPer100g = info.carbs();
        ingredient.fatPer100g = info.fat();
        ingredient.persist();

        LOG.infof("Created ingredient: %s (%.0f cal/100g)", ingredient.name, ingredient.caloriesPer100g);
        return ingredient;
    }
}
