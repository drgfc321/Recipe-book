package com.recipebook.recipe;

import com.recipebook.service.ApiClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RecipeService {

    private final ApiClient apiClient;

    private static final String RECIPE_FIELDS = """
            id name description category difficulty
            prepTime cookTime servings instructions imageUrl
            ownerId ownerUsername
            ingredients {
                ingredientId ingredientName ingredientCategory
                quantity unit
                macros { calories protein carbs fat }
            }
            totalMacros { calories protein carbs fat }
            perServingMacros { calories protein carbs fat }
            createdAt""";

    private static final String RECIPES_QUERY = """
            query($category: RecipeCategory, $difficulty: Difficulty, $search: String) {
                recipes(category: $category, difficulty: $difficulty, search: $search) {
                    %s
                }
            }
            """.formatted(RECIPE_FIELDS);

    private static final String RECIPE_QUERY = """
            query($id: BigInteger!) {
                recipe(id: $id) {
                    %s
                }
            }
            """.formatted(RECIPE_FIELDS);

    private static final String CREATE_RECIPE = """
            mutation($input: RecipeInput!) {
                createRecipe(input: $input) {
                    %s
                }
            }
            """.formatted(RECIPE_FIELDS);

    private static final String UPDATE_RECIPE = """
            mutation($id: BigInteger!, $input: RecipeInput!) {
                updateRecipe(id: $id, input: $input) {
                    %s
                }
            }
            """.formatted(RECIPE_FIELDS);

    private static final String DELETE_RECIPE = """
            mutation($id: BigInteger!) {
                deleteRecipe(id: $id)
            }
            """;

    private static final String INGREDIENTS_QUERY = """
            query {
                ingredients {
                    id name category
                }
            }
            """;

    public RecipeService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public String getBackendUrl() {
        return apiClient.getBackendUrl();
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public List<RecipeResponse> getRecipes(String category, String difficulty, String search) {
        Map<String, Object> vars = new HashMap<>();
        if (category != null && !category.isBlank()) vars.put("category", category);
        if (difficulty != null && !difficulty.isBlank()) vars.put("difficulty", difficulty);
        if (search != null && !search.isBlank()) vars.put("search", search);

        RecipeResponse[] result = apiClient.query(RECIPES_QUERY, vars, RecipeResponse[].class, "recipes");
        return result != null ? Arrays.asList(result) : List.of();
    }

    public RecipeResponse getRecipe(Long id) {
        return apiClient.query(RECIPE_QUERY, Map.of("id", id), RecipeResponse.class, "recipe");
    }

    public RecipeResponse createRecipe(Map<String, Object> input) {
        return apiClient.mutate(CREATE_RECIPE, Map.of("input", input), RecipeResponse.class, "createRecipe");
    }

    public RecipeResponse updateRecipe(Long id, Map<String, Object> input) {
        return apiClient.mutate(UPDATE_RECIPE, Map.of("id", id, "input", input), RecipeResponse.class, "updateRecipe");
    }

    public boolean deleteRecipe(Long id) {
        Boolean result = apiClient.mutate(DELETE_RECIPE, Map.of("id", id), Boolean.class, "deleteRecipe");
        return Boolean.TRUE.equals(result);
    }

    public List<IngredientOption> getIngredients() {
        IngredientOption[] result = apiClient.query(INGREDIENTS_QUERY, null, IngredientOption[].class, "ingredients");
        return result != null ? Arrays.asList(result) : List.of();
    }
}
