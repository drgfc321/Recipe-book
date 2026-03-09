package com.recipebook.nutritionhistory;

import com.recipebook.service.ApiClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class NutritionHistoryService {

    private final ApiClient apiClient;

    private static final String NUTRITION_HISTORY_QUERY = """
            query($startDate: Date!, $endDate: Date!) {
                nutritionHistory(startDate: $startDate, endDate: $endDate) {
                    date
                    totalCalories totalProtein totalCarbs totalFat
                    targetCalories targetProtein targetCarbs targetFat
                    dayType
                }
            }
            """;

    public NutritionHistoryService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<DailyNutritionSummary> getNutritionHistory(LocalDate startDate, LocalDate endDate) {
        DailyNutritionSummary[] result = apiClient.query(NUTRITION_HISTORY_QUERY,
                Map.of("startDate", startDate.toString(), "endDate", endDate.toString()),
                DailyNutritionSummary[].class, "nutritionHistory");
        return result != null ? Arrays.asList(result) : List.of();
    }
}
