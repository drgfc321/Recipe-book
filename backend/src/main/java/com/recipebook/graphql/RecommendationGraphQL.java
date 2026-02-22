package com.recipebook.graphql;

import com.recipebook.dto.RecipeRecommendationResponse;
import com.recipebook.dto.ShoppingListResponse;
import com.recipebook.entity.Recipe;
import com.recipebook.entity.RecipeIngredient;
import com.recipebook.entity.PantryItem;
import com.recipebook.entity.ShoppingListItem;
import com.recipebook.entity.Unit;
import com.recipebook.entity.User;
import com.recipebook.service.MacroCalculationService;
import com.recipebook.service.RecommendationService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@GraphQLApi
public class RecommendationGraphQL {

    @Inject
    JsonWebToken jwt;

    @Inject
    RecommendationService recommendationService;

    @Query("recipeRecommendations")
    @Description("Get recipe recommendations ranked by pantry ingredient match")
    @Authenticated
    public List<RecipeRecommendationResponse> getRecipeRecommendations(@Name("filter") RecommendationFilterInput filter) {
        Long userId = Long.parseLong(jwt.getSubject());
        return recommendationService.getRecommendations(userId, filter);
    }

    @Mutation("addMissingToShoppingList")
    @Description("Add missing ingredients for a recipe to the shopping list")
    @Authenticated
    @Transactional
    public boolean addMissingToShoppingList(@Name("recipeId") Long recipeId, @Name("weekStart") LocalDate weekStart) throws GraphQLException {
        Long userId = Long.parseLong(jwt.getSubject());

        Recipe recipe = Recipe.findById(recipeId);
        if (recipe == null) {
            throw new GraphQLException("Recipe not found");
        }

        User user = User.findById(userId);

        // Get pantry
        List<PantryItem> pantryItems = PantryItem.list("user.id", userId);
        Map<Long, Double> pantry = new HashMap<>();
        for (PantryItem pi : pantryItems) {
            pantry.put(pi.ingredient.id, MacroCalculationService.toGrams(pi.quantity, pi.unit));
        }

        // Add missing ingredients to shopping list
        for (RecipeIngredient ri : recipe.ingredients) {
            double requiredGrams = MacroCalculationService.toGrams(ri.quantity, ri.unit);
            double pantryGrams = pantry.getOrDefault(ri.ingredient.id, 0.0);
            double deficit = requiredGrams - pantryGrams;

            if (deficit > 0) {
                ShoppingListItem item = new ShoppingListItem();
                item.user = user;
                item.ingredient = ri.ingredient;
                item.ingredientName = ri.ingredient.name;
                item.quantity = deficit;
                item.unit = Unit.GRAMS;
                item.weekStartDate = weekStart;
                item.purchased = false;
                item.persist();
            }
        }

        return true;
    }
}
