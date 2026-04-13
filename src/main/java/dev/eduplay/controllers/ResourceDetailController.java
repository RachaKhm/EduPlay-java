package dev.eduplay.controllers;

import dev.eduplay.entities.EventResource;
import dev.eduplay.services.EventResourceService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class ResourceDetailController {

    @FXML private Button backBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button openUrlBtn;
    @FXML private Button openFileBtn;
    @FXML private Label eventInfoLabel;
    @FXML private Label titleValue;
    @FXML private Label typeValue;
    @FXML private Label createdAtValue;
    @FXML private TextArea contextValue;
    @FXML private Label urlValue;
    @FXML private Label filePathValue;
    @FXML private VBox urlBox;
    @FXML private VBox filePathBox;

    private EventResourceService service;
    private MainController mainController;
    private int eventId;
    private String eventTitle;
    private EventResource currentResource;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        service = new EventResourceService();
        setupActions();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setEventInfo(int eventId, String eventTitle) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        eventInfoLabel.setText("Événement : " + eventTitle);
    }

    public void setResource(EventResource resource) {
        this.currentResource = resource;
        displayResourceDetails();
    }

    private void setupActions() {
        backBtn.setOnAction(e -> goBack());
        editBtn.setOnAction(e -> goToEdit());
        deleteBtn.setOnAction(e -> deleteResource());
        openUrlBtn.setOnAction(e -> openUrl());
        openFileBtn.setOnAction(e -> openFile());
    }

    private void displayResourceDetails() {
        if (currentResource == null) return;

        titleValue.setText(currentResource.getTitle());

        // Style du type avec badge
        String type = currentResource.getType();
        String typeStyle = "";
        switch (type) {
            case "VIDEO":
                typeStyle = "-fx-text-fill: #ef4444;";
                break;
            case "DOCUMENT":
                typeStyle = "-fx-text-fill: #3b82f6;";
                break;
            case "LIEN":
                typeStyle = "-fx-text-fill: #10b981;";
                break;
            case "CHECKLIST":
                typeStyle = "-fx-text-fill: #f59e0b;";
                break;
            case "PLANNING":
                typeStyle = "-fx-text-fill: #8b5cf6;";
                break;
        }
        typeValue.setStyle(typeStyle + " -fx-font-weight: bold; -fx-font-size: 14px;");
        typeValue.setText(type);

        if (currentResource.getCreatedAt() != null) {
            createdAtValue.setText(currentResource.getCreatedAt().format(dateFormatter));
        }

        contextValue.setText(currentResource.getContext() != null ? currentResource.getContext() : "Aucune description");

        // Gestion de l'URL
        if (currentResource.getUrl() != null && !currentResource.getUrl().isEmpty()) {
            urlValue.setText(currentResource.getUrl());
            urlBox.setVisible(true);
            urlBox.setManaged(true);
        } else {
            urlBox.setVisible(false);
            urlBox.setManaged(false);
        }

        // Gestion du fichier
        if (currentResource.getFilePath() != null && !currentResource.getFilePath().isEmpty()) {
            filePathValue.setText(currentResource.getFilePath());
            filePathBox.setVisible(true);
            filePathBox.setManaged(true);
        } else {
            filePathBox.setVisible(false);
            filePathBox.setManaged(false);
        }
    }

    private void openUrl() {
        String url = urlValue.getText();
        if (url != null && !url.isEmpty()) {
            try {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                Desktop.getDesktop().browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                showAlert("Erreur", "Impossible d'ouvrir l'URL: " + e.getMessage());
            }
        }
    }

    private void openFile() {
        String filePath = filePathValue.getText();
        if (filePath != null && !filePath.isEmpty()) {
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                } else {
                    showAlert("Erreur", "Le fichier n'existe pas: " + filePath);
                }
            } catch (IOException e) {
                showAlert("Erreur", "Impossible d'ouvrir le fichier: " + e.getMessage());
            }
        }
    }

    private void goBack() {
        if (mainController != null) {
            mainController.goToEventResources(eventId, eventTitle);
        }
    }

    private void goToEdit() {
        if (mainController != null && currentResource != null) {
            mainController.goToEditResource(eventId, eventTitle, currentResource);
        }
    }

    private void deleteResource() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la ressource");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer la ressource \"" + currentResource.getTitle() + "\" ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                service.supprimer(currentResource);
                showAlert("Succès", "Ressource supprimée avec succès");
                goBack();
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer la ressource: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}