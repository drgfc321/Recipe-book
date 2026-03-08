package com.recipebook.graphql;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class GraphQLTestHelper {

    public static Response graphql(String query) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("query", query))
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .extract().response();
    }

    public static Response graphql(String query, String token) {
        return given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(Map.of("query", query))
                .when()
                .post("/graphql")
                .then()
                .statusCode(200)
                .extract().response();
    }

    public static Response graphqlRaw(String query) {
        return given()
                .contentType(ContentType.JSON)
                .body(Map.of("query", query))
                .when()
                .post("/graphql");
    }

    public static String registerAndGetToken(String suffix) {
        String email = "test_" + suffix + "@test.com";
        String username = "testuser_" + suffix;
        String query = "mutation { register(email: \"" + email + "\", username: \"" + username
                + "\", password: \"TestPass123!\", language: \"en\") { token userId } }";
        Response resp = graphql(query);
        return resp.jsonPath().getString("data.register.token");
    }

    public static String registerAndGetToken() {
        return registerAndGetToken(UUID.randomUUID().toString().substring(0, 8));
    }

    public static String loginAndGetToken(String email, String password) {
        String query = "mutation { login(email: \"" + email + "\", password: \"" + password
                + "\") { token userId } }";
        Response resp = graphql(query);
        return resp.jsonPath().getString("data.login.token");
    }
}
