package com.recipebook.pantry;

public record PantryItemResponse(
        Long id,
        Long ingredientId,
        String ingredientName,
        String ingredientCategory,
        double quantity,
        String unit,
        String expirationDate,
        boolean expiringSoon
) {
}
