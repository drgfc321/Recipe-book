package com.recipebook.views;

import com.recipebook.service.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("login")
@PageTitle("Login | Recipe Book")
public class LoginView extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(LoginView.class);

    private final AuthService authService;
    private final EmailField emailField;
    private final PasswordField passwordField;

    public LoginView(AuthService authService) {
        this.authService = authService;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Div card = new Div();
        card.addClassName("login-card");
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)")
                .set("padding", "var(--lumo-space-xl)")
                .set("max-width", "400px")
                .set("width", "100%");

        H1 title = new H1("Recipe Book");
        title.getStyle().set("text-align", "center").set("margin-bottom", "var(--lumo-space-l)");

        emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setRequiredIndicatorVisible(true);

        passwordField = new PasswordField("Password");
        passwordField.setWidthFull();
        passwordField.setRequiredIndicatorVisible(true);

        Button loginButton = new Button("Login", e -> login());
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();

        RouterLink forgotPasswordLink = new RouterLink("Forgot password?", ForgotPasswordView.class);
        Paragraph forgotText = new Paragraph();
        forgotText.add(forgotPasswordLink);
        forgotText.getStyle().set("text-align", "center");

        RouterLink registerLink = new RouterLink("Don't have an account? Register", RegisterView.class);
        Paragraph registerText = new Paragraph();
        registerText.add(registerLink);
        registerText.getStyle().set("text-align", "center");

        VerticalLayout formLayout = new VerticalLayout(title, emailField, passwordField, loginButton, forgotText, registerText);
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        card.add(formLayout);
        add(card);

        // Handle Enter key
        passwordField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> login());
    }

    private void login() {
        String email = emailField.getValue();
        String password = passwordField.getValue();

        if (email.isEmpty() || password.isEmpty()) {
            Notification.show("Please fill in all fields", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            authService.login(email, password);
            UI.getCurrent().navigate(DashboardView.class);
        } catch (Exception e) {
            LOG.warn("Login failed for email '{}': {}", email, e.getMessage());
            String message = e.getMessage();
            if (message != null && message.contains("401")) {
                message = "Invalid email or password";
            }
            Notification.show(message, 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
