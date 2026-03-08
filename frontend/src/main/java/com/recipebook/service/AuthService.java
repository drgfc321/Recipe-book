package com.recipebook.service;

import com.recipebook.dto.AuthResponse;
import com.recipebook.dto.OAuthConfig;
import com.recipebook.dto.UserInfo;
import com.vaadin.flow.server.VaadinSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private final ApiClient apiClient;

    public AuthService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public AuthResponse login(String email, String password) {
        LOG.debug("Login attempt for email: {}", email);
        String query = """
                mutation($email: String!, $password: String!) {
                    login(email: $email, password: $password) {
                        token userId username email role avatarUrl authProvider
                    }
                }
                """;
        AuthResponse response = apiClient.mutatePublic(query,
                Map.of("email", email, "password", password),
                AuthResponse.class, "login");
        storeToken(response.token());
        storeUserInfo(response);
        LOG.info("Login successful for user: {}", response.username());
        return response;
    }

    public AuthResponse register(String email, String username, String password, String language) {
        LOG.debug("Register attempt for email: {}, username: {}", email, username);
        String query = """
                mutation($email: String!, $username: String!, $password: String!, $language: String!) {
                    register(email: $email, username: $username, password: $password, language: $language) {
                        token userId username email role avatarUrl authProvider
                    }
                }
                """;
        AuthResponse response = apiClient.mutatePublic(query,
                Map.of("email", email, "username", username, "password", password, "language", language),
                AuthResponse.class, "register");
        storeToken(response.token());
        storeUserInfo(response);
        LOG.info("Registration successful for user: {}", response.username());
        return response;
    }

    public void logout() {
        LOG.debug("User logging out");
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute("jwt_token", null);
            session.setAttribute("user_info", null);
        }
    }

    public boolean isLoggedIn() {
        return getToken().isPresent();
    }

    public Optional<String> getToken() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            Object token = session.getAttribute("jwt_token");
            if (token != null) {
                return Optional.of(token.toString());
            }
        }
        return Optional.empty();
    }

    public Optional<UserInfo> getCurrentUser() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            Object userInfo = session.getAttribute("user_info");
            if (userInfo instanceof UserInfo) {
                return Optional.of((UserInfo) userInfo);
            }
        }
        return Optional.empty();
    }

    public UserInfo fetchCurrentUser() {
        try {
            String query = """
                    query {
                        me { id username email role avatarUrl authProvider }
                    }
                    """;
            return apiClient.query(query, null, UserInfo.class, "me");
        } catch (RuntimeException e) {
            LOG.warn("fetchCurrentUser failed, logging out: {}", e.getMessage());
            logout();
            return null;
        }
    }

    private void storeToken(String token) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute("jwt_token", token);
        }
    }

    public boolean requestPasswordReset(String email) {
        LOG.debug("Password reset request for email: {}", email);
        String query = """
                mutation($email: String!) {
                    requestPasswordReset(email: $email)
                }
                """;
        Boolean result = apiClient.mutatePublic(query,
                Map.of("email", email),
                Boolean.class, "requestPasswordReset");
        LOG.info("Password reset requested for email: {}", email);
        return result != null && result;
    }

    public AuthResponse resetPassword(String token, String newPassword) {
        LOG.debug("Password reset with token");
        String query = """
                mutation($token: String!, $newPassword: String!) {
                    resetPassword(token: $token, newPassword: $newPassword) {
                        token userId username email role avatarUrl authProvider
                    }
                }
                """;
        AuthResponse response = apiClient.mutatePublic(query,
                Map.of("token", token, "newPassword", newPassword),
                AuthResponse.class, "resetPassword");
        storeToken(response.token());
        storeUserInfo(response);
        LOG.info("Password reset successful for user: {}", response.username());
        return response;
    }

    public AuthResponse changePassword(String currentPassword, String newPassword) {
        LOG.debug("Password change attempt");
        String query = """
                mutation($currentPassword: String!, $newPassword: String!) {
                    changePassword(currentPassword: $currentPassword, newPassword: $newPassword) {
                        token userId username email role avatarUrl authProvider
                    }
                }
                """;
        AuthResponse response = apiClient.mutate(query,
                Map.of("currentPassword", currentPassword, "newPassword", newPassword),
                AuthResponse.class, "changePassword");
        storeToken(response.token());
        storeUserInfo(response);
        LOG.info("Password changed successfully for user: {}", response.username());
        return response;
    }

    public AuthResponse updateProfile(String username, String email, String avatarUrl) {
        LOG.debug("Updating profile for user: {}", username);
        String query = """
                mutation($username: String!, $email: String!, $avatarUrl: String) {
                    updateProfile(username: $username, email: $email, avatarUrl: $avatarUrl) {
                        token userId username email role avatarUrl authProvider
                    }
                }
                """;
        Map<String, Object> variables = new java.util.HashMap<>();
        variables.put("username", username);
        variables.put("email", email);
        variables.put("avatarUrl", avatarUrl);
        AuthResponse response = apiClient.mutate(query, variables, AuthResponse.class, "updateProfile");
        storeToken(response.token());
        storeUserInfo(response);
        LOG.info("Profile updated for user: {}", response.username());
        return response;
    }

    public void loginWithOAuthToken(String token) {
        LOG.info("OAuth token login");
        storeToken(token);
        UserInfo user = fetchCurrentUser();
        if (user != null) {
            VaadinSession session = VaadinSession.getCurrent();
            if (session != null) {
                session.setAttribute("user_info", user);
            }
            LOG.info("OAuth login successful for user: {}", user.username());
        }
    }

    public OAuthConfig getOAuthConfig() {
        try {
            return apiClient.getWebClient()
                    .get()
                    .uri(apiClient.getBackendUrl() + "/api/auth/oauth-config")
                    .retrieve()
                    .bodyToMono(OAuthConfig.class)
                    .block();
        } catch (Exception e) {
            LOG.warn("Failed to fetch OAuth config: {}", e.getMessage());
            return new OAuthConfig(false, false);
        }
    }

    private void storeUserInfo(AuthResponse response) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            UserInfo userInfo = new UserInfo(
                    response.userId(),
                    response.username(),
                    response.email(),
                    response.role(),
                    response.avatarUrl(),
                    response.authProvider()
            );
            session.setAttribute("user_info", userInfo);
        }
    }
}
