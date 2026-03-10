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

@Route("reset-password")
@PageTitle("Reset Password | Recipe Book")
public class ResetPasswordView extends VerticalLayout implements BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(ResetPasswordView.class);

    private final AuthService authService;
    private final PasswordField newPasswordField;
    private final PasswordField confirmPasswordField;
    private String token;

    public ResetPasswordView(AuthService authService) {
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

        H1 title = new H1("Reset Password");
        title.getStyle().set("text-align", "center").set("margin-bottom", "var(--lumo-space-l)");

        newPasswordField = new PasswordField("New Password");
        newPasswordField.setWidthFull();
        newPasswordField.setRequiredIndicatorVisible(true);
        newPasswordField.setMinLength(6);

        confirmPasswordField = new PasswordField("Confirm New Password");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRequiredIndicatorVisible(true);

        Button submitButton = new Button("Reset Password", e -> resetPassword());
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setWidthFull();

        RouterLink backToLogin = new RouterLink("Back to Login", LoginView.class);
        Paragraph backText = new Paragraph();
        backText.add(backToLogin);
        backText.getStyle().set("text-align", "center");

        VerticalLayout formLayout = new VerticalLayout(title, newPasswordField, confirmPasswordField, submitButton, backText);
        formLayout.setPadding(false);
        formLayout.setSpacing(true);

        card.add(formLayout);
        add(card);

        confirmPasswordField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> resetPassword());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
        List<String> tokenParams = params.get("token");
        if (tokenParams == null || tokenParams.isEmpty()) {
            Notification.show("Invalid or expired reset link", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            event.forwardTo(LoginView.class);
            return;
        }
        this.token = tokenParams.get(0);
    }

    private void resetPassword() {
        String newPassword = newPasswordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Notification.show("Please fill in all fields", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (newPassword.length() < 6) {
            Notification.show("Password must be at least 6 characters", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Notification.show("Passwords do not match", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            authService.resetPassword(token, newPassword);
            Notification.show("Password reset successfully!", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            UI.getCurrent().navigate(DashboardView.class);
        } catch (Exception e) {
            LOG.warn("Password reset failed: {}", e.getMessage());
            Notification.show("Invalid or expired reset link", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
