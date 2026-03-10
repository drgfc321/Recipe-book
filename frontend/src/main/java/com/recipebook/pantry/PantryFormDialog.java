package com.recipebook.pantry;

import com.recipebook.ingredient.IngredientResponse;
import com.recipebook.ingredient.IngredientService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
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
import com.vaadin.flow.component.textfield.NumberField;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PantryFormDialog extends Dialog {

    private final PantryService pantryService;
    private final PantryItemResponse existing;
    private final Runnable onSave;

    private final ComboBox<IngredientResponse> ingredientField = new ComboBox<>("Ingredient");
    private final NumberField quantityField = new NumberField("Quantity");
    private final ComboBox<String> unitField = new ComboBox<>("Unit");
    private final DatePicker expirationField = new DatePicker("Expiration Date");

    public PantryFormDialog(PantryService pantryService, IngredientService ingredientService,
                            PantryItemResponse existing, Runnable onSave) {
        this.pantryService = pantryService;
        this.existing = existing;
        this.onSave = onSave;

        setWidth("min(500px, 90vw)");
        setCloseOnOutsideClick(false);

        Span tag = new Span(existing != null ? "EDIT" : "NEW");
        tag.addClassName("dialog-header-tag");
        H2 title = new H2("Pantry Item");
        title.addClassName("dialog-header-title");
        getHeader().add(tag, title);

        buildForm(ingredientService);
        buildFooter();

        if (existing != null) {
            populateForm();
        }
    }

    private Div createFormSection(String title, VaadinIcon vaadinIcon, String variant, Component... content) {
        Icon icon = vaadinIcon.create();
        icon.setSize("14px");

        Div iconSquare = new Div(icon);
        iconSquare.addClassName("section-icon-square");

        Span label = new Span(title);
        label.addClassName("section-label");

        HorizontalLayout header = new HorizontalLayout(iconSquare, label);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(false);
        header.addClassName("section-header");

        Div body = new Div();
        body.addClassName("section-body");
        for (Component c : content) {
            body.add(c);
        }

        Div section = new Div();
        section.addClassNames("form-section", "form-section--" + variant);
        section.add(header, body);
        return section;
    }

    private void buildForm(IngredientService ingredientService) {
        // Load ingredients for ComboBox
        try {
            List<IngredientResponse> ingredients = ingredientService.getIngredients(null, null);
            ingredientField.setItems(ingredients);
            ingredientField.setItemLabelGenerator(IngredientResponse::name);
        } catch (Exception e) {
            // If loading fails, the combo will be empty
        }
        ingredientField.setRequired(true);
        ingredientField.setWidthFull();
        ingredientField.setEnabled(existing == null); // disable on edit

        quantityField.setMin(0.01);
        quantityField.setValue(1.0);
        quantityField.setWidthFull();

        unitField.setItems("GRAMS", "KILOGRAMS", "MILLILITERS", "LITERS", "PIECES", "TABLESPOONS", "TEASPOONS", "CUPS");
        unitField.setRequired(true);
        unitField.setWidthFull();

        expirationField.setWidthFull();

        // Section 1: Item Info
        FormLayout itemForm = new FormLayout();
        itemForm.setWidthFull();
        itemForm.add(ingredientField, 2);
        FormLayout quantityRow = new FormLayout();
        quantityRow.setWidthFull();
        quantityRow.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        quantityRow.add(quantityField, unitField);
        itemForm.add(quantityRow, 2);

        Div itemSection = createFormSection("Item", VaadinIcon.STORAGE, "primary", itemForm);

        // Section 2: Expiration
        FormLayout expirationForm = new FormLayout();
        expirationForm.setWidthFull();
        expirationForm.add(expirationField, 2);

        Div expirationSection = createFormSection("Expiration", VaadinIcon.CALENDAR, "warning", expirationForm);

        VerticalLayout content = new VerticalLayout(itemSection, expirationSection);
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidthFull();
        add(content);
    }

    private void buildFooter() {
        Button saveButton = new Button("Save", e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> close());
        cancelButton.addClassName("dialog-cancel-btn");

        HorizontalLayout footer = new HorizontalLayout(cancelButton, saveButton);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);
    }

    private void populateForm() {
        quantityField.setValue(existing.quantity());
        unitField.setValue(existing.unit());
        if (existing.expirationDate() != null && !existing.expirationDate().isBlank()) {
            expirationField.setValue(LocalDate.parse(existing.expirationDate()));
        }
    }

    private void save() {
        if (existing == null && ingredientField.getValue() == null) {
            Notification.show("Ingredient is required", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (unitField.getValue() == null) {
            Notification.show("Unit is required", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (quantityField.getValue() == null || quantityField.getValue() <= 0) {
            Notification.show("Quantity must be greater than 0", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            if (existing != null) {
                Map<String, Object> input = new HashMap<>();
                input.put("quantity", quantityField.getValue());
                input.put("unit", unitField.getValue());
                if (expirationField.getValue() != null) {
                    input.put("expirationDate", expirationField.getValue().toString());
                }
                pantryService.updatePantryItem(existing.id(), input);
            } else {
                Map<String, Object> input = new HashMap<>();
                input.put("ingredientId", ingredientField.getValue().id());
                input.put("quantity", quantityField.getValue());
                input.put("unit", unitField.getValue());
                if (expirationField.getValue() != null) {
                    input.put("expirationDate", expirationField.getValue().toString());
                }
                pantryService.addPantryItem(input);
            }
            Notification.show("Pantry item saved", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            onSave.run();
            close();
        } catch (Exception e) {
            Notification.show("Failed to save: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
