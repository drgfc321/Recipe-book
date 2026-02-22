package com.recipebook.graphql;

import com.recipebook.entity.Unit;
import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.NonNull;

@Input("RecipeIngredientInput")
public class RecipeIngredientInput {

    @NonNull
    public Long ingredientId;

    @NonNull
    public Double quantity;

    @NonNull
    public Unit unit;
}
