package com.recipebook.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "ingredient")
public class Ingredient extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public IngredientCategory category;

    @Column(name = "calories_per_100g")
    public double caloriesPer100g;

    @Column(name = "protein_per_100g")
    public double proteinPer100g;

    @Column(name = "carbs_per_100g")
    public double carbsPer100g;

    @Column(name = "fat_per_100g")
    public double fatPer100g;
}
