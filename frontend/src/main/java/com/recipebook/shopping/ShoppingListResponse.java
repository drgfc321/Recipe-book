package com.recipebook.shopping;

import java.util.List;

public record ShoppingListResponse(
        String weekStart,
        List<ShoppingListItemResponse> items,
        int totalItems,
        int purchasedItems,
        double progressPercent
) {
}
