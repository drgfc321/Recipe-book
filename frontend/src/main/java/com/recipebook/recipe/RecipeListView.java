package com.recipebook.recipe;

import com.recipebook.service.AuthService;
import com.recipebook.ui.MacroBar;
import com.recipebook.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
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

@Route(value = "recipes", layout = MainLayout.class)
@PageTitle("Recipes | Recipe Book")
public class RecipeListView extends VerticalLayout {

    private final RecipeService recipeService;
    private final AuthService authService;
    private final Grid<RecipeResponse> grid = new Grid<>(RecipeResponse.class, false);
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
        configureGrid();
        add(grid);
        expand(grid);

        refreshGrid();
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
        categoryFilter.addValueChangeListener(e -> refreshGrid());

        difficultyFilter.setItems("EASY", "MEDIUM", "HARD");
        difficultyFilter.setPlaceholder("All");
        difficultyFilter.setClearButtonVisible(true);
        difficultyFilter.addValueChangeListener(e -> refreshGrid());

        searchField.setPlaceholder("Search recipes...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> refreshGrid());

        HorizontalLayout filters = new HorizontalLayout(categoryFilter, difficultyFilter, searchField);
        filters.setAlignItems(FlexComponent.Alignment.BASELINE);
        return filters;
    }

    private void configureGrid() {
        grid.addColumn(RecipeResponse::name).setHeader("Name").setSortable(true).setFlexGrow(2);
        grid.addColumn(RecipeResponse::category).setHeader("Category").setSortable(true);
        grid.addColumn(RecipeResponse::difficulty).setHeader("Difficulty").setSortable(true);
        grid.addColumn(r -> r.prepTime() + r.cookTime() + " min").setHeader("Total Time").setSortable(true);
        grid.addColumn(r -> {
            MacroInfo m = r.perServingMacros();
            return m != null ? "%.0f kcal".formatted(m.calories()) : "";
        }).setHeader("Calories/Serving").setSortable(true);
        grid.addColumn(RecipeResponse::ownerUsername).setHeader("Owner").setSortable(true);

        grid.addItemClickListener(e ->
                UI.getCurrent().navigate("recipes/" + e.getItem().id()));
    }

    private void refreshGrid() {
        try {
            List<RecipeResponse> recipes = recipeService.getRecipes(
                    categoryFilter.getValue(),
                    difficultyFilter.getValue(),
                    searchField.getValue()
            );
            grid.setItems(recipes);
        } catch (Exception e) {
            Notification.show("Failed to load recipes: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void openCreateDialog() {
        RecipeFormDialog dialog = new RecipeFormDialog(recipeService, null, this::refreshGrid);
        dialog.open();
    }
}
