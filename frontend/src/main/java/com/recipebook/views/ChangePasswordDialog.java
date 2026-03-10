package com.recipebook.views;

import com.recipebook.service.AuthService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;

public class ChangePasswordDialog extends Dialog {

    private final AuthService authService;

    private final PasswordField currentPassword = new PasswordField("Current Password");
    private final PasswordField newPassword = new PasswordField("New Password");
    private final PasswordField confirmPassword = new PasswordField("Confirm New Password");

    public ChangePasswordDialog(AuthService authService) {
        this.authService = authService;

        setHeaderTitle("Change Password");
        setWidth("400px");
        setCloseOnOutsideClick(false);

        currentPassword.setWidthFull();
        currentPassword.setRequired(true);

        newPassword.setWidthFull();
        newPassword.setRequired(true);
        newPassword.setMinLength(6);
        newPassword.setHelperText("Minimum 6 characters");

        confirmPassword.setWidthFull();
        confirmPassword.setRequired(true);
        confirmPassword.addKeyPressListener(Key.ENTER, e -> submit());

        VerticalLayout content = new VerticalLayout(currentPassword, newPassword, confirmPassword);
        content.setPadding(false);
        content.setSpacing(true);
        add(content);

        Button submitButton = new Button("Change Password", e -> submit());
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout footer = new HorizontalLayout(cancelButton, submitButton);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setWidthFull();
        getFooter().add(footer);
    }

    private void submit() {
        String current = currentPassword.getValue();
        String newPwd = newPassword.getValue();
        String confirm = confirmPassword.getValue();

        if (current.isBlank() || newPwd.isBlank() || confirm.isBlank()) {
            Notification.show("All fields are required", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (newPwd.length() < 6) {
            Notification.show("New password must be at least 6 characters", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        if (!newPwd.equals(confirm)) {
            Notification.show("Passwords do not match", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            confirmPassword.setInvalid(true);
            return;
        }

        try {
            authService.changePassword(current, newPwd);
            Notification.show("Password changed successfully", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            close();
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Current password is incorrect")) {
                currentPassword.setInvalid(true);
                currentPassword.setErrorMessage("Current password is incorrect");
            }
            Notification.show(msg != null ? msg : "Failed to change password", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
