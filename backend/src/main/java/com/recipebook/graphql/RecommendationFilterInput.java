package com.recipebook.graphql;

import com.recipebook.entity.Difficulty;
import com.recipebook.entity.RecipeCategory;
import org.eclipse.microprofile.graphql.Input;

import java.util.Objects;

@Input("RecommendationFilterInput")
public class RecommendationFilterInput {

    public RecipeCategory category;

    public Difficulty difficulty;

    public Integer maxMissingIngredients;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecommendationFilterInput that = (RecommendationFilterInput) o;
        return category == that.category && difficulty == that.difficulty
                && Objects.equals(maxMissingIngredients, that.maxMissingIngredients);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, difficulty, maxMissingIngredients);
    }
}
