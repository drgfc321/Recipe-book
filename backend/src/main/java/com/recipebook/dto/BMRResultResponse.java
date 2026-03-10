package com.recipebook.dto;

public class BMRResultResponse {
    public double bmr;
    public double tdee;
    public double adjustedCalories;
    public double proteinGrams;
    public double carbsGrams;
    public double fatGrams;

    public BMRResultResponse() {}

    public BMRResultResponse(double bmr, double tdee, double adjustedCalories,
                              double proteinGrams, double carbsGrams, double fatGrams) {
        this.bmr = bmr;
        this.tdee = tdee;
        this.adjustedCalories = adjustedCalories;
        this.proteinGrams = proteinGrams;
        this.carbsGrams = carbsGrams;
        this.fatGrams = fatGrams;
    }
}
