package com.recipebook.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "user_nutrition_target",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "day_type"}))
public class UserNutritionTarget extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    public User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_type", nullable = false)
    public DayType dayType = DayType.DEFAULT;

    @Column(name = "daily_calories")
    public int dailyCalories = 2000;

    @Column(name = "daily_protein")
    public double dailyProtein = 150.0;

    @Column(name = "daily_carbs")
    public double dailyCarbs = 250.0;

    @Column(name = "daily_fat")
    public double dailyFat = 65.0;
}
