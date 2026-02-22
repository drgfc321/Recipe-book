package com.recipebook.graphql;

import com.recipebook.entity.Ingredient;
import com.recipebook.entity.IngredientCategory;
import io.quarkus.security.Authenticated;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@GraphQLApi
public class IngredientGraphQL {

    @Query("ingredients")
    @Description("List ingredients with optional filters")
    public List<Ingredient> getIngredients(@Name("search") String search,
                                           @Name("category") IngredientCategory category) {
        StringBuilder query = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (search != null && !search.isBlank()) {
            query.append(" and lower(name) like :search");
            params.put("search", "%" + search.toLowerCase() + "%");
        }
        if (category != null) {
            query.append(" and category = :category");
            params.put("category", category);
        }

        return Ingredient.find(query.toString(), params).list();
    }

    @Query("ingredient")
    @Description("Get a single ingredient by ID")
    public Ingredient getIngredient(@Name("id") Long id) throws GraphQLException {
        Ingredient ingredient = Ingredient.findById(id);
        if (ingredient == null) {
            throw new GraphQLException("Ingredient not found");
        }
        return ingredient;
    }

    @Mutation("createIngredient")
    @Description("Create a new ingredient (authenticated)")
    @Authenticated
    @Transactional
    public Ingredient createIngredient(@Name("input") IngredientInput input) throws GraphQLException {
        if (Ingredient.find("lower(name)", input.name.toLowerCase()).firstResult() != null) {
            throw new GraphQLException("Ingredient with this name already exists");
        }

        Ingredient ingredient = new Ingredient();
        ingredient.name = input.name;
        ingredient.category = input.category;
        ingredient.caloriesPer100g = input.caloriesPer100g;
        ingredient.proteinPer100g = input.proteinPer100g;
        ingredient.carbsPer100g = input.carbsPer100g;
        ingredient.fatPer100g = input.fatPer100g;
        ingredient.persist();

        return ingredient;
    }

    @Mutation("updateIngredient")
    @Description("Update an ingredient (authenticated)")
    @Authenticated
    @Transactional
    public Ingredient updateIngredient(@Name("id") Long id, @Name("input") IngredientInput input) throws GraphQLException {
        Ingredient ingredient = Ingredient.findById(id);
        if (ingredient == null) {
            throw new GraphQLException("Ingredient not found");
        }

        Ingredient existing = Ingredient.find("lower(name)", input.name.toLowerCase()).firstResult();
        if (existing != null && !existing.id.equals(id)) {
            throw new GraphQLException("Ingredient with this name already exists");
        }

        ingredient.name = input.name;
        ingredient.category = input.category;
        ingredient.caloriesPer100g = input.caloriesPer100g;
        ingredient.proteinPer100g = input.proteinPer100g;
        ingredient.carbsPer100g = input.carbsPer100g;
        ingredient.fatPer100g = input.fatPer100g;

        return ingredient;
    }

    @Mutation("deleteIngredient")
    @Description("Delete an ingredient (authenticated)")
    @Authenticated
    @Transactional
    public boolean deleteIngredient(@Name("id") Long id) throws GraphQLException {
        Ingredient ingredient = Ingredient.findById(id);
        if (ingredient == null) {
            throw new GraphQLException("Ingredient not found");
        }
        ingredient.delete();
        return true;
    }
}
