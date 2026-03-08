package com.recipebook.graphql;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class MealPlanGraphQLTest {

    private int getSeededRecipeId() {
        Response resp = GraphQLTestHelper.graphql("{ recipes(search: \"Carbonara\") { id } }");
        return resp.jsonPath().getInt("data.recipes[0].id");
    }

    private int getSecondRecipeId() {
        Response resp = GraphQLTestHelper.graphql("{ recipes(search: \"Stir-Fry\") { id } }");
        return resp.jsonPath().getInt("data.recipes[0].id");
    }

    @Test
    void assignMealPlan_success() {
        String token = GraphQLTestHelper.registerAndGetToken();
        int recipeId = getSeededRecipeId();

        String mutation = "mutation { assignMealPlan(input: { date: \"2026-04-01\", mealSlot: LUNCH"
                + ", recipeId: " + recipeId + " }) { id date mealSlot recipe { id name } } }";
        Response resp = GraphQLTestHelper.graphql(mutation, token);

        assertThat(resp.jsonPath().getInt("data.assignMealPlan.id"), greaterThan(0));
        assertThat(resp.jsonPath().getString("data.assignMealPlan.date"), equalTo("2026-04-01"));
        assertThat(resp.jsonPath().getString("data.assignMealPlan.mealSlot"), equalTo("LUNCH"));
        assertThat(resp.jsonPath().getInt("data.assignMealPlan.recipe.id"), equalTo(recipeId));
    }

    @Test
    void assignMealPlan_replaceExisting() {
        String token = GraphQLTestHelper.registerAndGetToken();
        int recipeId1 = getSeededRecipeId();
        int recipeId2 = getSecondRecipeId();

        String mutation1 = "mutation { assignMealPlan(input: { date: \"2026-04-02\", mealSlot: DINNER"
                + ", recipeId: " + recipeId1 + " }) { id recipe { id } } }";
        GraphQLTestHelper.graphql(mutation1, token);

        // Assign different recipe to same slot — should replace
        String mutation2 = "mutation { assignMealPlan(input: { date: \"2026-04-02\", mealSlot: DINNER"
                + ", recipeId: " + recipeId2 + " }) { id recipe { id name } } }";
        Response resp = GraphQLTestHelper.graphql(mutation2, token);

        assertThat(resp.jsonPath().getInt("data.assignMealPlan.recipe.id"), equalTo(recipeId2));
    }

    @Test
    void mealPlansByDateRange() {
        String token = GraphQLTestHelper.registerAndGetToken();
        int recipeId = getSeededRecipeId();

        String m1 = "mutation { assignMealPlan(input: { date: \"2026-04-03\", mealSlot: BREAKFAST"
                + ", recipeId: " + recipeId + " }) { id } }";
        String m2 = "mutation { assignMealPlan(input: { date: \"2026-04-04\", mealSlot: LUNCH"
                + ", recipeId: " + recipeId + " }) { id } }";
        GraphQLTestHelper.graphql(m1, token);
        GraphQLTestHelper.graphql(m2, token);

        String query = "{ mealPlansByDateRange(startDate: \"2026-04-03\", endDate: \"2026-04-04\") { id date mealSlot } }";
        Response resp = GraphQLTestHelper.graphql(query, token);

        List<?> plans = resp.jsonPath().getList("data.mealPlansByDateRange");
        assertThat(plans, hasSize(2));
    }

    @Test
    void weeklyMealPlan_structure() {
        String token = GraphQLTestHelper.registerAndGetToken();
        int recipeId = getSeededRecipeId();

        String m1 = "mutation { assignMealPlan(input: { date: \"2026-04-06\", mealSlot: LUNCH"
                + ", recipeId: " + recipeId + " }) { id } }";
        String m2 = "mutation { assignMealPlan(input: { date: \"2026-04-07\", mealSlot: DINNER"
                + ", recipeId: " + recipeId + " }) { id } }";
        GraphQLTestHelper.graphql(m1, token);
        GraphQLTestHelper.graphql(m2, token);

        String query = "{ weeklyMealPlan(weekStart: \"2026-04-06\") { weekStart weekEnd days { date meals { mealSlot } totalMacros { calories } } totalMacros { calories } } }";
        Response resp = GraphQLTestHelper.graphql(query, token);

        assertThat(resp.jsonPath().getString("data.weeklyMealPlan.weekStart"), equalTo("2026-04-06"));
        assertThat(resp.jsonPath().getString("data.weeklyMealPlan.weekEnd"), is(notNullValue()));
        assertThat(resp.jsonPath().getList("data.weeklyMealPlan.days"), is(notNullValue()));
        assertThat(resp.jsonPath().getDouble("data.weeklyMealPlan.totalMacros.calories"), greaterThan(0.0));
    }

    @Test
    void removeMealPlan_success() {
        String token = GraphQLTestHelper.registerAndGetToken();
        int recipeId = getSeededRecipeId();

        String mutation = "mutation { assignMealPlan(input: { date: \"2026-04-10\", mealSlot: SNACK"
                + ", recipeId: " + recipeId + " }) { id } }";
        GraphQLTestHelper.graphql(mutation, token);

        String removeMutation = "mutation { removeMealPlan(date: \"2026-04-10\", mealSlot: SNACK) }";
        Response removeResp = GraphQLTestHelper.graphql(removeMutation, token);
        assertThat(removeResp.jsonPath().getBoolean("data.removeMealPlan"), is(true));

        // Verify empty
        String query = "{ mealPlansByDateRange(startDate: \"2026-04-10\", endDate: \"2026-04-10\") { id } }";
        Response listResp = GraphQLTestHelper.graphql(query, token);
        List<?> plans = listResp.jsonPath().getList("data.mealPlansByDateRange");
        assertThat(plans, hasSize(0));
    }
}
