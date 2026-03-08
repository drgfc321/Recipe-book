package com.recipebook.dto;

public class UserPhysicalDataResponse {
    public Double weightKg;
    public Double heightCm;
    public String birthDate;
    public String gender;
    public String activityLevel;
    public String fitnessGoal;

    public UserPhysicalDataResponse() {}

    public UserPhysicalDataResponse(Double weightKg, Double heightCm, String birthDate,
                                     String gender, String activityLevel, String fitnessGoal) {
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.birthDate = birthDate;
        this.gender = gender;
        this.activityLevel = activityLevel;
        this.fitnessGoal = fitnessGoal;
    }
}
