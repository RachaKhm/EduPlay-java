package dev.eduplay.controllers;

import dev.eduplay.entities.Library;
import dev.eduplay.services.LibraryService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class LibraryFormController {

    @FXML private Label headerTitle;
    @FXML private Label headerSubtitle;
    @FXML private Button submitBtn;

    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField minAgeField;
    @FXML private TextField maxAgeField;
    @FXML private ComboBox<String> levelCombo;
    @FXML private TextField themeField;
    @FXML private TextField coverImageField;

    // Labels d'erreur
    @FXML private Label nameError;
    @FXML private Label minAgeError;
    @FXML private Label maxAgeError;
    @FXML private Label levelError;
    @FXML private Label themeError;

    // Flash message
    @FXML private HBox flashBox;
    @FXML private Label flashLabel;

    private final LibraryService service = new LibraryService();
    private Library libraryToEdit = null;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        levelCombo.setItems(FXCollections.observableArrayList(
                "Beginner", "Intermediate", "Advanced"
        ));

        // Listeners pour effacer les erreurs en temps réel
        nameField.textProperty().addListener((o, ov, nv) -> hideError(nameError));
        minAgeField.textProperty().addListener((o, ov, nv) -> hideError(minAgeError));
        maxAgeField.textProperty().addListener((o, ov, nv) -> hideError(maxAgeError));
        levelCombo.valueProperty().addListener((o, ov, nv) -> hideError(levelError));
        themeField.textProperty().addListener((o, ov, nv) -> hideError(themeError));
    }

    /**
     * Appeler cette méthode APRÈS le load() pour passer la bibliothèque à modifier.
     * Passe null pour un ajout.
     */
    public void setLibraryToEdit(Library lib) {
        this.libraryToEdit = lib;
        if (lib != null) {
            // Mode modification
            headerTitle.setText("📚 Modifier la bibliothèque");
            headerSubtitle.setText("Modifiez les informations ci-dessous");
            submitBtn.setText("💾  Mettre à jour");

            nameField.setText(lib.getName());
            descriptionField.setText(lib.getDescription());
            minAgeField.setText(String.valueOf(lib.getMinAge()));
            maxAgeField.setText(String.valueOf(lib.getMaxAge()));
            levelCombo.setValue(lib.getLevel());
            themeField.setText(lib.getTheme());
            coverImageField.setText(lib.getCoverImage() != null ? lib.getCoverImage() : "");
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    @FXML
    private void handleSubmit() {
        if (!validateForm()) return;

        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();
        String coverImage = coverImageField.getText().trim();
        int minAge = Integer.parseInt(minAgeField.getText().trim());
        int maxAge = Integer.parseInt(maxAgeField.getText().trim());
        String level = levelCombo.getValue();
        String theme = themeField.getText().trim();

        if (libraryToEdit == null) {
            // AJOUT
            service.ajouter(new Library(name, description, coverImage, minAge, maxAge, level, theme));
            showFlash("✅  Bibliothèque ajoutée avec succès !", true);
        } else {
            // MODIFICATION
            service.modifier(new Library(libraryToEdit.getId(), name, description, coverImage, minAge, maxAge, level, theme));
            showFlash("✏️  Bibliothèque mise à jour avec succès !", true);
        }

        // Refresh callback + retour après 1.5s
        if (onSaveCallback != null) onSaveCallback.run();
        new Thread(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(this::retourIndex);
        }).start();
    }

    @FXML
    private void handleRetour() {
        retourIndex();
    }

    @FXML
    private void handleChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image de couverture");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );
        File file = chooser.showOpenDialog(coverImageField.getScene().getWindow());
        if (file != null) {
            coverImageField.setText(file.getName());
        }
    }

    // ── Validation ────────────────────────────────────────────

    private boolean validateForm() {
        boolean valid = true;

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError(nameError, "Le nom est obligatoire.");
            valid = false;
        } else if (name.length() < 3 || name.length() > 20) {
            showError(nameError, "Le nom doit contenir entre 3 et 20 caractères.");
            valid = false;
        }

        try {
            int min = Integer.parseInt(minAgeField.getText().trim());
            if (min < 3) {
                showError(minAgeError, "L'âge minimum doit être au moins 3 ans.");
                valid = false;
            }
        } catch (NumberFormatException e) {
            showError(minAgeError, "Veuillez entrer un nombre valide.");
            valid = false;
        }

        try {
            int max = Integer.parseInt(maxAgeField.getText().trim());
            if (max > 18) {
                showError(maxAgeError, "L'âge maximum ne doit pas dépasser 18 ans.");
                valid = false;
            }
        } catch (NumberFormatException e) {
            showError(maxAgeError, "Veuillez entrer un nombre valide.");
            valid = false;
        }

        if (!minAgeField.getText().trim().isEmpty() && !maxAgeField.getText().trim().isEmpty()) {
            try {
                int min = Integer.parseInt(minAgeField.getText().trim());
                int max = Integer.parseInt(maxAgeField.getText().trim());
                if (min >= max) {
                    showError(maxAgeError, "L'âge maximum doit être supérieur à l'âge minimum.");
                    valid = false;
                }
            } catch (NumberFormatException ignored) {}
        }

        if (levelCombo.getValue() == null) {
            showError(levelError, "Veuillez sélectionner un niveau.");
            valid = false;
        }

        String theme = themeField.getText().trim();
        if (theme.isEmpty()) {
            showError(themeError, "Le thème est obligatoire.");
            valid = false;
        } else if (theme.length() < 2 || theme.length() > 20) {
            showError(themeError, "Le thème doit contenir entre 2 et 20 caractères.");
            valid = false;
        }

        return valid;
    }

    private void showError(Label label, String message) {
        label.setText("⚠️ " + message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void hideError(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }

    private void showFlash(String message, boolean success) {
        flashLabel.setText(message);
        flashBox.setStyle(success
                ? "-fx-background-color: #f0fdf4; -fx-padding: 10 32; -fx-border-color: #22c55e; -fx-border-width: 0 0 0 4;"
                : "-fx-background-color: #fef2f2; -fx-padding: 10 32; -fx-border-color: #ef4444; -fx-border-width: 0 0 0 4;");
        flashLabel.setStyle(success
                ? "-fx-text-fill: #15803d; -fx-font-weight: bold; -fx-font-size: 13px;"
                : "-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 13px;");
        flashBox.setVisible(true);
        flashBox.setManaged(true);
    }

    private void retourIndex() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LibraryIndex.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(new Scene(root, 1050, 700));
            stage.setTitle("EduPlay – Gestion des Bibliothèques");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
