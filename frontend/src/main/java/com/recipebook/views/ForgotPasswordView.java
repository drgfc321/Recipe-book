package com.recipebook.views;

import com.recipebook.service.AuthService;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("forgot-password")
@PageTitle("Forgot Password | Recipe Book")
public class ForgotPasswordView extends VerticalLayout {

    private static final Logger LOG = LoggerFactory.getLogger(ForgotPasswordView.class);

    private final AuthService authService;
    private final EmailField emailField;

    public ForgotPasswordView(AuthService authService) {
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

        H1 title = new H1("Forgot Password");
        title.getStyle().set("text-align", "center").set("margin-bottom", "var(--lumo-space-l)");

        emailField = new EmailField("Enter your email");
        emailField.setWidthFull();
        emailField.setRequiredIndicatorVisible(true);

        Button submitButton = new Button("Send Reset Link", e -> requestReset());
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setWidthFull();

        RouterLink backToLogin = new RouterLink("Back to Login", LoginView.class);
        Paragraph backText = new Paragraph();
        backText.add(backToLogin);
        backText.getStyle().set("text-align", "center");

        VerticalLayout formLayout = new VerticalLayout(title, emailField, submitButton, backText);
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        card.add(formLayout);
        add(card);

        emailField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> requestReset());
    }

    private void requestReset() {
        String email = emailField.getValue();

        if (email.isEmpty()) {
            Notification.show("Please enter your email", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            authService.requestPasswordReset(email);
            Notification.show("If an account exists with that email, a reset link has been sent.",
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        } catch (Exception e) {
            LOG.warn("Password reset request failed for email '{}': {}", email, e.getMessage());
            Notification.show("If an account exists with that email, a reset link has been sent.",
                            5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
    }
}
