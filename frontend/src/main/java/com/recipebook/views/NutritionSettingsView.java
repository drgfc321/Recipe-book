package com.recipebook.views;

import com.recipebook.foodlog.UserNutritionTargetResponse;
import com.recipebook.service.NutritionTargetUIService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Route(value = "nutrition-settings", layout = MainLayout.class)
@PageTitle("Nutrition Settings | Recipe Book")
public class NutritionSettingsView extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(NutritionSettingsView.class);

    private final NutritionTargetUIService nutritionTargetService;

    // Fields per day type
    private final NumberField defaultCalories = new NumberField();
    private final NumberField defaultProtein = new NumberField();
    private final NumberField defaultCarbs = new NumberField();
    private final NumberField defaultFat = new NumberField();

    private final NumberField trainingCalories = new NumberField();
    private final NumberField trainingProtein = new NumberField();
    private final NumberField trainingCarbs = new NumberField();
    private final NumberField trainingFat = new NumberField();

    private final NumberField restCalories = new NumberField();
    private final NumberField restProtein = new NumberField();
    private final NumberField restCarbs = new NumberField();
    private final NumberField restFat = new NumberField();

    public NutritionSettingsView(NutritionTargetUIService nutritionTargetService) {
        this.nutritionTargetService = nutritionTargetService;

        setPadding(true);
        setSpacing(true);
        setMaxWidth("700px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2(getTranslation("nutritionSettings.title", "Nutrition Targets"));
        title.getStyle().set("margin-bottom", "0");
        add(title);

        Paragraph subtitle = new Paragraph(getTranslation("nutritionSettings.subtitle",
                "Configure calorie and macro targets per day type"));
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0");
        add(subtitle);

        // Build 3 sections
        add(buildSection("DEFAULT", "form-section--primary",
                getTranslation("nutritionSettings.default", "Default Targets"),
                VaadinIcon.BULLSEYE, defaultCalories, defaultProtein, defaultCarbs, defaultFat));

        add(buildSection("TRAINING", "form-section--success",
                getTranslation("nutritionSettings.training", "Training Day"),
                VaadinIcon.TROPHY, trainingCalories, trainingProtein, trainingCarbs, trainingFat));

        add(buildSection("REST", "form-section--warning",
                getTranslation("nutritionSettings.rest", "Rest Day"),
                VaadinIcon.MOON, restCalories, restProtein, restCarbs, restFat));

        // Save All button
        Button saveBtn = new Button(getTranslation("nutritionSettings.save", "Save Targets"),
                VaadinIcon.CHECK.create(), e -> saveAll());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.setWidthFull();
        add(saveBtn);

        loadTargets();
    }

    private Div buildSection(String dayType, String themeClass, String label,
                             VaadinIcon icon,
                             NumberField caloriesField, NumberField proteinField,
                             NumberField carbsField, NumberField fatField) {
        Div section = new Div();
        section.addClassName("form-section");
        section.addClassName(themeClass);

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.addClassName("section-header");
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        Div iconSquare = new Div();
        iconSquare.addClassName("section-icon-square");
        Icon ic = icon.create();
        ic.setSize("14px");
        ic.setColor("white");
        iconSquare.add(ic);

        Span labelSpan = new Span(label.toUpperCase());
        labelSpan.addClassName("section-label");

        header.add(iconSquare, labelSpan);
        section.add(header);

        // Body
        VerticalLayout body = new VerticalLayout();
        body.addClassName("section-body");
        body.setPadding(false);
        body.setSpacing(true);

        configureCaloriesField(caloriesField);
        configureGramsField(proteinField, getTranslation("nutritionSettings.protein", "Protein (g)"));
        configureGramsField(carbsField, getTranslation("nutritionSettings.carbs", "Carbs (g)"));
        configureGramsField(fatField, getTranslation("nutritionSettings.fat", "Fat (g)"));

        body.add(caloriesField, proteinField, carbsField, fatField);
        section.add(body);

        return section;
    }

    private void configureCaloriesField(NumberField field) {
        field.setLabel(getTranslation("nutritionSettings.calories", "Calories (kcal)"));
        field.setMin(800);
        field.setMax(6000);
        field.setStep(1);
        field.setWidthFull();
    }

    private void configureGramsField(NumberField field, String label) {
        field.setLabel(label);
        field.setMin(0);
        field.setMax(1000);
        field.setStep(0.1);
        field.setWidthFull();
    }

    private void loadTargets() {
        try {
            List<UserNutritionTargetResponse> targets = nutritionTargetService.getAllTargets();
            Map<String, UserNutritionTargetResponse> byType = targets.stream()
                    .collect(Collectors.toMap(UserNutritionTargetResponse::dayType, Function.identity()));

            UserNutritionTargetResponse def = byType.get("DEFAULT");
            if (def != null) {
                populateFields(def, defaultCalories, defaultProtein, defaultCarbs, defaultFat);
            }

            UserNutritionTargetResponse training = byType.getOrDefault("TRAINING", def);
            if (training != null) {
                populateFields(training, trainingCalories, trainingProtein, trainingCarbs, trainingFat);
            }

            UserNutritionTargetResponse rest = byType.getOrDefault("REST", def);
            if (rest != null) {
                populateFields(rest, restCalories, restProtein, restCarbs, restFat);
            }
        } catch (Exception e) {
            LOG.error("Failed to load nutrition targets: {}", e.getMessage());
            Notification.show(getTranslation("nutritionSettings.error", "Failed to save targets"),
                            3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void populateFields(UserNutritionTargetResponse target,
                                NumberField caloriesField, NumberField proteinField,
                                NumberField carbsField, NumberField fatField) {
        caloriesField.setValue(target.calories());
        proteinField.setValue(target.protein());
        carbsField.setValue(target.carbs());
        fatField.setValue(target.fat());
    }

    private void saveAll() {
        try {
            saveSection("DEFAULT", defaultCalories, defaultProtein, defaultCarbs, defaultFat);
            saveSection("TRAINING", trainingCalories, trainingProtein, trainingCarbs, trainingFat);
            saveSection("REST", restCalories, restProtein, restCarbs, restFat);

            Notification.show(getTranslation("nutritionSettings.saved", "Targets saved successfully!"),
                            3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            LOG.error("Failed to save nutrition targets: {}", e.getMessage());
            Notification.show(getTranslation("nutritionSettings.error", "Failed to save targets"),
                            3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void saveSection(String dayType, NumberField caloriesField,
                             NumberField proteinField, NumberField carbsField, NumberField fatField) {
        int calories = caloriesField.getValue() != null ? caloriesField.getValue().intValue() : 0;
        double protein = proteinField.getValue() != null ? proteinField.getValue() : 0;
        double carbs = carbsField.getValue() != null ? carbsField.getValue() : 0;
        double fat = fatField.getValue() != null ? fatField.getValue() : 0;

        nutritionTargetService.updateTarget(dayType, calories, protein, carbs, fat);
    }
}
