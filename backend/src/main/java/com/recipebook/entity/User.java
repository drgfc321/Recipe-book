package com.recipebook.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String email;

    @Column(nullable = false, unique = true)
    public String username;

    @Column(name = "password_hash")
    public String passwordHash;

    @Column(name = "auth_provider")
    public String authProvider = "LOCAL";

    @Column(name = "provider_id")
    public String providerId;

    @Column(nullable = false)
    public String role = "USER";

    @Column(nullable = false)
    public String language = "en";

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_login")
    public LocalDateTime lastLogin;

    @Column(name = "avatar_url")
    public String avatarUrl;

    @Column(name = "password_reset_token")
    public String passwordResetToken;

    @Column(name = "password_reset_expiry")
    public LocalDateTime passwordResetExpiry;

    @Column(name = "weight_kg")
    public Double weightKg;

    @Column(name = "height_cm")
    public Double heightCm;

    @Column(name = "birth_date")
    public LocalDate birthDate;

    @Column(name = "gender")
    public String gender;

    @Column(name = "activity_level")
    public String activityLevel;

    @Column(name = "fitness_goal")
    public String fitnessGoal;
}
