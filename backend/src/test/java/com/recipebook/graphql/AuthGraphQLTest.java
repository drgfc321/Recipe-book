package com.recipebook.graphql;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class AuthGraphQLTest {

    @Test
    void register_success() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "reg_" + suffix + "@test.com";
        String username = "reg_" + suffix;
        String query = "mutation { register(email: \"" + email + "\", username: \"" + username
                + "\", password: \"Pass123!\", language: \"en\") { token userId username email role } }";

        Response resp = GraphQLTestHelper.graphql(query);

        assertThat(resp.jsonPath().getString("data.register.token"), is(notNullValue()));
        assertThat(resp.jsonPath().getInt("data.register.userId"), greaterThan(0));
        assertThat(resp.jsonPath().getString("data.register.username"), equalTo(username));
        assertThat(resp.jsonPath().getString("data.register.email"), equalTo(email));
        assertThat(resp.jsonPath().getString("data.register.role"), equalTo("USER"));
    }

    @Test
    void register_duplicateEmail_fails() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "dup_" + suffix + "@test.com";
        String username1 = "dup1_" + suffix;
        String username2 = "dup2_" + suffix;

        String query1 = "mutation { register(email: \"" + email + "\", username: \"" + username1
                + "\", password: \"Pass123!\", language: \"en\") { token } }";
        GraphQLTestHelper.graphql(query1);

        String query2 = "mutation { register(email: \"" + email + "\", username: \"" + username2
                + "\", password: \"Pass123!\", language: \"en\") { token } }";
        Response resp = GraphQLTestHelper.graphql(query2);

        assertThat(resp.jsonPath().getList("errors"), hasSize(greaterThan(0)));
        assertThat(resp.jsonPath().getString("errors[0].message"), containsString("Email already registered"));
    }

    @Test
    void login_success() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "login_" + suffix + "@test.com";
        String username = "login_" + suffix;
        String password = "Pass123!";

        String regQuery = "mutation { register(email: \"" + email + "\", username: \"" + username
                + "\", password: \"" + password + "\", language: \"en\") { userId } }";
        Response regResp = GraphQLTestHelper.graphql(regQuery);
        int registeredId = regResp.jsonPath().getInt("data.register.userId");

        String loginQuery = "mutation { login(email: \"" + email + "\", password: \"" + password
                + "\") { token userId } }";
        Response loginResp = GraphQLTestHelper.graphql(loginQuery);

        assertThat(loginResp.jsonPath().getString("data.login.token"), is(notNullValue()));
        assertThat(loginResp.jsonPath().getInt("data.login.userId"), equalTo(registeredId));
    }

    @Test
    void login_wrongPassword_fails() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "wrongpw_" + suffix + "@test.com";
        String username = "wrongpw_" + suffix;

        String regQuery = "mutation { register(email: \"" + email + "\", username: \"" + username
                + "\", password: \"Pass123!\", language: \"en\") { token } }";
        GraphQLTestHelper.graphql(regQuery);

        String loginQuery = "mutation { login(email: \"" + email + "\", password: \"WrongPass!\") { token } }";
        Response resp = GraphQLTestHelper.graphql(loginQuery);

        assertThat(resp.jsonPath().getList("errors"), hasSize(greaterThan(0)));
        assertThat(resp.jsonPath().getString("errors[0].message"), containsString("Invalid email or password"));
    }

    @Test
    void me_returnsAuthenticatedUser() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "me_" + suffix + "@test.com";
        String username = "me_" + suffix;

        String regQuery = "mutation { register(email: \"" + email + "\", username: \"" + username
                + "\", password: \"Pass123!\", language: \"en\") { token } }";
        Response regResp = GraphQLTestHelper.graphql(regQuery);
        String token = regResp.jsonPath().getString("data.register.token");

        String meQuery = "{ me { username email } }";
        Response meResp = GraphQLTestHelper.graphql(meQuery, token);

        assertThat(meResp.jsonPath().getString("data.me.username"), equalTo(username));
        assertThat(meResp.jsonPath().getString("data.me.email"), equalTo(email));
    }
}
