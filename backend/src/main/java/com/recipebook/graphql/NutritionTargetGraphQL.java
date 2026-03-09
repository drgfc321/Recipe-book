package com.recipebook.graphql;

import com.recipebook.dto.UserNutritionTargetResponse;
import com.recipebook.entity.DayType;
import com.recipebook.service.NutritionTargetService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.List;

@GraphQLApi
public class NutritionTargetGraphQL {

    @Inject
    JsonWebToken jwt;

    @Inject
    NutritionTargetService nutritionTargetService;

    @Query("nutritionTarget")
    @Description("Get the current user's nutrition targets for a given day type (defaults to DEFAULT)")
    @Authenticated
    public UserNutritionTargetResponse nutritionTarget(@Name("dayType") String dayType) {
        Long userId = Long.parseLong(jwt.getSubject());
        DayType dt = (dayType != null) ? DayType.valueOf(dayType) : DayType.DEFAULT;
        return nutritionTargetService.getTarget(userId, dt);
    }

    @Query("allNutritionTargets")
    @Description("Get all nutrition targets (DEFAULT, TRAINING, REST) for the current user")
    @Authenticated
    public List<UserNutritionTargetResponse> allNutritionTargets() {
        Long userId = Long.parseLong(jwt.getSubject());
        return nutritionTargetService.getAllTargets(userId);
    }

    @Mutation("updateNutritionTarget")
    @Description("Update the current user's nutrition targets for a given day type")
    @Authenticated
    @Transactional
    public UserNutritionTargetResponse updateNutritionTarget(
            @Name("calories") int calories,
            @Name("protein") double protein,
            @Name("carbs") double carbs,
            @Name("fat") double fat,
            @Name("dayType") String dayType) {
        Long userId = Long.parseLong(jwt.getSubject());
        DayType dt = (dayType != null) ? DayType.valueOf(dayType) : DayType.DEFAULT;
        return nutritionTargetService.updateTarget(userId, dt, calories, protein, carbs, fat);
    }
}
