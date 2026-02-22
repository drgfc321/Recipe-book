package com.recipebook.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipe")
public class Recipe extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public RecipeCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public Difficulty difficulty;

    @Column(name = "prep_time")
    public int prepTime;

    @Column(name = "cook_time")
    public int cookTime;

    public int servings;

    @Column(columnDefinition = "TEXT")
    public String instructions;

    @Column(name = "image_url")
    public String imageUrl;

    @ManyToOne
    public User owner;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<RecipeIngredient> ingredients = new ArrayList<>();

    @Column(name = "created_at")
    public LocalDateTime createdAt = LocalDateTime.now();
}
