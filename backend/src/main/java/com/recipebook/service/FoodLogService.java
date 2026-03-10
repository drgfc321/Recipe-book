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
import com.recipebook.exception.NotFoundException;
import com.recipebook.exception.ValidationException;
import com.recipebook.graphql.FoodLogInput;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class FoodLogService {

    private static final Logger LOG = Logger.getLogger(FoodLogService.class);

    @Inject
    MacroCalculationService macroService;

    @Inject
    NutritionTargetService nutritionTargetService;

    public DailyFoodLogResponse getDailyFoodLog(Long userId, LocalDate date) {
        LOG.debugf("getDailyFoodLog userId=%d, date=%s", userId, date);
        List<FoodLog> logs = FoodLog.find(
                "FROM FoodLog fl " +
                "LEFT JOIN FETCH fl.recipe r " +
                "LEFT JOIN FETCH r.owner " +
                "LEFT JOIN FETCH r.ingredients ri " +
                "LEFT JOIN FETCH ri.ingredient " +
                "LEFT JOIN FETCH fl.ingredient " +
                "WHERE fl.user.id = ?1 AND fl.date = ?2 " +
                "ORDER BY fl.mealSlot, fl.loggedAt",
                userId, date).list();
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

        UserNutritionTargetResponse targets = nutritionTargetService.getTargetForDate(userId, date);

        List<MealPlan> plans = MealPlan.listWithRecipeDetails(
                "mp.user.id = ?1 and mp.date = ?2", userId, date);

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

    public FoodLogResponse logFood(Long userId, FoodLogInput input) {
        boolean hasRecipe = input.recipeId != null;
        boolean hasIngredient = input.ingredientId != null;
        boolean hasCustom = input.customName != null && !input.customName.trim().isEmpty();

        int sourceCount = (hasRecipe ? 1 : 0) + (hasIngredient ? 1 : 0) + (hasCustom ? 1 : 0);
        if (sourceCount != 1) {
            throw new ValidationException("Exactly one of recipeId, ingredientId, or customName must be provided");
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
                throw new NotFoundException("Recipe not found");
            }
            log.recipe = recipe;
        } else if (hasIngredient) {
            Ingredient ingredient = Ingredient.findById(input.ingredientId);
            if (ingredient == null) {
                throw new NotFoundException("Ingredient not found");
            }
            if (input.ingredientQuantity == null || input.ingredientQuantity <= 0) {
                throw new ValidationException("ingredientQuantity must be > 0");
            }
            log.ingredient = ingredient;
            log.ingredientQuantity = input.ingredientQuantity;
        } else {
            log.customName = input.customName.trim();
            if (input.customCalories == null || input.customCalories < 0) {
                throw new ValidationException("Custom calories must be provided and >= 0");
            }
            log.customCalories = input.customCalories;
            log.customProtein = input.customProtein != null ? input.customProtein : 0.0;
            log.customCarbs = input.customCarbs != null ? input.customCarbs : 0.0;
            log.customFat = input.customFat != null ? input.customFat : 0.0;
            if (log.customProtein < 0 || log.customCarbs < 0 || log.customFat < 0) {
                throw new ValidationException("Macro values must be >= 0");
            }
        }

        if (input.mealPlanId != null) {
            log.sourceMealPlanId = input.mealPlanId;
        }

        log.persist();
        LOG.debugf("Food logged: id=%d, userId=%d, date=%s, slot=%s", log.id, userId, input.date, input.mealSlot);
        return toResponse(log);
    }

    public FoodLogResponse updateFoodLog(Long userId, Long id, double servings)  {
        FoodLog log = FoodLog.find(
                "FROM FoodLog fl " +
                "LEFT JOIN FETCH fl.recipe r " +
                "LEFT JOIN FETCH r.owner " +
                "LEFT JOIN FETCH r.ingredients ri " +
                "LEFT JOIN FETCH ri.ingredient " +
                "LEFT JOIN FETCH fl.ingredient " +
                "WHERE fl.id = ?1 AND fl.user.id = ?2", id, userId).firstResult();
        if (log == null) {
            throw new NotFoundException("Food log entry not found");
        }
        log.servings = servings;
        LOG.debugf("Food log updated: id=%s, newServings=%.1f", (Object) id, servings);
        return toResponse(log);
    }

    public boolean removeFoodLog(Long userId, Long id)  {
        FoodLog log = FoodLog.find("id = ?1 and user.id = ?2", id, userId).firstResult();
        if (log == null) {
            throw new NotFoundException("Food log entry not found");
        }
        log.delete();
        LOG.debugf("Food log removed: id=%d, userId=%d", id, userId);
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
