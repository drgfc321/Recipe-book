package com.recipebook.service;

import com.recipebook.dto.ShoppingListItemResponse;
import com.recipebook.dto.ShoppingListResponse;
import com.recipebook.entity.Ingredient;
import com.recipebook.entity.MealPlan;
import com.recipebook.entity.PantryItem;
import com.recipebook.entity.RecipeIngredient;
import com.recipebook.entity.ShoppingListItem;
import com.recipebook.entity.Unit;
import com.recipebook.entity.User;
import com.recipebook.graphql.ShoppingListItemInput;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.graphql.GraphQLException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShoppingListService {

    public ShoppingListResponse getShoppingList(Long userId, LocalDate weekStart) {
        List<ShoppingListItem> items = ShoppingListItem.find(
                "FROM ShoppingListItem si LEFT JOIN FETCH si.ingredient WHERE si.user.id = ?1 AND si.weekStartDate = ?2",
                userId, weekStart).list();
        return buildResponse(weekStart, items);
    }

    public ShoppingListResponse generateShoppingList(Long userId, LocalDate weekStart) throws GraphQLException {
        LocalDate weekEnd = weekStart.plusDays(6);

        List<MealPlan> plans = MealPlan.listWithRecipeDetails(
                "mp.user.id = ?1 and mp.date >= ?2 and mp.date <= ?3", userId, weekStart, weekEnd);

        if (plans.isEmpty()) {
            throw new GraphQLException("No meal plans found for this week");
        }

        // Aggregate all recipe ingredients -> Map<ingredientId, totalGrams>
        Map<Long, Double> needed = new HashMap<>();
        Map<Long, Ingredient> ingredientMap = new HashMap<>();
        for (MealPlan plan : plans) {
            for (RecipeIngredient ri : plan.recipe.ingredients) {
                double grams = MacroCalculationService.toGrams(ri.quantity, ri.unit);
                needed.merge(ri.ingredient.id, grams, Double::sum);
                ingredientMap.put(ri.ingredient.id, ri.ingredient);
            }
        }

        // Get pantry quantities
        List<PantryItem> pantryItems = PantryItem.find(
                "FROM PantryItem pi JOIN FETCH pi.ingredient WHERE pi.user.id = ?1", userId).list();
        Map<Long, Double> pantry = new HashMap<>();
        for (PantryItem pi : pantryItems) {
            double grams = MacroCalculationService.toGrams(pi.quantity, pi.unit);
            pantry.put(pi.ingredient.id, grams);
        }

        // Clear existing items for this week
        ShoppingListItem.delete("user.id = ?1 and weekStartDate = ?2", userId, weekStart);

        User user = User.findById(userId);

        // Create shopping list items for needed > pantry
        for (Map.Entry<Long, Double> entry : needed.entrySet()) {
            Long ingredientId = entry.getKey();
            double neededGrams = entry.getValue();
            double pantryGrams = pantry.getOrDefault(ingredientId, 0.0);
            double deficit = neededGrams - pantryGrams;

            if (deficit > 0) {
                Ingredient ingredient = ingredientMap.get(ingredientId);
                ShoppingListItem item = new ShoppingListItem();
                item.user = user;
                item.ingredient = ingredient;
                item.ingredientName = ingredient.name;
                item.quantity = deficit;
                item.unit = Unit.GRAMS;
                item.weekStartDate = weekStart;
                item.purchased = false;
                item.persist();
            }
        }

        List<ShoppingListItem> items = ShoppingListItem.find(
                "FROM ShoppingListItem si LEFT JOIN FETCH si.ingredient WHERE si.user.id = ?1 AND si.weekStartDate = ?2",
                userId, weekStart).list();
        return buildResponse(weekStart, items);
    }

    public ShoppingListItemResponse addShoppingListItem(Long userId, ShoppingListItemInput input) throws GraphQLException {
        Ingredient ingredient = Ingredient.findById(input.ingredientId);
        if (ingredient == null) {
            throw new GraphQLException("Ingredient not found");
        }

        User user = User.findById(userId);
        ShoppingListItem item = new ShoppingListItem();
        item.user = user;
        item.ingredient = ingredient;
        item.ingredientName = ingredient.name;
        item.quantity = input.quantity;
        item.unit = input.unit;
        item.weekStartDate = input.weekStart;
        item.purchased = false;
        item.persist();
        return toItemResponse(item);
    }

    public ShoppingListItemResponse toggleShoppingListItem(Long userId, Long itemId) throws GraphQLException {
        ShoppingListItem item = ShoppingListItem.find(
                "FROM ShoppingListItem si LEFT JOIN FETCH si.ingredient WHERE si.id = ?1 AND si.user.id = ?2",
                itemId, userId).firstResult();
        if (item == null) {
            throw new GraphQLException("Shopping list item not found");
        }
        item.purchased = !item.purchased;
        return toItemResponse(item);
    }

    public boolean removeShoppingListItem(Long userId, Long itemId) throws GraphQLException {
        ShoppingListItem item = ShoppingListItem.find("id = ?1 and user.id = ?2", itemId, userId).firstResult();
        if (item == null) {
            throw new GraphQLException("Shopping list item not found");
        }
        item.delete();
        return true;
    }

    public boolean clearShoppingList(Long userId, LocalDate weekStart) {
        ShoppingListItem.delete("user.id = ?1 and weekStartDate = ?2", userId, weekStart);
        return true;
    }

    private ShoppingListResponse buildResponse(LocalDate weekStart, List<ShoppingListItem> items) {
        ShoppingListResponse response = new ShoppingListResponse();
        response.weekStart = weekStart;
        response.items = items.stream().map(this::toItemResponse).collect(Collectors.toList());
        response.totalItems = items.size();
        response.purchasedItems = (int) items.stream().filter(i -> i.purchased).count();
        response.progressPercent = items.isEmpty() ? 0 : (response.purchasedItems * 100.0 / response.totalItems);
        return response;
    }

    private ShoppingListItemResponse toItemResponse(ShoppingListItem item) {
        ShoppingListItemResponse response = new ShoppingListItemResponse();
        response.id = item.id;
        response.ingredientId = item.ingredient != null ? item.ingredient.id : null;
        response.ingredientName = item.ingredientName != null ? item.ingredientName : (item.ingredient != null ? item.ingredient.name : null);
        response.ingredientCategory = item.ingredient != null ? item.ingredient.category : null;
        response.quantity = item.quantity;
        response.unit = item.unit;
        response.purchased = item.purchased;
        return response;
    }
}
