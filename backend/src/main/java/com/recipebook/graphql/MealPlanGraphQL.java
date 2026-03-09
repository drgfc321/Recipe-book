package com.recipebook.graphql;

import com.recipebook.dto.MacroInfo;
import com.recipebook.dto.MealPlanResponse;
import com.recipebook.dto.WeeklyMealPlanResponse;
import com.recipebook.entity.MealSlot;
import com.recipebook.service.MealPlanAutoGenerateService;
import com.recipebook.service.MealPlanService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.util.List;

@GraphQLApi
public class MealPlanGraphQL {

    private static final Logger LOG = Logger.getLogger(MealPlanGraphQL.class);

    @Inject
    JsonWebToken jwt;

    @Inject
    MealPlanService mealPlanService;

    @Inject
    MealPlanAutoGenerateService autoGenerateService;

    @Query("mealPlansByDateRange")
    @Description("List meal plans for a date range")
    @Authenticated
    public List<MealPlanResponse> getMealPlansByDateRange(@Name("startDate") LocalDate startDate,
                                                          @Name("endDate") LocalDate endDate) {
        Long userId = Long.parseLong(jwt.getSubject());
        LOG.debugf("getMealPlansByDateRange userId=%d, %s to %s", userId, startDate, endDate);
        return mealPlanService.getMealPlansByDateRange(userId, startDate, endDate);
    }

    @Query("weeklyMealPlan")
    @Description("Get structured weekly meal plan with macro summaries")
    @Authenticated
    public WeeklyMealPlanResponse getWeeklyMealPlan(@Name("weekStart") LocalDate weekStart) {
        Long userId = Long.parseLong(jwt.getSubject());
        LOG.debugf("getWeeklyMealPlan userId=%d, weekStart=%s", userId, weekStart);
        return mealPlanService.getWeeklyMealPlan(userId, weekStart);
    }

    @Query("dailyMacroSummary")
    @Description("Get macro summary for a single day")
    @Authenticated
    public MacroInfo getDailyMacroSummary(@Name("date") LocalDate date) {
        Long userId = Long.parseLong(jwt.getSubject());
        LOG.debugf("getDailyMacroSummary userId=%d, date=%s", userId, date);
        return mealPlanService.getDailyMacroSummary(userId, date);
    }

    @Mutation("assignMealPlan")
    @Description("Assign a recipe to a meal slot (creates or replaces)")
    @Authenticated
    @Transactional
    public MealPlanResponse assignMealPlan(@Name("input") MealPlanInput input) {
        Long userId = Long.parseLong(jwt.getSubject());
        LOG.infof("Assigning meal plan: userId=%d, date=%s, slot=%s, recipeId=%d", userId, input.date, input.mealSlot, input.recipeId);
        return mealPlanService.assignMealPlan(userId, input);
    }

    @Mutation("removeMealPlan")
    @Description("Remove a meal plan entry")
    @Authenticated
    @Transactional
    public boolean removeMealPlan(@Name("date") LocalDate date, @Name("mealSlot") MealSlot mealSlot) {
        Long userId = Long.parseLong(jwt.getSubject());
        LOG.infof("Removing meal plan: userId=%d, date=%s, slot=%s", userId, date, mealSlot);
        return mealPlanService.removeMealPlan(userId, date, mealSlot);
    }

    @Mutation("autoGenerateMealPlan")
    @Description("Auto-generate meal plan for a week")
    @Authenticated
    @Transactional
    public List<MealPlanResponse> autoGenerateMealPlan(@Name("input") AutoGenerateInput input) throws GraphQLException {
        Long userId = Long.parseLong(jwt.getSubject());
        LOG.infof("Auto-generating meal plan: userId=%d, weekStart=%s", userId, input.weekStart);
        try {
            return autoGenerateService.autoGenerate(userId, input.weekStart, input.replaceExisting, input.preferPantry);
        } catch (Exception e) {
            LOG.errorf(e, "Auto-generate failed for userId=%d, weekStart=%s", userId, input.weekStart);
            throw new GraphQLException("Auto-generate failed: " + e.getMessage());
        }
    }
}
