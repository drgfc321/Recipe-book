package com.recipebook.graphql;

import com.recipebook.dto.AuthResponse;
import com.recipebook.entity.User;
import com.recipebook.service.NutritionTargetService;
import com.recipebook.service.PasswordService;
import com.recipebook.service.TokenService;
import com.recipebook.exception.NotFoundException;
import com.recipebook.exception.ValidationException;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.graphql.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@GraphQLApi
public class AuthGraphQL {

    private static final Logger LOG = Logger.getLogger(AuthGraphQL.class);

    @Inject
    PasswordService passwordService;

    @Inject
    TokenService tokenService;

    @Inject
    NutritionTargetService nutritionTargetService;

    @Inject
    Mailer mailer;

    @Inject
    JsonWebToken jwt;

    @ConfigProperty(name = "password.reset.base-url")
    String resetBaseUrl;

    @ConfigProperty(name = "password.reset.expiry-minutes", defaultValue = "15")
    int resetExpiryMinutes;

    @Mutation("register")
    @Description("Register a new user")
    @Transactional
    public AuthResponse register(@Name("email") String email,
                                 @Name("username") String username,
                                 @Name("password") String password,
                                 @Name("language") String language) {
        if (User.find("email", email).firstResult() != null) {
            LOG.warnf("Register failed: email already registered: %s", email);
            throw new ValidationException("Email already registered");
        }
        if (User.find("username", username).firstResult() != null) {
            LOG.warnf("Register failed: username already taken: %s", username);
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
        LOG.infof("User registered successfully: %s (id=%d)", user.username, user.id);
        return new AuthResponse(token, user.id, user.username, user.email, user.role, user.avatarUrl, user.authProvider);
    }

    @Mutation("login")
    @Description("Login with email and password")
    @Transactional
    public AuthResponse login(@Name("email") String email,
                              @Name("password") String password) {
        User user = User.find("email", email).firstResult();
        if (user == null) {
            LOG.warnf("Login failed: unknown email: %s", email);
            throw new ValidationException("Invalid email or password");
        }

        if (!"LOCAL".equals(user.authProvider)) {
            LOG.warnf("Login failed: user %s uses %s authentication", email, user.authProvider);
            throw new ValidationException("This account uses " + user.authProvider + " login. Please use the " + user.authProvider + " button.");
        }

        if (!passwordService.verifyPassword(password, user.passwordHash)) {
            LOG.warnf("Login failed: wrong password for user: %s", email);
            throw new ValidationException("Invalid email or password");
        }

        user.lastLogin = LocalDateTime.now();

        String token = tokenService.generateToken(user);
        LOG.infof("User logged in: %s (id=%d)", user.username, user.id);
        return new AuthResponse(token, user.id, user.username, user.email, user.role, user.avatarUrl, user.authProvider);
    }

    @Query("me")
    @Description("Get the currently authenticated user")
    @Authenticated
    public User me() {
        Long userId = Long.parseLong(jwt.getSubject());
        LOG.debugf("Query me for userId=%d", userId);
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
        LOG.debug("Query all users");
        return User.listAll();
    }

    @Mutation("updateProfile")
    @Description("Update the authenticated user's profile")
    @Authenticated
    @Transactional
    public AuthResponse updateProfile(@Name("username") String username,
                                      @Name("email") String email,
                                      @Name("avatarUrl") String avatarUrl) {
        Long userId = Long.parseLong(jwt.getSubject());
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        if (username != null && !username.equals(user.username)) {
            User existing = User.find("username = ?1 and id != ?2", username, userId).firstResult();
            if (existing != null) {
                throw new ValidationException("Username already taken");
            }
            user.username = username;
        }

        if (email != null && !email.equals(user.email)) {
            User existing = User.find("email = ?1 and id != ?2", email, userId).firstResult();
            if (existing != null) {
                throw new ValidationException("Email already registered");
            }
            user.email = email;
        }

        if (avatarUrl != null) {
            user.avatarUrl = avatarUrl;
        }

        String token = tokenService.generateToken(user);
        LOG.infof("Profile updated for user: %s (id=%d)", user.username, user.id);
        return new AuthResponse(token, user.id, user.username, user.email, user.role, user.avatarUrl, user.authProvider);
    }

    @Mutation("changePassword")
    @Description("Change the authenticated user's password")
    @Authenticated
    @Transactional
    public AuthResponse changePassword(@Name("currentPassword") String currentPassword,
                                       @Name("newPassword") String newPassword) {
        Long userId = Long.parseLong(jwt.getSubject());
        User user = User.findById(userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }

        if (!"LOCAL".equals(user.authProvider)) {
            LOG.warnf("Password change rejected: user %d uses %s authentication", userId, user.authProvider);
            throw new ValidationException("Password change is not available for " + user.authProvider + " accounts");
        }

        if (!passwordService.verifyPassword(currentPassword, user.passwordHash)) {
            LOG.warnf("Password change failed: wrong current password for userId=%d", userId);
            throw new ValidationException("Current password is incorrect");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("New password must be at least 6 characters");
        }

        user.passwordHash = passwordService.hashPassword(newPassword);

        String token = tokenService.generateToken(user);
        LOG.infof("Password changed for user: %s (id=%d)", user.username, user.id);
        return new AuthResponse(token, user.id, user.username, user.email, user.role, user.avatarUrl, user.authProvider);
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
        LOG.infof("User deleted: id=%d", id);
        return true;
    }

    @Mutation("requestPasswordReset")
    @Description("Request a password reset email")
    @Transactional
    public boolean requestPasswordReset(@Name("email") String email) {
        User user = User.find("email", email).firstResult();
        if (user == null) {
            LOG.infof("Password reset requested for unknown email: %s", email);
            return true;
        }

        if (!"LOCAL".equals(user.authProvider)) {
            LOG.infof("Password reset skipped: user %s uses %s authentication", email, user.authProvider);
            return true;
        }

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = sha256(rawToken);

        user.passwordResetToken = tokenHash;
        user.passwordResetExpiry = LocalDateTime.now().plusMinutes(resetExpiryMinutes);

        String resetLink = resetBaseUrl + "?token=" + rawToken;
        mailer.send(
                Mail.withText(user.email,
                        "Recipe Book - Password Reset",
                        "Hello " + user.username + ",\n\n"
                                + "You requested a password reset. Click the link below to reset your password:\n\n"
                                + resetLink + "\n\n"
                                + "This link expires in " + resetExpiryMinutes + " minutes.\n\n"
                                + "If you did not request this, please ignore this email.")
        );

        LOG.infof("Password reset email sent to: %s", email);
        return true;
    }

    @Mutation("resetPassword")
    @Description("Reset password using a token")
    @Transactional
    public AuthResponse resetPassword(@Name("token") String token,
                                      @Name("newPassword") String newPassword) {
        String tokenHash = sha256(token);
        User user = User.find("passwordResetToken", tokenHash).firstResult();

        if (user == null || user.passwordResetExpiry == null
                || user.passwordResetExpiry.isBefore(LocalDateTime.now())) {
            LOG.warn("Password reset failed: invalid or expired token");
            throw new ValidationException("Invalid or expired reset link");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("New password must be at least 6 characters");
        }

        user.passwordHash = passwordService.hashPassword(newPassword);
        user.passwordResetToken = null;
        user.passwordResetExpiry = null;
        user.lastLogin = LocalDateTime.now();

        String jwtToken = tokenService.generateToken(user);
        LOG.infof("Password reset successful for user: %s (id=%d)", user.username, user.id);
        return new AuthResponse(jwtToken, user.id, user.username, user.email, user.role, user.avatarUrl, user.authProvider);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
