package com.recipebook.graphql;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ShoppingListGraphQLTest {

    private int getChickenBreastId() {
        Response resp = GraphQLTestHelper.graphql("{ ingredients(search: \"Chicken Breast\") { id } }");
        return resp.jsonPath().getInt("data.ingredients[0].id");
    }

    @Test
    void addShoppingListItem_success() {
        String token = GraphQLTestHelper.registerAndGetToken();
        int ingredientId = getChickenBreastId();

        String mutation = "mutation { addShoppingListItem(input: { ingredientId: " + ingredientId
                + ", quantity: 500.0, unit: GRAMS, weekStart: \"2026-04-06\" }) { id ingredientName quantity unit purchased } }";
        Response resp = GraphQLTestHelper.graphql(mutation, token);

        assertThat(resp.jsonPath().getInt("data.addShoppingListItem.id"), greaterThan(0));
        assertThat(resp.jsonPath().getString("data.addShoppingListItem.ingredientName"), equalTo("Chicken Breast"));
        assertThat(resp.jsonPath().getDouble("data.addShoppingListItem.quantity"), closeTo(500.0, 0.01));
        assertThat(resp.jsonPath().getString("data.addShoppingListItem.unit"), equalTo("GRAMS"));
        assertThat(resp.jsonPath().getBoolean("data.addShoppingListItem.purchased"), is(false));
    }

    @Test
    void toggleShoppingListItem() {
        String token = GraphQLTestHelper.registerAndGetToken();
        int ingredientId = getChickenBreastId();

        String addMutation = "mutation { addShoppingListItem(input: { ingredientId: " + ingredientId
                + ", quantity: 200.0, unit: GRAMS, weekStart: \"2026-04-13\" }) { id purchased } }";
        Response addResp = GraphQLTestHelper.graphql(addMutation, token);
        int itemId = addResp.jsonPath().getInt("data.addShoppingListItem.id");
        assertThat(addResp.jsonPath().getBoolean("data.addShoppingListItem.purchased"), is(false));

        String toggleMutation = "mutation { toggleShoppingListItem(id: " + itemId + ") { id purchased } }";
        Response toggleResp = GraphQLTestHelper.graphql(toggleMutation, token);
        assertThat(toggleResp.jsonPath().getBoolean("data.toggleShoppingListItem.purchased"), is(true));
    }

    @Test
    void clearShoppingList() {
        String token = GraphQLTestHelper.registerAndGetToken();
        int ingredientId = getChickenBreastId();

        // Add two items
        String add1 = "mutation { addShoppingListItem(input: { ingredientId: " + ingredientId
                + ", quantity: 100.0, unit: GRAMS, weekStart: \"2026-04-20\" }) { id } }";
        String add2 = "mutation { addShoppingListItem(input: { ingredientId: " + ingredientId
                + ", quantity: 200.0, unit: GRAMS, weekStart: \"2026-04-20\" }) { id } }";
        GraphQLTestHelper.graphql(add1, token);
        GraphQLTestHelper.graphql(add2, token);

        // Clear
        String clearMutation = "mutation { clearShoppingList(weekStart: \"2026-04-20\") }";
        Response clearResp = GraphQLTestHelper.graphql(clearMutation, token);
        assertThat(clearResp.jsonPath().getBoolean("data.clearShoppingList"), is(true));

        // Verify empty
        String query = "{ shoppingList(weekStart: \"2026-04-20\") { totalItems } }";
        Response listResp = GraphQLTestHelper.graphql(query, token);
        assertThat(listResp.jsonPath().getInt("data.shoppingList.totalItems"), equalTo(0));
    }
}
