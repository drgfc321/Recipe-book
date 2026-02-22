package com.recipebook.dto;

public class MacroInfo {
    public double calories;
    public double protein;
    public double carbs;
    public double fat;

    public MacroInfo() {}

    public MacroInfo(double calories, double protein, double carbs, double fat) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }
}
