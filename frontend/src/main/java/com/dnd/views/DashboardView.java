package com.dnd.views;

import com.dnd.dto.Campaign;
import com.dnd.dto.Character;
import com.dnd.service.ApiClient;
import com.dnd.service.AuthService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | D&D Campaign Manager")
public class DashboardView extends VerticalLayout {

    private final ApiClient apiClient;
    private final AuthService authService;

    public DashboardView(ApiClient apiClient, AuthService authService) {
        this.apiClient = apiClient;
        this.authService = authService;

        setPadding(true);
        setSpacing(true);

        // Welcome message
        authService.getCurrentUser().ifPresent(user -> {
            H2 welcome = new H2("Welcome, " + user.username() + "!");
            add(welcome);
        });

        // Stats cards
        add(createStatsSection());

        // Quick actions
        add(createQuickActionsSection());
    }

    private FlexLayout createStatsSection() {
        FlexLayout statsLayout = new FlexLayout();
        statsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        statsLayout.getStyle().set("gap", "var(--lumo-space-l)");

        try {
            List<Campaign> campaigns = apiClient.getList("/api/campaigns", Campaign.class);
            List<Character> characters = apiClient.getList("/api/characters", Character.class);

            long activeCampaigns = campaigns.stream()
                    .filter(c -> "ACTIVE".equals(c.getStatus()))
                    .count();

            statsLayout.add(createStatCard("Total Campaigns", String.valueOf(campaigns.size()), VaadinIcon.BOOK));
            statsLayout.add(createStatCard("Active Campaigns", String.valueOf(activeCampaigns), VaadinIcon.PLAY));
            statsLayout.add(createStatCard("Characters", String.valueOf(characters.size()), VaadinIcon.USER));

        } catch (Exception e) {
            statsLayout.add(createStatCard("Campaigns", "0", VaadinIcon.BOOK));
            statsLayout.add(createStatCard("Characters", "0", VaadinIcon.USER));
        }

        return statsLayout;
    }

    private Div createStatCard(String title, String value, VaadinIcon icon) {
        Div card = new Div();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("padding", "var(--lumo-space-l)")
                .set("min-width", "200px")
                .set("text-align", "center");

        Icon iconComponent = icon.create();
        iconComponent.setSize("48px");
        iconComponent.getStyle().set("color", "var(--lumo-primary-color)");

        H3 valueText = new H3(value);
        valueText.getStyle()
                .set("margin", "var(--lumo-space-s) 0")
                .set("font-size", "var(--lumo-font-size-xxl)");

        Span titleText = new Span(title);
        titleText.getStyle().set("color", "var(--lumo-secondary-text-color)");

        VerticalLayout content = new VerticalLayout(iconComponent, valueText, titleText);
        content.setAlignItems(Alignment.CENTER);
        content.setPadding(false);
        content.setSpacing(false);

        card.add(content);
        return card;
    }

    private VerticalLayout createQuickActionsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        H3 title = new H3("Quick Actions");
        section.add(title);

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        actions.add(createActionCard("New Campaign", "Start a new adventure", VaadinIcon.PLUS, "campaigns"));
        actions.add(createActionCard("New Character", "Create a hero", VaadinIcon.USER_STAR, "characters"));
        actions.add(createActionCard("Roll Dice", "Test your luck", VaadinIcon.CUBE, "dice"));

        section.add(actions);
        return section;
    }

    private Div createActionCard(String title, String description, VaadinIcon icon, String route) {
        Div card = new Div();
        card.getStyle()
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-l)")
                .set("cursor", "pointer")
                .set("transition", "background 0.2s");

        card.getElement().addEventListener("mouseover", e ->
                card.getStyle().set("background", "var(--lumo-contrast-10pct)"));
        card.getElement().addEventListener("mouseout", e ->
                card.getStyle().set("background", "var(--lumo-contrast-5pct)"));

        card.addClickListener(e -> getUI().ifPresent(ui -> ui.navigate(route)));

        Icon iconComponent = icon.create();
        iconComponent.setSize("32px");
        iconComponent.getStyle().set("color", "var(--lumo-primary-color)");

        H3 titleText = new H3(title);
        titleText.getStyle().set("margin", "var(--lumo-space-s) 0 0 0");

        Paragraph descText = new Paragraph(description);
        descText.getStyle()
                .set("margin", "0")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "var(--lumo-font-size-s)");

        VerticalLayout content = new VerticalLayout(iconComponent, titleText, descText);
        content.setPadding(false);
        content.setSpacing(false);

        card.add(content);
        return card;
    }
}
