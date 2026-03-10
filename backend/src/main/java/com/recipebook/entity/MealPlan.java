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

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "meal_plan", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "meal_date", "meal_slot"})
})
public class MealPlan extends PanacheEntity {

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;

    @Column(name = "meal_date", nullable = false)
    public LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_slot", nullable = false)
    public MealSlot mealSlot;

    @ManyToOne
    public Recipe recipe;

    public static List<MealPlan> listWithRecipeDetails(String whereClause, Object... params) {
        return find(
            "FROM MealPlan mp " +
            "JOIN FETCH mp.recipe r " +
            "LEFT JOIN FETCH r.owner " +
            "LEFT JOIN FETCH r.ingredients ri " +
            "LEFT JOIN FETCH ri.ingredient " +
            "WHERE " + whereClause +
            " ORDER BY mp.date, mp.mealSlot",
            params
        ).list();
    }
}
