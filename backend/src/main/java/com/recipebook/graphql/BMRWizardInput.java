package com.recipebook.graphql;

import org.eclipse.microprofile.graphql.Input;

@Input("BMRWizardInput")
public class BMRWizardInput {

    public double weightKg;
    public double heightCm;
    public String birthDate; // yyyy-MM-dd
    public String gender;    // MALE, FEMALE
    public String activityLevel; // SEDENTARY, LIGHTLY_ACTIVE, MODERATE, ACTIVE, VERY_ACTIVE
    public String fitnessGoal;   // LOSE, MAINTAIN, GAIN
    public int proteinPct;
    public int carbsPct;
    public int fatPct;
}
