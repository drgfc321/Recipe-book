package com.dnd.views;

import com.dnd.dto.Campaign;
import com.dnd.dto.Session;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.dnd.service.ApiClient;

import java.time.LocalDate;
import java.util.List;

@Route(value = "campaigns", layout = MainLayout.class)
@PageTitle("Campaign Details | D&D Campaign Manager")
public class CampaignDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final ApiClient apiClient;
    private Long campaignId;
    private Campaign campaign;
    private final Grid<Session> sessionsGrid;
    private final VerticalLayout contentLayout;

    public CampaignDetailView(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.sessionsGrid = new Grid<>(Session.class, false);
        this.contentLayout = new VerticalLayout();

        setPadding(true);
        setSpacing(true);
        add(contentLayout);
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        this.campaignId = parameter;
        loadCampaign();
    }

    private void loadCampaign() {
        contentLayout.removeAll();

        try {
            campaign = apiClient.get("/api/campaigns/" + campaignId, Campaign.class);

            // Back button
            Button backButton = new Button("Back to Campaigns", new Icon(VaadinIcon.ARROW_LEFT));
            backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            backButton.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(CampaignsView.class)));
            contentLayout.add(backButton);

            // Campaign header
            HorizontalLayout header = new HorizontalLayout();
            header.setWidthFull();
            header.setAlignItems(Alignment.CENTER);

            H2 title = new H2(campaign.getName());

            Span statusBadge = new Span(campaign.getStatus());
            statusBadge.getElement().getThemeList().add("badge");
            switch (campaign.getStatus()) {
                case "ACTIVE" -> statusBadge.getElement().getThemeList().add("success");
                case "PAUSED" -> statusBadge.getElement().getThemeList().add("contrast");
                case "COMPLETED" -> statusBadge.getElement().getThemeList().add("primary");
            }

            header.add(title, statusBadge);
            contentLayout.add(header);

            // Campaign details
            if (campaign.getSetting() != null && !campaign.getSetting().isEmpty()) {
                Paragraph setting = new Paragraph("Setting: " + campaign.getSetting());
                setting.getStyle().set("color", "var(--lumo-secondary-text-color)");
                contentLayout.add(setting);
            }

            if (campaign.getDescription() != null && !campaign.getDescription().isEmpty()) {
                Paragraph description = new Paragraph(campaign.getDescription());
                contentLayout.add(description);
            }

            // Sessions section
            contentLayout.add(createSessionsSection());

        } catch (Exception e) {
            contentLayout.add(new Paragraph("Failed to load campaign: " + e.getMessage()));
        }
    }

    private VerticalLayout createSessionsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setAlignItems(Alignment.CENTER);

        H3 title = new H3("Sessions");

        Button addButton = new Button("Add Session", new Icon(VaadinIcon.PLUS));
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openSessionDialog(null));

        header.add(title, addButton);
        section.add(header);

        // Sessions grid
        sessionsGrid.addColumn(Session::getSessionNumber).setHeader("#").setWidth("60px");
        sessionsGrid.addColumn(Session::getSessionDate).setHeader("Date").setAutoWidth(true);
        sessionsGrid.addColumn(Session::getSummary).setHeader("Summary").setFlexGrow(2);
        sessionsGrid.addColumn(session -> session.getXpAwarded() + " XP").setHeader("XP").setAutoWidth(true);

        sessionsGrid.addComponentColumn(session -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button editButton = new Button(new Icon(VaadinIcon.EDIT));
            editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openSessionDialog(session));

            Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
            deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDeleteSession(session));

            actions.add(editButton, deleteButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);

        sessionsGrid.setWidthFull();
        sessionsGrid.setHeight("400px");

        section.add(sessionsGrid);

        refreshSessions();

        return section;
    }

    private void refreshSessions() {
        try {
            List<Session> sessions = apiClient.getList("/api/campaigns/" + campaignId + "/sessions", Session.class);
            sessionsGrid.setItems(sessions);
        } catch (Exception e) {
            Notification.show("Failed to load sessions", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void openSessionDialog(Session session) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(session == null ? "New Session" : "Edit Session");
        dialog.setWidth("500px");

        IntegerField sessionNumberField = new IntegerField("Session Number");
        sessionNumberField.setWidthFull();
        sessionNumberField.setMin(1);

        DatePicker dateField = new DatePicker("Date");
        dateField.setWidthFull();
        dateField.setValue(LocalDate.now());

        TextArea summaryField = new TextArea("Summary");
        summaryField.setWidthFull();

        IntegerField xpField = new IntegerField("XP Awarded");
        xpField.setWidthFull();
        xpField.setMin(0);
        xpField.setValue(0);

        TextArea notesField = new TextArea("Notes");
        notesField.setWidthFull();

        if (session != null) {
            sessionNumberField.setValue(session.getSessionNumber());
            if (session.getSessionDate() != null) {
                dateField.setValue(session.getSessionDate());
            }
            summaryField.setValue(session.getSummary() != null ? session.getSummary() : "");
            xpField.setValue(session.getXpAwarded() != null ? session.getXpAwarded() : 0);
            notesField.setValue(session.getNotes() != null ? session.getNotes() : "");
        }

        FormLayout form = new FormLayout(sessionNumberField, dateField, xpField, summaryField, notesField);
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        dialog.add(form);

        Button saveButton = new Button("Save", e -> {
            if (sessionNumberField.isEmpty()) {
                Notification.show("Session number is required", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Session sessionToSave = session != null ? session : new Session();
            sessionToSave.setSessionNumber(sessionNumberField.getValue());
            sessionToSave.setSessionDate(dateField.getValue());
            sessionToSave.setSummary(summaryField.getValue());
            sessionToSave.setXpAwarded(xpField.getValue());
            sessionToSave.setNotes(notesField.getValue());

            try {
                if (session == null) {
                    apiClient.post("/api/campaigns/" + campaignId + "/sessions", sessionToSave, Session.class);
                    Notification.show("Session created!", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                } else {
                    apiClient.put("/api/sessions/" + session.getId(), sessionToSave, Session.class);
                    Notification.show("Session updated!", 3000, Notification.Position.MIDDLE)
                            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
                dialog.close();
                refreshSessions();
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

    private void confirmDeleteSession(Session session) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Delete Session?");
        dialog.setText("Are you sure you want to delete Session #" + session.getSessionNumber() + "?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Delete");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                apiClient.delete("/api/sessions/" + session.getId());
                Notification.show("Session deleted", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                refreshSessions();
            } catch (Exception ex) {
                Notification.show("Failed to delete: " + ex.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }
}
