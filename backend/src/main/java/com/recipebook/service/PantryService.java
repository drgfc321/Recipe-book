package com.recipebook.service;

import com.recipebook.dto.PantryItemResponse;
import com.recipebook.entity.Ingredient;
import com.recipebook.entity.PantryItem;
import com.recipebook.entity.Unit;
import com.recipebook.entity.User;
import com.recipebook.graphql.PantryItemInput;
import com.recipebook.graphql.PantryItemUpdateInput;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.graphql.GraphQLException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PantryService {

    public List<PantryItemResponse> getPantryItems(Long userId) {
        List<PantryItem> items = PantryItem.list("user.id", userId);
        return items.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<PantryItemResponse> getExpiringItems(Long userId, int withinDays) {
        LocalDate threshold = LocalDate.now().plusDays(withinDays);
        List<PantryItem> items = PantryItem.list(
                "user.id = ?1 and expirationDate is not null and expirationDate <= ?2",
                userId, threshold);
        return items.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public PantryItemResponse addPantryItem(Long userId, PantryItemInput input) throws GraphQLException {
        User user = User.findById(userId);
        Ingredient ingredient = Ingredient.findById(input.ingredientId);
        if (ingredient == null) {
            throw new GraphQLException("Ingredient not found");
        }

        PantryItem existing = PantryItem.find("user.id = ?1 and ingredient.id = ?2", userId, input.ingredientId).firstResult();
        if (existing != null) {
            double existingGrams = MacroCalculationService.toGrams(existing.quantity, existing.unit);
            double newGrams = MacroCalculationService.toGrams(input.quantity, input.unit);
            existing.quantity = existingGrams + newGrams;
            existing.unit = Unit.GRAMS;
            if (input.expirationDate != null) {
                if (existing.expirationDate == null || input.expirationDate.isBefore(existing.expirationDate)) {
                    existing.expirationDate = input.expirationDate;
                }
            }
            return toResponse(existing);
        }

        PantryItem item = new PantryItem();
        item.user = user;
        item.ingredient = ingredient;
        item.quantity = input.quantity;
        item.unit = input.unit;
        item.expirationDate = input.expirationDate;
        item.persist();
        return toResponse(item);
    }

    public PantryItemResponse updatePantryItem(Long userId, Long itemId, PantryItemUpdateInput input) throws GraphQLException {
        PantryItem item = PantryItem.findById(itemId);
        if (item == null) {
            throw new GraphQLException("Pantry item not found");
        }
        if (!item.user.id.equals(userId)) {
            throw new GraphQLException("You can only modify your own pantry items");
        }
        if (input.quantity != null) {
            item.quantity = input.quantity;
        }
        if (input.unit != null) {
            item.unit = input.unit;
        }
        if (input.expirationDate != null) {
            item.expirationDate = input.expirationDate;
        }
        return toResponse(item);
    }

    public boolean removePantryItem(Long userId, Long itemId) throws GraphQLException {
        PantryItem item = PantryItem.findById(itemId);
        if (item == null) {
            throw new GraphQLException("Pantry item not found");
        }
        if (!item.user.id.equals(userId)) {
            throw new GraphQLException("You can only modify your own pantry items");
        }
        item.delete();
        return true;
    }

    public PantryItemResponse toResponse(PantryItem item) {
        PantryItemResponse response = new PantryItemResponse();
        response.id = item.id;
        response.ingredientId = item.ingredient.id;
        response.ingredientName = item.ingredient.name;
        response.ingredientCategory = item.ingredient.category;
        response.quantity = item.quantity;
        response.unit = item.unit;
        response.expirationDate = item.expirationDate;
        response.expiringSoon = item.expirationDate != null && !item.expirationDate.isAfter(LocalDate.now().plusDays(3));
        return response;
    }
}
