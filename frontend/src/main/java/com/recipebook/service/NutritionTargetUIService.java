package com.recipebook.service;

import com.recipebook.foodlog.UserNutritionTargetResponse;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class NutritionTargetUIService {

    private final ApiClient apiClient;

    private static final String ALL_TARGETS_QUERY = """
            query {
                allNutritionTargets {
                    calories protein carbs fat dayType
                }
            }
            """;

    private static final String UPDATE_TARGET_MUTATION = """
            mutation($calories: Int!, $protein: Float!, $carbs: Float!, $fat: Float!, $dayType: String!) {
                updateNutritionTarget(calories: $calories, protein: $protein, carbs: $carbs, fat: $fat, dayType: $dayType) {
                    calories protein carbs fat dayType
                }
            }
            """;

    public NutritionTargetUIService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<UserNutritionTargetResponse> getAllTargets() {
        UserNutritionTargetResponse[] arr = apiClient.query(ALL_TARGETS_QUERY, null,
                UserNutritionTargetResponse[].class, "allNutritionTargets");
        return arr != null ? Arrays.asList(arr) : List.of();
    }

    public UserNutritionTargetResponse updateTarget(String dayType, int calories, double protein, double carbs, double fat) {
        Map<String, Object> vars = Map.of(
                "dayType", dayType,
                "calories", calories,
                "protein", protein,
                "carbs", carbs,
                "fat", fat
        );
        return apiClient.mutate(UPDATE_TARGET_MUTATION, vars,
                UserNutritionTargetResponse.class, "updateNutritionTarget");
    }
}
