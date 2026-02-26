package com.recipebook.recommendation;

import com.recipebook.service.ApiClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class RecommendationService {

    private final ApiClient apiClient;

    private static final String RECOMMENDATION_FIELDS = """
            recipe {
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
                createdAt
            }
            matchPercent totalIngredients matchedIngredients missingCount
            missingIngredients {
                ingredientId ingredientName ingredientCategory
                requiredQuantity pantryQuantity neededQuantity unit
            }""";

    private static final String RECOMMENDATIONS_QUERY = """
            query($filter: RecommendationFilterInput!) {
                recipeRecommendations(filter: $filter) {
                    %s
                }
            }
            """.formatted(RECOMMENDATION_FIELDS);

    private static final String ADD_MISSING_TO_LIST = """
            mutation($recipeId: BigInteger!, $weekStart: Date!) {
                addMissingToShoppingList(recipeId: $recipeId, weekStart: $weekStart)
            }
            """;

    public RecommendationService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<RecipeRecommendationResponse> getRecommendations(String category, String difficulty, Integer maxMissing) {
        Map<String, Object> filter = new HashMap<>();
        if (category != null && !category.isBlank()) filter.put("category", category);
        if (difficulty != null && !difficulty.isBlank()) filter.put("difficulty", difficulty);
        if (maxMissing != null) filter.put("maxMissingIngredients", maxMissing);

        RecipeRecommendationResponse[] result = apiClient.query(RECOMMENDATIONS_QUERY,
                Map.of("filter", filter),
                RecipeRecommendationResponse[].class, "recipeRecommendations");
        return result != null ? Arrays.asList(result) : List.of();
    }

    public boolean addMissingToShoppingList(Long recipeId, LocalDate weekStart) {
        Boolean result = apiClient.mutate(ADD_MISSING_TO_LIST,
                Map.of("recipeId", recipeId, "weekStart", weekStart.toString()),
                Boolean.class, "addMissingToShoppingList");
        return Boolean.TRUE.equals(result);
    }
}
