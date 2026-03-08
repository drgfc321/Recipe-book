package com.recipebook.graphql;

import com.recipebook.dto.MacroInfo;
import com.recipebook.dto.MealPlanResponse;
import com.recipebook.dto.WeeklyMealPlanResponse;
import com.recipebook.entity.MealSlot;
import com.recipebook.service.MealPlanService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.LocalDate;
import java.util.List;

@GraphQLApi
public class MealPlanGraphQL {

    @Inject
    JsonWebToken jwt;

    @Inject
    MealPlanService mealPlanService;

    @Query("mealPlansByDateRange")
    @Description("List meal plans for a date range")
    @Authenticated
    public List<MealPlanResponse> getMealPlansByDateRange(@Name("startDate") LocalDate startDate,
                                                          @Name("endDate") LocalDate endDate) {
        Long userId = Long.parseLong(jwt.getSubject());
        return mealPlanService.getMealPlansByDateRange(userId, startDate, endDate);
    }

    @Query("weeklyMealPlan")
    @Description("Get structured weekly meal plan with macro summaries")
    @Authenticated
    public WeeklyMealPlanResponse getWeeklyMealPlan(@Name("weekStart") LocalDate weekStart) {
        Long userId = Long.parseLong(jwt.getSubject());
        return mealPlanService.getWeeklyMealPlan(userId, weekStart);
    }

    @Query("dailyMacroSummary")
    @Description("Get macro summary for a single day")
    @Authenticated
    public MacroInfo getDailyMacroSummary(@Name("date") LocalDate date) {
        Long userId = Long.parseLong(jwt.getSubject());
        return mealPlanService.getDailyMacroSummary(userId, date);
    }

    @Mutation("assignMealPlan")
    @Description("Assign a recipe to a meal slot (creates or replaces)")
    @Authenticated
    @Transactional
    public MealPlanResponse assignMealPlan(@Name("input") MealPlanInput input) {
        Long userId = Long.parseLong(jwt.getSubject());
        return mealPlanService.assignMealPlan(userId, input);
    }

    @Mutation("removeMealPlan")
    @Description("Remove a meal plan entry")
    @Authenticated
    @Transactional
    public boolean removeMealPlan(@Name("date") LocalDate date, @Name("mealSlot") MealSlot mealSlot) {
        Long userId = Long.parseLong(jwt.getSubject());
        return mealPlanService.removeMealPlan(userId, date, mealSlot);
    }
}
