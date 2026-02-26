package com.recipebook.ingredient;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.vaadin.flow.component.textfield.TextField;


import java.util.HashMap;
import java.util.Map;

public class IngredientFormDialog extends Dialog {

    private final IngredientService ingredientService;
    private final IngredientResponse existing;
    private final Runnable onSave;

    private final TextField nameField = new TextField("Name");
    private final ComboBox<String> categoryField = new ComboBox<>("Category");
    private final NumberField caloriesField = new NumberField("Calories");
    private final NumberField proteinField = new NumberField("Protein");
    private final NumberField carbsField = new NumberField("Carbs");
    private final NumberField fatField = new NumberField("Fat");

    public IngredientFormDialog(IngredientService ingredientService, IngredientResponse existing, Runnable onSave) {
        this.ingredientService = ingredientService;
        this.existing = existing;
        this.onSave = onSave;

        setWidth("min(500px, 90vw)");
        setCloseOnOutsideClick(false);

        Span tag = new Span(existing != null ? "EDIT" : "NEW");
        tag.addClassName("dialog-header-tag");
        H2 title = new H2("Ingredient");
        title.addClassName("dialog-header-title");
        getHeader().add(tag, title);

        buildForm();
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

    private void buildForm() {
        nameField.setRequired(true);
        nameField.setWidthFull();
        nameField.setPrefixComponent(VaadinIcon.PENCIL.create());

        categoryField.setItems("DAIRY", "MEAT", "VEGETABLES", "FRUITS", "GRAINS", "SPICES", "OILS", "BEVERAGES", "OTHER");
        categoryField.setRequired(true);

        caloriesField.setMin(0);
        caloriesField.setValue(0.0);
        caloriesField.setSuffixComponent(new Div("kcal"));

        proteinField.setMin(0);
        proteinField.setValue(0.0);
        proteinField.setSuffixComponent(new Div("g"));

        carbsField.setMin(0);
        carbsField.setValue(0.0);
        carbsField.setSuffixComponent(new Div("g"));

        fatField.setMin(0);
        fatField.setValue(0.0);
        fatField.setSuffixComponent(new Div("g"));

        // Section 1: Basic Info
        FormLayout basicForm = new FormLayout();
        basicForm.setWidthFull();
        basicForm.add(nameField, 2);
        basicForm.add(categoryField, 2);

        Div basicSection = createFormSection("Basic Info", VaadinIcon.STOCK, "primary", basicForm);

        // Section 2: Nutrition per 100g
        FormLayout nutritionForm = new FormLayout();
        nutritionForm.setWidthFull();
        nutritionForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));
        nutritionForm.add(caloriesField, proteinField);
        nutritionForm.add(carbsField, fatField);

        Div nutritionSection = createFormSection("Nutrition per 100g", VaadinIcon.CHART, "error", nutritionForm);

        VerticalLayout content = new VerticalLayout(basicSection, nutritionSection);
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
        nameField.setValue(existing.name() != null ? existing.name() : "");
        categoryField.setValue(existing.category());
        caloriesField.setValue(existing.caloriesPer100g());
        proteinField.setValue(existing.proteinPer100g());
        carbsField.setValue(existing.carbsPer100g());
        fatField.setValue(existing.fatPer100g());
    }

    private void save() {
        if (nameField.getValue() == null || nameField.getValue().isBlank()) {
            Notification.show("Name is required", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (categoryField.getValue() == null) {
            Notification.show("Category is required", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Map<String, Object> input = new HashMap<>();
        input.put("name", nameField.getValue());
        input.put("category", categoryField.getValue());
        input.put("caloriesPer100g", caloriesField.getValue() != null ? caloriesField.getValue() : 0.0);
        input.put("proteinPer100g", proteinField.getValue() != null ? proteinField.getValue() : 0.0);
        input.put("carbsPer100g", carbsField.getValue() != null ? carbsField.getValue() : 0.0);
        input.put("fatPer100g", fatField.getValue() != null ? fatField.getValue() : 0.0);

        try {
            if (existing != null) {
                ingredientService.updateIngredient(existing.id(), input);
            } else {
                ingredientService.createIngredient(input);
            }
            Notification.show("Ingredient saved", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            onSave.run();
            close();
        } catch (Exception e) {
            Notification.show("Failed to save: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
