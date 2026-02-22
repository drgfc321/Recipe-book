package com.recipebook.service;

import com.recipebook.dto.DailyMealPlanResponse;
import com.recipebook.dto.MacroInfo;
import com.recipebook.dto.MealPlanResponse;
import com.recipebook.dto.RecipeResponse;
import com.recipebook.dto.WeeklyMealPlanResponse;
import com.recipebook.entity.MealPlan;
import com.recipebook.entity.MealSlot;
import com.recipebook.entity.Recipe;
import com.recipebook.entity.User;
import com.recipebook.graphql.MealPlanInput;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class MealPlanService {

    @Inject
    MacroCalculationService macroService;

    public List<MealPlanResponse> getMealPlansByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        List<MealPlan> plans = MealPlan.list(
                "user.id = ?1 and date >= ?2 and date <= ?3 order by date, mealSlot",
                userId, startDate, endDate);
        return plans.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public WeeklyMealPlanResponse getWeeklyMealPlan(Long userId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        List<MealPlan> plans = MealPlan.list(
                "user.id = ?1 and date >= ?2 and date <= ?3 order by date, mealSlot",
                userId, weekStart, weekEnd);

        Map<LocalDate, List<MealPlan>> byDate = plans.stream()
                .collect(Collectors.groupingBy(mp -> mp.date));

        double totalCal = 0, totalPro = 0, totalCarbs = 0, totalFat = 0;

        List<DailyMealPlanResponse> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            DailyMealPlanResponse day = new DailyMealPlanResponse();
            day.date = date;

            List<MealPlan> dayPlans = byDate.getOrDefault(date, List.of());
            day.meals = dayPlans.stream().map(this::toResponse).collect(Collectors.toList());

            double dayCal = 0, dayPro = 0, dayCarbs = 0, dayFat = 0;
            for (MealPlanResponse meal : day.meals) {
                if (meal.recipe != null && meal.recipe.perServingMacros != null) {
                    dayCal += meal.recipe.perServingMacros.calories;
                    dayPro += meal.recipe.perServingMacros.protein;
                    dayCarbs += meal.recipe.perServingMacros.carbs;
                    dayFat += meal.recipe.perServingMacros.fat;
                }
            }
            day.totalMacros = new MacroInfo(dayCal, dayPro, dayCarbs, dayFat);

            totalCal += dayCal;
            totalPro += dayPro;
            totalCarbs += dayCarbs;
            totalFat += dayFat;

            days.add(day);
        }

        WeeklyMealPlanResponse response = new WeeklyMealPlanResponse();
        response.weekStart = weekStart;
        response.weekEnd = weekEnd;
        response.days = days;
        response.totalMacros = new MacroInfo(totalCal, totalPro, totalCarbs, totalFat);
        response.averageDailyMacros = new MacroInfo(totalCal / 7, totalPro / 7, totalCarbs / 7, totalFat / 7);
        return response;
    }

    public MacroInfo getDailyMacroSummary(Long userId, LocalDate date) {
        List<MealPlan> plans = MealPlan.list("user.id = ?1 and date = ?2", userId, date);
        double cal = 0, pro = 0, carbs = 0, fat = 0;
        for (MealPlan plan : plans) {
            RecipeResponse recipe = macroService.toResponse(plan.recipe);
            if (recipe.perServingMacros != null) {
                cal += recipe.perServingMacros.calories;
                pro += recipe.perServingMacros.protein;
                carbs += recipe.perServingMacros.carbs;
                fat += recipe.perServingMacros.fat;
            }
        }
        return new MacroInfo(cal, pro, carbs, fat);
    }

    public MealPlanResponse assignMealPlan(Long userId, MealPlanInput input) throws GraphQLException {
        Recipe recipe = Recipe.findById(input.recipeId);
        if (recipe == null) {
            throw new GraphQLException("Recipe not found");
        }

        User user = User.findById(userId);

        MealPlan existing = MealPlan.find("user.id = ?1 and date = ?2 and mealSlot = ?3",
                userId, input.date, input.mealSlot).firstResult();

        if (existing != null) {
            existing.recipe = recipe;
            return toResponse(existing);
        }

        MealPlan plan = new MealPlan();
        plan.user = user;
        plan.date = input.date;
        plan.mealSlot = input.mealSlot;
        plan.recipe = recipe;
        plan.persist();
        return toResponse(plan);
    }

    public boolean removeMealPlan(Long userId, LocalDate date, MealSlot mealSlot) throws GraphQLException {
        MealPlan plan = MealPlan.find("user.id = ?1 and date = ?2 and mealSlot = ?3",
                userId, date, mealSlot).firstResult();
        if (plan == null) {
            throw new GraphQLException("Meal plan entry not found");
        }
        plan.delete();
        return true;
    }

    private MealPlanResponse toResponse(MealPlan plan) {
        MealPlanResponse response = new MealPlanResponse();
        response.id = plan.id;
        response.date = plan.date;
        response.mealSlot = plan.mealSlot;
        response.recipe = macroService.toResponse(plan.recipe);
        return response;
    }
}
