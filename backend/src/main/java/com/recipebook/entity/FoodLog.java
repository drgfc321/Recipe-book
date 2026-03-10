package com.recipebook.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "food_log")
public class FoodLog extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Column(name = "log_date", nullable = false)
    public LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_slot", nullable = false)
    public MealSlot mealSlot;

    @Column(nullable = false)
    public double servings = 1.0;

    @Column(name = "logged_at")
    public LocalDateTime loggedAt;

    @ManyToOne
    public Recipe recipe;

    @ManyToOne
    public Ingredient ingredient;

    @Column(name = "ingredient_quantity")
    public Double ingredientQuantity;

    @Column(name = "custom_name")
    public String customName;

    @Column(name = "custom_calories")
    public Double customCalories;

    @Column(name = "custom_protein")
    public Double customProtein;

    @Column(name = "custom_carbs")
    public Double customCarbs;

    @Column(name = "custom_fat")
    public Double customFat;

    @Column(name = "source_meal_plan_id")
    public Long sourceMealPlanId;
}
