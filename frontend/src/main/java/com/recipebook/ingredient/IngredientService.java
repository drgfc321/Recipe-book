package com.recipebook.ingredient;

import com.recipebook.service.ApiClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class IngredientService {

    private final ApiClient apiClient;

    private static final String INGREDIENT_FIELDS = """
            id name category caloriesPer100g proteinPer100g carbsPer100g fatPer100g""";

    private static final String INGREDIENTS_QUERY = """
            query($search: String, $category: IngredientCategory) {
                ingredients(search: $search, category: $category) {
                    %s
                }
            }
            """.formatted(INGREDIENT_FIELDS);

    private static final String INGREDIENT_QUERY = """
            query($id: BigInteger!) {
                ingredient(id: $id) {
                    %s
                }
            }
            """.formatted(INGREDIENT_FIELDS);

    private static final String CREATE_INGREDIENT = """
            mutation($input: IngredientInput!) {
                createIngredient(input: $input) {
                    %s
                }
            }
            """.formatted(INGREDIENT_FIELDS);

    private static final String UPDATE_INGREDIENT = """
            mutation($id: BigInteger!, $input: IngredientInput!) {
                updateIngredient(id: $id, input: $input) {
                    %s
                }
            }
            """.formatted(INGREDIENT_FIELDS);

    private static final String DELETE_INGREDIENT = """
            mutation($id: BigInteger!) {
                deleteIngredient(id: $id)
            }
            """;

    public IngredientService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<IngredientResponse> getIngredients(String search, String category) {
        Map<String, Object> vars = new HashMap<>();
        if (search != null && !search.isBlank()) vars.put("search", search);
        if (category != null && !category.isBlank()) vars.put("category", category);

        IngredientResponse[] result = apiClient.query(INGREDIENTS_QUERY, vars, IngredientResponse[].class, "ingredients");
        return result != null ? Arrays.asList(result) : List.of();
    }

    public IngredientResponse getIngredient(Long id) {
        return apiClient.query(INGREDIENT_QUERY, Map.of("id", id), IngredientResponse.class, "ingredient");
    }

    public IngredientResponse createIngredient(Map<String, Object> input) {
        return apiClient.mutate(CREATE_INGREDIENT, Map.of("input", input), IngredientResponse.class, "createIngredient");
    }

    public IngredientResponse updateIngredient(Long id, Map<String, Object> input) {
        return apiClient.mutate(UPDATE_INGREDIENT, Map.of("id", id, "input", input), IngredientResponse.class, "updateIngredient");
    }

    public boolean deleteIngredient(Long id) {
        Boolean result = apiClient.mutate(DELETE_INGREDIENT, Map.of("id", id), Boolean.class, "deleteIngredient");
        return Boolean.TRUE.equals(result);
    }
}
