package com.recipebook.graphql;

import com.recipebook.entity.Unit;
import org.eclipse.microprofile.graphql.Input;

import java.time.LocalDate;

@Input("PantryItemUpdateInput")
public class PantryItemUpdateInput {

    public Double quantity;

    public Unit unit;

    public LocalDate expirationDate;
}
