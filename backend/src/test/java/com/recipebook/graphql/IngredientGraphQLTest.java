package com.recipebook.graphql;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class IngredientGraphQLTest {

    @Test
    void ingredients_listAll() {
        String query = "{ ingredients { id name category } }";
        Response resp = GraphQLTestHelper.graphql(query);

        List<?> ingredients = resp.jsonPath().getList("data.ingredients");
        assertThat(ingredients, hasSize(greaterThanOrEqualTo(27)));
    }

    @Test
    void ingredients_filterByCategory() {
        String query = "{ ingredients(category: MEAT) { id name category } }";
        Response resp = GraphQLTestHelper.graphql(query);

        List<String> categories = resp.jsonPath().getList("data.ingredients.category");
        assertThat(categories, everyItem(equalTo("MEAT")));
        assertThat(categories, hasSize(greaterThanOrEqualTo(1)));
    }

    @Test
    void createIngredient_authenticated() {
        String token = GraphQLTestHelper.registerAndGetToken();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String name = "TestIngredient_" + suffix;

        String mutation = "mutation { createIngredient(input: { name: \"" + name
                + "\", category: OTHER, caloriesPer100g: 100.0, proteinPer100g: 10.0"
                + ", carbsPer100g: 20.0, fatPer100g: 5.0 }) { id name category caloriesPer100g } }";
        Response resp = GraphQLTestHelper.graphql(mutation, token);

        assertThat(resp.jsonPath().getInt("data.createIngredient.id"), greaterThan(0));
        assertThat(resp.jsonPath().getString("data.createIngredient.name"), equalTo(name));
        assertThat(resp.jsonPath().getString("data.createIngredient.category"), equalTo("OTHER"));
        assertThat(resp.jsonPath().getDouble("data.createIngredient.caloriesPer100g"), closeTo(100.0, 0.01));
    }

    @Test
    void createIngredient_duplicateName_fails() {
        String token = GraphQLTestHelper.registerAndGetToken();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String name = "DupIng_" + suffix;

        String mutation = "mutation { createIngredient(input: { name: \"" + name
                + "\", category: OTHER, caloriesPer100g: 50.0, proteinPer100g: 5.0"
                + ", carbsPer100g: 10.0, fatPer100g: 2.0 }) { id } }";
        GraphQLTestHelper.graphql(mutation, token);

        // Same name again
        Response resp = GraphQLTestHelper.graphql(mutation, token);
        assertThat(resp.jsonPath().getList("errors"), hasSize(greaterThan(0)));
        assertThat(resp.jsonPath().getString("errors[0].message"), containsString("Ingredient with this name already exists"));
    }
}
