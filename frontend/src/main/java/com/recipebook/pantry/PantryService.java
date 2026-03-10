package com.recipebook.pantry;

import com.recipebook.service.ApiClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PantryService {

    private final ApiClient apiClient;

    private static final String PANTRY_FIELDS = """
            id ingredientId ingredientName ingredientCategory quantity unit expirationDate expiringSoon""";

    private static final String PANTRY_ITEMS_QUERY = """
            query {
                pantryItems {
                    %s
                }
            }
            """.formatted(PANTRY_FIELDS);

    private static final String EXPIRING_ITEMS_QUERY = """
            query($withinDays: Int) {
                expiringPantryItems(withinDays: $withinDays) {
                    %s
                }
            }
            """.formatted(PANTRY_FIELDS);

    private static final String ADD_PANTRY_ITEM = """
            mutation($input: PantryItemInput!) {
                addPantryItem(input: $input) {
                    %s
                }
            }
            """.formatted(PANTRY_FIELDS);

    private static final String UPDATE_PANTRY_ITEM = """
            mutation($id: BigInteger!, $input: PantryItemUpdateInput!) {
                updatePantryItem(id: $id, input: $input) {
                    %s
                }
            }
            """.formatted(PANTRY_FIELDS);

    private static final String REMOVE_PANTRY_ITEM = """
            mutation($id: BigInteger!) {
                removePantryItem(id: $id)
            }
            """;

    public PantryService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<PantryItemResponse> getPantryItems() {
        PantryItemResponse[] result = apiClient.query(PANTRY_ITEMS_QUERY, null,
                PantryItemResponse[].class, "pantryItems");
        return result != null ? Arrays.asList(result) : List.of();
    }

    public List<PantryItemResponse> getExpiringItems(int withinDays) {
        PantryItemResponse[] result = apiClient.query(EXPIRING_ITEMS_QUERY,
                Map.of("withinDays", withinDays),
                PantryItemResponse[].class, "expiringPantryItems");
        return result != null ? Arrays.asList(result) : List.of();
    }

    public PantryItemResponse addPantryItem(Map<String, Object> input) {
        return apiClient.mutate(ADD_PANTRY_ITEM, Map.of("input", input),
                PantryItemResponse.class, "addPantryItem");
    }

    public PantryItemResponse updatePantryItem(Long id, Map<String, Object> input) {
        return apiClient.mutate(UPDATE_PANTRY_ITEM, Map.of("id", id, "input", input),
                PantryItemResponse.class, "updatePantryItem");
    }

    public boolean removePantryItem(Long id) {
        Boolean result = apiClient.mutate(REMOVE_PANTRY_ITEM, Map.of("id", id),
                Boolean.class, "removePantryItem");
        return Boolean.TRUE.equals(result);
    }
}
