package com.recipebook.dto;

import java.time.LocalDate;
import java.util.List;

public class ShoppingListResponse {
    public LocalDate weekStart;
    public List<ShoppingListItemResponse> items;
    public int totalItems;
    public int purchasedItems;
    public double progressPercent;
}
