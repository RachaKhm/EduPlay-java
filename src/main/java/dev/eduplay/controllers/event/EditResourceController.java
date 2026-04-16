package dev.eduplay.controllers.event;

import dev.eduplay.core.Router;
import dev.eduplay.entities.EventResource;
import dev.eduplay.services.EventResourceService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;

public class EditResourceController {

    @FXML private Button cancelBtn;
    @FXML private Button submitBtn;
    @FXML private Label eventTitleLabel;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField titleField;
    @FXML private TextArea contextArea;
    @FXML private VBox filePathBox;
    @FXML private VBox urlBox;
    @FXML private TextField filePathField;
    @FXML private TextField urlField;
    @FXML private Button browseFileBtn;
    @FXML private Label messageLabel;

    private EventResourceService service;
    private int eventId;
    private String eventTitle;
    private EventResource currentResource;

    @FXML
    public void initialize() {
        System.out.println("EditResourceController initialisé");
        service = new EventResourceService();

        typeCombo.getItems().addAll("VIDEO", "DOCUMENT", "LIEN", "CHECKLIST", "PLANNING");

        setupActions();
        setupTypeListener();
        setupValidation();
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
        System.out.println("setEventId reçu: " + eventId);
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
        System.out.println("setEventTitle reçu: " + eventTitle);
        eventTitleLabel.setText("Pour : " + eventTitle);
    }

    public void setResource(EventResource resource) {
        this.currentResource = resource;
        System.out.println("setResource reçu: " + resource.getTitle());
        loadResourceData();
    }

    private void loadResourceData() {
        if (currentResource == null) return;

        typeCombo.setValue(currentResource.getType());
        titleField.setText(currentResource.getTitle());
        contextArea.setText(currentResource.getContext());

        if (currentResource.getFilePath() != null && !currentResource.getFilePath().isEmpty()) {
            filePathField.setText(currentResource.getFilePath());
        }
        if (currentResource.getUrl() != null && !currentResource.getUrl().isEmpty()) {
            urlField.setText(currentResource.getUrl());
        }

        updateFieldsVisibility();
    }

    private void setupActions() {
        cancelBtn.setOnAction(e -> goBack());
        submitBtn.setOnAction(e -> updateResource());
        browseFileBtn.setOnAction(e -> browseFile());
    }

    private void setupTypeListener() {
        typeCombo.valueProperty().addListener((obs, old, newVal) -> {
            updateFieldsVisibility();
        });
    }

    private void updateFieldsVisibility() {
        String type = typeCombo.getValue();

        filePathBox.setVisible(false);
        filePathBox.setManaged(false);
        urlBox.setVisible(false);
        urlBox.setManaged(false);

        if ("DOCUMENT".equals(type)) {
            filePathBox.setVisible(true);
            filePathBox.setManaged(true);
        } else if ("LIEN".equals(type) || "VIDEO".equals(type)) {
            urlBox.setVisible(true);
            urlBox.setManaged(true);
        }
    }

    private void setupValidation() {
        titleField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                titleField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                titleField.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
            }
        });
    }

    private boolean isValidUrl(String url) {
        return url.matches("^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$");
    }

    private void browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier");

        FileChooser.ExtensionFilter allFiles = new FileChooser.ExtensionFilter("Tous les fichiers", "*.*");
        FileChooser.ExtensionFilter pdfFiles = new FileChooser.ExtensionFilter("PDF", "*.pdf");
        FileChooser.ExtensionFilter imageFiles = new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg");
        FileChooser.ExtensionFilter docFiles = new FileChooser.ExtensionFilter("Documents", "*.doc", "*.docx", "*.txt");

        fileChooser.getExtensionFilters().addAll(pdfFiles, imageFiles, docFiles, allFiles);

        Stage stage = (Stage) browseFileBtn.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            filePathField.setText(file.getAbsolutePath());
        }
    }

    private void updateResource() {
        String type = typeCombo.getValue();
        String title = titleField.getText().trim();
        String context = contextArea.getText().trim();
        String filePath = filePathField.getText().trim();
        String url = urlField.getText().trim();

        if (type == null || type.isEmpty()) {
            showError("Veuillez sélectionner un type");
            return;
        }

        if (title.isEmpty()) {
            showError("Le titre est obligatoire");
            titleField.requestFocus();
            return;
        }

        if (title.length() < 3) {
            showError("Le titre doit contenir au moins 3 caractères");
            titleField.requestFocus();
            return;
        }

        try {
            currentResource.setType(type);
            currentResource.setTitle(title);
            currentResource.setContext(context.isEmpty() ? null : context);
            currentResource.setFilePath(filePath.isEmpty() ? null : filePath);
            currentResource.setUrl(url.isEmpty() ? null : url);

            service.modifier(currentResource);

            showSuccess("✅ Ressource modifiée avec succès !");

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        Router.go("event_resource", eventId, eventTitle);
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            showError("Erreur base de données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void goBack() {
        Router.go("event_resource", eventId, eventTitle);
    }

    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        messageLabel.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> messageLabel.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showSuccess(String message) {
        messageLabel.setText("✅ " + message);
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        messageLabel.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> messageLabel.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}