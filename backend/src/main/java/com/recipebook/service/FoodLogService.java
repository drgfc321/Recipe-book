package com.recipebook.service;

import com.recipebook.dto.DailyFoodLogResponse;
import com.recipebook.dto.FoodLogResponse;
import com.recipebook.dto.FoodSourceType;
import com.recipebook.dto.MacroInfo;
import com.recipebook.dto.PlannedMealStatus;
import com.recipebook.dto.UserNutritionTargetResponse;
import com.recipebook.entity.FoodLog;
import com.recipebook.entity.Ingredient;
import com.recipebook.entity.MealPlan;
import com.recipebook.entity.Recipe;
import com.recipebook.entity.User;
import com.recipebook.graphql.FoodLogInput;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class FoodLogService {

    @Inject
    MacroCalculationService macroService;

    @Inject
    NutritionTargetService nutritionTargetService;

    public DailyFoodLogResponse getDailyFoodLog(Long userId, LocalDate date) {
        List<FoodLog> logs = FoodLog.list(
                "user.id = ?1 and date = ?2 order by mealSlot, loggedAt",
                userId, date);
        List<FoodLogResponse> entries = logs.stream().map(this::toResponse).collect(Collectors.toList());

        double actualCal = 0, actualPro = 0, actualCarbs = 0, actualFat = 0;
        for (FoodLogResponse entry : entries) {
            if (entry.actualMacros != null) {
                actualCal += entry.actualMacros.calories;
                actualPro += entry.actualMacros.protein;
                actualCarbs += entry.actualMacros.carbs;
                actualFat += entry.actualMacros.fat;
            }
        }

        UserNutritionTargetResponse targets = nutritionTargetService.getTarget(userId);

        List<MealPlan> plans = MealPlan.list(
                "user.id = ?1 and date = ?2 order by mealSlot", userId, date);

        List<PlannedMealStatus> plannedMeals = new ArrayList<>();
        for (MealPlan plan : plans) {
            FoodLog matchingLog = logs.stream()
                    .filter(l -> plan.id.equals(l.sourceMealPlanId))
                    .findFirst().orElse(null);

            PlannedMealStatus status = new PlannedMealStatus();
            status.mealPlanId = plan.id;
            status.mealSlot = plan.mealSlot;
            status.recipe = macroService.toResponse(plan.recipe);
            status.logged = matchingLog != null;
            status.foodLogId = matchingLog != null ? matchingLog.id : null;
            plannedMeals.add(status);
        }

        DailyFoodLogResponse response = new DailyFoodLogResponse();
        response.date = date;
        response.entries = entries;
        response.totalActualMacros = new MacroInfo(actualCal, actualPro, actualCarbs, actualFat);
        response.targets = targets;
        response.plannedMeals = plannedMeals;
        return response;
    }

    public FoodLogResponse logFood(Long userId, FoodLogInput input) throws GraphQLException {
        boolean hasRecipe = input.recipeId != null;
        boolean hasIngredient = input.ingredientId != null;
        boolean hasCustom = input.customName != null && !input.customName.trim().isEmpty();

        int sourceCount = (hasRecipe ? 1 : 0) + (hasIngredient ? 1 : 0) + (hasCustom ? 1 : 0);
        if (sourceCount != 1) {
            throw new GraphQLException("Exactly one of recipeId, ingredientId, or customName must be provided");
        }

        User user = User.findById(userId);
        FoodLog log = new FoodLog();
        log.user = user;
        log.date = input.date;
        log.mealSlot = input.mealSlot;
        log.servings = input.servings != null ? input.servings : 1.0;
        log.loggedAt = LocalDateTime.now();

        if (hasRecipe) {
            Recipe recipe = Recipe.findById(input.recipeId);
            if (recipe == null) {
                throw new GraphQLException("Recipe not found");
            }
            log.recipe = recipe;
        } else if (hasIngredient) {
            Ingredient ingredient = Ingredient.findById(input.ingredientId);
            if (ingredient == null) {
                throw new GraphQLException("Ingredient not found");
            }
            if (input.ingredientQuantity == null || input.ingredientQuantity <= 0) {
                throw new GraphQLException("ingredientQuantity must be > 0");
            }
            log.ingredient = ingredient;
            log.ingredientQuantity = input.ingredientQuantity;
        } else {
            log.customName = input.customName.trim();
            if (input.customCalories == null || input.customCalories < 0) {
                throw new GraphQLException("Custom calories must be provided and >= 0");
            }
            log.customCalories = input.customCalories;
            log.customProtein = input.customProtein != null ? input.customProtein : 0.0;
            log.customCarbs = input.customCarbs != null ? input.customCarbs : 0.0;
            log.customFat = input.customFat != null ? input.customFat : 0.0;
            if (log.customProtein < 0 || log.customCarbs < 0 || log.customFat < 0) {
                throw new GraphQLException("Macro values must be >= 0");
            }
        }

        if (input.mealPlanId != null) {
            log.sourceMealPlanId = input.mealPlanId;
        }

        log.persist();
        return toResponse(log);
    }

    public FoodLogResponse updateFoodLog(Long userId, Long id, double servings) throws GraphQLException {
        FoodLog log = FoodLog.findById(id);
        if (log == null || !log.user.id.equals(userId)) {
            throw new GraphQLException("Food log entry not found");
        }
        log.servings = servings;
        return toResponse(log);
    }

    public boolean removeFoodLog(Long userId, Long id) throws GraphQLException {
        FoodLog log = FoodLog.findById(id);
        if (log == null || !log.user.id.equals(userId)) {
            throw new GraphQLException("Food log entry not found");
        }
        log.delete();
        return true;
    }

    private FoodLogResponse toResponse(FoodLog log) {
        FoodLogResponse response = new FoodLogResponse();
        response.id = log.id;
        response.date = log.date;
        response.mealSlot = log.mealSlot;
        response.servings = log.servings;
        response.loggedAt = log.loggedAt;

        if (log.recipe != null) {
            response.sourceType = FoodSourceType.RECIPE;
            response.recipe = macroService.toResponse(log.recipe);
            if (response.recipe != null && response.recipe.perServingMacros != null) {
                MacroInfo perServing = response.recipe.perServingMacros;
                response.actualMacros = new MacroInfo(
                        perServing.calories * log.servings,
                        perServing.protein * log.servings,
                        perServing.carbs * log.servings,
                        perServing.fat * log.servings
                );
            }
        } else if (log.ingredient != null) {
            response.sourceType = FoodSourceType.INGREDIENT;
            response.ingredientName = log.ingredient.name;
            response.ingredientQuantity = log.ingredientQuantity;
            double factor = (log.ingredientQuantity != null ? log.ingredientQuantity : 0) / 100.0;
            response.actualMacros = new MacroInfo(
                    log.ingredient.caloriesPer100g * factor * log.servings,
                    log.ingredient.proteinPer100g * factor * log.servings,
                    log.ingredient.carbsPer100g * factor * log.servings,
                    log.ingredient.fatPer100g * factor * log.servings
            );
        } else {
            response.sourceType = FoodSourceType.CUSTOM;
            response.customName = log.customName;
            double cal = log.customCalories != null ? log.customCalories : 0;
            double pro = log.customProtein != null ? log.customProtein : 0;
            double carbs = log.customCarbs != null ? log.customCarbs : 0;
            double fat = log.customFat != null ? log.customFat : 0;
            response.actualMacros = new MacroInfo(
                    cal * log.servings,
                    pro * log.servings,
                    carbs * log.servings,
                    fat * log.servings
            );
        }

        return response;
    }
}
