package com.recipebook.service;

import com.recipebook.entity.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "jwt.duration.hours", defaultValue = "24")
    int durationHours;

    public String generateToken(User user) {
        Set<String> roles = new HashSet<>();
        roles.add(user.role);

        return Jwt.issuer(issuer)
                .subject(user.id.toString())
                .upn(user.email)
                .claim("userId", user.id)
                .claim("username", user.username)
                .claim("email", user.email)
                .claim("role", user.role)
                .groups(roles)
                .expiresIn(Duration.ofHours(durationHours))
                .sign();
    }
}
