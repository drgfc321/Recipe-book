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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("register")
@PageTitle("Register | Recipe Book")
public class RegisterView extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterView.class);

    private final AuthService authService;
    private final EmailField emailField;
    private final TextField usernameField;
    private final PasswordField passwordField;
    private final PasswordField confirmPasswordField;
    private final Select<String> languageSelect;

    public RegisterView(AuthService authService) {
        this.authService = authService;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Div card = new Div();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-m)")
                .set("padding", "var(--lumo-space-xl)")
                .set("max-width", "400px")
                .set("width", "100%");

        H1 title = new H1("Create Account");
        title.getStyle().set("text-align", "center").set("margin-bottom", "var(--lumo-space-l)");

        emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setRequiredIndicatorVisible(true);

        usernameField = new TextField("Username");
        usernameField.setWidthFull();
        usernameField.setRequiredIndicatorVisible(true);
        usernameField.setMinLength(3);
        usernameField.setMaxLength(50);

        passwordField = new PasswordField("Password");
        passwordField.setWidthFull();
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setMinLength(6);

        confirmPasswordField = new PasswordField("Confirm Password");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRequiredIndicatorVisible(true);

        languageSelect = new Select<>();
        languageSelect.setLabel("Language");
        languageSelect.setItems("en", "ro");
        languageSelect.setItemLabelGenerator(lang -> lang.equals("en") ? "English" : "Romanian");
        languageSelect.setValue("en");
        languageSelect.setWidthFull();

        Button registerButton = new Button("Register", e -> register());
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();

        RouterLink loginLink = new RouterLink("Already have an account? Login", LoginView.class);
        Paragraph loginText = new Paragraph();
        loginText.add(loginLink);
        loginText.getStyle().set("text-align", "center");

        VerticalLayout formLayout = new VerticalLayout(
                title, emailField, usernameField, passwordField,
                confirmPasswordField, languageSelect, registerButton, loginText
        );
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        card.add(formLayout);
        add(card);
    }

    private void register() {
        String email = emailField.getValue();
        String username = usernameField.getValue();
        String password = passwordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();
        String language = languageSelect.getValue();

        if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
            Notification.show("Please fill in all fields", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (username.length() < 3) {
            Notification.show("Username must be at least 3 characters", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (password.length() < 6) {
            Notification.show("Password must be at least 6 characters", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            Notification.show("Passwords do not match", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            authService.register(email, username, password, language);
            Notification.show("Registration successful!", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(DashboardView.class);
        } catch (Exception e) {
            LOG.warn("Registration failed for email '{}': {}", email, e.getMessage());
            String message = e.getMessage();
            if (message != null && message.contains("409")) {
                message = "Email or username already exists";
            }
            Notification.show(message, 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
