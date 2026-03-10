package com.recipebook.graphql;

import com.recipebook.dto.DayTypeEntry;
import com.recipebook.entity.DayType;
import com.recipebook.service.DayTypeService;
import io.quarkus.cache.CacheInvalidateAll;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@GraphQLApi
public class DayTypeGraphQL {

    @Inject
    JsonWebToken jwt;

    @Inject
    DayTypeService dayTypeService;

    @Query("dayTypesForWeek")
    @Description("Get day types (DEFAULT/TRAINING/REST) for a 7-day week")
    @Authenticated
    public List<DayTypeEntry> dayTypesForWeek(@Name("weekStart") LocalDate weekStart) {
        Long userId = Long.parseLong(jwt.getSubject());
        Map<LocalDate, DayType> map = dayTypeService.getDayTypesForWeek(userId, weekStart);

        List<DayTypeEntry> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            DayType dt = map.getOrDefault(date, DayType.DEFAULT);
            result.add(new DayTypeEntry(date, dt.name()));
        }
        return result;
    }

    @Mutation("setDayType")
    @Description("Set the day type (DEFAULT/TRAINING/REST) for a specific date")
    @Authenticated
    @Transactional
    @CacheInvalidateAll(cacheName = "weekly-mealplan")
    public DayTypeEntry setDayType(@Name("date") LocalDate date, @Name("dayType") String dayType) {
        Long userId = Long.parseLong(jwt.getSubject());
        DayType dt = DayType.valueOf(dayType);
        DayType result = dayTypeService.setDayType(userId, date, dt);
        return new DayTypeEntry(date, result.name());
    }
}
