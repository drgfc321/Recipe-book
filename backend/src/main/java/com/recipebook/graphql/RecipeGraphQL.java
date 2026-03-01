package com.recipebook.graphql;

import com.recipebook.dto.RecipeResponse;
import com.recipebook.entity.Ingredient;
import com.recipebook.entity.Recipe;
import com.recipebook.entity.RecipeCategory;
import com.recipebook.entity.Difficulty;
import com.recipebook.entity.RecipeIngredient;
import com.recipebook.entity.User;
import com.recipebook.service.MacroCalculationService;
import io.quarkus.security.Authenticated;
import io.smallrye.graphql.api.ErrorCode;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@GraphQLApi
public class RecipeGraphQL {

    @Inject
    JsonWebToken jwt;

    @Inject
    MacroCalculationService macroService;

    @Query("recipes")
    @Description("List recipes with optional filters")
    public List<RecipeResponse> getRecipes(@Name("category") RecipeCategory category,
                                           @Name("difficulty") Difficulty difficulty,
                                           @Name("search") String search,
                                           @Name("ingredientIds") List<Long> ingredientIds) {
        StringBuilder query = new StringBuilder("1=1");
        Map<String, Object> params = new HashMap<>();

        if (category != null) {
            query.append(" and category = :category");
            params.put("category", category);
        }
        if (difficulty != null) {
            query.append(" and difficulty = :difficulty");
            params.put("difficulty", difficulty);
        }
        if (search != null && !search.isBlank()) {
            query.append(" and lower(name) like :search");
            params.put("search", "%" + search.toLowerCase() + "%");
        }

        if (ingredientIds != null && !ingredientIds.isEmpty()) {
            query.append(" and id in (select ri.recipe.id from RecipeIngredient ri"
                + " where ri.ingredient.id in (:ingredientIds)"
                + " group by ri.recipe.id"
                + " having count(distinct ri.ingredient.id) = :ingredientCount)");
            params.put("ingredientIds", ingredientIds);
            params.put("ingredientCount", (long) ingredientIds.size());
        }

        query.append(" order by createdAt desc");

        List<Recipe> recipes = Recipe.find(query.toString(), params).list();
        return recipes.stream().map(macroService::toResponse).collect(Collectors.toList());
    }

    @Query("recipe")
    @Description("Get a single recipe by ID")
    public RecipeResponse getRecipe(@Name("id") Long id) throws GraphQLException {
        Recipe recipe = Recipe.findById(id);
        if (recipe == null) {
            throw new GraphQLException("Recipe not found");
        }
        return macroService.toResponse(recipe);
    }

    @Mutation("createRecipe")
    @Description("Create a new recipe (authenticated)")
    @Authenticated
    @Transactional
    public RecipeResponse createRecipe(@Name("input") RecipeInput input) throws GraphQLException {
        Long userId = Long.parseLong(jwt.getSubject());
        User owner = User.findById(userId);

        Recipe recipe = new Recipe();
        recipe.name = input.name;
        recipe.description = input.description;
        recipe.category = input.category;
        recipe.difficulty = input.difficulty;
        recipe.prepTime = input.prepTime;
        recipe.cookTime = input.cookTime;
        recipe.servings = input.servings;
        recipe.instructions = input.instructions;
        recipe.imageUrl = input.imageUrl;
        recipe.owner = owner;
        recipe.createdAt = LocalDateTime.now();

        recipe.persist();

        if (input.ingredients != null) {
            for (RecipeIngredientInput ri : input.ingredients) {
                Ingredient ingredient = Ingredient.findById(ri.ingredientId);
                if (ingredient == null) {
                    throw new GraphQLException("Ingredient not found: " + ri.ingredientId);
                }
                RecipeIngredient recipeIngredient = new RecipeIngredient();
                recipeIngredient.recipe = recipe;
                recipeIngredient.ingredient = ingredient;
                recipeIngredient.quantity = ri.quantity;
                recipeIngredient.unit = ri.unit;
                recipeIngredient.persist();
                recipe.ingredients.add(recipeIngredient);
            }
        }

        return macroService.toResponse(recipe);
    }

    @Mutation("updateRecipe")
    @Description("Update a recipe (owner only)")
    @Authenticated
    @Transactional
    public RecipeResponse updateRecipe(@Name("id") Long id, @Name("input") RecipeInput input) throws GraphQLException {
        Recipe recipe = Recipe.findById(id);
        if (recipe == null) {
            throw new GraphQLException("Recipe not found");
        }

        Long userId = Long.parseLong(jwt.getSubject());
        if (!recipe.owner.id.equals(userId)) {
            throw new GraphQLException("You can only edit your own recipes");
        }

        recipe.name = input.name;
        recipe.description = input.description;
        recipe.category = input.category;
        recipe.difficulty = input.difficulty;
        recipe.prepTime = input.prepTime;
        recipe.cookTime = input.cookTime;
        recipe.servings = input.servings;
        recipe.instructions = input.instructions;
        recipe.imageUrl = input.imageUrl;

        recipe.ingredients.clear();
        recipe.flush();

        if (input.ingredients != null) {
            for (RecipeIngredientInput ri : input.ingredients) {
                Ingredient ingredient = Ingredient.findById(ri.ingredientId);
                if (ingredient == null) {
                    throw new GraphQLException("Ingredient not found: " + ri.ingredientId);
                }
                RecipeIngredient recipeIngredient = new RecipeIngredient();
                recipeIngredient.recipe = recipe;
                recipeIngredient.ingredient = ingredient;
                recipeIngredient.quantity = ri.quantity;
                recipeIngredient.unit = ri.unit;
                recipeIngredient.persist();
                recipe.ingredients.add(recipeIngredient);
            }
        }

        return macroService.toResponse(recipe);
    }

    @Mutation("deleteRecipe")
    @Description("Delete a recipe (owner only)")
    @Authenticated
    @Transactional
    public boolean deleteRecipe(@Name("id") Long id) throws GraphQLException {
        Recipe recipe = Recipe.findById(id);
        if (recipe == null) {
            throw new GraphQLException("Recipe not found");
        }

        Long userId = Long.parseLong(jwt.getSubject());
        if (!recipe.owner.id.equals(userId)) {
            throw new GraphQLException("You can only delete your own recipes");
        }

        recipe.delete();
        return true;
    }
}
