package com.recipebook.graphql;

import com.recipebook.dto.AuthResponse;
import com.recipebook.entity.User;
import com.recipebook.service.NutritionTargetService;
import com.recipebook.service.PasswordService;
import com.recipebook.service.TokenService;
import com.recipebook.exception.NotFoundException;
import com.recipebook.exception.ValidationException;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.time.LocalDateTime;
import java.util.List;

@GraphQLApi
public class AuthGraphQL {

    @Inject
    PasswordService passwordService;

    @Inject
    TokenService tokenService;

    @Inject
    NutritionTargetService nutritionTargetService;

    @Inject
    JsonWebToken jwt;

    @Mutation("register")
    @Description("Register a new user")
    @Transactional
    public AuthResponse register(@Name("email") String email,
                                 @Name("username") String username,
                                 @Name("password") String password,
                                 @Name("language") String language) {
        if (User.find("email", email).firstResult() != null) {
            throw new ValidationException("Email already registered");
        }
        if (User.find("username", username).firstResult() != null) {
            throw new ValidationException("Username already taken");
        }

        User user = new User();
        user.email = email;
        user.username = username;
        user.passwordHash = passwordService.hashPassword(password);
        user.role = "USER";
        user.language = language != null ? language : "en";
        user.createdAt = LocalDateTime.now();
        user.persist();

        nutritionTargetService.createDefaultTarget(user);

        String token = tokenService.generateToken(user);
        return new AuthResponse(token, user.id, user.username, user.email, user.role);
    }

    @Mutation("login")
    @Description("Login with email and password")
    @Transactional
    public AuthResponse login(@Name("email") String email,
                              @Name("password") String password) {
        User user = User.find("email", email).firstResult();
        if (user == null) {
            throw new ValidationException("Invalid email or password");
        }

        if (!passwordService.verifyPassword(password, user.passwordHash)) {
            throw new ValidationException("Invalid email or password");
        }

        user.lastLogin = LocalDateTime.now();

        String token = tokenService.generateToken(user);
        return new AuthResponse(token, user.id, user.username, user.email, user.role);
    }

    @Query("me")
    @Description("Get the currently authenticated user")
    @Authenticated
    public User me() {
        Long userId = Long.parseLong(jwt.getSubject());
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    @Query("users")
    @Description("List all users (admin)")
    @Authenticated
    public List<User> users() {
        return User.listAll();
    }

    @Mutation("deleteUser")
    @Description("Delete a user (admin)")
    @Authenticated
    @Transactional
    public boolean deleteUser(@Name("id") Long id) {
        User user = User.findById(id);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        user.delete();
        return true;
    }
}
