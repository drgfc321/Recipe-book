package com.recipebook.graphql;

import com.recipebook.dto.UserNutritionTargetResponse;
import com.recipebook.service.NutritionTargetService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

@GraphQLApi
public class NutritionTargetGraphQL {

    @Inject
    JsonWebToken jwt;

    @Inject
    NutritionTargetService nutritionTargetService;

    @Query("nutritionTarget")
    @Description("Get the current user's nutrition targets")
    @Authenticated
    public UserNutritionTargetResponse nutritionTarget() {
        Long userId = Long.parseLong(jwt.getSubject());
        return nutritionTargetService.getTarget(userId);
    }

    @Mutation("updateNutritionTarget")
    @Description("Update the current user's nutrition targets")
    @Authenticated
    @Transactional
    public UserNutritionTargetResponse updateNutritionTarget(
            @Name("calories") int calories,
            @Name("protein") double protein,
            @Name("carbs") double carbs,
            @Name("fat") double fat) {
        Long userId = Long.parseLong(jwt.getSubject());
        return nutritionTargetService.updateTarget(userId, calories, protein, carbs, fat);
    }
}
