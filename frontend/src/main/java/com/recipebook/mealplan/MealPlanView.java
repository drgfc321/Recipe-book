package com.recipebook.mealplan;

import com.recipebook.recipe.MacroInfo;
import com.recipebook.recipe.RecipeResponse;
import com.recipebook.ui.MacroBar;
import com.recipebook.views.MainLayout;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Route(value = "meal-plan", layout = MainLayout.class)
public class MealPlanView extends VerticalLayout {

    private final MealPlanService mealPlanService;
    private LocalDate weekStart;
    private WeeklyMealPlanResponse weekData;

    private final Span weekLabel = new Span();
    private final Div calendarGrid = new Div();
    private final HorizontalLayout weeklySummary = new HorizontalLayout();

    private static final String[] MEAL_SLOTS = {"BREAKFAST", "LUNCH", "DINNER", "SNACK"};
    private static final String[] SLOT_LABELS = {"Breakfast", "Lunch", "Dinner", "Snack"};
    private static final String[] SLOT_CSS = {"breakfast", "lunch", "dinner", "snack"};

    public MealPlanView(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;

        LocalDate today = LocalDate.now();
        weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());

        calendarGrid.addClassName("meal-plan-grid");
        Div gridWrapper = new Div(calendarGrid);
        gridWrapper.getStyle()
                .set("overflow-x", "auto")
                .set("width", "100%")
                .set("flex-grow", "1");
        add(gridWrapper);

        weeklySummary.addClassName("meal-plan-weekly-summary");
        weeklySummary.setWidthFull();
        weeklySummary.setAlignItems(FlexComponent.Alignment.CENTER);
        add(weeklySummary);

        loadWeek();
    }

    private HorizontalLayout createHeader() {
        H2 title = new H2("Meal Planner");
        title.getStyle().set("margin", "0");

        Button prevBtn = new Button(new Icon(VaadinIcon.ANGLE_LEFT), e -> {
            weekStart = weekStart.minusWeeks(1);
            loadWeek();
        });
        prevBtn.addClassName("week-nav-btn");
        prevBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        weekLabel.getStyle()
                .set("font-weight", "600")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("min-width", "220px")
                .set("text-align", "center");

        Button nextBtn = new Button(new Icon(VaadinIcon.ANGLE_RIGHT), e -> {
            weekStart = weekStart.plusWeeks(1);
            loadWeek();
        });
        nextBtn.addClassName("week-nav-btn");
        nextBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button todayBtn = new Button("Today", e -> {
            weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            loadWeek();
        });
        todayBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout weekNav = new HorizontalLayout(prevBtn, weekLabel, nextBtn, todayBtn);
        weekNav.setAlignItems(FlexComponent.Alignment.CENTER);
        weekNav.addClassName("meal-plan-week-nav");

        HorizontalLayout header = new HorizontalLayout(title, weekNav);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        return header;
    }

    private void loadWeek() {
        LocalDate weekEnd = weekStart.plusDays(6);
        weekLabel.setText(formatWeekRange(weekStart, weekEnd));

        try {
            weekData = mealPlanService.getWeeklyMealPlan(weekStart);
        } catch (Exception e) {
            weekData = null;
            Notification.show("Failed to load meal plan: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        buildCalendar();
        updateWeeklySummary();
    }

    private void buildCalendar() {
        calendarGrid.removeAll();

        LocalDate today = LocalDate.now();

        // Empty top-left corner
        Div corner = new Div();
        corner.addClassName("meal-plan-corner");
        calendarGrid.add(corner);

        // Day headers
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            Div dayHeader = new Div();
            dayHeader.addClassName("meal-plan-day-header");
            if (date.equals(today)) {
                dayHeader.addClassName("today");
            }

            Span dayName = new Span(date.format(DateTimeFormatter.ofPattern("EEE")));
            dayName.getStyle().set("display", "block").set("font-size", "var(--lumo-font-size-xs)")
                    .set("text-transform", "uppercase").set("letter-spacing", "0.5px");

            Span dayNum = new Span(String.valueOf(date.getDayOfMonth()));
            dayNum.getStyle().set("display", "block").set("font-size", "var(--lumo-font-size-xl)")
                    .set("font-weight", "700").set("line-height", "1.2");

            dayHeader.add(dayName, dayNum);
            calendarGrid.add(dayHeader);
        }

        // Meal slot rows
        for (int s = 0; s < MEAL_SLOTS.length; s++) {
            // Slot label
            Div slotLabel = new Div();
            slotLabel.addClassNames("meal-plan-slot-label", SLOT_CSS[s]);
            slotLabel.setText(SLOT_LABELS[s]);
            calendarGrid.add(slotLabel);

            // Meal cells for each day
            for (int d = 0; d < 7; d++) {
                LocalDate date = weekStart.plusDays(d);
                MealPlanResponse meal = findMeal(d, MEAL_SLOTS[s]);

                if (meal != null && meal.recipe() != null) {
                    calendarGrid.add(createFilledCell(date, MEAL_SLOTS[s], SLOT_CSS[s], meal));
                } else {
                    calendarGrid.add(createEmptyCell(date, MEAL_SLOTS[s]));
                }
            }
        }

        // Daily macro summary row
        Div macroLabel = new Div();
        macroLabel.addClassName("meal-plan-macro-label");
        macroLabel.setText("Macros");
        calendarGrid.add(macroLabel);

        for (int d = 0; d < 7; d++) {
            DailyMealPlanResponse day = (weekData != null && weekData.days() != null && d < weekData.days().size())
                    ? weekData.days().get(d) : null;
            calendarGrid.add(createDailyMacroCell(day));
        }
    }

    private MealPlanResponse findMeal(int dayIndex, String mealSlot) {
        if (weekData == null || weekData.days() == null || dayIndex >= weekData.days().size()) return null;
        DailyMealPlanResponse day = weekData.days().get(dayIndex);
        if (day.meals() == null) return null;
        for (MealPlanResponse meal : day.meals()) {
            if (mealSlot.equals(meal.mealSlot())) {
                return meal;
            }
        }
        return null;
    }

    private Div createFilledCell(LocalDate date, String mealSlot, String slotCss, MealPlanResponse meal) {
        Div cell = new Div();
        cell.addClassNames("meal-cell", "meal-cell-filled", "slot-" + slotCss);

        Span name = new Span(meal.recipe().name());
        name.addClassName("meal-recipe-name");
        name.getStyle().set("cursor", "pointer");
        name.addClickListener(e -> UI.getCurrent().navigate("recipe/" + meal.recipe().id()));

        double cals = meal.recipe().perServingMacros() != null ? meal.recipe().perServingMacros().calories() : 0;
        Span calories = new Span("%.0f kcal".formatted(cals));
        calories.addClassName("meal-calorie-badge");

        Button removeBtn = new Button(VaadinIcon.CLOSE_SMALL.create());
        removeBtn.addClassName("meal-remove-btn");
        removeBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        removeBtn.addClickListener(e -> removeMeal(date, mealSlot));

        cell.add(name, calories, removeBtn);
        return cell;
    }

    private Div createEmptyCell(LocalDate date, String mealSlot) {
        Div cell = new Div();
        cell.addClassNames("meal-cell", "meal-cell-empty");

        Icon plusIcon = VaadinIcon.PLUS.create();
        plusIcon.setSize("20px");
        plusIcon.getStyle().set("color", "var(--lumo-tertiary-text-color)");

        cell.add(plusIcon);
        cell.addClickListener(e -> openRecipePicker(date, mealSlot));
        return cell;
    }

    private Div createDailyMacroCell(DailyMealPlanResponse day) {
        Div cell = new Div();
        cell.addClassName("meal-plan-macro-cell");

        if (day == null || day.totalMacros() == null || day.totalMacros().calories() == 0) {
            Span dash = new Span("\u2014");
            dash.getStyle().set("color", "var(--lumo-tertiary-text-color)");
            cell.add(dash);
            return cell;
        }

        MacroInfo m = day.totalMacros();

        Span cals = new Span("%.0f kcal".formatted(m.calories()));
        cals.getStyle()
                .set("font-weight", "600")
                .set("display", "block")
                .set("color", "var(--lumo-error-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        Div details = new Div();
        details.addClassName("macro-detail-row");
        details.add(
            createMacroPill("P", m.protein(), "var(--macro-protein)"),
            createMacroPill("C", m.carbs(),   "var(--macro-carbs)"),
            createMacroPill("F", m.fat(),     "var(--macro-fat)")
        );

        cell.add(cals, details);
        return cell;
    }

    private Span createMacroPill(String label, double value, String color) {
        Span pill = new Span("%s %.0fg".formatted(label, value));
        pill.addClassName("macro-pill");
        pill.getStyle().set("--pill-color", color);
        return pill;
    }

    private void openRecipePicker(LocalDate date, String mealSlot) {
        Dialog dialog = new Dialog();
        dialog.setWidth("min(500px, 90vw)");
        dialog.setHeight("min(600px, 80vh)");

        Span tag = new Span(mealSlot);
        tag.addClassName("dialog-header-tag");

        H2 dialogTitle = new H2("Pick a Recipe");
        dialogTitle.addClassName("dialog-header-title");

        dialog.getHeader().add(tag, dialogTitle);

        Button closeBtn = new Button(VaadinIcon.CLOSE.create(), e -> dialog.close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        dialog.getHeader().add(closeBtn);

        TextField searchField = new TextField();
        searchField.setPlaceholder("Search recipes...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setWidthFull();
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(300);
        searchField.setClearButtonVisible(true);

        VerticalLayout recipeList = new VerticalLayout();
        recipeList.setPadding(false);
        recipeList.setSpacing(false);
        recipeList.getStyle().set("overflow-y", "auto").set("flex-grow", "1");

        Runnable loadRecipes = () -> {
            recipeList.removeAll();
            try {
                List<RecipeResponse> recipes = mealPlanService.getRecipes(searchField.getValue());
                for (RecipeResponse recipe : recipes) {
                    recipeList.add(createPickerCard(recipe, date, mealSlot, dialog));
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

        searchField.addValueChangeListener(e -> loadRecipes.run());

        VerticalLayout content = new VerticalLayout(searchField, recipeList);
        content.setSizeFull();
        content.setPadding(false);
        content.setSpacing(true);
        content.expand(recipeList);

        dialog.add(content);
        dialog.open();
        loadRecipes.run();
    }

    private Div createPickerCard(RecipeResponse recipe, LocalDate date, String mealSlot, Dialog dialog) {
        Div card = new Div();
        card.addClassName("recipe-picker-card");

        Span name = new Span(recipe.name());
        name.getStyle().set("font-weight", "600").set("display", "block");

        HorizontalLayout meta = new HorizontalLayout();
        meta.setSpacing(true);
        meta.setPadding(false);
        meta.setAlignItems(FlexComponent.Alignment.CENTER);
        meta.getStyle().set("margin-top", "4px");

        if (recipe.category() != null) {
            Span category = new Span(recipe.category());
            category.getElement().getThemeList().add("badge small");
            category.getStyle()
                    .set("background", "var(--lumo-primary-color-10pct)")
                    .set("color", "var(--lumo-primary-text-color)")
                    .set("font-size", "var(--lumo-font-size-xs)");
            meta.add(category);
        }

        if (recipe.perServingMacros() != null) {
            Span cals = new Span("%.0f kcal/serving".formatted(recipe.perServingMacros().calories()));
            cals.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("font-size", "var(--lumo-font-size-xs)");
            meta.add(cals);
        }

        card.add(name, meta);

        card.addClickListener(e -> {
            try {
                mealPlanService.assignMealPlan(date, mealSlot, recipe.id());
                dialog.close();
                loadWeek();
                Notification.show(recipe.name() + " assigned", 2000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } catch (Exception ex) {
                Notification.show("Failed: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        return card;
    }

    private void removeMeal(LocalDate date, String mealSlot) {
        try {
            mealPlanService.removeMealPlan(date, mealSlot);
            loadWeek();
            Notification.show("Meal removed", 2000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Failed: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updateWeeklySummary() {
        weeklySummary.removeAll();

        if (weekData == null) return;

        Span totalLabel = new Span("Weekly Total");
        totalLabel.getStyle().set("font-weight", "600").set("white-space", "nowrap");

        MacroBar totalMacros = new MacroBar(weekData.totalMacros());

        Div totalSection = new Div();
        totalSection.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "var(--lumo-space-s)");
        totalSection.add(totalLabel, totalMacros);

        Span avgLabel = new Span("Daily Average");
        avgLabel.getStyle().set("font-weight", "600").set("white-space", "nowrap");

        MacroBar avgMacros = new MacroBar(weekData.averageDailyMacros());

        Div avgSection = new Div();
        avgSection.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "var(--lumo-space-s)");
        avgSection.add(avgLabel, avgMacros);

        weeklySummary.add(totalSection, avgSection);
        weeklySummary.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
    }

    private String formatWeekRange(LocalDate start, LocalDate end) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");
        DateTimeFormatter fmtYear = DateTimeFormatter.ofPattern("MMM d, yyyy");
        return start.format(fmt) + " \u2013 " + end.format(fmtYear);
    }
}
