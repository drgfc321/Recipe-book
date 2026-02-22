package com.recipebook.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;

@Entity
@Table(name = "pantry_item", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "ingredient_id"})
})
public class PantryItem extends PanacheEntity {

    @ManyToOne
    public User user;

    @ManyToOne
    public Ingredient ingredient;

    public double quantity;

    @Enumerated(EnumType.STRING)
    public Unit unit;

    @Column(name = "expiration_date")
    public LocalDate expirationDate;
}
