package com.recipebook.shopping;

import com.recipebook.service.AuthService;
import com.recipebook.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route(value = "shopping-list", layout = MainLayout.class)
@PageTitle("Shopping List | Recipe Book")
public class ShoppingListView extends VerticalLayout {

    private final ShoppingListService shoppingListService;
    private final AuthService authService;

    private LocalDate currentWeekStart;
    private final Span weekLabel = new Span();
    private final VerticalLayout itemsContainer = new VerticalLayout();
    private final ProgressBar progressBar = new ProgressBar();
    private final Span progressLabel = new Span();

    public ShoppingListView(ShoppingListService shoppingListService, AuthService authService) {
        this.shoppingListService = shoppingListService;
        this.authService = authService;

        this.currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        setPadding(true);
        setSpacing(true);
        setSizeFull();

        add(createToolbar());
        add(createWeekNav());
        add(createProgressSection());
        add(createActionButtons());

        itemsContainer.setPadding(false);
        itemsContainer.setSpacing(false);
        itemsContainer.setWidthFull();
        add(itemsContainer);
        expand(itemsContainer);

        refreshList();
    }

    private HorizontalLayout createToolbar() {
        H2 title = new H2("Shopping List");
        title.getStyle().set("margin", "0");

        HorizontalLayout toolbar = new HorizontalLayout(title);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setWidthFull();
        toolbar.expand(title);
        return toolbar;
    }

    private HorizontalLayout createWeekNav() {
        Button prevBtn = new Button(VaadinIcon.ANGLE_LEFT.create(), e -> {
            currentWeekStart = currentWeekStart.minusWeeks(1);
            refreshList();
        });
        prevBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        prevBtn.addClassName("week-nav-btn");

        Button todayBtn = new Button("Today", e -> {
            currentWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            refreshList();
        });
        todayBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button nextBtn = new Button(VaadinIcon.ANGLE_RIGHT.create(), e -> {
            currentWeekStart = currentWeekStart.plusWeeks(1);
            refreshList();
        });
        nextBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        nextBtn.addClassName("week-nav-btn");

        weekLabel.getStyle().set("font-weight", "600").set("font-size", "var(--lumo-font-size-l)");

        HorizontalLayout nav = new HorizontalLayout(prevBtn, todayBtn, nextBtn, weekLabel);
        nav.setAlignItems(FlexComponent.Alignment.CENTER);
        nav.addClassName("meal-plan-week-nav");
        return nav;
    }

    private VerticalLayout createProgressSection() {
        progressBar.addClassName("shopping-progress-bar");
        progressBar.setWidthFull();

        progressLabel.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout section = new VerticalLayout(progressBar, progressLabel);
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle().set("gap", "var(--lumo-space-xs)");
        return section;
    }

    private HorizontalLayout createActionButtons() {
        Button generateBtn = new Button("Generate from Meal Plan", VaadinIcon.MAGIC.create(), e -> generateList());
        generateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button clearBtn = new Button("Clear List", VaadinIcon.TRASH.create(), e -> confirmClear());
        clearBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout actions = new HorizontalLayout(generateBtn, clearBtn);
        actions.setSpacing(true);
        return actions;
    }

    private void updateWeekLabel() {
        LocalDate weekEnd = currentWeekStart.plusDays(6);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");
        weekLabel.setText(currentWeekStart.format(fmt) + " - " + weekEnd.format(fmt) + ", " + currentWeekStart.getYear());
    }

    private void refreshList() {
        updateWeekLabel();
        try {
            ShoppingListResponse list = shoppingListService.getShoppingList(currentWeekStart);
            renderList(list);
        } catch (Exception e) {
            itemsContainer.removeAll();
            progressBar.setValue(0);
            progressLabel.setText("0 / 0 items");
            // List might not exist yet, that's okay
        }
    }

    private void renderList(ShoppingListResponse list) {
        itemsContainer.removeAll();

        if (list == null || list.items() == null || list.items().isEmpty()) {
            progressBar.setValue(0);
            progressLabel.setText("0 / 0 items");

            Div empty = new Div();
            empty.getStyle()
                    .set("text-align", "center")
                    .set("padding", "var(--lumo-space-xl)")
                    .set("color", "var(--lumo-secondary-text-color)");
            Span text = new Span("No items yet. Generate a list from your meal plan!");
            empty.add(text);
            itemsContainer.add(empty);
            return;
        }

        // Update progress
        double progress = list.totalItems() > 0 ? (double) list.purchasedItems() / list.totalItems() : 0;
        progressBar.setValue(progress);
        progressLabel.setText("%d / %d items (%.0f%%)".formatted(
                list.purchasedItems(), list.totalItems(), list.progressPercent()));

        // Group by category
        Map<String, List<ShoppingListItemResponse>> grouped = list.items().stream()
                .collect(Collectors.groupingBy(
                        item -> item.ingredientCategory() != null ? item.ingredientCategory() : "OTHER",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        for (Map.Entry<String, List<ShoppingListItemResponse>> entry : grouped.entrySet()) {
            // Category header
            H3 categoryHeader = new H3(entry.getKey());
            categoryHeader.addClassName("shopping-category-header");
            itemsContainer.add(categoryHeader);

            for (ShoppingListItemResponse item : entry.getValue()) {
                itemsContainer.add(createItemRow(item));
            }
        }
    }

    private HorizontalLayout createItemRow(ShoppingListItemResponse item) {
        Checkbox checkbox = new Checkbox();
        checkbox.setValue(item.purchased());
        checkbox.addValueChangeListener(e -> toggleItem(item));

        Span name = new Span(item.ingredientName());
        name.getStyle().set("flex", "1");
        if (item.purchased()) {
            name.addClassName("shopping-item-purchased");
        } else {
            name.addClassName("shopping-item");
        }

        Span qty = new Span("%.1f %s".formatted(item.quantity(), item.unit()));
        qty.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)")
                .set("min-width", "80px")
                .set("text-align", "right");
        if (item.purchased()) {
            qty.addClassName("shopping-item-purchased");
        }

        Button deleteBtn = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> removeItem(item));
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout row = new HorizontalLayout(checkbox, name, qty, deleteBtn);
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setWidthFull();
        row.addClassName("shopping-item-row");
        row.getStyle()
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                .set("border-bottom", "1px solid rgba(216,220,228,0.06)");

        if (item.purchased()) {
            row.getStyle().set("opacity", "0.6");
        }

        return row;
    }

    private void toggleItem(ShoppingListItemResponse item) {
        try {
            shoppingListService.toggleItem(item.id());
            refreshList();
        } catch (Exception e) {
            Notification.show("Failed to update item: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void removeItem(ShoppingListItemResponse item) {
        try {
            shoppingListService.removeItem(item.id());
            Notification.show("Item removed", 2000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            refreshList();
        } catch (Exception e) {
            Notification.show("Failed to remove: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void generateList() {
        try {
            ShoppingListResponse list = shoppingListService.generateShoppingList(currentWeekStart);
            renderList(list);
            Notification.show("Shopping list generated!", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            Notification.show("Failed to generate: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmClear() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Clear Shopping List");
        dialog.setText("Are you sure you want to clear all items from this week's shopping list?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Clear");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            try {
                shoppingListService.clearList(currentWeekStart);
                Notification.show("Shopping list cleared", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshList();
            } catch (Exception ex) {
                Notification.show("Failed to clear: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        dialog.open();
    }
}
