package com.dnd.views;

import com.dnd.dto.DiceRollRequest;
import com.dnd.dto.DiceRollResponse;
import com.dnd.service.ApiClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route(value = "dice", layout = MainLayout.class)
@PageTitle("Dice Roller | D&D Campaign Manager")
public class DiceRollerView extends VerticalLayout {

    private final ApiClient apiClient;
    private final VerticalLayout historyLayout;
    private final List<String> rollHistory = new ArrayList<>();

    public DiceRollerView(ApiClient apiClient) {
        this.apiClient = apiClient;

        setPadding(true);
        setSpacing(true);

        H2 title = new H2("Dice Roller");
        add(title);

        // Quick roll buttons
        add(createQuickRollSection());

        // Custom roll section
        add(createCustomRollSection());

        // Roll history
        H3 historyTitle = new H3("Roll History");
        historyLayout = new VerticalLayout();
        historyLayout.setPadding(false);
        historyLayout.setSpacing(true);
        historyLayout.getStyle()
                .set("max-height", "300px")
                .set("overflow-y", "auto");

        add(historyTitle, historyLayout);
    }

    private VerticalLayout createQuickRollSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);

        H3 title = new H3("Quick Roll");

        FlexLayout diceButtons = new FlexLayout();
        diceButtons.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        diceButtons.getStyle().set("gap", "var(--lumo-space-m)");

        String[] diceTypes = {"d4", "d6", "d8", "d10", "d12", "d20", "d100"};

        for (String die : diceTypes) {
            Button dieButton = createDiceButton(die);
            diceButtons.add(dieButton);
        }

        section.add(title, diceButtons);
        return section;
    }

    private Button createDiceButton(String die) {
        Button button = new Button(die.toUpperCase());
        button.getStyle()
                .set("min-width", "80px")
                .set("min-height", "80px")
                .set("font-size", "var(--lumo-font-size-l)")
                .set("font-weight", "bold");
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        button.addClickListener(e -> rollDice("1" + die, 0, false, false));

        return button;
    }

    private VerticalLayout createCustomRollSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);

        H3 title = new H3("Custom Roll");

        FlexLayout controls = new FlexLayout();
        controls.setAlignItems(FlexLayout.Alignment.END);
        controls.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        controls.getStyle().set("gap", "var(--lumo-space-m)");

        IntegerField countField = new IntegerField("Number of Dice");
        countField.setMin(1);
        countField.setMax(20);
        countField.setValue(1);
        countField.setStepButtonsVisible(true);

        Select<String> dieSelect = new Select<>();
        dieSelect.setLabel("Die Type");
        dieSelect.setItems("d4", "d6", "d8", "d10", "d12", "d20", "d100");
        dieSelect.setValue("d20");

        IntegerField modifierField = new IntegerField("Modifier");
        modifierField.setValue(0);
        modifierField.setStepButtonsVisible(true);

        Checkbox advantageCheck = new Checkbox("Advantage");
        Checkbox disadvantageCheck = new Checkbox("Disadvantage");

        // Mutual exclusion for advantage/disadvantage
        advantageCheck.addValueChangeListener(e -> {
            if (e.getValue()) {
                disadvantageCheck.setValue(false);
                countField.setValue(1);
                dieSelect.setValue("d20");
            }
        });
        disadvantageCheck.addValueChangeListener(e -> {
            if (e.getValue()) {
                advantageCheck.setValue(false);
                countField.setValue(1);
                dieSelect.setValue("d20");
            }
        });

        Button rollButton = new Button("Roll!", e -> {
            String dice = countField.getValue() + dieSelect.getValue();
            int modifier = modifierField.getValue() != null ? modifierField.getValue() : 0;
            rollDice(dice, modifier, advantageCheck.getValue(), disadvantageCheck.getValue());
        });
        rollButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        controls.add(countField, dieSelect, modifierField, advantageCheck, disadvantageCheck, rollButton);
        section.add(title, controls);

        return section;
    }

    private void rollDice(String dice, int modifier, boolean advantage, boolean disadvantage) {
        try {
            DiceRollRequest request = new DiceRollRequest();
            request.setDice(dice);
            if (modifier != 0) {
                request.setModifier(modifier);
            }
            if (advantage) {
                request.setAdvantage(true);
            }
            if (disadvantage) {
                request.setDisadvantage(true);
            }

            DiceRollResponse response = apiClient.post("/api/dice/roll", request, DiceRollResponse.class);

            // Display result
            addToHistory(response);

            // Show notification with result
            Notification notification = Notification.show(
                    response.getDescription() + " = " + response.getTotal(),
                    3000,
                    Notification.Position.MIDDLE
            );
            notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);

        } catch (Exception e) {
            Notification.show("Failed to roll: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void addToHistory(DiceRollResponse response) {
        Div entry = new Div();
        entry.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-m)")
                .set("margin-bottom", "var(--lumo-space-s)");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);

        Span diceSpan = new Span(response.getDice());
        diceSpan.getStyle().set("font-weight", "bold");

        Span totalSpan = new Span("Total: " + response.getTotal());
        totalSpan.getStyle()
                .set("font-size", "var(--lumo-font-size-xl)")
                .set("font-weight", "bold")
                .set("color", "var(--lumo-primary-color)");

        header.add(diceSpan, totalSpan);

        Paragraph details = new Paragraph(response.getDescription());
        details.getStyle()
                .set("margin", "var(--lumo-space-xs) 0 0 0")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        // Show individual rolls
        StringBuilder rollsStr = new StringBuilder("Rolls: ");
        for (int i = 0; i < response.getRolls().size(); i++) {
            if (i > 0) rollsStr.append(", ");
            rollsStr.append(response.getRolls().get(i));
        }
        if (response.getModifier() != null && response.getModifier() != 0) {
            rollsStr.append(response.getModifier() > 0 ? " + " : " - ");
            rollsStr.append(Math.abs(response.getModifier()));
        }

        Paragraph rollsSpan = new Paragraph(rollsStr.toString());
        rollsSpan.getStyle()
                .set("margin", "0")
                .set("font-size", "var(--lumo-font-size-s)");

        entry.add(header, details, rollsSpan);

        // Add to top of history
        historyLayout.addComponentAsFirst(entry);

        // Keep only last 10 rolls
        while (historyLayout.getComponentCount() > 10) {
            historyLayout.remove(historyLayout.getComponentAt(historyLayout.getComponentCount() - 1));
        }
    }
}
