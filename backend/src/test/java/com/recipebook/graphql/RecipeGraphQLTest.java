package com.recipebook.graphql;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class RecipeGraphQLTest {

    @Test
    void recipes_listSeededRecipes() {
        String query = "{ recipes { id name category totalMacros { calories } } }";
        Response resp = GraphQLTestHelper.graphql(query);

        List<?> recipes = resp.jsonPath().getList("data.recipes");
        assertThat(recipes, hasSize(greaterThanOrEqualTo(5)));
    }

    @Test
    void recipes_filterByCategory() {
        String query = "{ recipes(category: BREAKFAST) { id name category } }";
        Response resp = GraphQLTestHelper.graphql(query);

        List<String> categories = resp.jsonPath().getList("data.recipes.category");
        assertThat(categories, everyItem(equalTo("BREAKFAST")));
        assertThat(categories, hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    void recipes_searchByName() {
        String query = "{ recipes(search: \"Carbonara\") { id name } }";
        Response resp = GraphQLTestHelper.graphql(query);

        List<String> names = resp.jsonPath().getList("data.recipes.name");
        assertThat(names, hasSize(1));
        assertThat(names.get(0), containsString("Carbonara"));
    }

    @Test
    void recipe_byId() {
        // Look up a seeded recipe by name to get its ID
        String searchQuery = "{ recipes(search: \"Carbonara\") { id } }";
        Response searchResp = GraphQLTestHelper.graphql(searchQuery);
        int recipeId = searchResp.jsonPath().getInt("data.recipes[0].id");

        String query = "{ recipe(id: " + recipeId + ") { id name category difficulty servings ingredients { ingredientName quantity unit } } }";
        Response resp = GraphQLTestHelper.graphql(query);

        assertThat(resp.jsonPath().getInt("data.recipe.id"), equalTo(recipeId));
        assertThat(resp.jsonPath().getString("data.recipe.name"), containsString("Carbonara"));
        assertThat(resp.jsonPath().getString("data.recipe.category"), equalTo("DINNER"));
        assertThat(resp.jsonPath().getList("data.recipe.ingredients"), hasSize(greaterThan(0)));
    }

    @Test
    void createRecipe_authenticated() {
        String token = GraphQLTestHelper.registerAndGetToken();

        // Look up a seeded ingredient
        String ingQuery = "{ ingredients(search: \"Chicken Breast\") { id } }";
        Response ingResp = GraphQLTestHelper.graphql(ingQuery);
        int ingredientId = ingResp.jsonPath().getInt("data.ingredients[0].id");

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String recipeName = "TestRecipe_" + suffix;
        String mutation = "mutation { createRecipe(input: { name: \"" + recipeName
                + "\", category: LUNCH, difficulty: EASY, prepTime: 10, cookTime: 20, servings: 2"
                + ", description: \"Test\", instructions: \"Step 1\""
                + ", ingredients: [{ ingredientId: " + ingredientId + ", quantity: 200.0, unit: GRAMS }]"
                + " }) { id name ownerId ingredients { ingredientName quantity unit } } }";
        Response resp = GraphQLTestHelper.graphql(mutation, token);

        assertThat(resp.jsonPath().getInt("data.createRecipe.id"), greaterThan(0));
        assertThat(resp.jsonPath().getString("data.createRecipe.name"), equalTo(recipeName));
        assertThat(resp.jsonPath().getInt("data.createRecipe.ownerId"), greaterThan(0));
        assertThat(resp.jsonPath().getList("data.createRecipe.ingredients"), hasSize(1));
        assertThat(resp.jsonPath().getString("data.createRecipe.ingredients[0].ingredientName"), equalTo("Chicken Breast"));
    }

    @Test
    void updateRecipe_ownerOnly() {
        String token1 = GraphQLTestHelper.registerAndGetToken();
        String token2 = GraphQLTestHelper.registerAndGetToken();

        // Owner creates recipe
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String createMutation = "mutation { createRecipe(input: { name: \"UpdTest_" + suffix
                + "\", category: LUNCH, difficulty: EASY, prepTime: 5, cookTime: 10, servings: 1"
                + ", description: \"Before\", instructions: \"Step 1\" }) { id name } }";
        Response createResp = GraphQLTestHelper.graphql(createMutation, token1);
        int recipeId = createResp.jsonPath().getInt("data.createRecipe.id");

        // Owner updates — should succeed
        String updateMutation = "mutation { updateRecipe(id: " + recipeId + ", input: { name: \"Updated_" + suffix
                + "\", category: DINNER, difficulty: MEDIUM, prepTime: 15, cookTime: 30, servings: 4"
                + ", description: \"After\", instructions: \"Step 1 revised\" }) { id name description } }";
        Response updateResp = GraphQLTestHelper.graphql(updateMutation, token1);
        assertThat(updateResp.jsonPath().getString("data.updateRecipe.name"), equalTo("Updated_" + suffix));
        assertThat(updateResp.jsonPath().getString("data.updateRecipe.description"), equalTo("After"));

        // Non-owner tries to update — should fail
        String otherUpdate = "mutation { updateRecipe(id: " + recipeId + ", input: { name: \"Hacked\""
                + ", category: DINNER, difficulty: EASY, prepTime: 1, cookTime: 1, servings: 1"
                + ", description: \"X\", instructions: \"X\" }) { id } }";
        Response otherResp = GraphQLTestHelper.graphql(otherUpdate, token2);
        assertThat(otherResp.jsonPath().getList("errors"), hasSize(greaterThan(0)));
        assertThat(otherResp.jsonPath().getString("errors[0].message"), containsString("You can only edit your own recipes"));
    }

    @Test
    void deleteRecipe_ownerOnly() {
        String token = GraphQLTestHelper.registerAndGetToken();

        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String createMutation = "mutation { createRecipe(input: { name: \"DelTest_" + suffix
                + "\", category: SNACK, difficulty: EASY, prepTime: 1, cookTime: 1, servings: 1"
                + ", description: \"Del\", instructions: \"X\" }) { id } }";
        Response createResp = GraphQLTestHelper.graphql(createMutation, token);
        int recipeId = createResp.jsonPath().getInt("data.createRecipe.id");

        // Delete
        String deleteMutation = "mutation { deleteRecipe(id: " + recipeId + ") }";
        Response delResp = GraphQLTestHelper.graphql(deleteMutation, token);
        assertThat(delResp.jsonPath().getBoolean("data.deleteRecipe"), is(true));

        // Verify gone
        String getQuery = "{ recipe(id: " + recipeId + ") { id } }";
        Response getResp = GraphQLTestHelper.graphql(getQuery);
        assertThat(getResp.jsonPath().getList("errors"), hasSize(greaterThan(0)));
        assertThat(getResp.jsonPath().getString("errors[0].message"), containsString("Recipe not found"));
    }
}
