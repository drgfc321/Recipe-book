package com.recipebook.views;

import com.recipebook.ingredient.IngredientService;
import com.recipebook.pantry.PantryItemResponse;
import com.recipebook.pantry.PantryService;
import com.recipebook.recipe.MacroInfo;
import com.recipebook.recipe.RecipeResponse;
import com.recipebook.recipe.RecipeService;
import com.recipebook.service.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.stream.Collectors;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Recipe Book")
public class DashboardView extends VerticalLayout {

    private final AuthService authService;
    private final RecipeService recipeService;
    private final IngredientService ingredientService;
    private final PantryService pantryService;

    public DashboardView(AuthService authService, RecipeService recipeService,
                         IngredientService ingredientService, PantryService pantryService) {
        this.authService = authService;
        this.recipeService = recipeService;
        this.ingredientService = ingredientService;
        this.pantryService = pantryService;

        setPadding(true);
        setSpacing(true);
        setMaxWidth("1200px");
        getStyle().set("margin", "0 auto");

        buildView();
    }

    private void buildView() {
        // Welcome header
        authService.getCurrentUser().ifPresentOrElse(
                user -> {
                    H2 welcome = new H2("Welcome back, " + user.username() + "!");
                    welcome.getStyle().set("margin-bottom", "0");
                    add(welcome);
                },
                () -> {
                    H2 welcome = new H2("Welcome to Recipe Book");
                    welcome.getStyle().set("margin-bottom", "0");
                    add(welcome);
                }
        );
        Paragraph subtitle = new Paragraph("Your personal recipe collection and meal planning hub");
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0");
        add(subtitle);

        // Stats cards
        add(createStatsRow());

        // Recent recipes
        add(createRecentRecipes());

        // Quick actions
        if (authService.isLoggedIn()) {
            add(createQuickActions());
        }
    }

    private HorizontalLayout createStatsRow() {
        long recipeCount = 0;
        long ingredientCount = 0;
        long categoryCount = 0;
        long pantryCount = 0;
        long expiringCount = 0;

        try {
            List<RecipeResponse> recipes = recipeService.getRecipes(null, null, null);
            recipeCount = recipes.size();
            categoryCount = recipes.stream()
                    .map(RecipeResponse::category)
                    .filter(c -> c != null && !c.isBlank())
                    .distinct()
                    .count();
        } catch (Exception ignored) {
        }

        try {
            ingredientCount = ingredientService.getIngredients(null, null).size();
        } catch (Exception ignored) {
        }

        try {
            List<PantryItemResponse> pantryItems = pantryService.getPantryItems();
            pantryCount = pantryItems.size();
        } catch (Exception ignored) {
        }

        try {
            expiringCount = pantryService.getExpiringItems(3).size();
        } catch (Exception ignored) {
        }

        HorizontalLayout statsRow = new HorizontalLayout();
        statsRow.setWidthFull();
        statsRow.setSpacing(true);
        statsRow.getStyle().set("flex-wrap", "wrap");

        statsRow.add(createStatCard(VaadinIcon.BOOK, String.valueOf(recipeCount), "Total Recipes", "var(--lumo-primary-color)"));
        statsRow.add(createStatCard(VaadinIcon.CART, String.valueOf(ingredientCount), "Total Ingredients", "var(--lumo-success-color)"));
        statsRow.add(createStatCard(VaadinIcon.TAGS, String.valueOf(categoryCount), "Categories Used", "var(--recipe-warning-color)"));
        statsRow.add(createStatCard(VaadinIcon.STORAGE, String.valueOf(pantryCount), "Pantry Items", "var(--lumo-success-color)"));
        if (expiringCount > 0) {
            statsRow.add(createStatCard(VaadinIcon.WARNING, String.valueOf(expiringCount), "Expiring Soon", "var(--lumo-error-color)"));
        }

        return statsRow;
    }

    private VerticalLayout createStatCard(VaadinIcon vaadinIcon, String count, String label, String accentColor) {
        Icon icon = vaadinIcon.create();
        icon.setSize("28px");
        icon.setColor(accentColor);

        H3 countLabel = new H3(count);
        countLabel.getStyle()
                .set("margin", "var(--lumo-space-xs) 0 0 0")
                .set("font-size", "var(--lumo-font-size-xxl)")
                .set("color", accentColor);

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout card = new VerticalLayout(icon, countLabel, labelSpan);
        card.setPadding(true);
        card.setSpacing(false);
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.addClassName("dashboard-stat-card");
        card.setWidth(null);
        card.getStyle()
                .set("flex", "1 1 200px")
                .set("min-width", "180px")
                .set("background", "#232838")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)");

        return card;
    }

    private VerticalLayout createRecentRecipes() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        H3 title = new H3("Recent Recipes");
        title.getStyle().set("margin-bottom", "0");
        section.add(title);

        List<RecipeResponse> recipes;
        try {
            recipes = recipeService.getRecipes(null, null, null);
        } catch (Exception e) {
            Span error = new Span("Could not load recipes");
            error.getStyle().set("color", "var(--lumo-secondary-text-color)");
            section.add(error);
            return section;
        }

        if (recipes.isEmpty()) {
            Span empty = new Span("No recipes yet. Create your first recipe!");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)");
            section.add(empty);
            return section;
        }

        // Show up to 6 most recent
        List<RecipeResponse> recent = recipes.stream().limit(6).collect(Collectors.toList());

        FlexLayout cardsContainer = new FlexLayout();
        cardsContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cardsContainer.getStyle().set("gap", "var(--lumo-space-m)");
        cardsContainer.setWidthFull();

        for (RecipeResponse recipe : recent) {
            cardsContainer.add(createRecipeCard(recipe));
        }

        section.add(cardsContainer);
        return section;
    }

    private VerticalLayout createRecipeCard(RecipeResponse recipe) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.addClassName("recipe-list-card");
        card.getStyle()
                .set("flex", "1 1 280px")
                .set("max-width", "380px")
                .set("cursor", "pointer");

        // Image or placeholder
        if (recipe.imageUrl() != null && !recipe.imageUrl().isBlank()) {
            String imgSrc = recipe.imageUrl().startsWith("http") ? recipe.imageUrl()
                    : recipeService.getBackendUrl() + recipe.imageUrl();
            Image img = new Image(imgSrc, recipe.name());
            img.setWidthFull();
            img.setHeight("160px");
            img.getStyle().set("object-fit", "cover")
                    .set("border-radius", "var(--lumo-border-radius-l) var(--lumo-border-radius-l) 0 0")
                    .set("margin", "calc(var(--lumo-space-m) * -1) calc(var(--lumo-space-m) * -1) 0 calc(var(--lumo-space-m) * -1)");
            card.add(img);
        } else {
            Div placeholder = new Div();
            Icon icon = VaadinIcon.CUTLERY.create();
            icon.setSize("48px");
            icon.setColor("var(--lumo-contrast-30pct)");
            placeholder.add(icon);
            placeholder.getStyle()
                    .set("width", "100%")
                    .set("height", "160px")
                    .set("display", "flex")
                    .set("align-items", "center")
                    .set("justify-content", "center")
                    .set("background", "var(--lumo-contrast-5pct)")
                    .set("border-radius", "var(--lumo-border-radius-l) var(--lumo-border-radius-l) 0 0")
                    .set("margin", "calc(var(--lumo-space-m) * -1) calc(var(--lumo-space-m) * -1) 0 calc(var(--lumo-space-m) * -1)");
            card.add(placeholder);
        }

        // Name
        Span name = new Span(recipe.name());
        name.getStyle().set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin-top", "var(--lumo-space-s)");
        card.add(name);

        // Category + Difficulty badges
        HorizontalLayout badges = new HorizontalLayout();
        badges.setSpacing(true);
        badges.setPadding(false);
        badges.getStyle().set("margin-top", "var(--lumo-space-xs)");

        if (recipe.category() != null) {
            Span catBadge = new Span(recipe.category());
            catBadge.getElement().getThemeList().add("badge small");
            catBadge.getStyle().set("background-color", "var(--lumo-primary-color)").set("color", "white");
            badges.add(catBadge);
        }
        if (recipe.difficulty() != null) {
            Span diffBadge = new Span(recipe.difficulty());
            diffBadge.getElement().getThemeList().add("badge small");
            diffBadge.getStyle().set("background-color", getDifficultyColor(recipe.difficulty())).set("color", "white");
            badges.add(diffBadge);
        }
        card.add(badges);

        // Time + calories info
        int totalTime = recipe.prepTime() + recipe.cookTime();
        StringBuilder info = new StringBuilder();
        if (totalTime > 0) info.append(totalTime).append(" min");
        MacroInfo macros = recipe.perServingMacros();
        if (macros != null) {
            if (!info.isEmpty()) info.append(" · ");
            info.append("%.0f kcal/serving".formatted(macros.calories()));
        }
        if (!info.isEmpty()) {
            Span infoSpan = new Span(info.toString());
            infoSpan.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("margin-top", "var(--lumo-space-xs)");
            card.add(infoSpan);
        }

        card.addClickListener(e -> UI.getCurrent().navigate("recipes/" + recipe.id()));

        return card;
    }

    private HorizontalLayout createQuickActions() {
        H3 title = new H3("Quick Actions");
        title.getStyle().set("margin-bottom", "0");

        Button newRecipe = new Button("New Recipe", VaadinIcon.PLUS.create(),
                e -> UI.getCurrent().navigate("recipes"));
        newRecipe.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button browseIngredients = new Button("Browse Ingredients", VaadinIcon.CART.create(),
                e -> UI.getCurrent().navigate("ingredients"));
        browseIngredients.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        Button mealPlanner = new Button("Meal Planner", VaadinIcon.CALENDAR.create(),
                e -> UI.getCurrent().navigate("meal-plan"));
        mealPlanner.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        Button pantry = new Button("Pantry", VaadinIcon.STORAGE.create(),
                e -> UI.getCurrent().navigate("pantry"));
        pantry.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        Button shoppingList = new Button("Shopping List", VaadinIcon.CART.create(),
                e -> UI.getCurrent().navigate("shopping-list"));
        shoppingList.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        HorizontalLayout buttons = new HorizontalLayout(newRecipe, browseIngredients, mealPlanner, pantry, shoppingList);
        buttons.setSpacing(true);

        VerticalLayout section = new VerticalLayout(title, buttons);
        section.setPadding(false);
        section.setSpacing(true);

        HorizontalLayout wrapper = new HorizontalLayout(section);
        wrapper.setWidthFull();
        return wrapper;
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
