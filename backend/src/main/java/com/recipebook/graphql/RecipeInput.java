package com.recipebook.graphql;

import com.recipebook.entity.Difficulty;
import com.recipebook.entity.RecipeCategory;
import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.NonNull;

import java.util.List;

@Input("RecipeInput")
public class RecipeInput {

    @NonNull
    public String name;

    public String description;

    @NonNull
    public RecipeCategory category;

    @NonNull
    public Difficulty difficulty;

    public int prepTime;
    public int cookTime;
    public int servings;
    public String instructions;
    public String imageUrl;

    public List<RecipeIngredientInput> ingredients;
}
