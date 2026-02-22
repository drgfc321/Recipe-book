package com.recipebook.graphql;

import com.recipebook.entity.Unit;
import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.NonNull;

import java.time.LocalDate;

@Input("ShoppingListItemInput")
public class ShoppingListItemInput {

    @NonNull
    public Long ingredientId;

    @NonNull
    public Double quantity;

    @NonNull
    public Unit unit;

    @NonNull
    public LocalDate weekStart;
}
