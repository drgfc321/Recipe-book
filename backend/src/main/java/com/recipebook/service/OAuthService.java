package com.recipebook.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipebook.dto.OAuthUserProfile;
import com.recipebook.entity.User;
import com.recipebook.exception.OAuthException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@ApplicationScoped
public class OAuthService {

    private static final Logger LOG = Logger.getLogger(OAuthService.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    @Inject
    NutritionTargetService nutritionTargetService;

    @ConfigProperty(name = "oauth.google.client-id")
    Optional<String> googleClientId;

    @ConfigProperty(name = "oauth.google.client-secret")
    Optional<String> googleClientSecret;

    @ConfigProperty(name = "oauth.google.redirect-uri")
    Optional<String> googleRedirectUri;

    @ConfigProperty(name = "oauth.github.client-id")
    Optional<String> githubClientId;

    @ConfigProperty(name = "oauth.github.client-secret")
    Optional<String> githubClientSecret;

    @ConfigProperty(name = "oauth.github.redirect-uri")
    Optional<String> githubRedirectUri;

    public boolean isGoogleEnabled() {
        return googleClientId.filter(s -> !s.isBlank()).isPresent()
                && googleClientSecret.filter(s -> !s.isBlank()).isPresent();
    }

    public boolean isGithubEnabled() {
        return githubClientId.filter(s -> !s.isBlank()).isPresent()
                && githubClientSecret.filter(s -> !s.isBlank()).isPresent();
    }

    public String getGoogleAuthUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + encode(googleClientId.orElse(""))
                + "&redirect_uri=" + encode(googleRedirectUri.orElse(""))
                + "&response_type=code"
                + "&scope=" + encode("openid email profile")
                + "&access_type=offline";
    }

    public String getGithubAuthUrl() {
        return "https://github.com/login/oauth/authorize"
                + "?client_id=" + encode(githubClientId.orElse(""))
                + "&redirect_uri=" + encode(githubRedirectUri.orElse(""))
                + "&scope=" + encode("user:email read:user");
    }

    public String exchangeGoogleCode(String code) {
        LOG.infof("Exchanging Google authorization code");
        String body = "code=" + encode(code)
                + "&client_id=" + encode(googleClientId.orElse(""))
                + "&client_secret=" + encode(googleClientSecret.orElse(""))
                + "&redirect_uri=" + encode(googleRedirectUri.orElse(""))
                + "&grant_type=authorization_code";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOG.errorf("Google token exchange failed: status=%d, body=%s", response.statusCode(), response.body());
                throw new OAuthException("Google token exchange failed");
            }

            JsonNode json = mapper.readTree(response.body());
            return json.get("access_token").asText();
        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Google token exchange error", e);
            throw new OAuthException("Google token exchange failed");
        }
    }

    public OAuthUserProfile fetchGoogleProfile(String accessToken) {
        LOG.info("Fetching Google user profile");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.googleapis.com/oauth2/v2/userinfo"))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOG.errorf("Google profile fetch failed: status=%d", response.statusCode());
                throw new OAuthException("Failed to fetch Google profile");
            }

            JsonNode json = mapper.readTree(response.body());
            return new OAuthUserProfile(
                    json.get("id").asText(),
                    json.get("email").asText(),
                    json.has("name") ? json.get("name").asText() : json.get("email").asText().split("@")[0],
                    json.has("picture") ? json.get("picture").asText() : null
            );
        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Google profile fetch error", e);
            throw new OAuthException("Failed to fetch Google profile");
        }
    }

    public String exchangeGithubCode(String code) {
        LOG.infof("Exchanging GitHub authorization code");
        String body = "code=" + encode(code)
                + "&client_id=" + encode(githubClientId.orElse(""))
                + "&client_secret=" + encode(githubClientSecret.orElse(""))
                + "&redirect_uri=" + encode(githubRedirectUri.orElse(""));

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://github.com/login/oauth/access_token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                LOG.errorf("GitHub token exchange failed: status=%d, body=%s", response.statusCode(), response.body());
                throw new OAuthException("GitHub token exchange failed");
            }

            JsonNode json = mapper.readTree(response.body());
            if (json.has("error")) {
                LOG.errorf("GitHub token exchange error: %s", json.get("error").asText());
                throw new OAuthException("GitHub token exchange failed");
            }
            return json.get("access_token").asText();
        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("GitHub token exchange error", e);
            throw new OAuthException("GitHub token exchange failed");
        }
    }

    public OAuthUserProfile fetchGithubProfile(String accessToken) {
        LOG.info("Fetching GitHub user profile");
        try {
            // Fetch user info
            HttpRequest userRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/user"))
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> userResponse = httpClient.send(userRequest, HttpResponse.BodyHandlers.ofString());
            if (userResponse.statusCode() != 200) {
                LOG.errorf("GitHub user fetch failed: status=%d", userResponse.statusCode());
                throw new OAuthException("Failed to fetch GitHub profile");
            }

            JsonNode userJson = mapper.readTree(userResponse.body());
            String email = userJson.has("email") && !userJson.get("email").isNull()
                    ? userJson.get("email").asText() : null;

            // If email is not public, fetch from emails endpoint
            if (email == null) {
                HttpRequest emailRequest = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.github.com/user/emails"))
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Accept", "application/json")
                        .GET()
                        .build();

                HttpResponse<String> emailResponse = httpClient.send(emailRequest, HttpResponse.BodyHandlers.ofString());
                if (emailResponse.statusCode() != 200) {
                    LOG.errorf("GitHub emails fetch failed: status=%d", emailResponse.statusCode());
                    throw new OAuthException("Failed to fetch GitHub email");
                }

                JsonNode emails = mapper.readTree(emailResponse.body());
                for (JsonNode e : emails) {
                    if (e.get("primary").asBoolean()) {
                        email = e.get("email").asText();
                        break;
                    }
                }
            }

            if (email == null) {
                throw new OAuthException("No email found in GitHub account");
            }

            String name = userJson.has("name") && !userJson.get("name").isNull()
                    ? userJson.get("name").asText()
                    : userJson.get("login").asText();

            String avatarUrl = userJson.has("avatar_url") && !userJson.get("avatar_url").isNull()
                    ? userJson.get("avatar_url").asText() : null;

            return new OAuthUserProfile(
                    String.valueOf(userJson.get("id").asLong()),
                    email,
                    name,
                    avatarUrl
            );
        } catch (OAuthException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("GitHub profile fetch error", e);
            throw new OAuthException("Failed to fetch GitHub profile");
        }
    }

    @Transactional
    public User findOrCreateUser(String provider, OAuthUserProfile profile) {
        LOG.infof("Finding or creating user for provider=%s, providerId=%s, email=%s",
                provider, profile.providerId(), profile.email());

        // 1. Find by provider + providerId
        User user = User.find("authProvider = ?1 and providerId = ?2", provider, profile.providerId())
                .firstResult();
        if (user != null) {
            LOG.infof("Found existing OAuth user: id=%d", user.id);
            user.lastLogin = LocalDateTime.now();
            if (profile.avatarUrl() != null && user.avatarUrl == null) {
                user.avatarUrl = profile.avatarUrl();
            }
            return user;
        }

        // 2. Find by email (link existing account)
        user = User.find("email", profile.email()).firstResult();
        if (user != null) {
            LOG.infof("Linking OAuth provider=%s to existing user: id=%d", provider, user.id);
            user.authProvider = provider;
            user.providerId = profile.providerId();
            user.lastLogin = LocalDateTime.now();
            if (profile.avatarUrl() != null && user.avatarUrl == null) {
                user.avatarUrl = profile.avatarUrl();
            }
            return user;
        }

        // 3. Create new user
        LOG.infof("Creating new OAuth user: provider=%s, email=%s", provider, profile.email());
        user = new User();
        user.email = profile.email();
        user.username = generateUniqueUsername(profile.name());
        user.authProvider = provider;
        user.providerId = profile.providerId();
        user.role = "USER";
        user.language = "en";
        user.avatarUrl = profile.avatarUrl();
        user.createdAt = LocalDateTime.now();
        user.lastLogin = LocalDateTime.now();
        user.persist();

        nutritionTargetService.createDefaultTarget(user);

        LOG.infof("OAuth user created: id=%d, username=%s", user.id, user.username);
        return user;
    }

    private String generateUniqueUsername(String name) {
        String base = name.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (base.isBlank()) {
            base = "user";
        }
        if (base.length() > 20) {
            base = base.substring(0, 20);
        }

        String candidate = base;
        int suffix = 1;
        while (User.find("username", candidate).firstResult() != null) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
