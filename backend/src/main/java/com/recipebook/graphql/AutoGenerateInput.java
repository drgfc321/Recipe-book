package com.recipebook.graphql;

import org.eclipse.microprofile.graphql.Input;
import org.eclipse.microprofile.graphql.NonNull;

import java.time.LocalDate;

@Input("AutoGenerateInput")
public class AutoGenerateInput {

    @NonNull
    public LocalDate weekStart;

    public boolean replaceExisting = false;

    public boolean preferPantry = true;
}
