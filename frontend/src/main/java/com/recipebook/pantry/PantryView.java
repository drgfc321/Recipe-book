package com.recipebook.pantry;

import com.recipebook.ingredient.IngredientService;
import com.recipebook.service.AuthService;
import com.recipebook.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
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
import java.util.stream.Collectors;

@Route(value = "pantry", layout = MainLayout.class)
@PageTitle("Pantry | Recipe Book")
public class PantryView extends VerticalLayout {

    private final PantryService pantryService;
    private final IngredientService ingredientService;
    private final AuthService authService;
    private final Grid<PantryItemResponse> grid = new Grid<>(PantryItemResponse.class, false);
    private final ComboBox<String> categoryFilter = new ComboBox<>("Category");
    private final TextField searchField = new TextField("Search");
    private final Div expiringBanner = new Div();

    private List<PantryItemResponse> allItems = List.of();

    public PantryView(PantryService pantryService, IngredientService ingredientService, AuthService authService) {
        this.pantryService = pantryService;
        this.ingredientService = ingredientService;
        this.authService = authService;

        setPadding(true);
        setSpacing(true);
        setSizeFull();
        getStyle().set("overflow-x", "hidden");

        add(createExpiringBanner());
        add(createToolbar());
        add(createFilters());
        configureGrid();
        add(grid);
        expand(grid);

        refreshGrid();
    }

    private Div createExpiringBanner() {
        expiringBanner.addClassName("pantry-expiring-banner");
        expiringBanner.setVisible(false);
        return expiringBanner;
    }

    private void updateExpiringBanner() {
        try {
            List<PantryItemResponse> expiring = pantryService.getExpiringItems(3);
            if (!expiring.isEmpty()) {
                expiringBanner.removeAll();
                Icon warnIcon = VaadinIcon.WARNING.create();
                warnIcon.setSize("16px");
                warnIcon.getStyle().set("margin-right", "var(--lumo-space-xs)");
                Span text = new Span(expiring.size() + " item(s) expiring within 3 days!");
                text.getStyle().set("font-weight", "600");
                expiringBanner.add(warnIcon, text);
                expiringBanner.setVisible(true);
            } else {
                expiringBanner.setVisible(false);
            }
        } catch (Exception e) {
            expiringBanner.setVisible(false);
        }
    }

    private HorizontalLayout createToolbar() {
        H2 title = new H2("Pantry");
        title.getStyle().set("margin", "0");

        HorizontalLayout toolbar = new HorizontalLayout(title);
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setWidthFull();
        toolbar.expand(title);

        if (authService.isLoggedIn()) {
            Button addButton = new Button("Add Item", VaadinIcon.PLUS.create(), e -> openCreateDialog());
            addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            toolbar.add(addButton);
        }

        return toolbar;
    }

    private FlexLayout createFilters() {
        categoryFilter.setItems("DAIRY", "MEAT", "VEGETABLES", "FRUITS", "GRAINS", "SPICES", "OILS", "BEVERAGES", "OTHER");
        categoryFilter.setPlaceholder("All");
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> applyFilters());

        searchField.setPlaceholder("Search pantry...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> applyFilters());

        FlexLayout filters = new FlexLayout(categoryFilter, searchField);
        filters.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        filters.getStyle()
                .set("gap", "var(--lumo-space-s)")
                .set("align-items", "baseline");
        filters.setWidthFull();
        return filters;
    }

    private void configureGrid() {
        grid.addComponentColumn(item -> {
            Span name = new Span(item.ingredientName());
            name.getStyle().set("font-weight", "600");
            return name;
        }).setHeader("Ingredient").setSortable(true).setFlexGrow(2);

        grid.addComponentColumn(item -> {
            Span badge = new Span(item.ingredientCategory());
            badge.getElement().getThemeList().add("badge small");
            badge.getStyle()
                    .set("background-color", getCategoryColor(item.ingredientCategory()))
                    .set("color", "white");
            return badge;
        }).setHeader("Category").setSortable(true);

        grid.addComponentColumn(item -> {
            Span qty = new Span("%.1f %s".formatted(item.quantity(), item.unit()));
            return qty;
        }).setHeader("Quantity").setSortable(true);

        grid.addComponentColumn(item -> {
            if (item.expirationDate() == null || item.expirationDate().isBlank()) {
                Span none = new Span("-");
                none.getStyle().set("color", "var(--lumo-secondary-text-color)");
                return none;
            }
            Span date = new Span(item.expirationDate());
            if (item.expiringSoon()) {
                date.getStyle()
                        .set("color", "var(--lumo-error-color)")
                        .set("font-weight", "600");
            }
            return date;
        }).setHeader("Expiration").setSortable(true);

        if (authService.isLoggedIn()) {
            grid.addComponentColumn(item -> {
                Button editBtn = new Button(VaadinIcon.EDIT.create(), e -> openEditDialog(item));
                editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

                Button deleteBtn = new Button(VaadinIcon.TRASH.create(), e -> confirmDelete(item));
                deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

                HorizontalLayout actions = new HorizontalLayout(editBtn, deleteBtn);
                actions.setSpacing(false);
                return actions;
            }).setHeader("").setWidth("120px").setFlexGrow(0);
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
            allItems = pantryService.getPantryItems();
            applyFilters();
            updateExpiringBanner();
        } catch (Exception e) {
            Notification.show("Failed to load pantry: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void applyFilters() {
        String search = searchField.getValue();
        String category = categoryFilter.getValue();

        List<PantryItemResponse> filtered = allItems.stream()
                .filter(item -> {
                    if (search != null && !search.isBlank()) {
                        return item.ingredientName() != null &&
                                item.ingredientName().toLowerCase().contains(search.toLowerCase());
                    }
                    return true;
                })
                .filter(item -> {
                    if (category != null && !category.isBlank()) {
                        return category.equals(item.ingredientCategory());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        grid.setItems(filtered);
    }

    private void openCreateDialog() {
        PantryFormDialog dialog = new PantryFormDialog(pantryService, ingredientService, null, this::refreshGrid);
        dialog.open();
    }

    private void openEditDialog(PantryItemResponse item) {
        PantryFormDialog dialog = new PantryFormDialog(pantryService, ingredientService, item, this::refreshGrid);
        dialog.open();
    }

    private void confirmDelete(PantryItemResponse item) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Remove Pantry Item");
        dialog.setText("Are you sure you want to remove \"" + item.ingredientName() + "\" from your pantry?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Remove");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            try {
                pantryService.removePantryItem(item.id());
                Notification.show("Item removed", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Failed to remove: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        dialog.open();
    }
}
