package com.recipebook.views;

import com.recipebook.service.AuthService;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | Recipe Book")
public class DashboardView extends VerticalLayout {

    private final AuthService authService;

    public DashboardView(AuthService authService) {
        this.authService = authService;

        setPadding(true);
        setSpacing(true);

        authService.getCurrentUser().ifPresent(user -> {
            H2 welcome = new H2("Welcome, " + user.username() + "!");
            add(welcome);
        });

        Paragraph placeholder = new Paragraph("Welcome to Recipe Book");
        placeholder.getStyle().set("color", "var(--lumo-secondary-text-color)");
        add(placeholder);
    }
}
