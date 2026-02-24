package com.recipebook.recipe;

import com.recipebook.service.AuthService;
import com.recipebook.ui.MacroBar;
import com.recipebook.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "recipes", layout = MainLayout.class)
@PageTitle("Recipe Detail | Recipe Book")
public class RecipeDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final RecipeService recipeService;
    private final AuthService authService;
    private RecipeResponse recipe;

    public RecipeDetailView(RecipeService recipeService, AuthService authService) {
        this.recipeService = recipeService;
        this.authService = authService;
        setPadding(true);
        setSpacing(true);
        addClassName("recipe-detail");
    }

    @Override
    public void setParameter(BeforeEvent event, Long id) {
        try {
            recipe = recipeService.getRecipe(id);
            if (recipe == null) {
                Notification.show("Recipe not found", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                UI.getCurrent().navigate(RecipeListView.class);
                return;
            }
            buildView();
        } catch (Exception e) {
            Notification.show("Error loading recipe: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            UI.getCurrent().navigate(RecipeListView.class);
        }
    }

    private void buildView() {
        removeAll();

        // Back button — outside the card
        Button backButton = new Button("Back to Recipes", VaadinIcon.ARROW_LEFT.create(),
                e -> UI.getCurrent().navigate(RecipeListView.class));
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backButton.getStyle().set("align-self", "flex-start");

        // Card container
        Div card = new Div();
        card.addClassName("recipe-card");

        // --- Header section ---
        H2 name = new H2(recipe.name());
        name.addClassName("recipe-title");

        HorizontalLayout headerMeta = new HorizontalLayout();
        headerMeta.setSpacing(true);
        headerMeta.setAlignItems(FlexComponent.Alignment.CENTER);
        headerMeta.add(createBadge(recipe.category(), getDifficultyColor(null)));
        headerMeta.add(createBadge(recipe.difficulty(), getDifficultyColor(recipe.difficulty())));
        Span owner = new Span("by " + recipe.ownerUsername());
        owner.addClassName("recipe-owner");
        headerMeta.add(owner);

        // Header row: badges left, actions right
        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.addClassName("recipe-header-row");
        headerRow.setAlignItems(FlexComponent.Alignment.CENTER);
        headerRow.add(headerMeta);
        headerRow.setFlexGrow(1, headerMeta);

        // Edit/Delete buttons in header if owner
        authService.getCurrentUser().ifPresent(user -> {
            if (user.id().equals(recipe.ownerId())) {
                Button editButton = new Button("Edit", VaadinIcon.EDIT.create(), e -> openEditDialog());
                editButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

                Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create(), e -> confirmDelete());
                deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);

                HorizontalLayout actions = new HorizontalLayout(editButton, deleteButton);
                actions.setSpacing(true);
                headerRow.add(actions);
            }
        });

        // --- Info chips row ---
        HorizontalLayout infoRow = new HorizontalLayout();
        infoRow.setSpacing(true);
        infoRow.getStyle().set("flex-wrap", "wrap");
        infoRow.add(createInfoChip(VaadinIcon.CLOCK, "Prep: " + recipe.prepTime() + " min"));
        infoRow.add(createInfoChip(VaadinIcon.TIMER, "Cook: " + recipe.cookTime() + " min"));
        infoRow.add(createInfoChip(VaadinIcon.GROUP, recipe.servings() + " servings"));

        card.add(name, headerRow, infoRow);

        // --- Description ---
        if (recipe.description() != null && !recipe.description().isBlank()) {
            Paragraph desc = new Paragraph(recipe.description());
            desc.addClassName("recipe-description");
            card.add(desc);
        }

        // --- Per Serving Macros ---
        card.add(createSection("Per Serving", new MacroBar(recipe.perServingMacros())));

        // --- Ingredients ---
        if (recipe.ingredients() != null && !recipe.ingredients().isEmpty()) {
            Grid<RecipeIngredientResponse> ingredientGrid = new Grid<>(RecipeIngredientResponse.class, false);
            ingredientGrid.addColumn(RecipeIngredientResponse::ingredientName).setHeader("Ingredient").setFlexGrow(2);
            ingredientGrid.addColumn(r -> r.quantity() + " " + r.unit()).setHeader("Amount");
            ingredientGrid.addColumn(RecipeIngredientResponse::ingredientCategory).setHeader("Category");
            ingredientGrid.addColumn(r -> {
                MacroInfo m = r.macros();
                return m != null ? "%.0f kcal".formatted(m.calories()) : "";
            }).setHeader("Calories");
            ingredientGrid.setItems(recipe.ingredients());
            ingredientGrid.setAllRowsVisible(true);
            card.add(createSection("Ingredients", ingredientGrid));
        }

        // --- Total Macros ---
        card.add(createSection("Total Macros", new MacroBar(recipe.totalMacros())));

        // --- Instructions ---
        if (recipe.instructions() != null && !recipe.instructions().isBlank()) {
            Paragraph instructions = new Paragraph(recipe.instructions());
            instructions.getStyle().set("white-space", "pre-wrap").set("margin", "0");
            card.add(createSection("Instructions", instructions));
        }

        add(backButton, card);
    }

    private Div createSection(String title, Component... content) {
        H3 header = new H3(title);
        Div section = new Div();
        section.addClassName("recipe-section");
        section.add(header);
        for (Component c : content) {
            section.add(c);
        }
        return section;
    }

    private void openEditDialog() {
        RecipeFormDialog dialog = new RecipeFormDialog(recipeService, recipe, () -> {
            recipe = recipeService.getRecipe(recipe.id());
            buildView();
        });
        dialog.open();
    }

    private void confirmDelete() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Recipe");
        dialog.setText("Are you sure you want to delete \"" + recipe.name() + "\"?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            try {
                recipeService.deleteRecipe(recipe.id());
                Notification.show("Recipe deleted", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                UI.getCurrent().navigate(RecipeListView.class);
            } catch (Exception ex) {
                Notification.show("Failed to delete: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        dialog.open();
    }

    private Span createBadge(String text, String color) {
        Span badge = new Span(text);
        badge.getElement().getThemeList().add("badge small");
        badge.getStyle()
                .set("background-color", color)
                .set("color", "white")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "2px 10px");
        return badge;
    }

    private Span createInfoChip(VaadinIcon icon, String text) {
        Span iconSpan = new Span(icon.create());
        Span chip = new Span(iconSpan, new Span(text));
        chip.addClassName("info-chip");
        return chip;
    }

    private String getDifficultyColor(String difficulty) {
        if (difficulty == null) return "var(--lumo-primary-color)";
        return switch (difficulty) {
            case "EASY" -> "var(--lumo-success-color)";
            case "MEDIUM" -> "hsl(40, 80%, 45%)";
            case "HARD" -> "var(--lumo-error-color)";
            default -> "var(--lumo-contrast-50pct)";
        };
    }
}
