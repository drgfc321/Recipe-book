package com.recipebook.dto;

public record BMRResultResponse(
        double bmr,
        double tdee,
        double adjustedCalories,
        double proteinGrams,
        double carbsGrams,
        double fatGrams
) {}
