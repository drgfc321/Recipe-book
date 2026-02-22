package com.recipebook.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "shopping_list_item")
public class ShoppingListItem extends PanacheEntity {

    @ManyToOne
    public User user;

    @ManyToOne
    public Ingredient ingredient;

    @Column(name = "ingredient_name")
    public String ingredientName;

    public double quantity;

    @Enumerated(EnumType.STRING)
    public Unit unit;

    @Column(nullable = false)
    public boolean purchased = false;

    @Column(name = "week_start_date")
    public LocalDate weekStartDate;
}
