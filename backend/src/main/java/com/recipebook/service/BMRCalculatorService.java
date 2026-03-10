package com.recipebook.service;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.Period;

@ApplicationScoped
public class BMRCalculatorService {

    public record BMRResult(
            double bmr,
            double tdee,
            double adjustedCalories,
            double proteinGrams,
            double carbsGrams,
            double fatGrams
    ) {}

    public int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    public double calculateBMR(double weightKg, double heightCm, int age, String gender) {
        // Mifflin-St Jeor equation
        double base = 10 * weightKg + 6.25 * heightCm - 5 * age;
        return "MALE".equalsIgnoreCase(gender) ? base + 5 : base - 161;
    }

    public double calculateTDEE(double bmr, String activityLevel) {
        double multiplier = switch (activityLevel.toUpperCase()) {
            case "SEDENTARY" -> 1.2;
            case "LIGHTLY_ACTIVE" -> 1.375;
            case "MODERATE" -> 1.55;
            case "ACTIVE" -> 1.725;
            case "VERY_ACTIVE" -> 1.9;
            default -> 1.2;
        };
        return bmr * multiplier;
    }

    public double applyGoalAdjustment(double tdee, String fitnessGoal) {
        return switch (fitnessGoal.toUpperCase()) {
            case "LOSE" -> tdee - 500;
            case "GAIN" -> tdee + 300;
            default -> tdee; // MAINTAIN
        };
    }

    public BMRResult calculate(double weightKg, double heightCm, LocalDate birthDate,
                                String gender, String activityLevel, String fitnessGoal,
                                int proteinPct, int carbsPct, int fatPct) {
        int age = calculateAge(birthDate);
        double bmr = calculateBMR(weightKg, heightCm, age, gender);
        double tdee = calculateTDEE(bmr, activityLevel);
        double adjustedCalories = applyGoalAdjustment(tdee, fitnessGoal);

        double proteinGrams = (adjustedCalories * proteinPct / 100.0) / 4.0;
        double carbsGrams = (adjustedCalories * carbsPct / 100.0) / 4.0;
        double fatGrams = (adjustedCalories * fatPct / 100.0) / 9.0;

        return new BMRResult(
                Math.round(bmr * 100.0) / 100.0,
                Math.round(tdee * 100.0) / 100.0,
                Math.round(adjustedCalories * 100.0) / 100.0,
                Math.round(proteinGrams * 100.0) / 100.0,
                Math.round(carbsGrams * 100.0) / 100.0,
                Math.round(fatGrams * 100.0) / 100.0
        );
    }
}
