package com.dnd.views;

import com.dnd.dto.Campaign;
import com.dnd.dto.Character;
import com.dnd.service.ApiClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "characters", layout = MainLayout.class)
@PageTitle("Characters | D&D Campaign Manager")
public class CharactersView extends VerticalLayout {

    private final ApiClient apiClient;
    private final Grid<Character> grid;

    private static final String[] RACES = {"Human", "Elf", "Dwarf", "Halfling", "Dragonborn", "Gnome", "Half-Elf", "Half-Orc", "Tiefling"};
    private static final String[] CLASSES = {"Barbarian", "Bard", "Cleric", "Druid", "Fighter", "Monk", "Paladin", "Ranger", "Rogue", "Sorcerer", "Warlock", "Wizard"};

    public CharactersView(ApiClient apiClient) {
        this.apiClient = apiClient;

        setPadding(true);
        setSpacing(true);

        // Header
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Characters");

        Button addButton = new Button("New Character", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openCharacterDialog(null));

        header.add(title, addButton);
        add(header);

        // Grid
        grid = new Grid<>(Character.class, false);
        grid.addColumn(Character::getName).setHeader("Name").setSortable(true);
        grid.addColumn(Character::getRace).setHeader("Race").setSortable(true);
        grid.addColumn(Character::getCharacterClass).setHeader("Class").setSortable(true);
        grid.addColumn(Character::getLevel).setHeader("Level").setAutoWidth(true);
        grid.addColumn(character -> character.getHitPoints() + " HP").setHeader("HP").setAutoWidth(true);
        grid.addColumn(character -> "AC " + character.getArmorClass()).setHeader("AC").setAutoWidth(true);

        grid.addComponentColumn(character -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button viewButton = new Button(new Icon(VaadinIcon.EYE));
            viewButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            viewButton.addClickListener(e -> openCharacterDetailsDialog(character));

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openCharacterDialog(character));

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDelete(character));

            actions.add(viewButton, editButton, deleteButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeight("500px");

        add(grid);
        refreshGrid();
    }

    private void refreshGrid() {
        try {
            List<Character> characters = apiClient.getList("/api/characters", Character.class);
            grid.setItems(characters);
        } catch (Exception e) {
            Notification.show("Failed to load characters: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void openCharacterDialog(Character character) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(character == null ? "New Character" : "Edit Character");
        dialog.setWidth("600px");

        // Basic info
        TextField nameField = new TextField("Name");
        nameField.setWidthFull();
        nameField.setRequired(true);

        Select<String> raceSelect = new Select<>();
        raceSelect.setLabel("Race");
        raceSelect.setItems(RACES);
        raceSelect.setWidthFull();

        Select<String> classSelect = new Select<>();
        classSelect.setLabel("Class");
        classSelect.setItems(CLASSES);
        classSelect.setWidthFull();

        IntegerField levelField = new IntegerField("Level");
        levelField.setMin(1);
        levelField.setMax(20);
        levelField.setValue(1);
        levelField.setWidthFull();

        // Stats
        IntegerField strField = createStatField("STR");
        IntegerField dexField = createStatField("DEX");
        IntegerField conField = createStatField("CON");
        IntegerField intField = createStatField("INT");
        IntegerField wisField = createStatField("WIS");
        IntegerField chaField = createStatField("CHA");

        // Combat stats
        IntegerField hpField = new IntegerField("Hit Points");
        hpField.setMin(1);
        hpField.setValue(10);
        hpField.setWidthFull();

        IntegerField acField = new IntegerField("Armor Class");
        acField.setMin(1);
        acField.setValue(10);
        acField.setWidthFull();

        IntegerField speedField = new IntegerField("Speed");
        speedField.setMin(0);
        speedField.setValue(30);
        speedField.setWidthFull();

        TextArea backstoryField = new TextArea("Backstory");
        backstoryField.setWidthFull();

        // Campaign selection
        Select<Campaign> campaignSelect = new Select<>();
        campaignSelect.setLabel("Campaign (Optional)");
        campaignSelect.setWidthFull();
        campaignSelect.setEmptySelectionAllowed(true);
        campaignSelect.setItemLabelGenerator(campaign -> {
            if (campaign == null) return "";
            return campaign.getName() != null ? campaign.getName() : "Unnamed Campaign";
        });

        // Populate fields if editing
        if (character != null) {
            nameField.setValue(character.getName() != null ? character.getName() : "");
            raceSelect.setValue(character.getRace());
            classSelect.setValue(character.getCharacterClass());
            levelField.setValue(character.getLevel() != null ? character.getLevel() : 1);
            strField.setValue(character.getStrength() != null ? character.getStrength() : 10);
            dexField.setValue(character.getDexterity() != null ? character.getDexterity() : 10);
            conField.setValue(character.getConstitution() != null ? character.getConstitution() : 10);
            intField.setValue(character.getIntelligence() != null ? character.getIntelligence() : 10);
            wisField.setValue(character.getWisdom() != null ? character.getWisdom() : 10);
            chaField.setValue(character.getCharisma() != null ? character.getCharisma() : 10);
            hpField.setValue(character.getHitPoints() != null ? character.getHitPoints() : 10);
            acField.setValue(character.getArmorClass() != null ? character.getArmorClass() : 10);
            speedField.setValue(character.getSpeed() != null ? character.getSpeed() : 30);
            backstoryField.setValue(character.getBackstory() != null ? character.getBackstory() : "");
        }

        // Form layout
        FormLayout basicForm = new FormLayout(nameField, raceSelect, classSelect, levelField);
        basicForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        H4 statsHeader = new H4("Ability Scores");
        FormLayout statsForm = new FormLayout(strField, dexField, conField, intField, wisField, chaField);
        statsForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));

        H4 combatHeader = new H4("Combat Stats");
        FormLayout combatForm = new FormLayout(hpField, acField, speedField);
        combatForm.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 3));

        VerticalLayout content = new VerticalLayout(basicForm, statsHeader, statsForm, combatHeader, combatForm, campaignSelect, backstoryField);
        content.setPadding(false);
        content.setSpacing(true);

        dialog.add(content);

        Button saveButton = new Button("Save", e -> {
            if (nameField.isEmpty()) {
                Notification.show("Name is required", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Character charToSave = character != null ? character : new Character();
            charToSave.setName(nameField.getValue());
            charToSave.setRace(raceSelect.getValue());
            charToSave.setCharacterClass(classSelect.getValue());
            charToSave.setLevel(levelField.getValue());
            charToSave.setStrength(strField.getValue());
            charToSave.setDexterity(dexField.getValue());
            charToSave.setConstitution(conField.getValue());
            charToSave.setIntelligence(intField.getValue());
            charToSave.setWisdom(wisField.getValue());
            charToSave.setCharisma(chaField.getValue());
            charToSave.setHitPoints(hpField.getValue());
            charToSave.setArmorClass(acField.getValue());
            charToSave.setSpeed(speedField.getValue());
            charToSave.setBackstory(backstoryField.getValue());

            if (campaignSelect.getValue() != null) {
                charToSave.setCampaignId(campaignSelect.getValue().getId());
            }

            try {
                if (character == null) {
                    apiClient.post("/api/characters", charToSave, Character.class);
                    Notification.show("Character created!", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    apiClient.put("/api/characters/" + character.getId(), charToSave, Character.class);
                    Notification.show("Character updated!", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
                dialog.close();
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Failed to save: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();

        // Load campaigns AFTER dialog is open to avoid blocking
        try {
            List<Campaign> campaigns = apiClient.getList("/api/campaigns", Campaign.class);
            if (campaigns != null) {
                // Filter out any null campaigns
                campaignSelect.setItems(campaigns.stream().filter(c -> c != null).toList());
            }
        } catch (Exception e) {
            // Campaigns not available - dropdown stays empty
        }
    }

    private IntegerField createStatField(String label) {
        IntegerField field = new IntegerField(label);
        field.setMin(1);
        field.setMax(30);
        field.setValue(10);
        field.setStepButtonsVisible(true);
        return field;
    }

    private void openCharacterDetailsDialog(Character character) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(character.getName());
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);

        // Basic info
        content.add(new Span("Level " + character.getLevel() + " " + character.getRace() + " " + character.getCharacterClass()));

        // Stats
        H4 statsHeader = new H4("Ability Scores");
        HorizontalLayout stats = new HorizontalLayout();
        stats.add(createStatDisplay("STR", character.getStrength()));
        stats.add(createStatDisplay("DEX", character.getDexterity()));
        stats.add(createStatDisplay("CON", character.getConstitution()));
        stats.add(createStatDisplay("INT", character.getIntelligence()));
        stats.add(createStatDisplay("WIS", character.getWisdom()));
        stats.add(createStatDisplay("CHA", character.getCharisma()));

        content.add(statsHeader, stats);

        // Combat
        H4 combatHeader = new H4("Combat");
        HorizontalLayout combat = new HorizontalLayout();
        combat.add(new Span("HP: " + character.getHitPoints()));
        combat.add(new Span("AC: " + character.getArmorClass()));
        combat.add(new Span("Speed: " + character.getSpeed() + " ft"));
        content.add(combatHeader, combat);

        // Backstory
        if (character.getBackstory() != null && !character.getBackstory().isEmpty()) {
            H4 backstoryHeader = new H4("Backstory");
            content.add(backstoryHeader, new Span(character.getBackstory()));
        }

        dialog.add(content);

        Button closeButton = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(closeButton);
        dialog.open();
    }

    private VerticalLayout createStatDisplay(String label, Integer value) {
        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(Alignment.CENTER);
        layout.setPadding(false);
        layout.setSpacing(false);
        layout.getStyle()
                .set("border", "1px solid var(--lumo-contrast-20pct)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-s)")
                .set("min-width", "60px");

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-size", "var(--lumo-font-size-xs)");

        Span valueSpan = new Span(String.valueOf(value != null ? value : 10));
        valueSpan.getStyle().set("font-size", "var(--lumo-font-size-l)").set("font-weight", "bold");

        int modifier = ((value != null ? value : 10) - 10) / 2;
        String modStr = modifier >= 0 ? "+" + modifier : String.valueOf(modifier);
        Span modSpan = new Span(modStr);
        modSpan.getStyle().set("font-size", "var(--lumo-font-size-s)").set("color", "var(--lumo-secondary-text-color)");

        layout.add(labelSpan, valueSpan, modSpan);
        return layout;
    }

    private void confirmDelete(Character character) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Character?");
        dialog.setText("Are you sure you want to delete \"" + character.getName() + "\"?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                apiClient.delete("/api/characters/" + character.getId());
                Notification.show("Character deleted", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshGrid();
            } catch (Exception ex) {
                Notification.show("Failed to delete: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }
}
