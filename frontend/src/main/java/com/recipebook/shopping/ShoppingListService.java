package com.recipebook.shopping;

import com.recipebook.service.ApiClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
public class ShoppingListService {

    private final ApiClient apiClient;

    private static final String ITEM_FIELDS = """
            id ingredientId ingredientName ingredientCategory quantity unit purchased""";

    private static final String LIST_FIELDS = """
            weekStart
            items { %s }
            totalItems purchasedItems progressPercent""".formatted(ITEM_FIELDS);

    private static final String SHOPPING_LIST_QUERY = """
            query($weekStart: Date!) {
                shoppingList(weekStart: $weekStart) {
                    %s
                }
            }
            """.formatted(LIST_FIELDS);

    private static final String GENERATE_LIST = """
            mutation($weekStart: Date!) {
                generateShoppingList(weekStart: $weekStart) {
                    %s
                }
            }
            """.formatted(LIST_FIELDS);

    private static final String TOGGLE_ITEM = """
            mutation($id: BigInteger!) {
                toggleShoppingListItem(id: $id) {
                    %s
                }
            }
            """.formatted(ITEM_FIELDS);

    private static final String REMOVE_ITEM = """
            mutation($id: BigInteger!) {
                removeShoppingListItem(id: $id)
            }
            """;

    private static final String CLEAR_LIST = """
            mutation($weekStart: Date!) {
                clearShoppingList(weekStart: $weekStart)
            }
            """;

    public ShoppingListService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ShoppingListResponse getShoppingList(LocalDate weekStart) {
        return apiClient.query(SHOPPING_LIST_QUERY,
                Map.of("weekStart", weekStart.toString()),
                ShoppingListResponse.class, "shoppingList");
    }

    public ShoppingListResponse generateShoppingList(LocalDate weekStart) {
        return apiClient.mutate(GENERATE_LIST,
                Map.of("weekStart", weekStart.toString()),
                ShoppingListResponse.class, "generateShoppingList");
    }

    public ShoppingListItemResponse toggleItem(Long id) {
        return apiClient.mutate(TOGGLE_ITEM,
                Map.of("id", id),
                ShoppingListItemResponse.class, "toggleShoppingListItem");
    }

    public boolean removeItem(Long id) {
        Boolean result = apiClient.mutate(REMOVE_ITEM,
                Map.of("id", id), Boolean.class, "removeShoppingListItem");
        return Boolean.TRUE.equals(result);
    }

    public boolean clearList(LocalDate weekStart) {
        Boolean result = apiClient.mutate(CLEAR_LIST,
                Map.of("weekStart", weekStart.toString()),
                Boolean.class, "clearShoppingList");
        return Boolean.TRUE.equals(result);
    }
}
