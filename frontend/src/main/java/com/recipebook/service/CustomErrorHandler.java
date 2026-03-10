package com.recipebook.service;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClientRequestException;

public class CustomErrorHandler implements ErrorHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CustomErrorHandler.class);

    @Override
    public void error(ErrorEvent event) {
        Throwable throwable = event.getThrowable();
        LOG.error("Unhandled UI error", throwable);

        Throwable root = getRootCause(throwable);

        if (isBackendUnavailable(root)) {
            showInUI("Server is unavailable. Please try again later.");
        } else {
            String message = root.getMessage();
            if (message == null || message.isBlank()) {
                message = "An unexpected error occurred";
            }
            showInUI(message);
        }
    }

    private boolean isBackendUnavailable(Throwable cause) {
        if (cause instanceof WebClientRequestException) {
            return true;
        }
        if (cause instanceof java.net.ConnectException) {
            return true;
        }
        String msg = cause.getMessage();
        if (msg != null) {
            String lower = msg.toLowerCase();
            return lower.contains("connection refused")
                    || lower.contains("connection reset")
                    || lower.contains("connection timed out");
        }
        return false;
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        return root;
    }

    private void showInUI(String message) {
        UI ui = UI.getCurrent();
        if (ui != null) {
            ui.access(() -> {
                Notification notification = Notification.show(message, 5000, Notification.Position.MIDDLE);
                notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            });
        }
    }
}
