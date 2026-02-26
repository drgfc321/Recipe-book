package com.recipebook.recommendation;

import com.recipebook.recipe.RecipeResponse;
import com.recipebook.service.AuthService;
import com.recipebook.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Route(value = "recommendations", layout = MainLayout.class)
@PageTitle("Recommendations | Recipe Book")
public class RecommendationView extends VerticalLayout {

    private final RecommendationService recommendationService;
    private final AuthService authService;

    private final ComboBox<String> categoryFilter = new ComboBox<>("Category");
    private final ComboBox<String> difficultyFilter = new ComboBox<>("Difficulty");
    private final ComboBox<Integer> maxMissingFilter = new ComboBox<>("Max Missing");
    private final FlexLayout cardsContainer = new FlexLayout();

    public RecommendationView(RecommendationService recommendationService, AuthService authService) {
        this.recommendationService = recommendationService;
        this.authService = authService;

        setPadding(true);
        setSpacing(true);
        setSizeFull();

        add(createHeader());
        add(createFilters());
        configureCardsContainer();
        add(cardsContainer);
        expand(cardsContainer);

        refreshRecommendations();
    }

    private VerticalLayout createHeader() {
        H2 title = new H2("Recipe Recommendations");
        title.getStyle().set("margin", "0");

        Span subtitle = new Span("Based on your pantry");
        subtitle.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("margin-top", "0");

        VerticalLayout header = new VerticalLayout(title, subtitle);
        header.setPadding(false);
        header.setSpacing(false);
        header.getStyle().set("gap", "var(--lumo-space-xs)");
        return header;
    }

    private HorizontalLayout createFilters() {
        categoryFilter.setItems("BREAKFAST", "LUNCH", "DINNER", "DESSERT", "SNACK", "OTHER");
        categoryFilter.setPlaceholder("All");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> refreshRecommendations());

        difficultyFilter.setItems("EASY", "MEDIUM", "HARD");
        difficultyFilter.setPlaceholder("All");
        difficultyFilter.setClearButtonVisible(true);
        difficultyFilter.addValueChangeListener(e -> refreshRecommendations());

        maxMissingFilter.setItems(1, 2, 3, 4, 5);
        maxMissingFilter.setPlaceholder("Any");
        maxMissingFilter.setClearButtonVisible(true);
        maxMissingFilter.addValueChangeListener(e -> refreshRecommendations());

        HorizontalLayout filters = new HorizontalLayout(categoryFilter, difficultyFilter, maxMissingFilter);
        filters.setAlignItems(FlexComponent.Alignment.BASELINE);
        return filters;
    }

    private void configureCardsContainer() {
        cardsContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cardsContainer.getStyle().set("gap", "var(--lumo-space-m)");
        cardsContainer.setWidthFull();
    }

    private void refreshRecommendations() {
        cardsContainer.removeAll();

        try {
            List<RecipeRecommendationResponse> recommendations = recommendationService.getRecommendations(
                    categoryFilter.getValue(),
                    difficultyFilter.getValue(),
                    maxMissingFilter.getValue()
            );

            if (recommendations.isEmpty()) {
                Div empty = new Div();
                empty.getStyle()
                        .set("text-align", "center")
                        .set("padding", "var(--lumo-space-xl)")
                        .set("color", "var(--lumo-secondary-text-color)")
                        .set("width", "100%");
                empty.add(new Span("No recommendations found. Add items to your pantry first!"));
                cardsContainer.add(empty);
                return;
            }

            for (RecipeRecommendationResponse rec : recommendations) {
                cardsContainer.add(createRecommendationCard(rec));
            }
        } catch (Exception e) {
            Notification.show("Failed to load recommendations: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private VerticalLayout createRecommendationCard(RecipeRecommendationResponse rec) {
        RecipeResponse recipe = rec.recipe();

        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.addClassName("recommendation-card");
        card.getStyle()
                .set("flex", "1 1 300px")
                .set("max-width", "400px")
                .set("background", "#232838")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)")
                .set("cursor", "pointer");

        // Recipe name
        Span name = new Span(recipe.name());
        name.getStyle().set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-l)");
        card.add(name);

        // Badges row
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

        // Match percentage
        Span matchBadge = new Span("%.0f%% match".formatted(rec.matchPercent()));
        matchBadge.addClassName("match-badge");
        matchBadge.getElement().getThemeList().add("badge small");
        matchBadge.getStyle()
                .set("background-color", getMatchColor(rec.matchPercent()))
                .set("color", "white")
                .set("font-weight", "600")
                .set("margin-top", "var(--lumo-space-s)");
        card.add(matchBadge);

        // Match progress bar
        ProgressBar matchBar = new ProgressBar();
        matchBar.setValue(rec.matchPercent() / 100.0);
        matchBar.setWidthFull();
        matchBar.getStyle().set("margin-top", "var(--lumo-space-xs)");
        matchBar.getElement().getStyle().set("--lumo-primary-color", getMatchColor(rec.matchPercent()));
        card.add(matchBar);

        // Ingredient count
        Span countLabel = new Span("%d / %d ingredients available".formatted(rec.matchedIngredients(), rec.totalIngredients()));
        countLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("margin-top", "var(--lumo-space-xs)");
        card.add(countLabel);

        // Missing ingredients (collapsible)
        if (rec.missingIngredients() != null && !rec.missingIngredients().isEmpty()) {
            VerticalLayout missingList = new VerticalLayout();
            missingList.setPadding(false);
            missingList.setSpacing(false);
            missingList.getStyle().set("gap", "var(--lumo-space-xs)");

            for (MissingIngredientResponse missing : rec.missingIngredients()) {
                Span missingItem = new Span("%.1f %s %s".formatted(
                        missing.neededQuantity(), missing.unit(), missing.ingredientName()));
                missingItem.getStyle()
                        .set("font-size", "var(--lumo-font-size-s)")
                        .set("color", "var(--lumo-error-text-color)");
                missingList.add(missingItem);
            }

            Details missingDetails = new Details("Missing (%d)".formatted(rec.missingCount()), missingList);
            missingDetails.getStyle().set("margin-top", "var(--lumo-space-xs)");
            missingDetails.setOpened(false);
            card.add(missingDetails);

            // Add to shopping list button
            Button addToListBtn = new Button("Add missing to list", VaadinIcon.CART.create(), e -> {
                e.getSource().getElement().getNode().runWhenAttached(ui -> {
                    // Prevent card click
                });
                addMissingToShoppingList(recipe.id());
            });
            addToListBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            addToListBtn.getStyle().set("margin-top", "var(--lumo-space-xs)");
            addToListBtn.addClickListener(e -> e.getSource().getElement().executeJs("event.stopPropagation()"));
            card.add(addToListBtn);
        }

        // Card click → navigate to recipe
        card.addClickListener(e -> UI.getCurrent().navigate("recipes/" + recipe.id()));

        return card;
    }

    private String getMatchColor(double percent) {
        if (percent >= 75) return "var(--lumo-success-color)";
        if (percent >= 50) return "var(--recipe-warning-color)";
        return "var(--lumo-error-color)";
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

    private void addMissingToShoppingList(Long recipeId) {
        try {
            LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            recommendationService.addMissingToShoppingList(recipeId, weekStart);
            Notification.show("Missing ingredients added to shopping list!", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Failed: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
