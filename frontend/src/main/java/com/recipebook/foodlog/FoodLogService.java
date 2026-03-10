package com.recipebook.foodlog;

import com.recipebook.ingredient.IngredientResponse;
import com.recipebook.recipe.RecipeResponse;
import com.recipebook.service.ApiClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class FoodLogService {

    private final ApiClient apiClient;

    private static final String FOOD_LOG_FIELDS = """
            id date mealSlot servings loggedAt
            sourceType customName ingredientName ingredientQuantity
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
            actualMacros { calories protein carbs fat }""";

    private static final String DAILY_FOOD_LOG_QUERY = """
            query($date: Date!) {
                dailyFoodLog(date: $date) {
                    date
                    entries { %s }
                    totalActualMacros { calories protein carbs fat }
                    targets { calories protein carbs fat }
                    plannedMeals {
                        mealPlanId mealSlot logged foodLogId
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
                    }
                }
            }
            """.formatted(FOOD_LOG_FIELDS);

    private static final String LOG_FOOD_MUTATION = """
            mutation($input: FoodLogInput!) {
                logFood(input: $input) {
                    %s
                }
            }
            """.formatted(FOOD_LOG_FIELDS);

    private static final String UPDATE_FOOD_LOG_MUTATION = """
            mutation($id: BigInteger!, $servings: Float!) {
                updateFoodLog(id: $id, servings: $servings) {
                    %s
                }
            }
            """.formatted(FOOD_LOG_FIELDS);

    private static final String REMOVE_FOOD_LOG_MUTATION = """
            mutation($id: BigInteger!) {
                removeFoodLog(id: $id)
            }
            """;

    private static final String RECIPES_QUERY = """
            query($search: String) {
                recipes(search: $search) {
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
            }
            """;

    private static final String INGREDIENTS_QUERY = """
            query($search: String) {
                ingredients(search: $search) {
                    id name category caloriesPer100g proteinPer100g carbsPer100g fatPer100g
                }
            }
            """;

    public FoodLogService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public DailyFoodLogResponse getDailyFoodLog(LocalDate date) {
        return apiClient.query(DAILY_FOOD_LOG_QUERY,
                Map.of("date", date.toString()),
                DailyFoodLogResponse.class, "dailyFoodLog");
    }

    public FoodLogResponse logFood(LocalDate date, String mealSlot, Long recipeId, double servings, Long mealPlanId) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("date", date.toString());
        input.put("mealSlot", mealSlot);
        if (recipeId != null) input.put("recipeId", recipeId);
        input.put("servings", servings);
        if (mealPlanId != null) input.put("mealPlanId", mealPlanId);
        return apiClient.mutate(LOG_FOOD_MUTATION,
                Map.of("input", input),
                FoodLogResponse.class, "logFood");
    }

    public FoodLogResponse logIngredient(LocalDate date, String mealSlot, Long ingredientId,
                                          double quantityGrams, double servings) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("date", date.toString());
        input.put("mealSlot", mealSlot);
        input.put("ingredientId", ingredientId);
        input.put("ingredientQuantity", quantityGrams);
        input.put("servings", servings);
        return apiClient.mutate(LOG_FOOD_MUTATION,
                Map.of("input", input),
                FoodLogResponse.class, "logFood");
    }

    public FoodLogResponse logCustomFood(LocalDate date, String mealSlot, String name,
                                          double cal, double pro, double carbs, double fat,
                                          double servings) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("date", date.toString());
        input.put("mealSlot", mealSlot);
        input.put("customName", name);
        input.put("customCalories", cal);
        input.put("customProtein", pro);
        input.put("customCarbs", carbs);
        input.put("customFat", fat);
        input.put("servings", servings);
        return apiClient.mutate(LOG_FOOD_MUTATION,
                Map.of("input", input),
                FoodLogResponse.class, "logFood");
    }

    public FoodLogResponse updateFoodLog(Long id, double servings) {
        return apiClient.mutate(UPDATE_FOOD_LOG_MUTATION,
                Map.of("id", id, "servings", servings),
                FoodLogResponse.class, "updateFoodLog");
    }

    public boolean removeFoodLog(Long id) {
        Boolean result = apiClient.mutate(REMOVE_FOOD_LOG_MUTATION,
                Map.of("id", id),
                Boolean.class, "removeFoodLog");
        return Boolean.TRUE.equals(result);
    }

    public List<RecipeResponse> searchRecipes(String query) {
        Map<String, Object> vars = new HashMap<>();
        if (query != null && !query.isBlank()) vars.put("search", query);
        RecipeResponse[] result = apiClient.query(RECIPES_QUERY, vars,
                RecipeResponse[].class, "recipes");
        return result != null ? Arrays.asList(result) : List.of();
    }

    public List<IngredientResponse> searchIngredients(String query) {
        Map<String, Object> vars = new HashMap<>();
        if (query != null && !query.isBlank()) vars.put("search", query);
        IngredientResponse[] result = apiClient.query(INGREDIENTS_QUERY, vars,
                IngredientResponse[].class, "ingredients");
        return result != null ? Arrays.asList(result) : List.of();
    }
}
