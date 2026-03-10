package com.recipebook.service;

import com.recipebook.dto.DailyNutritionSummary;
import com.recipebook.dto.MacroInfo;
import com.recipebook.dto.RecipeResponse;
import com.recipebook.dto.UserNutritionTargetResponse;
import com.recipebook.entity.DayType;
import com.recipebook.entity.FoodLog;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.util.*;

@ApplicationScoped
public class NutritionHistoryService {

    private static final Logger LOG = Logger.getLogger(NutritionHistoryService.class);

    @Inject
    MacroCalculationService macroService;

    @Inject
    DayTypeService dayTypeService;

    @Inject
    NutritionTargetService nutritionTargetService;

    public List<DailyNutritionSummary> getNutritionHistory(Long userId, LocalDate startDate, LocalDate endDate) {
        LOG.debugf("getNutritionHistory userId=%d, start=%s, end=%s", userId, startDate, endDate);

        // 1. Fetch all food logs in range
        List<FoodLog> logs = FoodLog.find(
                "FROM FoodLog fl " +
                "LEFT JOIN FETCH fl.recipe r " +
                "LEFT JOIN FETCH r.ingredients ri " +
                "LEFT JOIN FETCH ri.ingredient " +
                "LEFT JOIN FETCH fl.ingredient " +
                "WHERE fl.user.id = ?1 AND fl.date >= ?2 AND fl.date <= ?3",
                userId, startDate, endDate).list();

        // 2. Fetch day types for range
        Map<LocalDate, DayType> dayTypes = dayTypeService.getDayTypesForRange(userId, startDate, endDate);

        // 3. Fetch all nutrition targets
        List<UserNutritionTargetResponse> allTargets = nutritionTargetService.getAllTargets(userId);
        Map<String, UserNutritionTargetResponse> targetMap = new HashMap<>();
        for (UserNutritionTargetResponse t : allTargets) {
            targetMap.put(t.dayType, t);
        }

        // 4. Group logs by date
        Map<LocalDate, List<FoodLog>> logsByDate = new LinkedHashMap<>();
        for (FoodLog log : logs) {
            logsByDate.computeIfAbsent(log.date, k -> new ArrayList<>()).add(log);
        }

        // 5. Build summaries for each date in range
        List<DailyNutritionSummary> result = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            DayType dayType = dayTypes.getOrDefault(current, DayType.DEFAULT);
            UserNutritionTargetResponse target = targetMap.get(dayType.name());
            if (target == null) {
                target = targetMap.getOrDefault("DEFAULT",
                        new UserNutritionTargetResponse(2000, 150.0, 250.0, 65.0, "DEFAULT"));
            }

            double totalCal = 0, totalPro = 0, totalCarbs = 0, totalFat = 0;
            List<FoodLog> dayLogs = logsByDate.getOrDefault(current, List.of());
            for (FoodLog log : dayLogs) {
                MacroInfo macros = computeMacros(log);
                totalCal += macros.calories;
                totalPro += macros.protein;
                totalCarbs += macros.carbs;
                totalFat += macros.fat;
            }

            result.add(new DailyNutritionSummary(
                    current,
                    totalCal, totalPro, totalCarbs, totalFat,
                    target.calories, target.protein, target.carbs, target.fat,
                    dayType.name()
            ));
            current = current.plusDays(1);
        }

        return result;
    }

    private MacroInfo computeMacros(FoodLog log) {
        if (log.recipe != null) {
            RecipeResponse response = macroService.toResponse(log.recipe);
            if (response.perServingMacros != null) {
                MacroInfo ps = response.perServingMacros;
                return new MacroInfo(
                        ps.calories * log.servings,
                        ps.protein * log.servings,
                        ps.carbs * log.servings,
                        ps.fat * log.servings
                );
            }
            return new MacroInfo(0, 0, 0, 0);
        } else if (log.ingredient != null) {
            double factor = (log.ingredientQuantity != null ? log.ingredientQuantity : 0) / 100.0;
            return new MacroInfo(
                    log.ingredient.caloriesPer100g * factor * log.servings,
                    log.ingredient.proteinPer100g * factor * log.servings,
                    log.ingredient.carbsPer100g * factor * log.servings,
                    log.ingredient.fatPer100g * factor * log.servings
            );
        } else {
            double cal = log.customCalories != null ? log.customCalories : 0;
            double pro = log.customProtein != null ? log.customProtein : 0;
            double carbs = log.customCarbs != null ? log.customCarbs : 0;
            double fat = log.customFat != null ? log.customFat : 0;
            return new MacroInfo(
                    cal * log.servings,
                    pro * log.servings,
                    carbs * log.servings,
                    fat * log.servings
            );
        }
    }
}
