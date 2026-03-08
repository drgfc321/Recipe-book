package com.recipebook.views;

import com.recipebook.dto.UserInfo;
import com.recipebook.service.ApiClient;
import com.recipebook.service.AuthService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profile")
public class ProfileView extends VerticalLayout {

    private final AuthService authService;
    private final ApiClient apiClient;

    private final TextField usernameField = new TextField("Username");
    private final TextField emailField = new TextField("Email");
    private final Image avatarPreview = new Image();
    private final Div avatarContainer = new Div();
    private final MemoryBuffer uploadBuffer = new MemoryBuffer();
    private final Upload avatarUpload = new Upload(uploadBuffer);

    private String currentAvatarUrl;

    public ProfileView(AuthService authService, ApiClient apiClient) {
        this.authService = authService;
        this.apiClient = apiClient;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setPadding(true);

        buildForm();
        populateForm();
    }

    private void buildForm() {
        H2 title = new H2("My Profile");

        // Avatar section
        avatarPreview.setWidth("120px");
        avatarPreview.setHeight("120px");
        avatarPreview.getStyle()
                .set("border-radius", "50%")
                .set("object-fit", "cover")
                .set("border", "2px solid var(--lumo-contrast-20pct)");

        avatarContainer.getStyle()
                .set("width", "120px")
                .set("height", "120px")
                .set("border-radius", "50%")
                .set("background-color", "var(--lumo-contrast-10pct)")
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("overflow", "hidden");

        avatarUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        avatarUpload.setMaxFileSize(5 * 1024 * 1024);
        avatarUpload.setDropAllowed(true);
        avatarUpload.setMaxFiles(1);
        avatarUpload.setWidthFull();

        avatarUpload.addSucceededListener(event -> {
            try {
                uploadAvatar(uploadBuffer.getInputStream(), event.getFileName(), event.getMIMEType());
            } catch (Exception e) {
                Notification.show("Upload failed: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        avatarUpload.addFileRejectedListener(event ->
                Notification.show(event.getErrorMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR));

        Button removeAvatarButton = new Button("Remove", VaadinIcon.CLOSE_SMALL.create(), e -> {
            currentAvatarUrl = null;
            avatarContainer.removeAll();
            avatarUpload.clearFileList();
        });
        removeAvatarButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        VerticalLayout avatarSection = new VerticalLayout(avatarContainer, avatarUpload, removeAvatarButton);
        avatarSection.setAlignItems(Alignment.CENTER);
        avatarSection.setPadding(false);
        avatarSection.setSpacing(true);

        // Form fields
        usernameField.setWidthFull();
        usernameField.setRequired(true);
        usernameField.setPrefixComponent(VaadinIcon.USER.create());

        emailField.setWidthFull();
        emailField.setRequired(true);
        emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());

        // Save button
        Button saveButton = new Button("Save Changes", VaadinIcon.CHECK.create(), e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setWidthFull();

        // Change password button (hidden for OAuth users)
        Button changePasswordButton = new Button("Change Password", VaadinIcon.KEY.create(),
                e -> new ChangePasswordDialog(authService).open());
        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        changePasswordButton.setWidthFull();

        boolean isLocalUser = authService.getCurrentUser()
                .map(u -> "LOCAL".equals(u.authProvider()))
                .orElse(true);
        changePasswordButton.setVisible(isLocalUser);

        // Card layout
        VerticalLayout card = new VerticalLayout(title, avatarSection, usernameField, emailField, saveButton, changePasswordButton);
        card.setAlignItems(Alignment.CENTER);
        card.setMaxWidth("480px");
        card.setWidthFull();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("padding", "var(--lumo-space-l)");

        add(card);
    }

    private void populateForm() {
        authService.getCurrentUser().ifPresent(user -> {
            usernameField.setValue(user.username() != null ? user.username() : "");
            emailField.setValue(user.email() != null ? user.email() : "");
            currentAvatarUrl = user.avatarUrl();
            showAvatarPreview();
        });
    }

    private void showAvatarPreview() {
        avatarContainer.removeAll();
        if (currentAvatarUrl != null && !currentAvatarUrl.isBlank()) {
            avatarPreview.setSrc(currentAvatarUrl);
            avatarPreview.setAlt("User avatar");
            avatarContainer.add(avatarPreview);
        }
    }

    private void uploadAvatar(InputStream inputStream, String fileName, String mimeType) throws IOException {
        byte[] fileBytes = inputStream.readAllBytes();

        String token = null;
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            Object t = session.getAttribute("jwt_token");
            if (t != null) token = t.toString();
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", fileBytes)
                .filename(fileName)
                .contentType(MediaType.parseMediaType(mimeType));

        var requestSpec = apiClient.getWebClient()
                .post()
                .uri("/api/images")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()));

        if (token != null) {
            final String bearerToken = "Bearer " + token;
            requestSpec = apiClient.getWebClient()
                    .post()
                    .uri("/api/images")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .header(HttpHeaders.AUTHORIZATION, bearerToken)
                    .body(BodyInserters.fromMultipartData(builder.build()));
        }

        String json = requestSpec
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        currentAvatarUrl = root.get("imageUrl").asText();
        showAvatarPreview();
    }

    private void save() {
        String username = usernameField.getValue();
        String email = emailField.getValue();

        if (username == null || username.isBlank()) {
            Notification.show("Username is required", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (email == null || email.isBlank()) {
            Notification.show("Email is required", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            authService.updateProfile(username, email, currentAvatarUrl);
            Notification.show("Profile updated successfully!", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.getPage().reload());
        } catch (Exception e) {
            Notification.show("Failed to update profile: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
