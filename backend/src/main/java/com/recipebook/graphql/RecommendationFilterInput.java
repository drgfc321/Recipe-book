package com.recipebook.graphql;

import com.recipebook.entity.Difficulty;
import com.recipebook.entity.RecipeCategory;
import org.eclipse.microprofile.graphql.Input;

@Input("RecommendationFilterInput")
public class RecommendationFilterInput {

    public RecipeCategory category;

    public Difficulty difficulty;

    public Integer maxMissingIngredients;
}
