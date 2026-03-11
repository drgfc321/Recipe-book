package com.recipebook.foodlog;

import com.recipebook.ingredient.IngredientResponse;
import com.recipebook.recipe.MacroInfo;
import com.recipebook.recipe.RecipeResponse;
import com.recipebook.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "food-log", layout = MainLayout.class)
@PageTitle("Food Log | Recipe Book")
public class FoodLogView extends VerticalLayout {

    private final FoodLogService foodLogService;
    private LocalDate currentDate;
    private DailyFoodLogResponse dailyData;

    private final Span dateLabel = new Span();
    private final VerticalLayout contentArea = new VerticalLayout();
    private final Div macroSummaryCard = new Div();

    private static final String[] MEAL_SLOTS = {"BREAKFAST", "LUNCH", "DINNER", "SNACK"};
    private static final String[] SLOT_LABELS = {"Breakfast", "Lunch", "Dinner", "Snack"};

    public FoodLogView(FoodLogService foodLogService) {
        this.foodLogService = foodLogService;
        this.currentDate = LocalDate.now();

        setPadding(true);
        setSpacing(true);
        setMaxWidth("900px");
        getStyle().set("margin", "0 auto");

        add(createHeader());

        macroSummaryCard.addClassName("food-log-macro-summary");
        add(macroSummaryCard);

        contentArea.setPadding(false);
        contentArea.setSpacing(true);
        add(contentArea);

        loadDay();
    }

    private HorizontalLayout createHeader() {
        H2 title = new H2("Food Log");
        title.getStyle().set("margin", "0");

        Button prevBtn = new Button(new Icon(VaadinIcon.ANGLE_LEFT), e -> {
            currentDate = currentDate.minusDays(1);
            loadDay();
        });
        prevBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dateLabel.getStyle()
                .set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("min-width", "180px")
                .set("text-align", "center");

        Button nextBtn = new Button(new Icon(VaadinIcon.ANGLE_RIGHT), e -> {
            currentDate = currentDate.plusDays(1);
            loadDay();
        });
        nextBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button todayBtn = new Button("Today", e -> {
            currentDate = LocalDate.now();
            loadDay();
        });
        todayBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout dateNav = new HorizontalLayout(prevBtn, dateLabel, nextBtn, todayBtn);
        dateNav.setAlignItems(FlexComponent.Alignment.CENTER);

        Button logBtn = new Button("Log a Meal", VaadinIcon.PLUS.create(), e -> openLogDialog());
        logBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout header = new HorizontalLayout(title, dateNav, logBtn);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("flex-wrap", "wrap").set("gap", "var(--lumo-space-s)");
        return header;
    }

    private void loadDay() {
        dateLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")));

        try {
            dailyData = foodLogService.getDailyFoodLog(currentDate);
        } catch (Exception e) {
            dailyData = null;
            Notification.show("Failed to load food log: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        buildMacroSummary();
        buildEntries();
    }

    private void buildMacroSummary() {
        macroSummaryCard.removeAll();

        MacroInfo actual = dailyData != null ? dailyData.totalActualMacros() : null;
        UserNutritionTargetResponse targets = dailyData != null ? dailyData.targets() : null;

        if (actual == null) actual = new MacroInfo(0, 0, 0, 0);
        double targetCal = targets != null ? targets.calories() : 2000;
        double targetPro = targets != null ? targets.protein() : 150;
        double targetCarbs = targets != null ? targets.carbs() : 250;
        double targetFat = targets != null ? targets.fat() : 65;

        macroSummaryCard.getStyle()
                .set("background", "#232838")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-m)")
                .set("box-shadow", "var(--lumo-box-shadow-m)")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        H3 summaryTitle = new H3("Daily Nutrition");
        summaryTitle.getStyle().set("margin", "0 0 var(--lumo-space-s) 0")
                .set("font-size", "var(--lumo-font-size-l)");
        macroSummaryCard.add(summaryTitle);

        macroSummaryCard.add(createProgressBar("Calories", actual.calories(), targetCal, "kcal", "var(--lumo-error-color)"));
        macroSummaryCard.add(createProgressBar("Protein", actual.protein(), targetPro, "g", "var(--macro-protein, #4caf50)"));
        macroSummaryCard.add(createProgressBar("Carbs", actual.carbs(), targetCarbs, "g", "var(--macro-carbs, #ff9800)"));
        macroSummaryCard.add(createProgressBar("Fat", actual.fat(), targetFat, "g", "var(--macro-fat, #2196f3)"));
    }

    private Div createProgressBar(String label, double actual, double target, String unit, String color) {
        Div row = new Div();
        row.getStyle()
                .set("margin-bottom", "var(--lumo-space-s)")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("gap", "2px")
                .set("width", "100%")
                .set("box-sizing", "border-box");

        HorizontalLayout labelRow = new HorizontalLayout();
        labelRow.setWidthFull();
        labelRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        labelRow.setPadding(false);
        labelRow.setSpacing(false);

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-weight", "500").set("font-size", "var(--lumo-font-size-s)");

        String valueText = target > 0
                ? "%.0f / %.0f %s".formatted(actual, target, unit)
                : "%.0f %s".formatted(actual, unit);
        Span valueSpan = new Span(valueText);
        valueSpan.getStyle().set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)");

        labelRow.add(labelSpan, valueSpan);

        Div track = new Div();
        track.getStyle()
                .set("width", "100%")
                .set("height", "8px")
                .set("background", "var(--lumo-contrast-10pct)")
                .set("border-radius", "4px")
                .set("overflow", "hidden");

        double pct = target > 0 ? Math.min((actual / target) * 100, 100) : 0;
        boolean over = target > 0 && actual > target;

        Div fill = new Div();
        fill.getStyle()
                .set("width", "%.1f%%".formatted(pct))
                .set("height", "100%")
                .set("background", over ? "var(--lumo-error-color)" : color)
                .set("border-radius", "4px")
                .set("transition", "width 0.3s ease");
        track.add(fill);

        row.add(labelRow, track);
        return row;
    }

    private void buildEntries() {
        contentArea.removeAll();

        boolean hasPlannedMeals = dailyData != null && dailyData.plannedMeals() != null
                && !dailyData.plannedMeals().isEmpty();
        boolean hasEntries = dailyData != null && dailyData.entries() != null
                && !dailyData.entries().isEmpty();

        if (hasPlannedMeals) {
            buildPlannedMealsSection();
        }

        if (hasEntries) {
            List<Long> plannedFoodLogIds = new ArrayList<>();
            if (hasPlannedMeals) {
                for (PlannedMealStatus pm : dailyData.plannedMeals()) {
                    if (pm.logged() && pm.foodLogId() != null) {
                        plannedFoodLogIds.add(pm.foodLogId());
                    }
                }
            }

            List<FoodLogResponse> additionalEntries = dailyData.entries().stream()
                    .filter(e -> !plannedFoodLogIds.contains(e.id()))
                    .collect(Collectors.toList());

            if (!additionalEntries.isEmpty()) {
                buildFoodLogSection(additionalEntries);
            }
        }

        if (!hasPlannedMeals && !hasEntries) {
            Span empty = new Span("No food logged for this day. Click \"Log a Meal\" to get started.");
            empty.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("padding", "var(--lumo-space-l)")
                    .set("text-align", "center")
                    .set("display", "block");
            contentArea.add(empty);
        }
    }

    private void buildPlannedMealsSection() {
        HorizontalLayout sectionHeader = new HorizontalLayout();
        sectionHeader.setWidthFull();
        sectionHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        sectionHeader.setAlignItems(FlexComponent.Alignment.CENTER);

        H3 sectionTitle = new H3("Planned Meals");
        sectionTitle.getStyle().set("margin", "0");

        List<PlannedMealStatus> unchecked = dailyData.plannedMeals().stream()
                .filter(pm -> !pm.logged())
                .collect(Collectors.toList());

        if (!unchecked.isEmpty()) {
            Button logAllBtn = new Button("Log All as Eaten", VaadinIcon.CHECK.create(), e -> {
                int success = 0;
                int failed = 0;
                for (PlannedMealStatus pm : unchecked) {
                    try {
                        foodLogService.logFood(currentDate, pm.mealSlot(),
                                pm.recipe().id(), 1.0, pm.mealPlanId());
                        success++;
                    } catch (Exception ex) {
                        failed++;
                    }
                }
                String msg = failed == 0
                        ? "Logged %d of %d meals".formatted(success, unchecked.size())
                        : "Logged %d of %d meals, %d failed".formatted(success, unchecked.size(), failed);
                Notification n = Notification.show(msg, 3000, Notification.Position.BOTTOM_START);
                if (failed == 0) n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                else n.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
                loadDay();
            });
            logAllBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            sectionHeader.add(sectionTitle, logAllBtn);
        } else {
            sectionHeader.add(sectionTitle);
        }

        contentArea.add(sectionHeader);

        Map<String, List<PlannedMealStatus>> grouped = dailyData.plannedMeals().stream()
                .collect(Collectors.groupingBy(PlannedMealStatus::mealSlot));

        for (int i = 0; i < MEAL_SLOTS.length; i++) {
            List<PlannedMealStatus> slotPlanned = grouped.get(MEAL_SLOTS[i]);
            if (slotPlanned == null || slotPlanned.isEmpty()) continue;

            Span slotLabel = new Span(SLOT_LABELS[i]);
            slotLabel.getStyle()
                    .set("font-weight", "600")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("margin-top", "var(--lumo-space-xs)");
            contentArea.add(slotLabel);

            for (PlannedMealStatus pm : slotPlanned) {
                contentArea.add(createPlannedMealCard(pm));
            }
        }
    }

    private Div createPlannedMealCard(PlannedMealStatus pm) {
        Div card = new Div();
        card.getStyle()
                .set("background", pm.logged() ? "#1b3a2a" : "#232838")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "var(--lumo-space-m)");

        Checkbox checkbox = new Checkbox();
        checkbox.setValue(pm.logged());
        checkbox.addValueChangeListener(e -> {
            if (e.isFromClient()) {
                if (e.getValue()) {
                    try {
                        foodLogService.logFood(currentDate, pm.mealSlot(),
                                pm.recipe().id(), 1.0, pm.mealPlanId());
                        loadDay();
                    } catch (Exception ex) {
                        Notification.show("Failed: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                                .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        checkbox.setValue(false);
                    }
                } else {
                    if (pm.foodLogId() != null) {
                        try {
                            foodLogService.removeFoodLog(pm.foodLogId());
                            loadDay();
                        } catch (Exception ex) {
                            Notification.show("Failed: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                            checkbox.setValue(true);
                        }
                    }
                }
            }
        });

        Span name = new Span(pm.recipe() != null ? pm.recipe().name() : "Unknown");
        name.getStyle().set("font-weight", "600").set("flex", "1");
        if (pm.logged()) {
            name.getStyle().set("text-decoration", "line-through")
                    .set("color", "var(--lumo-secondary-text-color)");
        }

        HorizontalLayout macros = new HorizontalLayout();
        macros.setSpacing(true);
        macros.setPadding(false);
        macros.setAlignItems(FlexComponent.Alignment.CENTER);
        if (pm.recipe() != null && pm.recipe().perServingMacros() != null) {
            MacroInfo m = pm.recipe().perServingMacros();
            macros.add(createMacroPill("%.0f kcal".formatted(m.calories()), "var(--lumo-error-color)"));
            macros.add(createMacroPill("P %.0fg".formatted(m.protein()), "var(--macro-protein, #4caf50)"));
            macros.add(createMacroPill("C %.0fg".formatted(m.carbs()), "var(--macro-carbs, #ff9800)"));
            macros.add(createMacroPill("F %.0fg".formatted(m.fat()), "var(--macro-fat, #2196f3)"));
        }

        card.add(checkbox, name, macros);
        return card;
    }

    private void buildFoodLogSection(List<FoodLogResponse> entries) {
        H3 sectionTitle = new H3("Food Log");
        sectionTitle.getStyle().set("margin", "var(--lumo-space-m) 0 0 0");
        contentArea.add(sectionTitle);

        Map<String, List<FoodLogResponse>> grouped = entries.stream()
                .collect(Collectors.groupingBy(FoodLogResponse::mealSlot));

        for (int i = 0; i < MEAL_SLOTS.length; i++) {
            List<FoodLogResponse> slotEntries = grouped.get(MEAL_SLOTS[i]);
            if (slotEntries == null || slotEntries.isEmpty()) continue;

            Span slotLabel = new Span(SLOT_LABELS[i]);
            slotLabel.getStyle()
                    .set("font-weight", "600")
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("margin-top", "var(--lumo-space-xs)");
            contentArea.add(slotLabel);

            for (FoodLogResponse entry : slotEntries) {
                contentArea.add(createEntryCard(entry));
            }
        }
    }

    private Div createEntryCard(FoodLogResponse entry) {
        Div card = new Div();
        card.getStyle()
                .set("background", "#232838")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "var(--lumo-space-m)");

        // Source badge + name
        String displayName;
        String badgeText;
        String badgeColor;

        FoodSourceType st = entry.sourceType();
        if (st == FoodSourceType.INGREDIENT) {
            String qty = entry.ingredientQuantity() != null ? "%.0fg".formatted(entry.ingredientQuantity()) : "";
            displayName = entry.ingredientName() != null ? entry.ingredientName() + " (" + qty + ")" : "Ingredient";
            badgeText = "Ingredient";
            badgeColor = "var(--lumo-success-color)";
        } else if (st == FoodSourceType.CUSTOM) {
            displayName = entry.customName() != null ? entry.customName() : "Custom";
            badgeText = "Custom";
            badgeColor = "var(--lumo-contrast-50pct)";
        } else {
            displayName = entry.recipe() != null ? entry.recipe().name() : "Recipe";
            badgeText = "Recipe";
            badgeColor = "var(--lumo-primary-color)";
        }

        HorizontalLayout nameArea = new HorizontalLayout();
        nameArea.setSpacing(true);
        nameArea.setPadding(false);
        nameArea.setAlignItems(FlexComponent.Alignment.CENTER);
        nameArea.getStyle().set("flex", "1");

        Span badge = new Span(badgeText);
        badge.getElement().getThemeList().add("badge small");
        badge.getStyle()
                .set("background", badgeColor)
                .set("color", "white")
                .set("font-size", "var(--lumo-font-size-xs)");

        Span name = new Span(displayName);
        name.getStyle().set("font-weight", "600");
        nameArea.add(badge, name);

        // Macro pills
        HorizontalLayout macros = new HorizontalLayout();
        macros.setSpacing(true);
        macros.setPadding(false);
        macros.setAlignItems(FlexComponent.Alignment.CENTER);
        if (entry.actualMacros() != null) {
            macros.add(createMacroPill("%.0f kcal".formatted(entry.actualMacros().calories()), "var(--lumo-error-color)"));
            macros.add(createMacroPill("P %.0fg".formatted(entry.actualMacros().protein()), "var(--macro-protein, #4caf50)"));
            macros.add(createMacroPill("C %.0fg".formatted(entry.actualMacros().carbs()), "var(--macro-carbs, #ff9800)"));
            macros.add(createMacroPill("F %.0fg".formatted(entry.actualMacros().fat()), "var(--macro-fat, #2196f3)"));
        }

        // Servings
        Span servingsSpan = new Span("%.1fx".formatted(entry.servings()));
        servingsSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-s)")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("cursor", "pointer")
                .set("text-decoration", "underline dotted");
        servingsSpan.addClickListener(e -> openEditServingsDialog(entry));

        // Delete
        Button deleteBtn = new Button(VaadinIcon.TRASH.create());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        deleteBtn.addClickListener(e -> {
            try {
                foodLogService.removeFoodLog(entry.id());
                loadDay();
                Notification.show("Entry removed", 2000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Failed: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        card.add(nameArea, macros, servingsSpan, deleteBtn);
        return card;
    }

    private Span createMacroPill(String text, String color) {
        Span pill = new Span(text);
        pill.getStyle()
                .set("font-size", "var(--lumo-font-size-xs)")
                .set("padding", "2px 6px")
                .set("border-radius", "var(--lumo-border-radius-s)")
                .set("background", color)
                .set("color", "white")
                .set("white-space", "nowrap");
        return pill;
    }

    private void openEditServingsDialog(FoodLogResponse entry) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit Servings");
        dialog.setWidth("300px");

        NumberField servingsField = new NumberField("Servings");
        servingsField.setValue(entry.servings());
        servingsField.setMin(0.1);
        servingsField.setStep(0.5);
        servingsField.setWidthFull();
        dialog.add(servingsField);

        Button saveBtn = new Button("Save", e -> {
            try {
                foodLogService.updateFoodLog(entry.id(), servingsField.getValue());
                dialog.close();
                loadDay();
                Notification.show("Servings updated", 2000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Failed: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Cancel", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        dialog.getFooter().add(cancelBtn, saveBtn);
        dialog.open();
    }

    private void openLogDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("min(550px, 90vw)");
        dialog.setHeight("min(700px, 85vh)");
        dialog.setHeaderTitle("Log a Meal");

        ComboBox<String> slotCombo = new ComboBox<>("Meal Slot");
        slotCombo.setItems(SLOT_LABELS);
        slotCombo.setValue("Lunch");
        slotCombo.setWidthFull();

        NumberField servingsField = new NumberField("Servings");
        servingsField.setValue(1.0);
        servingsField.setMin(0.1);
        servingsField.setStep(0.5);
        servingsField.setWidthFull();

        FlexLayout controls = new FlexLayout(slotCombo, servingsField);
        controls.setWidthFull();
        controls.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        controls.getStyle().set("gap", "var(--lumo-space-s)");

        // Toggle buttons: Recipe / Ingredient / Custom
        Button recipeBtn = new Button("Recipe");
        Button ingredientBtn = new Button("Ingredient");
        Button customBtn = new Button("Custom");
        recipeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        ingredientBtn.addClassName("toggle-inactive");
        customBtn.addClassName("toggle-inactive");

        HorizontalLayout toggleRow = new HorizontalLayout(recipeBtn, ingredientBtn, customBtn);
        toggleRow.setSpacing(true);
        toggleRow.setPadding(false);

        // --- Recipe tab ---
        TextField recipeSearch = new TextField();
        recipeSearch.setPlaceholder("Search recipes...");
        recipeSearch.setPrefixComponent(VaadinIcon.SEARCH.create());
        recipeSearch.setWidthFull();
        recipeSearch.setValueChangeMode(ValueChangeMode.LAZY);
        recipeSearch.setValueChangeTimeout(300);
        recipeSearch.setClearButtonVisible(true);

        VerticalLayout recipeList = new VerticalLayout();
        recipeList.setPadding(false);
        recipeList.setSpacing(false);
        recipeList.getStyle().set("overflow-y", "auto").set("flex-grow", "1").set("min-height", "0");

        VerticalLayout recipeView = new VerticalLayout(recipeSearch, recipeList);
        recipeView.setPadding(false);
        recipeView.setSpacing(true);
        recipeView.setSizeFull();
        recipeView.getStyle().set("min-height", "0");
        recipeView.expand(recipeList);

        // --- Ingredient tab ---
        TextField ingredientSearch = new TextField();
        ingredientSearch.setPlaceholder("Search ingredients...");
        ingredientSearch.setPrefixComponent(VaadinIcon.SEARCH.create());
        ingredientSearch.setWidthFull();
        ingredientSearch.setValueChangeMode(ValueChangeMode.LAZY);
        ingredientSearch.setValueChangeTimeout(300);
        ingredientSearch.setClearButtonVisible(true);

        VerticalLayout ingredientList = new VerticalLayout();
        ingredientList.setPadding(false);
        ingredientList.setSpacing(false);
        ingredientList.getStyle().set("overflow-y", "auto").set("flex-grow", "1").set("min-height", "0");

        NumberField quantityField = new NumberField("Quantity (grams)");
        quantityField.setValue(100.0);
        quantityField.setMin(1);
        quantityField.setStep(10);
        quantityField.setWidthFull();

        Button logIngredientBtn = new Button("Log Ingredient");
        logIngredientBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        logIngredientBtn.setWidthFull();
        logIngredientBtn.setEnabled(false);

        final Long[] selectedIngredientId = {null};

        logIngredientBtn.addClickListener(e -> {
            if (selectedIngredientId[0] == null) {
                Notification.show("Select an ingredient first", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            double qty = quantityField.getValue() != null ? quantityField.getValue() : 100;
            double srvs = servingsField.getValue() != null ? servingsField.getValue() : 1.0;
            String slot = labelToSlot(slotCombo.getValue());
            try {
                foodLogService.logIngredient(currentDate, slot, selectedIngredientId[0], qty, srvs);
                dialog.close();
                loadDay();
                Notification.show("Ingredient logged", 2000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Failed: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        VerticalLayout ingredientView = new VerticalLayout(ingredientSearch, ingredientList, quantityField, logIngredientBtn);
        ingredientView.setPadding(false);
        ingredientView.setSpacing(true);
        ingredientView.setSizeFull();
        ingredientView.getStyle().set("min-height", "0");
        ingredientView.expand(ingredientList);
        ingredientView.setVisible(false);

        // --- Custom tab ---
        TextField customNameField = new TextField("Meal Name");
        customNameField.setWidthFull();
        customNameField.setRequired(true);

        NumberField caloriesField = new NumberField("Calories");
        caloriesField.setWidthFull();
        caloriesField.setMin(0);
        caloriesField.setStep(1);

        NumberField proteinField = new NumberField("Protein (g)");
        proteinField.setWidthFull();
        proteinField.setMin(0);
        proteinField.setStep(0.1);
        proteinField.setValue(0.0);

        NumberField carbsField = new NumberField("Carbs (g)");
        carbsField.setWidthFull();
        carbsField.setMin(0);
        carbsField.setStep(0.1);
        carbsField.setValue(0.0);

        NumberField fatField = new NumberField("Fat (g)");
        fatField.setWidthFull();
        fatField.setMin(0);
        fatField.setStep(0.1);
        fatField.setValue(0.0);

        HorizontalLayout macroRow = new HorizontalLayout(proteinField, carbsField, fatField);
        macroRow.setWidthFull();
        macroRow.setSpacing(true);

        Button logCustomBtn = new Button("Log Custom Meal", e -> {
            String cName = customNameField.getValue();
            if (cName == null || cName.trim().isEmpty()) {
                Notification.show("Meal name is required", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            Double cals = caloriesField.getValue();
            if (cals == null || cals < 0) {
                Notification.show("Calories must be >= 0", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }
            double pro = proteinField.getValue() != null ? proteinField.getValue() : 0;
            double carbs = carbsField.getValue() != null ? carbsField.getValue() : 0;
            double fat = fatField.getValue() != null ? fatField.getValue() : 0;
            double srvs = servingsField.getValue() != null ? servingsField.getValue() : 1.0;
            String slot = labelToSlot(slotCombo.getValue());

            try {
                foodLogService.logCustomFood(currentDate, slot, cName.trim(), cals, pro, carbs, fat, srvs);
                dialog.close();
                loadDay();
                Notification.show(cName.trim() + " logged", 2000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Failed: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        logCustomBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        logCustomBtn.setWidthFull();

        VerticalLayout customView = new VerticalLayout(customNameField, caloriesField, macroRow, logCustomBtn);
        customView.setPadding(false);
        customView.setSpacing(true);
        customView.setVisible(false);

        // Toggle logic: active = LUMO_PRIMARY, inactive = toggle-inactive class
        Runnable setRecipeActive = () -> {
            recipeView.setVisible(true);
            ingredientView.setVisible(false);
            customView.setVisible(false);
            recipeBtn.removeClassName("toggle-inactive");
            recipeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            ingredientBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            ingredientBtn.addClassName("toggle-inactive");
            customBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            customBtn.addClassName("toggle-inactive");
        };
        Runnable setIngredientActive = () -> {
            recipeView.setVisible(false);
            ingredientView.setVisible(true);
            customView.setVisible(false);
            ingredientBtn.removeClassName("toggle-inactive");
            ingredientBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            recipeBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            recipeBtn.addClassName("toggle-inactive");
            customBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            customBtn.addClassName("toggle-inactive");
        };
        Runnable setCustomActive = () -> {
            recipeView.setVisible(false);
            ingredientView.setVisible(false);
            customView.setVisible(true);
            customBtn.removeClassName("toggle-inactive");
            customBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            recipeBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            recipeBtn.addClassName("toggle-inactive");
            ingredientBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            ingredientBtn.addClassName("toggle-inactive");
        };

        recipeBtn.addClickListener(e -> setRecipeActive.run());
        ingredientBtn.addClickListener(e -> setIngredientActive.run());
        customBtn.addClickListener(e -> setCustomActive.run());

        // Recipe search loader
        Runnable loadRecipes = () -> {
            recipeList.removeAll();
            try {
                List<RecipeResponse> recipes = foodLogService.searchRecipes(recipeSearch.getValue());
                for (RecipeResponse recipe : recipes) {
                    Div card = new Div();
                    card.addClassName("recipe-picker-card");
                    card.getStyle().set("padding", "var(--lumo-space-s) var(--lumo-space-m)");

                    Span recipeName = new Span(recipe.name());
                    recipeName.getStyle().set("font-weight", "600").set("display", "block");

                    HorizontalLayout meta = new HorizontalLayout();
                    meta.setSpacing(true);
                    meta.setPadding(false);
                    meta.getStyle().set("margin-top", "4px");

                    if (recipe.perServingMacros() != null) {
                        MacroInfo m = recipe.perServingMacros();
                        Span cals = new Span("%.0f kcal · P%.0f C%.0f F%.0f".formatted(
                                m.calories(), m.protein(), m.carbs(), m.fat()));
                        cals.getStyle()
                                .set("color", "var(--lumo-secondary-text-color)")
                                .set("font-size", "var(--lumo-font-size-xs)");
                        meta.add(cals);
                    }

                    card.add(recipeName, meta);
                    card.addClickListener(ev -> {
                        String slot = labelToSlot(slotCombo.getValue());
                        double srvs = servingsField.getValue() != null ? servingsField.getValue() : 1.0;
                        try {
                            foodLogService.logFood(currentDate, slot, recipe.id(), srvs, null);
                            dialog.close();
                            loadDay();
                            Notification.show(recipe.name() + " logged", 2000, Notification.Position.BOTTOM_START)
                                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                        } catch (Exception ex) {
                            Notification.show("Failed: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
                        }
                    });
                    recipeList.add(card);
                }
                if (recipes.isEmpty()) {
                    Span empty = new Span("No recipes found");
                    empty.getStyle()
                            .set("color", "var(--lumo-secondary-text-color)")
                            .set("padding", "var(--lumo-space-l)")
                            .set("text-align", "center")
                            .set("display", "block");
                    recipeList.add(empty);
                }
            } catch (Exception e) {
                Notification.show("Failed to load recipes: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        };

        // Ingredient search loader
        Runnable loadIngredients = () -> {
            ingredientList.removeAll();
            selectedIngredientId[0] = null;
            logIngredientBtn.setEnabled(false);
            try {
                List<IngredientResponse> ingredients = foodLogService.searchIngredients(ingredientSearch.getValue());
                for (IngredientResponse ing : ingredients) {
                    Div card = new Div();
                    card.addClassName("recipe-picker-card");
                    card.getStyle().set("padding", "var(--lumo-space-s) var(--lumo-space-m)");

                    Span ingName = new Span(ing.name());
                    ingName.getStyle().set("font-weight", "600").set("display", "block");

                    Span macroText = new Span("per 100g: %.0f kcal · P%.1f C%.1f F%.1f".formatted(
                            ing.caloriesPer100g(), ing.proteinPer100g(), ing.carbsPer100g(), ing.fatPer100g()));
                    macroText.getStyle()
                            .set("color", "var(--lumo-secondary-text-color)")
                            .set("font-size", "var(--lumo-font-size-xs)")
                            .set("display", "block")
                            .set("margin-top", "2px");

                    card.add(ingName, macroText);
                    card.addClickListener(ev -> {
                        selectedIngredientId[0] = ing.id();
                        logIngredientBtn.setEnabled(true);
                        logIngredientBtn.setText("Log " + ing.name());
                        // Highlight selected
                        ingredientList.getChildren().forEach(c ->
                                c.getElement().getStyle().remove("border"));
                        card.getStyle().set("border", "2px solid var(--lumo-primary-color)");
                    });
                    ingredientList.add(card);
                }
                if (ingredients.isEmpty()) {
                    Span empty = new Span("No ingredients found");
                    empty.getStyle()
                            .set("color", "var(--lumo-secondary-text-color)")
                            .set("padding", "var(--lumo-space-l)")
                            .set("text-align", "center")
                            .set("display", "block");
                    ingredientList.add(empty);
                }
            } catch (Exception e) {
                Notification.show("Failed to load ingredients: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        };

        recipeSearch.addValueChangeListener(e -> loadRecipes.run());
        ingredientSearch.addValueChangeListener(e -> loadIngredients.run());

        VerticalLayout content = new VerticalLayout(controls, toggleRow, recipeView, ingredientView, customView);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(true);
        content.getStyle().set("min-height", "0");
        content.expand(recipeView);

        dialog.add(content);
        dialog.open();
        loadRecipes.run();
    }

    private String labelToSlot(String label) {
        if (label == null) return "LUNCH";
        return switch (label) {
            case "Breakfast" -> "BREAKFAST";
            case "Lunch" -> "LUNCH";
            case "Dinner" -> "DINNER";
            case "Snack" -> "SNACK";
            default -> "LUNCH";
        };
    }
}
