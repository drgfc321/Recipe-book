package com.recipebook.views;

import com.recipebook.dto.BMRResultResponse;
import com.recipebook.dto.UserPhysicalDataResponse;
import com.recipebook.service.BMRService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Route(value = "bmr-wizard", layout = MainLayout.class)
@PageTitle("BMR Calculator | Recipe Book")
public class BMRWizardView extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(BMRWizardView.class);

    private final BMRService bmrService;

    private int currentStep = 0;
    private final VerticalLayout stepContent = new VerticalLayout();
    private final Div stepIndicator = new Div();
    private final Button prevBtn = new Button(getTranslation("bmr.prev", "Previous"));
    private final Button nextBtn = new Button(getTranslation("bmr.next", "Next"));

    // Step 1 fields
    private final NumberField weightField = new NumberField(getTranslation("bmr.weight", "Weight (kg)"));
    private final NumberField heightField = new NumberField(getTranslation("bmr.height", "Height (cm)"));
    private final DatePicker birthDatePicker = new DatePicker(getTranslation("bmr.birthDate", "Birth Date"));
    private final RadioButtonGroup<String> genderGroup = new RadioButtonGroup<>();

    // Step 2 fields
    private final RadioButtonGroup<String> activityGroup = new RadioButtonGroup<>();

    // Step 3 fields
    private final RadioButtonGroup<String> goalGroup = new RadioButtonGroup<>();
    private final RadioButtonGroup<String> presetGroup = new RadioButtonGroup<>();
    private final NumberField proteinPctField = new NumberField(getTranslation("bmr.proteinPct", "Protein %"));
    private final NumberField carbsPctField = new NumberField(getTranslation("bmr.carbsPct", "Carbs %"));
    private final NumberField fatPctField = new NumberField(getTranslation("bmr.fatPct", "Fat %"));
    private final Span macroSumLabel = new Span();

    // Step 4 - review
    private final VerticalLayout reviewLayout = new VerticalLayout();

    // Activity level keys
    private static final Map<String, String> ACTIVITY_LEVELS = new LinkedHashMap<>();
    static {
        ACTIVITY_LEVELS.put("SEDENTARY", "bmr.activity.sedentary");
        ACTIVITY_LEVELS.put("LIGHTLY_ACTIVE", "bmr.activity.lightlyActive");
        ACTIVITY_LEVELS.put("MODERATE", "bmr.activity.moderate");
        ACTIVITY_LEVELS.put("ACTIVE", "bmr.activity.active");
        ACTIVITY_LEVELS.put("VERY_ACTIVE", "bmr.activity.veryActive");
    }

    // Goal keys
    private static final Map<String, String> GOALS = new LinkedHashMap<>();
    static {
        GOALS.put("LOSE", "bmr.goal.lose");
        GOALS.put("MAINTAIN", "bmr.goal.maintain");
        GOALS.put("GAIN", "bmr.goal.gain");
    }

    public BMRWizardView(BMRService bmrService) {
        this.bmrService = bmrService;

        setPadding(true);
        setSpacing(true);
        setMaxWidth("700px");
        getStyle().set("margin", "0 auto");

        H2 title = new H2(getTranslation("bmr.title", "BMR / TDEE Calculator"));
        title.getStyle().set("margin-bottom", "0");
        add(title);

        Paragraph subtitle = new Paragraph(getTranslation("bmr.subtitle",
                "Calculate your personalized nutrition targets"));
        subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0");
        add(subtitle);

        // Step indicator
        stepIndicator.addClassName("wizard-step-indicator");
        add(stepIndicator);

        // Step content container wrapped in card
        stepContent.setPadding(false);
        stepContent.setSpacing(true);
        stepContent.setWidthFull();
        Div wizardCard = new Div(stepContent);
        wizardCard.addClassName("wizard-card");
        add(wizardCard);

        // Navigation buttons
        prevBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        prevBtn.setIcon(VaadinIcon.ARROW_LEFT.create());
        prevBtn.addClickListener(e -> navigateStep(-1));

        nextBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nextBtn.setIconAfterText(true);
        nextBtn.setIcon(VaadinIcon.ARROW_RIGHT.create());
        nextBtn.addClickListener(e -> navigateStep(1));

        HorizontalLayout navButtons = new HorizontalLayout(prevBtn, nextBtn);
        navButtons.setWidthFull();
        navButtons.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        add(navButtons);

        initFields();
        prefillData();
        showStep(0);
    }

    private void initFields() {
        // Step 1 - Personal data
        weightField.setMin(20);
        weightField.setMax(300);
        weightField.setStep(0.1);
        weightField.setWidthFull();

        heightField.setMin(100);
        heightField.setMax(250);
        heightField.setStep(0.1);
        heightField.setWidthFull();

        birthDatePicker.setMax(LocalDate.now().minusYears(10));
        birthDatePicker.setMin(LocalDate.now().minusYears(120));
        birthDatePicker.setWidthFull();

        genderGroup.setLabel(getTranslation("bmr.gender", "Gender"));
        genderGroup.setItems("MALE", "FEMALE");
        genderGroup.setItemLabelGenerator(g -> g.equals("MALE")
                ? getTranslation("bmr.gender.male", "Male")
                : getTranslation("bmr.gender.female", "Female"));
        genderGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        // Step 2 - Activity level
        activityGroup.setLabel(getTranslation("bmr.activityLevel", "Activity Level"));
        activityGroup.setItems(ACTIVITY_LEVELS.keySet());
        activityGroup.setItemLabelGenerator(key ->
                getTranslation(ACTIVITY_LEVELS.get(key), key));
        activityGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        // Step 3 - Goal & Macros
        goalGroup.setLabel(getTranslation("bmr.fitnessGoal", "Fitness Goal"));
        goalGroup.setItems(GOALS.keySet());
        goalGroup.setItemLabelGenerator(key ->
                getTranslation(GOALS.get(key), key));
        goalGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        presetGroup.setLabel(getTranslation("bmr.macroPreset", "Macro Preset"));
        presetGroup.setItems("BALANCED", "KETO", "HIGH_CARB", "CUSTOM");
        presetGroup.setItemLabelGenerator(p -> switch (p) {
            case "BALANCED" -> getTranslation("bmr.preset.balanced", "Balanced (30/40/30)");
            case "KETO" -> getTranslation("bmr.preset.keto", "Keto (30/5/65)");
            case "HIGH_CARB" -> getTranslation("bmr.preset.highCarb", "High-Carb (25/55/20)");
            case "CUSTOM" -> getTranslation("bmr.preset.custom", "Custom");
            default -> p;
        });
        presetGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        presetGroup.setValue("BALANCED");
        presetGroup.addValueChangeListener(e -> applyPreset(e.getValue()));

        proteinPctField.setMin(5);
        proteinPctField.setMax(80);
        proteinPctField.setStep(1);
        proteinPctField.setValue(30.0);

        carbsPctField.setMin(5);
        carbsPctField.setMax(80);
        carbsPctField.setStep(1);
        carbsPctField.setValue(40.0);

        fatPctField.setMin(5);
        fatPctField.setMax(80);
        fatPctField.setStep(1);
        fatPctField.setValue(30.0);

        proteinPctField.addValueChangeListener(e -> updateMacroSum());
        carbsPctField.addValueChangeListener(e -> updateMacroSum());
        fatPctField.addValueChangeListener(e -> updateMacroSum());

        // Initially disable pct fields for non-custom preset
        proteinPctField.setReadOnly(true);
        carbsPctField.setReadOnly(true);
        fatPctField.setReadOnly(true);

        updateMacroSum();
    }

    private void prefillData() {
        try {
            UserPhysicalDataResponse data = bmrService.getUserPhysicalData();
            if (data != null) {
                if (data.weightKg() != null) weightField.setValue(data.weightKg());
                if (data.heightCm() != null) heightField.setValue(data.heightCm());
                if (data.birthDate() != null) birthDatePicker.setValue(LocalDate.parse(data.birthDate()));
                if (data.gender() != null) genderGroup.setValue(data.gender());
                if (data.activityLevel() != null) activityGroup.setValue(data.activityLevel());
                if (data.fitnessGoal() != null) goalGroup.setValue(data.fitnessGoal());
            }
        } catch (Exception e) {
            LOG.debug("No previous physical data to prefill: {}", e.getMessage());
        }
    }

    private void applyPreset(String preset) {
        boolean custom = "CUSTOM".equals(preset);
        proteinPctField.setReadOnly(!custom);
        carbsPctField.setReadOnly(!custom);
        fatPctField.setReadOnly(!custom);

        switch (preset) {
            case "BALANCED" -> { proteinPctField.setValue(30.0); carbsPctField.setValue(40.0); fatPctField.setValue(30.0); }
            case "KETO" -> { proteinPctField.setValue(30.0); carbsPctField.setValue(5.0); fatPctField.setValue(65.0); }
            case "HIGH_CARB" -> { proteinPctField.setValue(25.0); carbsPctField.setValue(55.0); fatPctField.setValue(20.0); }
            // CUSTOM: keep current values
        }
        updateMacroSum();
    }

    private void updateMacroSum() {
        int sum = safeInt(proteinPctField) + safeInt(carbsPctField) + safeInt(fatPctField);
        macroSumLabel.setText(getTranslation("bmr.macroSum", "Total: {0}%", String.valueOf(sum)));
        if (Math.abs(sum - 100) > 1) {
            macroSumLabel.getStyle().set("color", "var(--lumo-error-color)");
        } else {
            macroSumLabel.getStyle().set("color", "var(--lumo-success-color)");
        }
    }

    private void showStep(int step) {
        currentStep = step;
        stepContent.removeAll();
        updateStepIndicator();

        switch (step) {
            case 0 -> buildStep1();
            case 1 -> buildStep2();
            case 2 -> buildStep3();
            case 3 -> buildStep4();
        }

        prevBtn.setVisible(step > 0);
        if (step < 3) {
            nextBtn.setText(getTranslation("bmr.next", "Next"));
            nextBtn.setIcon(VaadinIcon.ARROW_RIGHT.create());
            nextBtn.setIconAfterText(true);
            nextBtn.removeThemeVariants(ButtonVariant.LUMO_SUCCESS);
            nextBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        } else {
            nextBtn.setText(getTranslation("bmr.save", "Save Targets"));
            nextBtn.setIcon(VaadinIcon.CHECK.create());
            nextBtn.setIconAfterText(false);
            nextBtn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            nextBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        }
    }

    private void updateStepIndicator() {
        stepIndicator.removeAll();
        String[] stepLabels = {
                getTranslation("bmr.step1", "Personal Data"),
                getTranslation("bmr.step2", "Activity"),
                getTranslation("bmr.step3", "Goal & Macros"),
                getTranslation("bmr.step4", "Review")
        };
        for (int i = 0; i < 4; i++) {
            Div stepItem = new Div();
            stepItem.addClassName("wizard-step-item");

            Div dot = new Div();
            dot.addClassName("wizard-step-dot");
            dot.setText(String.valueOf(i + 1));
            if (i == currentStep) {
                dot.addClassName("active");
            } else if (i < currentStep) {
                dot.addClassName("completed");
            }

            Span label = new Span(stepLabels[i]);
            label.addClassName("wizard-step-label");
            label.getStyle().set("color", i == currentStep ? "var(--lumo-primary-color)" : "var(--lumo-secondary-text-color)");

            // Connecting line between dots (not on the last step)
            if (i < 3) {
                Div line = new Div();
                line.addClassName("wizard-step-line");
                if (i < currentStep) {
                    line.addClassName("completed");
                }
                stepItem.add(line);
            }

            stepItem.add(dot, label);
            stepIndicator.add(stepItem);
        }
    }

    private void buildStep1() {
        H3 stepTitle = new H3(getTranslation("bmr.step1.title", "Personal Data"));
        stepTitle.getStyle().set("margin", "0");
        stepContent.add(stepTitle, weightField, heightField, birthDatePicker, genderGroup);
    }

    private void buildStep2() {
        H3 stepTitle = new H3(getTranslation("bmr.step2.title", "Activity Level"));
        stepTitle.getStyle().set("margin", "0");
        stepContent.add(stepTitle, activityGroup);
    }

    private void buildStep3() {
        H3 stepTitle = new H3(getTranslation("bmr.step3.title", "Goal & Macro Split"));
        stepTitle.getStyle().set("margin", "0");
        stepContent.add(stepTitle, goalGroup, presetGroup);

        HorizontalLayout pctRow = new HorizontalLayout(proteinPctField, carbsPctField, fatPctField);
        pctRow.setWidthFull();
        pctRow.setSpacing(true);
        proteinPctField.setWidthFull();
        carbsPctField.setWidthFull();
        fatPctField.setWidthFull();

        stepContent.add(pctRow, macroSumLabel);
    }

    private void buildStep4() {
        H3 stepTitle = new H3(getTranslation("bmr.step4.title", "Review Your Results"));
        stepTitle.getStyle().set("margin", "0");
        stepContent.add(stepTitle);

        reviewLayout.removeAll();
        reviewLayout.setPadding(false);
        reviewLayout.setSpacing(false);

        try {
            BMRResultResponse result = bmrService.previewBMR(
                    weightField.getValue(),
                    heightField.getValue(),
                    birthDatePicker.getValue().toString(),
                    genderGroup.getValue(),
                    activityGroup.getValue(),
                    goalGroup.getValue(),
                    safeInt(proteinPctField),
                    safeInt(carbsPctField),
                    safeInt(fatPctField)
            );

            // Input summary section
            Div inputSection = createReviewSection(getTranslation("bmr.review.inputTitle", "Your Data"),
                    createReviewRow(getTranslation("bmr.weight", "Weight (kg)"),
                            String.format("%.1f kg", weightField.getValue())),
                    createReviewRow(getTranslation("bmr.height", "Height (cm)"),
                            String.format("%.1f cm", heightField.getValue())),
                    createReviewRow(getTranslation("bmr.birthDate", "Birth Date"),
                            birthDatePicker.getValue().toString()),
                    createReviewRow(getTranslation("bmr.gender", "Gender"),
                            genderGroup.getValue()),
                    createReviewRow(getTranslation("bmr.activityLevel", "Activity Level"),
                            getTranslation(ACTIVITY_LEVELS.get(activityGroup.getValue()), activityGroup.getValue())),
                    createReviewRow(getTranslation("bmr.fitnessGoal", "Fitness Goal"),
                            getTranslation(GOALS.get(goalGroup.getValue()), goalGroup.getValue()))
            );
            reviewLayout.add(inputSection);

            // Results section
            Div resultsSection = createReviewSection(getTranslation("bmr.review.resultsTitle", "Calculated Targets"),
                    createReviewRow(getTranslation("bmr.review.bmr", "BMR"),
                            String.format("%.0f kcal", result.bmr())),
                    createReviewRow(getTranslation("bmr.review.tdee", "TDEE"),
                            String.format("%.0f kcal", result.tdee())),
                    createReviewRow(getTranslation("bmr.review.adjustedCalories", "Daily Calories"),
                            String.format("%.0f kcal", result.adjustedCalories())),
                    createReviewRow(getTranslation("bmr.review.protein", "Protein"),
                            String.format("%.0fg (%d%%)", result.proteinGrams(), safeInt(proteinPctField))),
                    createReviewRow(getTranslation("bmr.review.carbs", "Carbs"),
                            String.format("%.0fg (%d%%)", result.carbsGrams(), safeInt(carbsPctField))),
                    createReviewRow(getTranslation("bmr.review.fat", "Fat"),
                            String.format("%.0fg (%d%%)", result.fatGrams(), safeInt(fatPctField)))
            );
            reviewLayout.add(resultsSection);

        } catch (Exception e) {
            LOG.error("Failed to preview BMR: {}", e.getMessage());
            Span error = new Span(getTranslation("bmr.review.error",
                    "Failed to calculate. Please check your inputs."));
            error.getStyle().set("color", "var(--lumo-error-color)");
            reviewLayout.add(error);
        }

        stepContent.add(reviewLayout);
    }

    private Div createReviewSection(String title, Div... rows) {
        Div section = new Div();
        section.addClassName("wizard-review-section");

        Div header = new Div();
        header.addClassName("wizard-review-section-header");
        header.setText(title);
        section.add(header);

        for (Div row : rows) {
            section.add(row);
        }
        return section;
    }

    private Div createReviewRow(String label, String value) {
        Div row = new Div();
        row.addClassName("wizard-review-row");
        row.getStyle().set("width", "100%");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("wizard-review-label");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("wizard-review-value");

        row.add(labelSpan, valueSpan);
        return row;
    }

    private void navigateStep(int direction) {
        // On step 4 (review), clicking "Save" triggers save
        if (currentStep == 3 && direction > 0) {
            saveWizard();
            return;
        }

        int targetStep = currentStep + direction;
        if (targetStep < 0 || targetStep > 3) return;

        // Validate current step before advancing
        if (direction > 0 && !validateCurrentStep()) return;

        showStep(targetStep);
    }

    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 0 -> {
                if (weightField.getValue() == null || weightField.getValue() <= 0) {
                    showError(getTranslation("bmr.validation.weight", "Please enter your weight"));
                    return false;
                }
                if (heightField.getValue() == null || heightField.getValue() <= 0) {
                    showError(getTranslation("bmr.validation.height", "Please enter your height"));
                    return false;
                }
                if (birthDatePicker.getValue() == null) {
                    showError(getTranslation("bmr.validation.birthDate", "Please select your birth date"));
                    return false;
                }
                if (genderGroup.getValue() == null) {
                    showError(getTranslation("bmr.validation.gender", "Please select your gender"));
                    return false;
                }
            }
            case 1 -> {
                if (activityGroup.getValue() == null) {
                    showError(getTranslation("bmr.validation.activity", "Please select your activity level"));
                    return false;
                }
            }
            case 2 -> {
                if (goalGroup.getValue() == null) {
                    showError(getTranslation("bmr.validation.goal", "Please select your fitness goal"));
                    return false;
                }
                int sum = safeInt(proteinPctField) + safeInt(carbsPctField) + safeInt(fatPctField);
                if (Math.abs(sum - 100) > 1) {
                    showError(getTranslation("bmr.validation.macroSum",
                            "Macro percentages must sum to 100% (currently {0}%)", String.valueOf(sum)));
                    return false;
                }
            }
            case 3 -> {
                // Save step — handled separately
            }
        }
        return true;
    }

    private void saveWizard() {
        try {
            bmrService.saveBMRWizard(
                    weightField.getValue(),
                    heightField.getValue(),
                    birthDatePicker.getValue().toString(),
                    genderGroup.getValue(),
                    activityGroup.getValue(),
                    goalGroup.getValue(),
                    safeInt(proteinPctField),
                    safeInt(carbsPctField),
                    safeInt(fatPctField)
            );

            Notification.show(
                    getTranslation("bmr.save.success", "Nutrition targets updated successfully!"),
                    3000, Notification.Position.TOP_CENTER
            ).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            UI.getCurrent().navigate("");
        } catch (Exception e) {
            LOG.error("Failed to save BMR wizard: {}", e.getMessage());
            showError(getTranslation("bmr.save.error", "Failed to save. Please try again."));
        }
    }

    private void showError(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private int safeInt(NumberField field) {
        return field.getValue() != null ? field.getValue().intValue() : 0;
    }
}
