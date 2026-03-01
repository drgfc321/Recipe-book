package com.recipebook.graphql;

import com.recipebook.dto.DailyFoodLogResponse;
import com.recipebook.dto.FoodLogResponse;
import com.recipebook.service.FoodLogService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.math.BigInteger;
import java.time.LocalDate;

@GraphQLApi
public class FoodLogGraphQL {

    @Inject
    JsonWebToken jwt;

    @Inject
    FoodLogService foodLogService;

    @Query("dailyFoodLog")
    @Description("Get daily food log with nutrition summary and planned meals")
    @Authenticated
    public DailyFoodLogResponse getDailyFoodLog(@Name("date") LocalDate date) {
        Long userId = Long.parseLong(jwt.getSubject());
        return foodLogService.getDailyFoodLog(userId, date);
    }

    @Mutation("logFood")
    @Description("Log a food entry")
    @Authenticated
    @Transactional
    public FoodLogResponse logFood(@Name("input") FoodLogInput input) throws GraphQLException {
        Long userId = Long.parseLong(jwt.getSubject());
        return foodLogService.logFood(userId, input);
    }

    @Mutation("updateFoodLog")
    @Description("Update servings of a food log entry")
    @Authenticated
    @Transactional
    public FoodLogResponse updateFoodLog(@Name("id") BigInteger id,
                                          @Name("servings") Float servings) throws GraphQLException {
        Long userId = Long.parseLong(jwt.getSubject());
        return foodLogService.updateFoodLog(userId, id.longValue(), servings.doubleValue());
    }

    @Mutation("removeFoodLog")
    @Description("Remove a food log entry")
    @Authenticated
    @Transactional
    public boolean removeFoodLog(@Name("id") BigInteger id) throws GraphQLException {
        Long userId = Long.parseLong(jwt.getSubject());
        return foodLogService.removeFoodLog(userId, id.longValue());
    }
}
