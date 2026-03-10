package com.recipebook.rest;

import com.recipebook.dto.OAuthUserProfile;
import com.recipebook.entity.User;
import com.recipebook.service.OAuthService;
import com.recipebook.service.TokenService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Path("/api/auth")
public class OAuthResource {

    private static final Logger LOG = Logger.getLogger(OAuthResource.class);

    @Inject
    OAuthService oAuthService;

    @Inject
    TokenService tokenService;

    @ConfigProperty(name = "oauth.frontend.url", defaultValue = "http://localhost:8081")
    String frontendUrl;

    @GET
    @Path("/google")
    public Response googleLogin() {
        if (!oAuthService.isGoogleEnabled()) {
            LOG.warn("Google OAuth not configured");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        LOG.info("Redirecting to Google OAuth");
        return Response.status(Response.Status.FOUND)
                .location(URI.create(oAuthService.getGoogleAuthUrl()))
                .build();
    }

    @GET
    @Path("/callback/google")
    public Response googleCallback(@QueryParam("code") String code, @QueryParam("error") String error) {
        if (error != null) {
            LOG.warnf("Google OAuth error: %s", error);
            return redirectToLoginWithError();
        }
        if (code == null || code.isBlank()) {
            LOG.warn("Google OAuth callback: missing code");
            return redirectToLoginWithError();
        }

        try {
            String accessToken = oAuthService.exchangeGoogleCode(code);
            OAuthUserProfile profile = oAuthService.fetchGoogleProfile(accessToken);
            User user = oAuthService.findOrCreateUser("GOOGLE", profile);
            String jwt = tokenService.generateToken(user);
            LOG.infof("Google OAuth login successful for user: %s (id=%d)", user.username, user.id);
            return redirectWithToken(jwt);
        } catch (Exception e) {
            LOG.error("Google OAuth callback failed", e);
            return redirectToLoginWithError();
        }
    }

    @GET
    @Path("/github")
    public Response githubLogin() {
        if (!oAuthService.isGithubEnabled()) {
            LOG.warn("GitHub OAuth not configured");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        LOG.info("Redirecting to GitHub OAuth");
        return Response.status(Response.Status.FOUND)
                .location(URI.create(oAuthService.getGithubAuthUrl()))
                .build();
    }

    @GET
    @Path("/callback/github")
    public Response githubCallback(@QueryParam("code") String code, @QueryParam("error") String error) {
        if (error != null) {
            LOG.warnf("GitHub OAuth error: %s", error);
            return redirectToLoginWithError();
        }
        if (code == null || code.isBlank()) {
            LOG.warn("GitHub OAuth callback: missing code");
            return redirectToLoginWithError();
        }

        try {
            String accessToken = oAuthService.exchangeGithubCode(code);
            OAuthUserProfile profile = oAuthService.fetchGithubProfile(accessToken);
            User user = oAuthService.findOrCreateUser("GITHUB", profile);
            String jwt = tokenService.generateToken(user);
            LOG.infof("GitHub OAuth login successful for user: %s (id=%d)", user.username, user.id);
            return redirectWithToken(jwt);
        } catch (Exception e) {
            LOG.error("GitHub OAuth callback failed", e);
            return redirectToLoginWithError();
        }
    }

    @GET
    @Path("/oauth-config")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOAuthConfig() {
        return Response.ok(Map.of(
                "googleEnabled", oAuthService.isGoogleEnabled(),
                "githubEnabled", oAuthService.isGithubEnabled()
        )).build();
    }

    private Response redirectWithToken(String token) {
        String url = frontendUrl + "/oauth-callback?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        return Response.status(Response.Status.FOUND)
                .location(URI.create(url))
                .build();
    }

    private Response redirectToLoginWithError() {
        String url = frontendUrl + "/login?error=oauth_failed";
        return Response.status(Response.Status.FOUND)
                .location(URI.create(url))
                .build();
    }
}
