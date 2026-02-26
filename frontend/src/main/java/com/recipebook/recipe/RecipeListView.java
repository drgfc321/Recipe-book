package com.recipebook.recipe;

import com.recipebook.service.AuthService;
import com.recipebook.ui.MacroBar;
import com.recipebook.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "recipes", layout = MainLayout.class)
@PageTitle("Recipes | Recipe Book")
public class RecipeListView extends VerticalLayout {

    private final RecipeService recipeService;
    private final AuthService authService;
    private final FlexLayout cardContainer = new FlexLayout();
    private final ComboBox<String> categoryFilter = new ComboBox<>("Category");
    private final ComboBox<String> difficultyFilter = new ComboBox<>("Difficulty");
    private final TextField searchField = new TextField("Search");

    public RecipeListView(RecipeService recipeService, AuthService authService) {
        this.recipeService = recipeService;
        this.authService = authService;

        setPadding(true);
        setSpacing(true);
        setSizeFull();

        add(createToolbar());
        add(createFilters());
        configureCardContainer();
        add(cardContainer);
        setFlexGrow(1, cardContainer);

        refreshCards();
    }

    private HorizontalLayout createToolbar() {
        H2 title = new H2("Recipes");
        title.getStyle().set("margin", "0");

        HorizontalLayout toolbar = new HorizontalLayout(title);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setWidthFull();
        toolbar.expand(title);

        if (authService.isLoggedIn()) {
            Button newButton = new Button("New Recipe", VaadinIcon.PLUS.create(), e -> openCreateDialog());
            newButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            toolbar.add(newButton);
        }

        return toolbar;
    }

    private HorizontalLayout createFilters() {
        categoryFilter.setItems("BREAKFAST", "LUNCH", "DINNER", "DESSERT", "SNACK", "OTHER");
        categoryFilter.setPlaceholder("All");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> refreshCards());

        difficultyFilter.setItems("EASY", "MEDIUM", "HARD");
        difficultyFilter.setPlaceholder("All");
        difficultyFilter.setClearButtonVisible(true);
        difficultyFilter.addValueChangeListener(e -> refreshCards());

        searchField.setPlaceholder("Search recipes...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> refreshCards());

        HorizontalLayout filters = new HorizontalLayout(categoryFilter, difficultyFilter, searchField);
        filters.setAlignItems(FlexComponent.Alignment.BASELINE);
        return filters;
    }

    private void configureCardContainer() {
        cardContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cardContainer.getStyle()
                .set("gap", "var(--lumo-space-m)")
                .set("overflow-y", "auto")
                .set("align-content", "flex-start");
        cardContainer.setWidthFull();
    }

    private VerticalLayout createRecipeCard(RecipeResponse recipe) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.addClassName("recipe-list-card");
        card.getStyle()
                .set("flex", "1 1 280px")
                .set("max-width", "380px");

        // Image or placeholder
        if (recipe.imageUrl() != null && !recipe.imageUrl().isBlank()) {
            String imgSrc = recipe.imageUrl().startsWith("http") ? recipe.imageUrl()
                    : recipeService.getBackendUrl() + recipe.imageUrl();
            Image img = new Image(imgSrc, recipe.name());
            img.addClassName("recipe-list-card-img");
            card.add(img);
        } else {
            Div placeholder = new Div();
            Icon icon = VaadinIcon.CUTLERY.create();
            icon.setSize("48px");
            icon.setColor("var(--lumo-contrast-30pct)");
            placeholder.add(icon);
            placeholder.addClassName("recipe-list-card-img-placeholder");
            card.add(placeholder);
        }

        // Category badge overlay
        if (recipe.category() != null) {
            Span catBadge = new Span(recipe.category());
            catBadge.getElement().getThemeList().add("badge small");
            catBadge.getStyle()
                    .set("background-color", "var(--lumo-primary-color)")
                    .set("color", "white")
                    .set("margin-top", "var(--lumo-space-s)");
            card.add(catBadge);
        }

        // Recipe name
        H3 name = new H3(recipe.name());
        name.getStyle().set("margin", "var(--lumo-space-xs) 0 0 0");
        card.add(name);

        // Owner
        if (recipe.ownerUsername() != null) {
            Span owner = new Span("by " + recipe.ownerUsername());
            owner.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-size", "var(--lumo-font-size-s)");
            card.add(owner);
        }

        // Difficulty + Time + Servings row
        HorizontalLayout infoRow = new HorizontalLayout();
        infoRow.setSpacing(true);
        infoRow.setPadding(false);
        infoRow.setAlignItems(FlexComponent.Alignment.CENTER);
        infoRow.getStyle().set("margin-top", "var(--lumo-space-s)").set("flex-wrap", "wrap");

        if (recipe.difficulty() != null) {
            Span diffBadge = new Span(recipe.difficulty());
            diffBadge.getElement().getThemeList().add("badge small");
            diffBadge.getStyle()
                    .set("background-color", getDifficultyColor(recipe.difficulty()))
                    .set("color", "white");
            infoRow.add(diffBadge);
        }

        int totalTime = recipe.prepTime() + recipe.cookTime();
        if (totalTime > 0) {
            Icon clockIcon = VaadinIcon.CLOCK.create();
            clockIcon.setSize("14px");
            clockIcon.setColor("var(--lumo-secondary-text-color)");
            Span timeSpan = new Span(clockIcon, new Span(totalTime + " min"));
            timeSpan.getStyle()
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("display", "inline-flex")
                    .set("align-items", "center")
                    .set("gap", "var(--lumo-space-xs)");
            infoRow.add(timeSpan);
        }

        if (recipe.servings() > 0) {
            Icon groupIcon = VaadinIcon.GROUP.create();
            groupIcon.setSize("14px");
            groupIcon.setColor("var(--lumo-secondary-text-color)");
            Span servingsSpan = new Span(groupIcon, new Span(String.valueOf(recipe.servings())));
            servingsSpan.getStyle()
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("display", "inline-flex")
                    .set("align-items", "center")
                    .set("gap", "var(--lumo-space-xs)");
            infoRow.add(servingsSpan);
        }

        card.add(infoRow);

        // Macro bar
        if (recipe.perServingMacros() != null) {
            MacroBar macroBar = new MacroBar(recipe.perServingMacros());
            macroBar.getStyle().set("margin-top", "var(--lumo-space-s)");
            card.add(macroBar);
        }

        // Click navigation
        card.addClickListener(e -> UI.getCurrent().navigate("recipes/" + recipe.id()));

        return card;
    }

    private void refreshCards() {
        try {
            List<RecipeResponse> recipes = recipeService.getRecipes(
                    categoryFilter.getValue(),
                    difficultyFilter.getValue(),
                    searchField.getValue()
            );
            cardContainer.removeAll();
            for (RecipeResponse recipe : recipes) {
                cardContainer.add(createRecipeCard(recipe));
            }
            if (recipes.isEmpty()) {
                Span empty = new Span("No recipes found");
                empty.getStyle().set("color", "var(--lumo-secondary-text-color)").set("padding", "var(--lumo-space-l)");
                cardContainer.add(empty);
            }
        } catch (Exception e) {
            Notification.show("Failed to load recipes: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void openCreateDialog() {
        RecipeFormDialog dialog = new RecipeFormDialog(recipeService, null, this::refreshCards);
        dialog.open();
    }

    private String getDifficultyColor(String difficulty) {
        if (difficulty == null) return "var(--lumo-contrast-50pct)";
        return switch (difficulty) {
            case "EASY" -> "var(--lumo-success-color)";
            case "MEDIUM" -> "var(--recipe-warning-color)";
            case "HARD" -> "var(--lumo-error-color)";
            default -> "var(--lumo-contrast-50pct)";
        };
    }
}
