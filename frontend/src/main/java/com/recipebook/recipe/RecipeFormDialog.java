package com.recipebook.recipe;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeFormDialog extends Dialog {

    private final RecipeService recipeService;
    private final RecipeResponse existing;
    private final Runnable onSave;

    private final TextField nameField = new TextField("Name");
    private final TextArea descriptionField = new TextArea("Description");
    private final ComboBox<String> categoryField = new ComboBox<>("Category");
    private final ComboBox<String> difficultyField = new ComboBox<>("Difficulty");
    private final IntegerField prepTimeField = new IntegerField("Prep Time (min)");
    private final IntegerField cookTimeField = new IntegerField("Cook Time (min)");
    private final IntegerField servingsField = new IntegerField("Servings");
    private final TextArea instructionsField = new TextArea("Instructions");
    private final TextField imageUrlField = new TextField("Image URL");

    private final VerticalLayout ingredientRows = new VerticalLayout();
    private final List<IngredientRow> ingredients = new ArrayList<>();
    private List<IngredientOption> ingredientOptions = List.of();

    public RecipeFormDialog(RecipeService recipeService, RecipeResponse existing, Runnable onSave) {
        this.recipeService = recipeService;
        this.existing = existing;
        this.onSave = onSave;

        setHeaderTitle(existing != null ? "Edit Recipe" : "New Recipe");
        setWidth("700px");
        setCloseOnOutsideClick(false);

        try {
            ingredientOptions = recipeService.getIngredients();
        } catch (Exception e) {
            Notification.show("Failed to load ingredients", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        buildForm();
        buildFooter();

        if (existing != null) {
            populateForm();
        }
    }

    private void buildForm() {
        categoryField.setItems("BREAKFAST", "LUNCH", "DINNER", "DESSERT", "SNACK", "OTHER");
        categoryField.setRequired(true);

        difficultyField.setItems("EASY", "MEDIUM", "HARD");
        difficultyField.setRequired(true);

        nameField.setRequired(true);
        nameField.setWidthFull();
        descriptionField.setWidthFull();
        descriptionField.setMaxLength(500);
        instructionsField.setWidthFull();
        instructionsField.setHeight("150px");
        imageUrlField.setWidthFull();

        prepTimeField.setMin(0);
        prepTimeField.setValue(0);
        cookTimeField.setMin(0);
        cookTimeField.setValue(0);
        servingsField.setMin(1);
        servingsField.setValue(1);

        FormLayout form = new FormLayout();
        form.add(nameField, 2);
        form.add(descriptionField, 2);
        form.add(categoryField, difficultyField);
        form.add(prepTimeField, cookTimeField);
        form.add(servingsField, imageUrlField);
        form.add(instructionsField, 2);

        // Ingredients section
        ingredientRows.setPadding(false);
        ingredientRows.setSpacing(false);

        Button addIngredientButton = new Button("Add Ingredient", VaadinIcon.PLUS.create(), e -> addIngredientRow(null));
        addIngredientButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        VerticalLayout content = new VerticalLayout(form, new H3("Ingredients"), ingredientRows, addIngredientButton);
        content.setPadding(false);
        content.setSpacing(true);
        add(content);
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
        descriptionField.setValue(existing.description() != null ? existing.description() : "");
        categoryField.setValue(existing.category());
        difficultyField.setValue(existing.difficulty());
        prepTimeField.setValue(existing.prepTime());
        cookTimeField.setValue(existing.cookTime());
        servingsField.setValue(existing.servings());
        instructionsField.setValue(existing.instructions() != null ? existing.instructions() : "");
        imageUrlField.setValue(existing.imageUrl() != null ? existing.imageUrl() : "");

        if (existing.ingredients() != null) {
            for (RecipeIngredientResponse ri : existing.ingredients()) {
                addIngredientRow(ri);
            }
        }
    }

    private void addIngredientRow(RecipeIngredientResponse prefill) {
        IngredientRow row = new IngredientRow(ingredientOptions, () -> {
            // Remove callback
        });

        if (prefill != null) {
            row.setValues(prefill);
        }

        ingredients.add(row);
        HorizontalLayout rowLayout = row.getLayout();

        Button removeButton = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> {
            ingredients.remove(row);
            ingredientRows.remove(rowLayout);
        });
        removeButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        rowLayout.add(removeButton);

        ingredientRows.add(rowLayout);
    }

    private void save() {
        if (nameField.getValue().isBlank()) {
            Notification.show("Name is required", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (categoryField.getValue() == null) {
            Notification.show("Category is required", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (difficultyField.getValue() == null) {
            Notification.show("Difficulty is required", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Map<String, Object> input = new HashMap<>();
        input.put("name", nameField.getValue());
        input.put("description", descriptionField.getValue());
        input.put("category", categoryField.getValue());
        input.put("difficulty", difficultyField.getValue());
        input.put("prepTime", prepTimeField.getValue());
        input.put("cookTime", cookTimeField.getValue());
        input.put("servings", servingsField.getValue());
        input.put("instructions", instructionsField.getValue());
        input.put("imageUrl", imageUrlField.getValue());

        List<Map<String, Object>> ingredientInputs = new ArrayList<>();
        for (IngredientRow row : ingredients) {
            Map<String, Object> ri = row.toMap();
            if (ri != null) {
                ingredientInputs.add(ri);
            }
        }
        input.put("ingredients", ingredientInputs);

        try {
            if (existing != null) {
                recipeService.updateRecipe(existing.id(), input);
            } else {
                recipeService.createRecipe(input);
            }
            Notification.show("Recipe saved", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            onSave.run();
            close();
        } catch (Exception e) {
            Notification.show("Failed to save: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private static class IngredientRow {
        private final ComboBox<IngredientOption> ingredientCombo = new ComboBox<>("Ingredient");
        private final NumberField quantityField = new NumberField("Quantity");
        private final ComboBox<String> unitCombo = new ComboBox<>("Unit");
        private final HorizontalLayout layout;

        IngredientRow(List<IngredientOption> options, Runnable onRemove) {
            ingredientCombo.setItems(options);
            ingredientCombo.setItemLabelGenerator(o -> o.name() + " (" + o.category() + ")");
            ingredientCombo.setWidthFull();

            quantityField.setMin(0);
            quantityField.setValue(100.0);
            quantityField.setWidth("120px");

            unitCombo.setItems("GRAMS", "KILOGRAMS", "MILLILITERS", "LITERS", "PIECES", "TABLESPOONS", "TEASPOONS", "CUPS");
            unitCombo.setValue("GRAMS");
            unitCombo.setWidth("150px");

            layout = new HorizontalLayout(ingredientCombo, quantityField, unitCombo);
            layout.setAlignItems(FlexComponent.Alignment.BASELINE);
            layout.setWidthFull();
            layout.expand(ingredientCombo);
        }

        void setValues(RecipeIngredientResponse ri) {
            ingredientCombo.getListDataView().getItems()
                    .filter(o -> o.id().equals(ri.ingredientId()))
                    .findFirst()
                    .ifPresent(ingredientCombo::setValue);
            quantityField.setValue(ri.quantity());
            unitCombo.setValue(ri.unit());
        }

        HorizontalLayout getLayout() {
            return layout;
        }

        Map<String, Object> toMap() {
            IngredientOption selected = ingredientCombo.getValue();
            if (selected == null || quantityField.getValue() == null || unitCombo.getValue() == null) {
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("ingredientId", selected.id());
            map.put("quantity", quantityField.getValue());
            map.put("unit", unitCombo.getValue());
            return map;
        }
    }
}
