package com.recipebook.mealplan;

import com.recipebook.recipe.RecipeResponse;
import com.recipebook.service.ApiClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class MealPlanService {

    private final ApiClient apiClient;

    private static final String MEAL_PLAN_FIELDS = """
            id date mealSlot
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
            }""";

    private static final String WEEKLY_QUERY = """
            query($weekStart: Date!) {
                weeklyMealPlan(weekStart: $weekStart) {
                    weekStart weekEnd
                    days {
                        date
                        meals { %s }
                        totalMacros { calories protein carbs fat }
                    }
                    totalMacros { calories protein carbs fat }
                    averageDailyMacros { calories protein carbs fat }
                }
            }
            """.formatted(MEAL_PLAN_FIELDS);

    private static final String ASSIGN_MUTATION = """
            mutation($input: MealPlanInput!) {
                assignMealPlan(input: $input) {
                    %s
                }
            }
            """.formatted(MEAL_PLAN_FIELDS);

    private static final String REMOVE_MUTATION = """
            mutation($date: Date!, $mealSlot: MealSlot!) {
                removeMealPlan(date: $date, mealSlot: $mealSlot)
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

    public MealPlanService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public WeeklyMealPlanResponse getWeeklyMealPlan(LocalDate weekStart) {
        return apiClient.query(WEEKLY_QUERY,
                Map.of("weekStart", weekStart.toString()),
                WeeklyMealPlanResponse.class, "weeklyMealPlan");
    }

    public MealPlanResponse assignMealPlan(LocalDate date, String mealSlot, Long recipeId) {
        Map<String, Object> input = Map.of(
                "date", date.toString(),
                "mealSlot", mealSlot,
                "recipeId", recipeId
        );
        return apiClient.mutate(ASSIGN_MUTATION,
                Map.of("input", input),
                MealPlanResponse.class, "assignMealPlan");
    }

    public boolean removeMealPlan(LocalDate date, String mealSlot) {
        Boolean result = apiClient.mutate(REMOVE_MUTATION,
                Map.of("date", date.toString(), "mealSlot", mealSlot),
                Boolean.class, "removeMealPlan");
        return Boolean.TRUE.equals(result);
    }

    public List<RecipeResponse> getRecipes(String search) {
        Map<String, Object> vars = new HashMap<>();
        if (search != null && !search.isBlank()) vars.put("search", search);
        RecipeResponse[] result = apiClient.query(RECIPES_QUERY, vars,
                RecipeResponse[].class, "recipes");
        return result != null ? Arrays.asList(result) : List.of();
    }
}
