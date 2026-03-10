package com.recipebook.graphql;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class PantryGraphQLTest {

    private int getChickenBreastId() {
        Response resp = GraphQLTestHelper.graphql("{ ingredients(search: \"Chicken Breast\") { id } }");
        return resp.jsonPath().getInt("data.ingredients[0].id");
    }

    private int getRiceId() {
        Response resp = GraphQLTestHelper.graphql("{ ingredients(search: \"Rice\") { id } }");
        return resp.jsonPath().getInt("data.ingredients[0].id");
    }

    @Test
    void addPantryItem_success() {
        String token = GraphQLTestHelper.registerAndGetToken();
        int ingredientId = getChickenBreastId();

        String mutation = "mutation { addPantryItem(input: { ingredientId: " + ingredientId
                + ", quantity: 500.0, unit: GRAMS }) { id ingredientName quantity unit } }";
        Response resp = GraphQLTestHelper.graphql(mutation, token);

        assertThat(resp.jsonPath().getInt("data.addPantryItem.id"), greaterThan(0));
        assertThat(resp.jsonPath().getString("data.addPantryItem.ingredientName"), equalTo("Chicken Breast"));
        assertThat(resp.jsonPath().getDouble("data.addPantryItem.quantity"), closeTo(500.0, 0.01));
        assertThat(resp.jsonPath().getString("data.addPantryItem.unit"), equalTo("GRAMS"));
    }

    @Test
    void addPantryItem_mergesExisting() {
        String token = GraphQLTestHelper.registerAndGetToken();
        int ingredientId = getRiceId();

        String mutation = "mutation { addPantryItem(input: { ingredientId: " + ingredientId
                + ", quantity: 200.0, unit: GRAMS }) { id quantity unit } }";
        GraphQLTestHelper.graphql(mutation, token);

        // Add again — should merge
        String mutation2 = "mutation { addPantryItem(input: { ingredientId: " + ingredientId
                + ", quantity: 300.0, unit: GRAMS }) { id quantity unit } }";
        Response resp = GraphQLTestHelper.graphql(mutation2, token);

        assertThat(resp.jsonPath().getDouble("data.addPantryItem.quantity"), closeTo(500.0, 0.01));
        assertThat(resp.jsonPath().getString("data.addPantryItem.unit"), equalTo("GRAMS"));
    }

    @Test
    void pantryItems_returnsUserItems() {
        String token = GraphQLTestHelper.registerAndGetToken();
        int ingredientId = getChickenBreastId();

        String mutation = "mutation { addPantryItem(input: { ingredientId: " + ingredientId
                + ", quantity: 300.0, unit: GRAMS }) { id } }";
        GraphQLTestHelper.graphql(mutation, token);

        String query = "{ pantryItems { id ingredientName quantity } }";
        Response resp = GraphQLTestHelper.graphql(query, token);

        List<?> items = resp.jsonPath().getList("data.pantryItems");
        assertThat(items, hasSize(greaterThanOrEqualTo(1)));

        List<String> names = resp.jsonPath().getList("data.pantryItems.ingredientName");
        assertThat(names, hasItem("Chicken Breast"));
    }

    @Test
    void removePantryItem_success() {
        String token = GraphQLTestHelper.registerAndGetToken();
        int ingredientId = getChickenBreastId();

        String addMutation = "mutation { addPantryItem(input: { ingredientId: " + ingredientId
                + ", quantity: 100.0, unit: GRAMS }) { id } }";
        Response addResp = GraphQLTestHelper.graphql(addMutation, token);
        int itemId = addResp.jsonPath().getInt("data.addPantryItem.id");

        String removeMutation = "mutation { removePantryItem(id: " + itemId + ") }";
        Response removeResp = GraphQLTestHelper.graphql(removeMutation, token);
        assertThat(removeResp.jsonPath().getBoolean("data.removePantryItem"), is(true));

        // Verify it's gone
        String query = "{ pantryItems { id } }";
        Response listResp = GraphQLTestHelper.graphql(query, token);
        List<Integer> ids = listResp.jsonPath().getList("data.pantryItems.id");
        assertThat(ids, not(hasItem(itemId)));
    }
}
