package com.recipebook.graphql;

import com.recipebook.entity.IngredientCategory;
import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.NonNull;

@Input("IngredientInput")
public class IngredientInput {

    @NonNull
    public String name;

    @NonNull
    public IngredientCategory category;

    public double caloriesPer100g;
    public double proteinPer100g;
    public double carbsPer100g;
    public double fatPer100g;
}
