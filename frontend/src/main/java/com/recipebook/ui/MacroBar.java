package com.recipebook.ui;

import com.recipebook.recipe.MacroInfo;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class MacroBar extends HorizontalLayout {

    public MacroBar(MacroInfo macros) {
        setSpacing(true);
        setPadding(false);
        setAlignItems(Alignment.CENTER);
        addClassName("macro-bar");

        if (macros == null) {
            add(new Span("No macro data"));
            return;
        }

        add(createBadge("Calories", "%.0f kcal".formatted(macros.calories()), "calories"));
        add(createBadge("Protein", "%.1fg".formatted(macros.protein()), "protein"));
        add(createBadge("Carbs", "%.1fg".formatted(macros.carbs()), "carbs"));
        add(createBadge("Fat", "%.1fg".formatted(macros.fat()), "fat"));
    }

    private Span createBadge(String label, String value, String colorClass) {
        Span labelSpan = new Span(label);
        labelSpan.addClassName("macro-label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("macro-value");

        Span badge = new Span(labelSpan, valueSpan);
        badge.getElement().getThemeList().add("badge small");
        badge.addClassNames("macro-badge", colorClass);
        return badge;
    }
}
