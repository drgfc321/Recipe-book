package com.recipebook.service;

import com.recipebook.dto.BMRResultResponse;
import com.recipebook.dto.UserPhysicalDataResponse;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BMRService {

    private final ApiClient apiClient;

    private static final String USER_PHYSICAL_DATA_QUERY = """
            query {
                userPhysicalData {
                    weightKg heightCm birthDate gender activityLevel fitnessGoal
                }
            }
            """;

    private static final String PREVIEW_BMR_QUERY = """
            query($input: BMRWizardInput!) {
                previewBMR(input: $input) {
                    bmr tdee adjustedCalories proteinGrams carbsGrams fatGrams
                }
            }
            """;

    private static final String SAVE_BMR_MUTATION = """
            mutation($input: BMRWizardInput!) {
                saveBMRWizard(input: $input) {
                    bmr tdee adjustedCalories proteinGrams carbsGrams fatGrams
                }
            }
            """;

    public BMRService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public UserPhysicalDataResponse getUserPhysicalData() {
        return apiClient.query(USER_PHYSICAL_DATA_QUERY, null,
                UserPhysicalDataResponse.class, "userPhysicalData");
    }

    public BMRResultResponse previewBMR(double weightKg, double heightCm, String birthDate,
                                         String gender, String activityLevel, String fitnessGoal,
                                         int proteinPct, int carbsPct, int fatPct) {
        Map<String, Object> input = buildInput(weightKg, heightCm, birthDate,
                gender, activityLevel, fitnessGoal, proteinPct, carbsPct, fatPct);
        return apiClient.query(PREVIEW_BMR_QUERY, Map.of("input", input),
                BMRResultResponse.class, "previewBMR");
    }

    public BMRResultResponse saveBMRWizard(double weightKg, double heightCm, String birthDate,
                                            String gender, String activityLevel, String fitnessGoal,
                                            int proteinPct, int carbsPct, int fatPct) {
        Map<String, Object> input = buildInput(weightKg, heightCm, birthDate,
                gender, activityLevel, fitnessGoal, proteinPct, carbsPct, fatPct);
        return apiClient.mutate(SAVE_BMR_MUTATION, Map.of("input", input),
                BMRResultResponse.class, "saveBMRWizard");
    }

    private Map<String, Object> buildInput(double weightKg, double heightCm, String birthDate,
                                            String gender, String activityLevel, String fitnessGoal,
                                            int proteinPct, int carbsPct, int fatPct) {
        Map<String, Object> input = new LinkedHashMap<>();
        input.put("weightKg", weightKg);
        input.put("heightCm", heightCm);
        input.put("birthDate", birthDate);
        input.put("gender", gender);
        input.put("activityLevel", activityLevel);
        input.put("fitnessGoal", fitnessGoal);
        input.put("proteinPct", proteinPct);
        input.put("carbsPct", carbsPct);
        input.put("fatPct", fatPct);
        return input;
    }
}
