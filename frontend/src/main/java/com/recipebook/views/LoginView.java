package com.recipebook.views;

import com.recipebook.service.ApiClient;
import com.recipebook.service.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Route("login")
@PageTitle("Login | Recipe Book")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(LoginView.class);

    private final AuthService authService;
    private final ApiClient apiClient;
    private final EmailField emailField;
    private final PasswordField passwordField;

    public LoginView(AuthService authService, ApiClient apiClient) {
        this.authService = authService;
        this.apiClient = apiClient;

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

        // OAuth buttons (always shown; backend returns 404 if provider not configured)
        Div divider = new Div();
        divider.addClassName("oauth-divider");
        Span dividerText = new Span(getTranslation("oauth.or", "or continue with"));
        divider.add(dividerText);
        formLayout.add(divider);

        Anchor googleBtn = new Anchor("/api/auth/google",
                getTranslation("oauth.google", "Sign in with Google"));
        googleBtn.addClassNames("oauth-btn", "oauth-btn-google");
        googleBtn.setWidthFull();
        formLayout.add(googleBtn);

        Anchor githubBtn = new Anchor("/api/auth/github",
                getTranslation("oauth.github", "Sign in with GitHub"));
        githubBtn.addClassNames("oauth-btn", "oauth-btn-github");
        githubBtn.setWidthFull();
        formLayout.add(githubBtn);

        card.add(formLayout);
        add(card);

        // Handle Enter key
        passwordField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> login());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
        List<String> errorParams = params.get("error");
        if (errorParams != null && !errorParams.isEmpty()) {
            Notification.show(getTranslation("oauth.error", "Login with external provider failed. Please try again."),
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
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
