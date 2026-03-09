package com.recipebook.graphql;

import com.recipebook.dto.DailyNutritionSummary;
import com.recipebook.service.NutritionHistoryService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@GraphQLApi
public class NutritionHistoryGraphQL {

    @Inject
    JsonWebToken jwt;

    @Inject
    NutritionHistoryService nutritionHistoryService;

    @Query("nutritionHistory")
    @Description("Get daily nutrition summaries (actuals vs targets) for a date range")
    @Authenticated
    public List<DailyNutritionSummary> nutritionHistory(
            @Name("startDate") LocalDate startDate,
            @Name("endDate") LocalDate endDate) {
        Long userId = Long.parseLong(jwt.getSubject());

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        if (days < 0 || days > 90) {
            throw new IllegalArgumentException("Date range must be between 0 and 90 days");
        }

        return nutritionHistoryService.getNutritionHistory(userId, startDate, endDate);
    }
}
