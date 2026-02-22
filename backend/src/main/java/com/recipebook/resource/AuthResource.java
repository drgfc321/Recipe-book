package com.recipebook.resource;

import com.recipebook.dto.AuthResponse;
import com.recipebook.dto.LoginRequest;
import com.recipebook.dto.RegisterRequest;
import com.recipebook.entity.User;
import com.recipebook.service.PasswordService;
import com.recipebook.service.TokenService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.LocalDateTime;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    PasswordService passwordService;

    @Inject
    TokenService tokenService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/register")
    @Transactional
    public Response register(@Valid RegisterRequest request) {
        // Check if email already exists
        if (User.find("email", request.email).firstResult() != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorMessage("Email already registered"))
                    .build();
        }

        // Check if username already exists
        if (User.find("username", request.username).firstResult() != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorMessage("Username already taken"))
                    .build();
        }

        // Create new user
        User user = new User();
        user.email = request.email;
        user.username = request.username;
        user.passwordHash = passwordService.hashPassword(request.password);
        user.role = "USER";
        user.language = request.language != null ? request.language : "en";
        user.createdAt = LocalDateTime.now();
        user.persist();

        // Generate token
        String token = tokenService.generateToken(user);

        return Response.status(Response.Status.CREATED)
                .entity(new AuthResponse(token, user.id, user.username, user.email, user.role))
                .build();
    }

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request) {
        // Find user by email
        User user = User.find("email", request.email).firstResult();

        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorMessage("Invalid email or password"))
                    .build();
        }

        // Verify password
        if (!passwordService.verifyPassword(request.password, user.passwordHash)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(new ErrorMessage("Invalid email or password"))
                    .build();
        }

        // Update last login
        updateLastLogin(user.id);

        // Generate token
        String token = tokenService.generateToken(user);

        return Response.ok(new AuthResponse(token, user.id, user.username, user.email, user.role))
                .build();
    }

    @Transactional
    void updateLastLogin(Long userId) {
        User user = User.findById(userId);
        if (user != null) {
            user.lastLogin = LocalDateTime.now();
        }
    }

    @GET
    @Path("/me")
    @Authenticated
    public Response me(@Context SecurityContext securityContext) {
        Long userId = Long.parseLong(jwt.getSubject());
        User user = User.findById(userId);

        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorMessage("User not found"))
                    .build();
        }

        return Response.ok(user).build();
    }

    // Simple error message class
    public static class ErrorMessage {
        public String message;

        public ErrorMessage(String message) {
            this.message = message;
        }
    }
}
