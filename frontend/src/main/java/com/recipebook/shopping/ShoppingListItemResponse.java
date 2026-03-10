package com.recipebook.shopping;

public record ShoppingListItemResponse(
        Long id,
        Long ingredientId,
        String ingredientName,
        String ingredientCategory,
        double quantity,
        String unit,
        boolean purchased
) {
}
