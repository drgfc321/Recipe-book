package com.recipebook.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "recipe_ingredient")
public class RecipeIngredient extends PanacheEntity {

    @ManyToOne
    public Recipe recipe;

    @ManyToOne
    public Ingredient ingredient;

    @Column(nullable = false)
    public double quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Unit unit;
}
