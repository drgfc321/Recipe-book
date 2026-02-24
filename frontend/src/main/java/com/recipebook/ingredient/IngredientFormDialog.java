package com.recipebook.ingredient;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
    private final NumberField caloriesField = new NumberField("Calories / 100g");
    private final NumberField proteinField = new NumberField("Protein / 100g");
    private final NumberField carbsField = new NumberField("Carbs / 100g");
    private final NumberField fatField = new NumberField("Fat / 100g");

    public IngredientFormDialog(IngredientService ingredientService, IngredientResponse existing, Runnable onSave) {
        this.ingredientService = ingredientService;
        this.existing = existing;
        this.onSave = onSave;

        setHeaderTitle(existing != null ? "Edit Ingredient" : "New Ingredient");
        setWidth("500px");
        setCloseOnOutsideClick(false);

        buildForm();
        buildFooter();

        if (existing != null) {
            populateForm();
        }
    }

    private void buildForm() {
        nameField.setRequired(true);
        nameField.setWidthFull();

        categoryField.setItems("DAIRY", "MEAT", "VEGETABLES", "FRUITS", "GRAINS", "SPICES", "OILS", "BEVERAGES", "OTHER");
        categoryField.setRequired(true);

        caloriesField.setMin(0);
        caloriesField.setValue(0.0);
        proteinField.setMin(0);
        proteinField.setValue(0.0);
        carbsField.setMin(0);
        carbsField.setValue(0.0);
        fatField.setMin(0);
        fatField.setValue(0.0);

        FormLayout form = new FormLayout();
        form.add(nameField, 2);
        form.add(categoryField, 2);
        form.add(caloriesField, proteinField);
        form.add(carbsField, fatField);

        add(form);
    }

    private void buildFooter() {
        Button saveButton = new Button("Save", e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> close());

        HorizontalLayout footer = new HorizontalLayout(saveButton, cancelButton);
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
