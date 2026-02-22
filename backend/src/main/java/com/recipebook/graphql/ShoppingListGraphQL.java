package com.recipebook.graphql;

import com.recipebook.dto.ShoppingListItemResponse;
import com.recipebook.dto.ShoppingListResponse;
import com.recipebook.service.ShoppingListService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.LocalDate;

@GraphQLApi
public class ShoppingListGraphQL {

    @Inject
    JsonWebToken jwt;

    @Inject
    ShoppingListService shoppingListService;

    @Query("shoppingList")
    @Description("Get shopping list for a week with progress")
    @Authenticated
    public ShoppingListResponse getShoppingList(@Name("weekStart") LocalDate weekStart) {
        Long userId = Long.parseLong(jwt.getSubject());
        return shoppingListService.getShoppingList(userId, weekStart);
    }

    @Mutation("generateShoppingList")
    @Description("Auto-generate shopping list from meal plan, subtracting pantry")
    @Authenticated
    @Transactional
    public ShoppingListResponse generateShoppingList(@Name("weekStart") LocalDate weekStart) throws GraphQLException {
        Long userId = Long.parseLong(jwt.getSubject());
        return shoppingListService.generateShoppingList(userId, weekStart);
    }

    @Mutation("addShoppingListItem")
    @Description("Manually add an item to the shopping list")
    @Authenticated
    @Transactional
    public ShoppingListItemResponse addShoppingListItem(@Name("input") ShoppingListItemInput input) throws GraphQLException {
        Long userId = Long.parseLong(jwt.getSubject());
        return shoppingListService.addShoppingListItem(userId, input);
    }

    @Mutation("toggleShoppingListItem")
    @Description("Toggle purchased status of a shopping list item")
    @Authenticated
    @Transactional
    public ShoppingListItemResponse toggleShoppingListItem(@Name("id") Long id) throws GraphQLException {
        Long userId = Long.parseLong(jwt.getSubject());
        return shoppingListService.toggleShoppingListItem(userId, id);
    }

    @Mutation("removeShoppingListItem")
    @Description("Remove a shopping list item")
    @Authenticated
    @Transactional
    public boolean removeShoppingListItem(@Name("id") Long id) throws GraphQLException {
        Long userId = Long.parseLong(jwt.getSubject());
        return shoppingListService.removeShoppingListItem(userId, id);
    }

    @Mutation("clearShoppingList")
    @Description("Clear all shopping list items for a week")
    @Authenticated
    @Transactional
    public boolean clearShoppingList(@Name("weekStart") LocalDate weekStart) {
        Long userId = Long.parseLong(jwt.getSubject());
        return shoppingListService.clearShoppingList(userId, weekStart);
    }
}
