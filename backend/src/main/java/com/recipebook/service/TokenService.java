package com.recipebook.service;

import com.recipebook.entity.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    private static final Logger LOG = Logger.getLogger(TokenService.class);

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "jwt.duration.hours", defaultValue = "24")
    int durationHours;

    public String generateToken(User user) {
        LOG.debugf("Generating token for userId=%s, role=%s, duration=%dh", (Object) user.id, user.role, durationHours);
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
