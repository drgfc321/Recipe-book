package com.recipebook.service;

import com.recipebook.dto.ShoppingListItemResponse;
import com.recipebook.dto.ShoppingListResponse;
import com.recipebook.entity.Ingredient;
import com.recipebook.entity.IngredientCategory;
import com.recipebook.entity.ShoppingListItem;
import com.recipebook.entity.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingListServiceTest {

    private ShoppingListService service;
    private final LocalDate weekStart = LocalDate.of(2026, 3, 2);

    @BeforeEach
    void setUp() {
        service = new ShoppingListService();
    }

    // --- buildResponse tests ---

    @Test
    void buildResponse_emptyList() {
        ShoppingListResponse response = service.buildResponse(weekStart, List.of());

        assertEquals(0, response.totalItems);
        assertEquals(0, response.purchasedItems);
        assertEquals(0.0, response.progressPercent, 0.01);
        assertTrue(response.items.isEmpty());
    }

    @Test
    void buildResponse_allPurchased() {
        List<ShoppingListItem> items = List.of(
                createItem(1L, "Chicken", true),
                createItem(2L, "Rice", true)
        );

        ShoppingListResponse response = service.buildResponse(weekStart, items);

        assertEquals(2, response.totalItems);
        assertEquals(2, response.purchasedItems);
        assertEquals(100.0, response.progressPercent, 0.01);
    }

    @Test
    void buildResponse_nonePurchased() {
        List<ShoppingListItem> items = List.of(
                createItem(1L, "Chicken", false),
                createItem(2L, "Rice", false),
                createItem(3L, "Salt", false)
        );

        ShoppingListResponse response = service.buildResponse(weekStart, items);

        assertEquals(3, response.totalItems);
        assertEquals(0, response.purchasedItems);
        assertEquals(0.0, response.progressPercent, 0.01);
    }

    @Test
    void buildResponse_mixedPurchased() {
        List<ShoppingListItem> items = List.of(
                createItem(1L, "Chicken", true),
                createItem(2L, "Rice", true),
                createItem(3L, "Salt", false),
                createItem(4L, "Pepper", false)
        );

        ShoppingListResponse response = service.buildResponse(weekStart, items);

        assertEquals(4, response.totalItems);
        assertEquals(2, response.purchasedItems);
        assertEquals(50.0, response.progressPercent, 0.01);
    }

    @Test
    void buildResponse_weekStartSet() {
        ShoppingListResponse response = service.buildResponse(weekStart, List.of());

        assertEquals(weekStart, response.weekStart);
    }

    // --- toItemResponse tests ---

    @Test
    void toItemResponse_withIngredient() {
        ShoppingListItem item = new ShoppingListItem();
        item.id = 10L;
        item.ingredientName = "Chicken Breast";
        item.quantity = 500;
        item.unit = Unit.GRAMS;
        item.purchased = true;
        Ingredient ingredient = new Ingredient();
        ingredient.id = 5L;
        ingredient.name = "Chicken Breast";
        ingredient.category = IngredientCategory.MEAT;
        item.ingredient = ingredient;

        ShoppingListItemResponse response = service.toItemResponse(item);

        assertEquals(10L, response.id);
        assertEquals(5L, response.ingredientId);
        assertEquals("Chicken Breast", response.ingredientName);
        assertEquals(IngredientCategory.MEAT, response.ingredientCategory);
        assertEquals(500, response.quantity, 0.01);
        assertEquals(Unit.GRAMS, response.unit);
        assertTrue(response.purchased);
    }

    @Test
    void toItemResponse_nullIngredient() {
        ShoppingListItem item = new ShoppingListItem();
        item.id = 11L;
        item.ingredient = null;
        item.ingredientName = "Custom Item";
        item.quantity = 1;
        item.unit = Unit.PIECES;
        item.purchased = false;

        ShoppingListItemResponse response = service.toItemResponse(item);

        assertNull(response.ingredientId);
        assertEquals("Custom Item", response.ingredientName);
        assertNull(response.ingredientCategory);
        assertFalse(response.purchased);
    }

    @Test
    void toItemResponse_nullIngredientName() {
        ShoppingListItem item = new ShoppingListItem();
        item.id = 12L;
        item.ingredientName = null;
        item.quantity = 200;
        item.unit = Unit.GRAMS;
        item.purchased = false;
        Ingredient ingredient = new Ingredient();
        ingredient.id = 7L;
        ingredient.name = "Tomato";
        ingredient.category = IngredientCategory.VEGETABLES;
        item.ingredient = ingredient;

        ShoppingListItemResponse response = service.toItemResponse(item);

        assertEquals(7L, response.ingredientId);
        assertEquals("Tomato", response.ingredientName);
    }

    // --- helpers ---

    private ShoppingListItem createItem(Long id, String name, boolean purchased) {
        ShoppingListItem item = new ShoppingListItem();
        item.id = id;
        item.ingredientName = name;
        item.quantity = 100;
        item.unit = Unit.GRAMS;
        item.purchased = purchased;
        Ingredient ingredient = new Ingredient();
        ingredient.id = id;
        ingredient.name = name;
        ingredient.category = IngredientCategory.OTHER;
        item.ingredient = ingredient;
        return item;
    }
}
