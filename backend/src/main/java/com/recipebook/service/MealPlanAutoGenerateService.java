package com.recipebook.service;

import com.recipebook.dto.MacroInfo;
import com.recipebook.dto.MealPlanResponse;
import com.recipebook.dto.RecipeRecommendationResponse;
import com.recipebook.dto.RecipeResponse;
import com.recipebook.dto.UserNutritionTargetResponse;
import com.recipebook.entity.DayType;
import com.recipebook.entity.MealPlan;
import com.recipebook.entity.MealSlot;
import com.recipebook.entity.PantryItem;
import com.recipebook.entity.Recipe;
import com.recipebook.entity.RecipeCategory;
import com.recipebook.entity.User;
import io.quarkus.cache.CacheInvalidateAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class MealPlanAutoGenerateService {

    private static final Logger LOG = Logger.getLogger(MealPlanAutoGenerateService.class);

    private static final MealSlot[] SLOTS = {MealSlot.BREAKFAST, MealSlot.LUNCH, MealSlot.DINNER, MealSlot.SNACK};

    private static final Map<MealSlot, Double> SLOT_CALORIE_PROPORTIONS = Map.of(
            MealSlot.BREAKFAST, 0.25,
            MealSlot.LUNCH, 0.30,
            MealSlot.DINNER, 0.30,
            MealSlot.SNACK, 0.15
    );

    @Inject
    MacroCalculationService macroService;

    @Inject
    RecommendationService recommendationService;

    @Inject
    DayTypeService dayTypeService;

    @Inject
    NutritionTargetService nutritionTargetService;

    @CacheInvalidateAll(cacheName = "weekly-mealplan")
    public List<MealPlanResponse> autoGenerate(Long userId, LocalDate weekStart,
                                                boolean replaceExisting, boolean preferPantry) {
        LOG.infof("Auto-generating meal plan: userId=%d, weekStart=%s, replace=%b, pantry=%b",
                userId, weekStart, replaceExisting, preferPantry);

        // 1. Load all recipes
        List<Recipe> allRecipes = Recipe.listWithDetails("1=1", Map.of());
        if (allRecipes.isEmpty()) {
            LOG.info("No recipes available for auto-generation");
            return List.of();
        }

        // 2. Build pantry map
        Map<Long, Double> pantryMap = new HashMap<>();
        if (preferPantry) {
            List<PantryItem> pantryItems = PantryItem.find(
                    "FROM PantryItem pi JOIN FETCH pi.ingredient WHERE pi.user.id = ?1", userId).list();
            for (PantryItem pi : pantryItems) {
                pantryMap.put(pi.ingredient.id, MacroCalculationService.toGrams(pi.quantity, pi.unit));
            }
        }

        // 3. Precompute recipe responses and pantry scores
        Map<Long, RecipeResponse> recipeResponses = new HashMap<>();
        Map<Long, Double> pantryScores = new HashMap<>();
        for (Recipe recipe : allRecipes) {
            RecipeResponse resp = macroService.toResponse(recipe);
            recipeResponses.put(recipe.id, resp);
            if (preferPantry) {
                RecipeRecommendationResponse rec = recommendationService.analyzeRecipe(recipe, pantryMap);
                pantryScores.put(recipe.id, rec.matchPercent / 100.0);
            }
        }

        // 4. Load day types and existing meals
        Map<LocalDate, DayType> dayTypes = dayTypeService.getDayTypesForWeek(userId, weekStart);
        LocalDate weekEnd = weekStart.plusDays(6);
        List<MealPlan> existingPlans = MealPlan.listWithRecipeDetails(
                "mp.user.id = ?1 and mp.date >= ?2 and mp.date <= ?3",
                userId, weekStart, weekEnd);

        // Build lookup: date+slot -> existing MealPlan
        Map<String, MealPlan> existingMap = new HashMap<>();
        for (MealPlan mp : existingPlans) {
            existingMap.put(mp.date + ":" + mp.mealSlot, mp);
        }

        // 5. Precompute nutrition targets per day type (cache to avoid repeated lookups)
        Map<DayType, UserNutritionTargetResponse> targetCache = new HashMap<>();

        // 6. Track weekly recipe usage for repetition penalty
        Map<Long, Integer> weeklyUsage = new HashMap<>();
        // Count existing meals that will remain
        for (MealPlan mp : existingPlans) {
            if (!replaceExisting) {
                weeklyUsage.merge(mp.recipe.id, 1, Integer::sum);
            }
        }

        User user = User.findById(userId);
        List<MealPlanResponse> generated = new ArrayList<>();

        // 7. Greedy slot-by-slot assignment
        for (int d = 0; d < 7; d++) {
            LocalDate date = weekStart.plusDays(d);
            DayType dayType = dayTypes.getOrDefault(date, DayType.DEFAULT);
            UserNutritionTargetResponse target = targetCache.computeIfAbsent(dayType,
                    dt -> nutritionTargetService.getTarget(userId, dt));

            // Track consumed macros for the day from existing (non-replaced) meals
            double consumedCal = 0, consumedPro = 0, consumedCarbs = 0, consumedFat = 0;
            int emptySlots = 0;

            // First pass: count empty slots and consumed macros
            for (MealSlot slot : SLOTS) {
                String key = date + ":" + slot;
                MealPlan existing = existingMap.get(key);
                if (existing != null && !replaceExisting) {
                    RecipeResponse resp = recipeResponses.get(existing.recipe.id);
                    if (resp != null && resp.perServingMacros != null) {
                        consumedCal += resp.perServingMacros.calories;
                        consumedPro += resp.perServingMacros.protein;
                        consumedCarbs += resp.perServingMacros.carbs;
                        consumedFat += resp.perServingMacros.fat;
                    }
                } else {
                    emptySlots++;
                }
            }

            if (emptySlots == 0) continue;

            // Build list of remaining (empty) slots and set of recipe IDs already on this day
            List<MealSlot> remainingSlots = new ArrayList<>();
            Set<Long> dayRecipeIds = new HashSet<>();
            for (MealSlot s : SLOTS) {
                String k = date + ":" + s;
                MealPlan ex = existingMap.get(k);
                if (ex != null && !replaceExisting) {
                    dayRecipeIds.add(ex.recipe.id);
                } else {
                    remainingSlots.add(s);
                }
            }

            // Second pass: fill empty slots
            for (MealSlot slot : SLOTS) {
                String key = date + ":" + slot;
                MealPlan existing = existingMap.get(key);

                if (existing != null && !replaceExisting) {
                    continue; // skip occupied
                }

                if (existing != null && replaceExisting) {
                    existing.delete();
                    MealPlan.flush();
                }

                // Score all candidate recipes
                double bestScore = -1;
                Recipe bestRecipe = null;

                double remainingCal = target.calories - consumedCal;
                double remainingPro = target.protein - consumedPro;
                double remainingCarbs = target.carbs - consumedCarbs;
                double remainingFat = target.fat - consumedFat;

                for (Recipe recipe : allRecipes) {
                    RecipeResponse resp = recipeResponses.get(recipe.id);
                    if (resp == null || resp.perServingMacros == null) continue;

                    double score = computeScore(resp, recipe.category, slot,
                            remainingCal, remainingPro, remainingCarbs, remainingFat,
                            slot, remainingSlots,
                            weeklyUsage.getOrDefault(recipe.id, 0),
                            dayRecipeIds.contains(recipe.id),
                            preferPantry ? pantryScores.getOrDefault(recipe.id, 0.0) : 0.0,
                            preferPantry);

                    if (score > bestScore) {
                        bestScore = score;
                        bestRecipe = recipe;
                    }
                }

                if (bestRecipe == null) continue;

                // Persist
                MealPlan plan = new MealPlan();
                plan.user = user;
                plan.date = date;
                plan.mealSlot = slot;
                plan.recipe = bestRecipe;
                plan.persist();

                // Update tracking
                RecipeResponse bestResp = recipeResponses.get(bestRecipe.id);
                if (bestResp.perServingMacros != null) {
                    consumedCal += bestResp.perServingMacros.calories;
                    consumedPro += bestResp.perServingMacros.protein;
                    consumedCarbs += bestResp.perServingMacros.carbs;
                    consumedFat += bestResp.perServingMacros.fat;
                }
                weeklyUsage.merge(bestRecipe.id, 1, Integer::sum);
                dayRecipeIds.add(bestRecipe.id);
                remainingSlots.remove(slot);

                // Build response
                MealPlanResponse response = new MealPlanResponse();
                response.id = plan.id;
                response.date = plan.date;
                response.mealSlot = plan.mealSlot;
                response.recipe = bestResp;
                generated.add(response);
            }
        }

        LOG.infof("Auto-generated %d meals for userId=%d", generated.size(), userId);
        return generated;
    }

    private double computeScore(RecipeResponse recipe, RecipeCategory category, MealSlot slot,
                                 double remainingCal, double remainingPro,
                                 double remainingCarbs, double remainingFat,
                                 MealSlot currentSlot, List<MealSlot> remainingSlots,
                                 int weekUsageCount, boolean alreadyOnDay,
                                 double pantryScore, boolean preferPantry) {

        // Same-day duplicate penalty: effectively block recipe
        if (alreadyOnDay) return -1.0;

        // Weights
        double wMacro = preferPantry ? 0.40 : 0.55;
        double wPantry = preferPantry ? 0.25 : 0.0;
        double wCategory = 0.20;
        double wRepetition = 0.15;

        // 1. Macro fit score (slot-proportional)
        double macroFitScore = computeMacroFitScore(recipe, remainingCal, remainingPro,
                remainingCarbs, remainingFat, currentSlot, remainingSlots);

        // 2. Category match score
        double categoryScore = computeCategoryScore(category, slot);

        // 3. Repetition penalty
        double repetitionPenalty = 0.0;
        if (weekUsageCount == 1) repetitionPenalty = 0.5;
        else if (weekUsageCount >= 2) repetitionPenalty = 1.0;

        return wMacro * macroFitScore + wPantry * pantryScore + wCategory * categoryScore
                - wRepetition * repetitionPenalty;
    }

    private double computeMacroFitScore(RecipeResponse recipe,
                                         double remainingCal, double remainingPro,
                                         double remainingCarbs, double remainingFat,
                                         MealSlot currentSlot, List<MealSlot> remainingSlots) {
        if (remainingSlots.isEmpty()) return 0.0;

        MacroInfo macros = recipe.perServingMacros;

        // Slot-proportional ideal ratio
        double sumProportions = remainingSlots.stream()
                .mapToDouble(s -> SLOT_CALORIE_PROPORTIONS.getOrDefault(s, 0.15))
                .sum();
        double idealRatio = SLOT_CALORIE_PROPORTIONS.getOrDefault(currentSlot, 0.15) / sumProportions;

        // Compute actual ratio for each macro (how much of remaining this recipe fills)
        double calRatio = remainingCal > 0 ? macros.calories / remainingCal : (macros.calories == 0 ? idealRatio : 2.0);
        double proRatio = remainingPro > 0 ? macros.protein / remainingPro : (macros.protein == 0 ? idealRatio : 2.0);
        double carbsRatio = remainingCarbs > 0 ? macros.carbs / remainingCarbs : (macros.carbs == 0 ? idealRatio : 2.0);
        double fatRatio = remainingFat > 0 ? macros.fat / remainingFat : (macros.fat == 0 ? idealRatio : 2.0);

        // Weighted distance (cal 40%, protein 30%, carbs 15%, fat 15%)
        double distance = 0.40 * Math.abs(calRatio - idealRatio)
                + 0.30 * Math.abs(proRatio - idealRatio)
                + 0.15 * Math.abs(carbsRatio - idealRatio)
                + 0.15 * Math.abs(fatRatio - idealRatio);

        return Math.max(0.0, Math.min(1.0, 1.0 - distance));
    }

    private double computeCategoryScore(RecipeCategory recipeCategory, MealSlot slot) {
        if (recipeCategory == null) return 0.0;

        // Direct match: recipe category matches slot name
        String slotName = slot.name();
        String catName = recipeCategory.name();

        if (slotName.equals(catName)) return 1.0;

        // SNACK slot accepts DESSERT/SNACK
        if (slot == MealSlot.SNACK && recipeCategory == RecipeCategory.DESSERT) return 1.0;

        // DESSERT and OTHER get partial score for any slot
        if (recipeCategory == RecipeCategory.DESSERT || recipeCategory == RecipeCategory.OTHER) return 0.5;

        return 0.0;
    }
}
