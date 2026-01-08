package com.dnd.views;

import com.dnd.dto.Campaign;
import com.dnd.service.ApiClient;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "campaigns", layout = MainLayout.class)
@PageTitle("Campaigns | D&D Campaign Manager")
public class CampaignsView extends VerticalLayout {

    private final ApiClient apiClient;
    private final Grid<Campaign> grid;

    public CampaignsView(ApiClient apiClient) {
        this.apiClient = apiClient;

        setPadding(true);
        setSpacing(true);

        // Header with title and add button
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H2 title = new H2("Campaigns");

        Button addButton = new Button("New Campaign", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openCampaignDialog(null));

        header.add(title, addButton);
        add(header);

        // Grid
        grid = new Grid<>(Campaign.class, false);
        grid.addColumn(Campaign::getName).setHeader("Name").setSortable(true);
        grid.addColumn(Campaign::getSetting).setHeader("Setting").setSortable(true);
        grid.addComponentColumn(campaign -> createStatusBadge(campaign.getStatus()))
                .setHeader("Status")
                .setAutoWidth(true);
        grid.addColumn(Campaign::getDescription).setHeader("Description").setFlexGrow(2);

        grid.addComponentColumn(campaign -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button viewButton = new Button(new Icon(VaadinIcon.EYE));
            viewButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            viewButton.addClickListener(e -> getUI().ifPresent(ui ->
                    ui.navigate("campaigns/" + campaign.getId())));

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openCampaignDialog(campaign));

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDelete(campaign));

            actions.add(viewButton, editButton, deleteButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);

        grid.setWidthFull();
        grid.setHeight("500px");

        add(grid);

        refreshGrid();
    }

    private Span createStatusBadge(String status) {
        Span badge = new Span(status);
        badge.getElement().getThemeList().add("badge");

        switch (status) {
            case "ACTIVE" -> badge.getElement().getThemeList().add("success");
            case "PAUSED" -> badge.getElement().getThemeList().add("contrast");
            case "COMPLETED" -> badge.getElement().getThemeList().add("primary");
        }

        return badge;
    }

    private void refreshGrid() {
        try {
            List<Campaign> campaigns = apiClient.getList("/api/campaigns", Campaign.class);
            grid.setItems(campaigns);
        } catch (Exception e) {
            Notification.show("Failed to load campaigns: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void openCampaignDialog(Campaign campaign) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(campaign == null ? "New Campaign" : "Edit Campaign");
        dialog.setWidth("500px");

        TextField nameField = new TextField("Name");
        nameField.setWidthFull();
        nameField.setRequired(true);

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setWidthFull();

        TextField settingField = new TextField("Setting");
        settingField.setWidthFull();
        settingField.setPlaceholder("e.g., Forgotten Realms, Eberron, Homebrew...");

        Select<String> statusSelect = new Select<>();
        statusSelect.setLabel("Status");
        statusSelect.setItems("ACTIVE", "PAUSED", "COMPLETED");
        statusSelect.setValue("ACTIVE");
        statusSelect.setWidthFull();

        if (campaign != null) {
            nameField.setValue(campaign.getName() != null ? campaign.getName() : "");
            descriptionField.setValue(campaign.getDescription() != null ? campaign.getDescription() : "");
            settingField.setValue(campaign.getSetting() != null ? campaign.getSetting() : "");
            statusSelect.setValue(campaign.getStatus() != null ? campaign.getStatus() : "ACTIVE");
        }

        FormLayout form = new FormLayout(nameField, settingField, statusSelect, descriptionField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        dialog.add(form);

        Button saveButton = new Button("Save", e -> {
            if (nameField.isEmpty()) {
                Notification.show("Name is required", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Campaign campaignToSave = campaign != null ? campaign : new Campaign();
            campaignToSave.setName(nameField.getValue());
            campaignToSave.setDescription(descriptionField.getValue());
            campaignToSave.setSetting(settingField.getValue());
            campaignToSave.setStatus(statusSelect.getValue());

            try {
                if (campaign == null) {
                    apiClient.post("/api/campaigns", campaignToSave, Campaign.class);
                    Notification.show("Campaign created!", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    apiClient.put("/api/campaigns/" + campaign.getId(), campaignToSave, Campaign.class);
                    Notification.show("Campaign updated!", 3000, Notification.Position.MIDDLE)
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
    }

    private void confirmDelete(Campaign campaign) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Campaign?");
        dialog.setText("Are you sure you want to delete \"" + campaign.getName() + "\"? This action cannot be undone.");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                apiClient.delete("/api/campaigns/" + campaign.getId());
                Notification.show("Campaign deleted", 3000, Notification.Position.MIDDLE)
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
