package com.recipebook.graphql;

import com.recipebook.dto.BMRResultResponse;
import com.recipebook.dto.UserPhysicalDataResponse;
import com.recipebook.entity.User;
import com.recipebook.service.BMRCalculatorService;
import com.recipebook.service.NutritionTargetService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.time.LocalDate;

@GraphQLApi
public class BMRGraphQL {

    private static final Logger LOG = Logger.getLogger(BMRGraphQL.class);

    @Inject
    JsonWebToken jwt;

    @Inject
    BMRCalculatorService bmrCalculatorService;

    @Inject
    NutritionTargetService nutritionTargetService;

    @Query("previewBMR")
    @Description("Preview BMR/TDEE calculation without saving")
    @Authenticated
    public BMRResultResponse previewBMR(@Name("input") BMRWizardInput input) {
        validateMacroPercentages(input);
        LocalDate birthDate = LocalDate.parse(input.birthDate);
        BMRCalculatorService.BMRResult result = bmrCalculatorService.calculate(
                input.weightKg, input.heightCm, birthDate,
                input.gender, input.activityLevel, input.fitnessGoal,
                input.proteinPct, input.carbsPct, input.fatPct
        );
        return toResponse(result);
    }

    @Mutation("saveBMRWizard")
    @Description("Save BMR wizard data and update nutrition targets")
    @Authenticated
    @Transactional
    public BMRResultResponse saveBMRWizard(@Name("input") BMRWizardInput input) {
        validateMacroPercentages(input);
        Long userId = Long.parseLong(jwt.getSubject());
        LocalDate birthDate = LocalDate.parse(input.birthDate);

        // Save physical data to user
        User user = User.findById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        user.weightKg = input.weightKg;
        user.heightCm = input.heightCm;
        user.birthDate = birthDate;
        user.gender = input.gender;
        user.activityLevel = input.activityLevel;
        user.fitnessGoal = input.fitnessGoal;
        user.persist();

        LOG.infof("Saved physical data for userId=%d: weight=%.1f, height=%.1f, gender=%s, activity=%s, goal=%s",
                userId, input.weightKg, input.heightCm, input.gender, input.activityLevel, input.fitnessGoal);

        // Calculate and save nutrition targets
        BMRCalculatorService.BMRResult result = bmrCalculatorService.calculate(
                input.weightKg, input.heightCm, birthDate,
                input.gender, input.activityLevel, input.fitnessGoal,
                input.proteinPct, input.carbsPct, input.fatPct
        );

        nutritionTargetService.updateTarget(userId,
                (int) Math.round(result.adjustedCalories()),
                result.proteinGrams(),
                result.carbsGrams(),
                result.fatGrams()
        );

        LOG.infof("Updated nutrition targets for userId=%d: cal=%.0f, pro=%.1f, carbs=%.1f, fat=%.1f",
                userId, result.adjustedCalories(), result.proteinGrams(), result.carbsGrams(), result.fatGrams());

        return toResponse(result);
    }

    @Query("userPhysicalData")
    @Description("Get stored physical data for pre-filling the wizard")
    @Authenticated
    public UserPhysicalDataResponse userPhysicalData() {
        Long userId = Long.parseLong(jwt.getSubject());
        User user = User.findById(userId);
        if (user == null) {
            return null;
        }
        return new UserPhysicalDataResponse(
                user.weightKg,
                user.heightCm,
                user.birthDate != null ? user.birthDate.toString() : null,
                user.gender,
                user.activityLevel,
                user.fitnessGoal
        );
    }

    private void validateMacroPercentages(BMRWizardInput input) {
        int sum = input.proteinPct + input.carbsPct + input.fatPct;
        if (Math.abs(sum - 100) > 1) {
            throw new ValidationException("Macro percentages must sum to 100% (got " + sum + "%)");
        }
    }

    private BMRResultResponse toResponse(BMRCalculatorService.BMRResult result) {
        return new BMRResultResponse(
                result.bmr(), result.tdee(), result.adjustedCalories(),
                result.proteinGrams(), result.carbsGrams(), result.fatGrams()
        );
    }
}
