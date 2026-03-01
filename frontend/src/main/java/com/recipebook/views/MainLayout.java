package com.recipebook.views;

import com.recipebook.foodlog.FoodLogView;
import com.recipebook.ingredient.IngredientListView;
import com.recipebook.mealplan.MealPlanView;
import com.recipebook.pantry.PantryView;
import com.recipebook.recipe.RecipeListView;
import com.recipebook.recommendation.RecommendationView;
import com.recipebook.service.AuthService;
import com.recipebook.shopping.ShoppingListView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final AuthService authService;

    public MainLayout(AuthService authService) {
        this.authService = authService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Recipe Book");
        logo.getStyle()
                .set("font-size", "var(--lumo-font-size-l)")
                .set("margin", "0")
                .set("padding-left", "var(--lumo-space-m)");

        // User info and logout button
        HorizontalLayout userSection = new HorizontalLayout();
        userSection.setAlignItems(FlexComponent.Alignment.CENTER);
        userSection.setSpacing(true);

        authService.getCurrentUser().ifPresent(user -> {
            Span username = new Span(user.username());
            username.getStyle().set("font-weight", "500");

            Span role = new Span("(" + user.role() + ")");
            role.getStyle()
                    .set("font-size", "var(--lumo-font-size-s)")
                    .set("color", "var(--lumo-secondary-text-color)");

            userSection.add(username, role);
        });

        Button logoutButton = new Button("Logout", new Icon(VaadinIcon.SIGN_OUT), e -> logout());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        userSection.add(logoutButton);

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, userSection);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.setPadding(true);

        addToNavbar(header);
    }

    private void createDrawer() {
        SideNav nav = new SideNav();

        nav.addItem(new SideNavItem("Dashboard", DashboardView.class, VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Recipes", RecipeListView.class, VaadinIcon.BOOK.create()));
        nav.addItem(new SideNavItem("Ingredients", IngredientListView.class, VaadinIcon.STOCK.create()));
        nav.addItem(new SideNavItem("Meal Planner", MealPlanView.class, VaadinIcon.CALENDAR.create()));
        nav.addItem(new SideNavItem("Food Log", FoodLogView.class, VaadinIcon.NOTEBOOK.create()));
        nav.addItem(new SideNavItem("Pantry", PantryView.class, VaadinIcon.STORAGE.create()));
        nav.addItem(new SideNavItem("Shopping List", ShoppingListView.class, VaadinIcon.CART.create()));
        nav.addItem(new SideNavItem("Recommendations", RecommendationView.class, VaadinIcon.MAGIC.create()));

        VerticalLayout drawerContent = new VerticalLayout(nav);
        drawerContent.setSizeFull();
        drawerContent.setPadding(true);

        addToDrawer(drawerContent);
    }

    private void logout() {
        authService.logout();
        UI.getCurrent().navigate(LoginView.class);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!authService.isLoggedIn()) {
            event.rerouteTo(LoginView.class);
        }
    }
}
