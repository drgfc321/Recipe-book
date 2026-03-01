package com.recipebook.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_nutrition_target")
public class UserNutritionTarget extends PanacheEntity {

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    public User user;

    @Column(name = "daily_calories")
    public int dailyCalories = 2000;

    @Column(name = "daily_protein")
    public double dailyProtein = 150.0;

    @Column(name = "daily_carbs")
    public double dailyCarbs = 250.0;

    @Column(name = "daily_fat")
    public double dailyFat = 65.0;
}
