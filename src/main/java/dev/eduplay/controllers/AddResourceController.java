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
        System.out.println("AddResourceController initialisé");
        service = new EventResourceService();

        // Initialisation du ComboBox
        typeCombo.getItems().addAll("VIDEO", "DOCUMENT", "LIEN", "CHECKLIST", "PLANNING");
        typeCombo.setValue("VIDEO");

        setupActions();
        setupTypeListener();
        setupValidation();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setEventId(int eventId, String eventTitle) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        eventTitleLabel.setText("Pour : " + eventTitle);
    }

    // ✅ Méthode pour la modification
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

    // ✅ Afficher/Masquer les champs selon le type
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

    // ✅ Contrôles de saisie
    private void setupValidation() {
        // Le titre ne peut pas être vide
        titleField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                titleField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                titleField.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
            }
        });

        // Validation du champ fichier
        filePathField.textProperty().addListener((obs, old, newVal) -> {
            if ("DOCUMENT".equals(typeCombo.getValue())) {
                if (newVal == null || newVal.trim().isEmpty()) {
                    filePathField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
                } else {
                    filePathField.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
                }
            }
        });

        // Validation du champ URL
        urlField.textProperty().addListener((obs, old, newVal) -> {
            String type = typeCombo.getValue();
            if ("LIEN".equals(type) || "VIDEO".equals(type)) {
                if (newVal == null || newVal.trim().isEmpty()) {
                    urlField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
                } else if (!isValidUrl(newVal)) {
                    urlField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
                } else {
                    urlField.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
                }
            }
        });
    }

    // ✅ Vérifier si l'URL est valide
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

    private void saveResource() {
        String type = typeCombo.getValue();
        String title = titleField.getText().trim();
        String context = contextArea.getText().trim();
        String filePath = filePathField.getText().trim();
        String url = urlField.getText().trim();

        // ✅ CONTROLES DE SAISIE
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

        if ("DOCUMENT".equals(type) && filePath.isEmpty()) {
            showError("Veuillez sélectionner un fichier");
            return;
        }

        if (("LIEN".equals(type) || "VIDEO".equals(type)) && url.isEmpty()) {
            showError("Veuillez saisir une URL");
            return;
        }

        if (("LIEN".equals(type) || "VIDEO".equals(type)) && !isValidUrl(url)) {
            showError("Veuillez saisir une URL valide (ex: https://example.com)");
            urlField.requestFocus();
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

            // Retour à la liste après succès
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