package com.recipebook.recipe;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.reactive.function.BodyInserters;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeFormDialog extends Dialog {

    private final RecipeService recipeService;
    private final RecipeResponse existing;
    private final Runnable onSave;

    private final TextField nameField = new TextField("Name");
    private final TextArea descriptionField = new TextArea("Description");
    private final ComboBox<String> categoryField = new ComboBox<>("Category");
    private final ComboBox<String> difficultyField = new ComboBox<>("Difficulty");
    private final IntegerField prepTimeField = new IntegerField("Prep Time");
    private final IntegerField cookTimeField = new IntegerField("Cook Time");
    private final IntegerField servingsField = new IntegerField("Servings");
    private final TextArea instructionsField = new TextArea("Instructions");

    // Image upload
    private String currentImageUrl;
    private final Image imagePreview = new Image();
    private final Div imagePreviewContainer = new Div();
    private final MemoryBuffer uploadBuffer = new MemoryBuffer();
    private final Upload imageUpload = new Upload(uploadBuffer);

    private final VerticalLayout ingredientRows = new VerticalLayout();
    private final List<IngredientRow> ingredients = new ArrayList<>();
    private List<IngredientOption> ingredientOptions = List.of();

    public RecipeFormDialog(RecipeService recipeService, RecipeResponse existing, Runnable onSave) {
        this.recipeService = recipeService;
        this.existing = existing;
        this.onSave = onSave;

        setWidth("min(700px, 90vw)");
        setCloseOnOutsideClick(false);

        Span tag = new Span(existing != null ? "EDIT" : "NEW");
        tag.addClassName("dialog-header-tag");
        H2 title = new H2("Recipe");
        title.addClassName("dialog-header-title");
        getHeader().add(tag, title);

        try {
            ingredientOptions = recipeService.getIngredients();
        } catch (Exception e) {
            Notification.show("Failed to load ingredients", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }

        buildForm();
        buildFooter();

        if (existing != null) {
            populateForm();
        }
    }

    private Div createFormSection(String title, VaadinIcon vaadinIcon, String variant, Component... content) {
        Icon icon = vaadinIcon.create();
        icon.setSize("14px");

        Div iconSquare = new Div(icon);
        iconSquare.addClassName("section-icon-square");

        Span label = new Span(title);
        label.addClassName("section-label");

        HorizontalLayout header = new HorizontalLayout(iconSquare, label);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(false);
        header.addClassName("section-header");

        Div body = new Div();
        body.addClassName("section-body");
        for (Component c : content) {
            body.add(c);
        }

        Div section = new Div();
        section.addClassNames("form-section", "form-section--" + variant);
        section.add(header, body);

        return section;
    }

    private void buildForm() {
        categoryField.setItems("BREAKFAST", "LUNCH", "DINNER", "DESSERT", "SNACK", "OTHER");
        categoryField.setRequired(true);

        difficultyField.setItems("EASY", "MEDIUM", "HARD");
        difficultyField.setRequired(true);

        nameField.setRequired(true);
        nameField.setWidthFull();
        descriptionField.setWidthFull();
        descriptionField.setMaxLength(500);
        instructionsField.setWidthFull();
        instructionsField.setHeight("150px");
        instructionsField.setPlaceholder("Enter step-by-step instructions...");

        prepTimeField.setMin(0);
        prepTimeField.setValue(0);
        prepTimeField.setSuffixComponent(new Div("min"));
        prepTimeField.setPrefixComponent(VaadinIcon.CLOCK.create());

        cookTimeField.setMin(0);
        cookTimeField.setValue(0);
        cookTimeField.setSuffixComponent(new Div("min"));
        cookTimeField.setPrefixComponent(VaadinIcon.TIMER.create());

        servingsField.setMin(1);
        servingsField.setValue(1);
        servingsField.setPrefixComponent(VaadinIcon.GROUP.create());

        // Section 1: Basic Info
        FormLayout basicForm = new FormLayout();
        basicForm.setWidthFull();
        basicForm.add(nameField, 2);
        basicForm.add(descriptionField, 2);
        basicForm.add(categoryField, difficultyField);

        Div basicSection = createFormSection("Basic Info", VaadinIcon.BOOK, "primary", basicForm);

        // Section 2: Timing & Servings
        FormLayout timingForm = new FormLayout();
        timingForm.setWidthFull();
        timingForm.add(prepTimeField, cookTimeField);
        timingForm.add(servingsField);

        Div timingSection = createFormSection("Timing & Servings", VaadinIcon.CLOCK, "success", timingForm);

        // Section 3: Image Upload
        configureImageUpload();
        Div imageSection = createFormSection("Image", VaadinIcon.PICTURE, "primary", imageUpload, imagePreviewContainer);

        // Section 4: Instructions
        FormLayout instructionsForm = new FormLayout();
        instructionsForm.setWidthFull();
        instructionsForm.add(instructionsField, 2);

        Div instructionsSection = createFormSection("Instructions", VaadinIcon.LIST_OL, "warning", instructionsForm);

        // Section 5: Ingredients
        ingredientRows.setPadding(false);
        ingredientRows.setSpacing(false);

        Button addIngredientButton = new Button("Add Ingredient", VaadinIcon.PLUS.create(), e -> addIngredientRow(null));
        addIngredientButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);

        Div ingredientsSection = createFormSection("Ingredients", VaadinIcon.STOCK, "error", ingredientRows, addIngredientButton);

        VerticalLayout content = new VerticalLayout(basicSection, timingSection, imageSection, instructionsSection, ingredientsSection);
        content.setPadding(false);
        content.setSpacing(true);
        content.setWidthFull();
        add(content);
    }

    private void configureImageUpload() {
        imageUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        imageUpload.setMaxFileSize(5 * 1024 * 1024); // 5MB
        imageUpload.setDropAllowed(true);
        imageUpload.setWidthFull();

        imageUpload.addSucceededListener(event -> {
            try {
                uploadImageToBackend(uploadBuffer.getInputStream(), event.getFileName(), event.getMIMEType());
            } catch (Exception e) {
                Notification.show("Upload failed: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        imageUpload.addFileRejectedListener(event ->
                Notification.show(event.getErrorMessage(), 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR));

        // Preview container
        imagePreview.setWidthFull();
        imagePreview.setMaxHeight("200px");
        imagePreview.getStyle()
                .set("object-fit", "cover")
                .set("border-radius", "var(--lumo-border-radius-m)");

        imagePreviewContainer.setWidthFull();
        imagePreviewContainer.setVisible(false);

        Button removeImageButton = new Button("Remove image", VaadinIcon.CLOSE_SMALL.create(), e -> {
            currentImageUrl = null;
            imagePreviewContainer.setVisible(false);
            imageUpload.clearFileList();
        });
        removeImageButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        imagePreviewContainer.add(imagePreview, removeImageButton);
    }

    private void uploadImageToBackend(InputStream inputStream, String fileName, String mimeType) throws IOException {
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

        var requestSpec = recipeService.getApiClient().getWebClient()
                .post()
                .uri("/api/images")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()));

        if (token != null) {
            final String bearerToken = "Bearer " + token;
            requestSpec = recipeService.getApiClient().getWebClient()
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
        currentImageUrl = root.get("imageUrl").asText();
        showImagePreview();
    }

    private void showImagePreview() {
        if (currentImageUrl != null && !currentImageUrl.isBlank()) {
            String src = resolveImageUrl(currentImageUrl);
            imagePreview.setSrc(src);
            imagePreview.setAlt("Recipe image");
            imagePreviewContainer.setVisible(true);
        }
    }

    private String resolveImageUrl(String url) {
        return url;
    }

    private void buildFooter() {
        Button saveButton = new Button("Save", e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> close());
        cancelButton.addClassName("dialog-cancel-btn");

        HorizontalLayout footer = new HorizontalLayout(cancelButton, saveButton);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        getFooter().add(footer);
    }

    private void populateForm() {
        nameField.setValue(existing.name() != null ? existing.name() : "");
        descriptionField.setValue(existing.description() != null ? existing.description() : "");
        categoryField.setValue(existing.category());
        difficultyField.setValue(existing.difficulty());
        prepTimeField.setValue(existing.prepTime());
        cookTimeField.setValue(existing.cookTime());
        servingsField.setValue(existing.servings());
        instructionsField.setValue(existing.instructions() != null ? existing.instructions() : "");

        if (existing.imageUrl() != null && !existing.imageUrl().isBlank()) {
            currentImageUrl = existing.imageUrl();
            showImagePreview();
        }

        if (existing.ingredients() != null) {
            for (RecipeIngredientResponse ri : existing.ingredients()) {
                addIngredientRow(ri);
            }
        }
    }

    private void addIngredientRow(RecipeIngredientResponse prefill) {
        IngredientRow row = new IngredientRow(ingredientOptions, () -> {
            // Remove callback
        });

        if (prefill != null) {
            row.setValues(prefill);
        }

        ingredients.add(row);
        HorizontalLayout rowLayout = row.getLayout();
        rowLayout.addClassName("ingredient-row");

        Button removeButton = new Button(VaadinIcon.CLOSE_SMALL.create(), e -> {
            ingredients.remove(row);
            ingredientRows.remove(rowLayout);
        });
        removeButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        rowLayout.add(removeButton);

        ingredientRows.add(rowLayout);
    }

    private void save() {
        if (nameField.getValue().isBlank()) {
            Notification.show("Name is required", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (categoryField.getValue() == null) {
            Notification.show("Category is required", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }
        if (difficultyField.getValue() == null) {
            Notification.show("Difficulty is required", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        Map<String, Object> input = new HashMap<>();
        input.put("name", nameField.getValue());
        input.put("description", descriptionField.getValue());
        input.put("category", categoryField.getValue());
        input.put("difficulty", difficultyField.getValue());
        input.put("prepTime", prepTimeField.getValue());
        input.put("cookTime", cookTimeField.getValue());
        input.put("servings", servingsField.getValue());
        input.put("instructions", instructionsField.getValue());
        input.put("imageUrl", currentImageUrl != null ? currentImageUrl : "");

        List<Map<String, Object>> ingredientInputs = new ArrayList<>();
        for (IngredientRow row : ingredients) {
            Map<String, Object> ri = row.toMap();
            if (ri != null) {
                ingredientInputs.add(ri);
            }
        }
        input.put("ingredients", ingredientInputs);

        try {
            if (existing != null) {
                recipeService.updateRecipe(existing.id(), input);
            } else {
                recipeService.createRecipe(input);
            }
            Notification.show("Recipe saved", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            onSave.run();
            close();
        } catch (Exception e) {
            Notification.show("Failed to save: " + e.getMessage(), 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private static class IngredientRow {
        private final ComboBox<IngredientOption> ingredientCombo = new ComboBox<>("Ingredient");
        private final NumberField quantityField = new NumberField("Quantity");
        private final ComboBox<String> unitCombo = new ComboBox<>("Unit");
        private final HorizontalLayout layout;

        IngredientRow(List<IngredientOption> options, Runnable onRemove) {
            ingredientCombo.setItems(options);
            ingredientCombo.setItemLabelGenerator(o -> o.name() + " (" + o.category() + ")");
            ingredientCombo.setWidthFull();

            quantityField.setMin(0);
            quantityField.setValue(100.0);
            quantityField.setWidth("120px");

            unitCombo.setItems("GRAMS", "KILOGRAMS", "MILLILITERS", "LITERS", "PIECES", "TABLESPOONS", "TEASPOONS", "CUPS");
            unitCombo.setValue("GRAMS");
            unitCombo.setWidth("150px");

            layout = new HorizontalLayout(ingredientCombo, quantityField, unitCombo);
            layout.setAlignItems(FlexComponent.Alignment.BASELINE);
            layout.setWidthFull();
            layout.expand(ingredientCombo);
        }

        void setValues(RecipeIngredientResponse ri) {
            ingredientCombo.getListDataView().getItems()
                    .filter(o -> o.id().equals(ri.ingredientId()))
                    .findFirst()
                    .ifPresent(ingredientCombo::setValue);
            quantityField.setValue(ri.quantity());
            unitCombo.setValue(ri.unit());
        }

        HorizontalLayout getLayout() {
            return layout;
        }

        Map<String, Object> toMap() {
            IngredientOption selected = ingredientCombo.getValue();
            if (selected == null || quantityField.getValue() == null || unitCombo.getValue() == null) {
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("ingredientId", selected.id());
            map.put("quantity", quantityField.getValue());
            map.put("unit", unitCombo.getValue());
            return map;
        }
    }
}
