package com.recipebook.service;

import com.recipebook.dto.MissingIngredientResponse;
import com.recipebook.dto.RecipeRecommendationResponse;
import com.recipebook.entity.Ingredient;
import com.recipebook.entity.PantryItem;
import com.recipebook.entity.Recipe;
import com.recipebook.entity.RecipeIngredient;
import com.recipebook.entity.Unit;
import com.recipebook.graphql.RecommendationFilterInput;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class RecommendationService {

    private static final Logger LOG = Logger.getLogger(RecommendationService.class);

    @Inject
    MacroCalculationService macroService;

    @CacheResult(cacheName = "recommendations-cache")
    public List<RecipeRecommendationResponse> getRecommendations(Long userId, RecommendationFilterInput filter) {
        // Get pantry as map of ingredientId -> grams
        List<PantryItem> pantryItems = PantryItem.find(
                "FROM PantryItem pi JOIN FETCH pi.ingredient WHERE pi.user.id = ?1", userId).list();
        LOG.debugf("Loaded %d pantry items for userId=%d", pantryItems.size(), userId);
        Map<Long, Double> pantry = new HashMap<>();
        for (PantryItem pi : pantryItems) {
            pantry.put(pi.ingredient.id, MacroCalculationService.toGrams(pi.quantity, pi.unit));
        }

        // Get recipes with optional filters
        StringBuilder query = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();
        if (filter != null) {
            if (filter.category != null) {
                query.append(" and r.category = :category");
                params.put("category", filter.category);
            }
            if (filter.difficulty != null) {
                query.append(" and r.difficulty = :difficulty");
                params.put("difficulty", filter.difficulty);
            }
        }

        List<Recipe> recipes = Recipe.listWithDetails(query.toString(), params);
        LOG.debugf("Evaluating %d recipes for recommendations", recipes.size());

        List<RecipeRecommendationResponse> recommendations = new ArrayList<>();
        for (Recipe recipe : recipes) {
            RecipeRecommendationResponse rec = analyzeRecipe(recipe, pantry);
            if (filter != null && filter.maxMissingIngredients != null && rec.missingCount > filter.maxMissingIngredients) {
                continue;
            }
            recommendations.add(rec);
        }

        recommendations.sort(Comparator.comparingDouble((RecipeRecommendationResponse r) -> r.matchPercent).reversed());
        LOG.debugf("Returning %d recommendations", recommendations.size());
        return recommendations;
    }

    public List<MissingIngredientResponse> getMissingIngredients(Long userId, Long recipeId) {
        Recipe recipe = Recipe.findByIdWithDetails(recipeId);
        if (recipe == null) {
            return List.of();
        }

        List<PantryItem> pantryItems = PantryItem.find(
                "FROM PantryItem pi JOIN FETCH pi.ingredient WHERE pi.user.id = ?1", userId).list();
        Map<Long, Double> pantry = new HashMap<>();
        for (PantryItem pi : pantryItems) {
            pantry.put(pi.ingredient.id, MacroCalculationService.toGrams(pi.quantity, pi.unit));
        }

        RecipeRecommendationResponse rec = analyzeRecipe(recipe, pantry);
        return rec.missingIngredients;
    }

    // package-private for testing
    RecipeRecommendationResponse analyzeRecipe(Recipe recipe, Map<Long, Double> pantry) {
        RecipeRecommendationResponse rec = new RecipeRecommendationResponse();
        rec.recipe = macroService.toResponse(recipe);
        rec.totalIngredients = recipe.ingredients.size();
        rec.missingIngredients = new ArrayList<>();

        int matched = 0;
        for (RecipeIngredient ri : recipe.ingredients) {
            double requiredGrams = MacroCalculationService.toGrams(ri.quantity, ri.unit);
            double pantryGrams = pantry.getOrDefault(ri.ingredient.id, 0.0);

            if (pantryGrams >= requiredGrams) {
                matched++;
            } else {
                MissingIngredientResponse missing = new MissingIngredientResponse();
                missing.ingredientId = ri.ingredient.id;
                missing.ingredientName = ri.ingredient.name;
                missing.ingredientCategory = ri.ingredient.category;
                missing.requiredQuantity = requiredGrams;
                missing.pantryQuantity = pantryGrams;
                missing.neededQuantity = requiredGrams - pantryGrams;
                missing.unit = Unit.GRAMS;
                rec.missingIngredients.add(missing);
            }
        }

        rec.matchedIngredients = matched;
        rec.missingCount = rec.totalIngredients - matched;
        rec.matchPercent = rec.totalIngredients > 0 ? (matched * 100.0 / rec.totalIngredients) : 0;
        return rec;
    }
}
