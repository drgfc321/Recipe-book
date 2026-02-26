package com.recipebook.ingredient;

import com.recipebook.service.AuthService;
import com.recipebook.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "ingredients", layout = MainLayout.class)
@PageTitle("Ingredients | Recipe Book")
public class IngredientListView extends VerticalLayout {

    private final IngredientService ingredientService;
    private final AuthService authService;
    private final Grid<IngredientResponse> grid = new Grid<>(IngredientResponse.class, false);
    private final ComboBox<String> categoryFilter = new ComboBox<>("Category");
    private final TextField searchField = new TextField("Search");

    public IngredientListView(IngredientService ingredientService, AuthService authService) {
        this.ingredientService = ingredientService;
        this.authService = authService;

        setPadding(true);
        setSpacing(true);
        setSizeFull();

        add(createToolbar());
        add(createFilters());
        configureGrid();
        add(grid);
        expand(grid);

        refreshGrid();
    }

    private HorizontalLayout createToolbar() {
        H2 title = new H2("Ingredients");
        title.getStyle().set("margin", "0");

        HorizontalLayout toolbar = new HorizontalLayout(title);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setWidthFull();
        toolbar.expand(title);

        if (authService.isLoggedIn()) {
            Button newButton = new Button("New Ingredient", VaadinIcon.PLUS.create(), e -> openCreateDialog());
            newButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            toolbar.add(newButton);
        }

        return toolbar;
    }

    private HorizontalLayout createFilters() {
        categoryFilter.setItems("DAIRY", "MEAT", "VEGETABLES", "FRUITS", "GRAINS", "SPICES", "OILS", "BEVERAGES", "OTHER");
        categoryFilter.setPlaceholder("All");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> refreshGrid());

        searchField.setPlaceholder("Search ingredients...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> refreshGrid());

        HorizontalLayout filters = new HorizontalLayout(categoryFilter, searchField);
        filters.setAlignItems(FlexComponent.Alignment.BASELINE);
        return filters;
    }

    private void configureGrid() {
        // Name column — bold
        grid.addComponentColumn(ingredient -> {
            Span name = new Span(ingredient.name());
            name.getStyle().set("font-weight", "600");
            return name;
        }).setHeader("Name").setSortable(true).setFlexGrow(2);

        // Category column — colored badge
        grid.addComponentColumn(ingredient -> {
            Span badge = new Span(ingredient.category());
            badge.getElement().getThemeList().add("badge small");
            badge.getStyle()
                    .set("background-color", getCategoryColor(ingredient.category()))
                    .set("color", "white");
            return badge;
        }).setHeader("Category").setSortable(true);

        // Calories column — badge
        grid.addComponentColumn(ingredient -> {
            Span badge = new Span("%.0f kcal".formatted(ingredient.caloriesPer100g()));
            badge.getElement().getThemeList().add("badge small");
            badge.addClassNames("macro-badge", "calories");
            return badge;
        }).setHeader("Calories/100g").setSortable(true);

        // Protein column — badge
        grid.addComponentColumn(ingredient -> {
            Span badge = new Span("%.1f g".formatted(ingredient.proteinPer100g()));
            badge.getElement().getThemeList().add("badge small");
            badge.addClassNames("macro-badge", "protein");
            return badge;
        }).setHeader("Protein/100g").setSortable(true);

        // Carbs column — badge
        grid.addComponentColumn(ingredient -> {
            Span badge = new Span("%.1f g".formatted(ingredient.carbsPer100g()));
            badge.getElement().getThemeList().add("badge small");
            badge.addClassNames("macro-badge", "carbs");
            return badge;
        }).setHeader("Carbs/100g").setSortable(true);

        // Fat column — badge
        grid.addComponentColumn(ingredient -> {
            Span badge = new Span("%.1f g".formatted(ingredient.fatPer100g()));
            badge.getElement().getThemeList().add("badge small");
            badge.addClassNames("macro-badge", "fat");
            return badge;
        }).setHeader("Fat/100g").setSortable(true);

        if (authService.isLoggedIn()) {
            grid.addComponentColumn(ingredient -> {
                Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> confirmDelete(ingredient));
                deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
                return deleteButton;
            }).setHeader("").setWidth("80px").setFlexGrow(0);
        }

        grid.addItemClickListener(e -> {
            if (authService.isLoggedIn()) {
                openEditDialog(e.getItem());
            }
        });
    }

    private String getCategoryColor(String category) {
        if (category == null) return "var(--lumo-contrast-50pct)";
        return switch (category) {
            case "MEAT" -> "var(--lumo-error-color)";
            case "VEGETABLES" -> "var(--lumo-success-color)";
            case "FRUITS" -> "#7eb05a";
            case "DAIRY" -> "var(--lumo-contrast-60pct)";
            case "GRAINS" -> "var(--recipe-warning-color)";
            case "SPICES" -> "var(--lumo-primary-color)";
            case "OILS" -> "#a08040";
            case "BEVERAGES" -> "#5080b0";
            default -> "var(--lumo-contrast-50pct)";
        };
    }

    private void refreshGrid() {
        try {
            List<IngredientResponse> ingredients = ingredientService.getIngredients(
                    searchField.getValue(),
                    categoryFilter.getValue()
            );
            grid.setItems(ingredients);
        } catch (Exception e) {
            Notification.show("Failed to load ingredients: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void openCreateDialog() {
        IngredientFormDialog dialog = new IngredientFormDialog(ingredientService, null, this::refreshGrid);
        dialog.open();
    }

    private void openEditDialog(IngredientResponse ingredient) {
        IngredientFormDialog dialog = new IngredientFormDialog(ingredientService, ingredient, this::refreshGrid);
        dialog.open();
    }

    private void confirmDelete(IngredientResponse ingredient) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Ingredient");
        dialog.setText("Are you sure you want to delete \"" + ingredient.name() + "\"?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            try {
                ingredientService.deleteIngredient(ingredient.id());
                Notification.show("Ingredient deleted", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Failed to delete: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        dialog.open();
    }
}
