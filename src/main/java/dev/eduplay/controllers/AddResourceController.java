package dev.eduplay.controllers;

import dev.eduplay.entities.EventResource;
import dev.eduplay.services.EventResourceService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class AddResourceController {

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
    private MainController mainController;
    private int eventId;
    private String eventTitle;
    private EventResource resourceToModify;
    private boolean isModification = false;

    @FXML
    public void initialize() {
        service = new EventResourceService();
        setupActions();
        setupTypeListener();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setEventId(int eventId, String eventTitle) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        eventTitleLabel.setText("Pour : " + eventTitle);
    }

    // ✅ Méthode pour la modification (reçoit l'objet complet)
    public void setResourceToModify(EventResource resource) {
        this.resourceToModify = resource;
        this.isModification = true;

        eventTitleLabel.setText("✏️ Modification : " + resource.getTitle());
        submitBtn.setText("✅ Mettre à jour");

        typeCombo.setValue(resource.getType());
        titleField.setText(resource.getTitle());
        contextArea.setText(resource.getContext());

        if (resource.getFilePath() != null && !resource.getFilePath().isEmpty()) {
            filePathField.setText(resource.getFilePath());
        }
        if (resource.getUrl() != null && !resource.getUrl().isEmpty()) {
            urlField.setText(resource.getUrl());
        }

        updateFieldsVisibility();
    }

    private void setupActions() {
        cancelBtn.setOnAction(e -> goBack());
        submitBtn.setOnAction(e -> saveResource());
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
        } else if ("LIEN".equals(type)) {
            urlBox.setVisible(true);
            urlBox.setManaged(true);
        }
    }

    private void browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier");

        FileChooser.ExtensionFilter allFiles = new FileChooser.ExtensionFilter("Tous les fichiers", "*.*");
        fileChooser.getExtensionFilters().add(allFiles);

        Stage stage = (Stage) browseFileBtn.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            filePathField.setText(file.getAbsolutePath());
        }
    }

    private void saveResource() {
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
            return;
        }

        if ("DOCUMENT".equals(type) && filePath.isEmpty()) {
            showError("Veuillez sélectionner un fichier");
            return;
        }

        if ("LIEN".equals(type) && url.isEmpty()) {
            showError("Veuillez saisir une URL");
            return;
        }

        try {
            if (isModification && resourceToModify != null) {
                // MODIFICATION
                resourceToModify.setType(type);
                resourceToModify.setTitle(title);
                resourceToModify.setContext(context.isEmpty() ? null : context);
                resourceToModify.setFilePath(filePath.isEmpty() ? null : filePath);
                resourceToModify.setUrl(url.isEmpty() ? null : url);
                service.modifier(resourceToModify);
                showSuccess("✅ Ressource modifiée avec succès !");
            } else {
                // AJOUT
                EventResource resource = new EventResource();
                resource.setType(type);
                resource.setTitle(title);
                resource.setContext(context.isEmpty() ? null : context);
                resource.setFilePath(filePath.isEmpty() ? null : filePath);
                resource.setUrl(url.isEmpty() ? null : url);
                resource.setCreatedAt(LocalDateTime.now());
                service.ajouter(resource, eventId);
                showSuccess("✅ Ressource ajoutée avec succès !");
            }

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        if (mainController != null) {
                            mainController.goToEventResources(eventId, eventTitle);
                        }
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
        if (mainController != null) {
            mainController.goToEventResources(eventId, eventTitle);
        }
    }

    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        messageLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        messageLabel.setText("✅ " + message);
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        messageLabel.setVisible(true);
    }
}