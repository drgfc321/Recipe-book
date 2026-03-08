package com.recipebook.dto;

public record UserPhysicalDataResponse(
        Double weightKg,
        Double heightCm,
        String birthDate,
        String gender,
        String activityLevel,
        String fitnessGoal
) {}
