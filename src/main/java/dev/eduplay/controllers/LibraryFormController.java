package dev.eduplay.controllers;

import dev.eduplay.entities.Library;
import dev.eduplay.services.LibraryService;
import dev.eduplay.tools.ImageLoader;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class LibraryFormController {

    @FXML private Label    headerTitle;
    @FXML private Label    headerSubtitle;
    @FXML private Button   submitBtn;

    @FXML private TextField  nameField;
    @FXML private TextArea   descriptionField;
    @FXML private TextField  minAgeField;
    @FXML private TextField  maxAgeField;
    @FXML private ComboBox<String> levelCombo;
    @FXML private TextField  themeField;
    @FXML private TextField  coverImageField;   // stocke le chemin COMPLET

    // Preview de l'image choisie
    @FXML private VBox       imagePreviewBox;   // fx:id dans le FXML
    @FXML private ImageView  imagePreview;       // fx:id dans le FXML

    // Labels d'erreur
    @FXML private Label nameError;
    @FXML private Label minAgeError;
    @FXML private Label maxAgeError;
    @FXML private Label levelError;
    @FXML private Label themeError;
    @FXML private Label imageError;
    @FXML private Label descriptionError;

    // Flash
    @FXML private HBox  flashBox;
    @FXML private Label flashLabel;

    private final LibraryService service = new LibraryService();
    private Library libraryToEdit = null;
    private Runnable onSaveCallback;

    // Chemin complet du fichier sélectionné
    private String selectedImagePath = null;

    @FXML
    public void initialize() {
        levelCombo.setItems(FXCollections.observableArrayList(
                "Beginner", "Intermediate", "Advanced"
        ));

        // Effacer erreurs en temps réel
        nameField.textProperty().addListener((o, ov, nv) -> hideError(nameError));
        descriptionField.textProperty().addListener((o, ov, nv) -> hideError(descriptionError));
        minAgeField.textProperty().addListener((o, ov, nv) -> hideError(minAgeError));
        maxAgeField.textProperty().addListener((o, ov, nv) -> hideError(maxAgeError));
        levelCombo.valueProperty().addListener((o, ov, nv) -> hideError(levelError));
        themeField.textProperty().addListener((o, ov, nv) -> hideError(themeError));

        Object data = dev.eduplay.core.Router.getTransitData();
        if (data instanceof Library) {
            setLibraryToEdit((Library) data);
        } else {
            setLibraryToEdit(null);
        }
    }

    public void setLibraryToEdit(Library lib) {
        this.libraryToEdit = lib;
        if (lib != null) {
            headerTitle.setText("📚 Modifier la bibliothèque");
            headerSubtitle.setText("Modifiez les informations ci-dessous");
            submitBtn.setText("💾  Mettre à jour");

            nameField.setText(lib.getName());
            descriptionField.setText(lib.getDescription() != null ? lib.getDescription() : "");
            minAgeField.setText(String.valueOf(lib.getMinAge()));
            maxAgeField.setText(String.valueOf(lib.getMaxAge()));
            levelCombo.setValue(lib.getLevel());
            themeField.setText(lib.getTheme());

            // Afficher l'image existante
            if (lib.getCoverImage() != null && !lib.getCoverImage().isBlank()) {
                selectedImagePath = lib.getCoverImage();
                coverImageField.setText(lib.getCoverImage());
                showImagePreview(lib.getCoverImage());
            }
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    // ── Choisir une image ────────────────────────────────────────

    @FXML
    private void handleChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir une image de couverture");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp", "*.bmp")
        );

        // Ouvrir dans le dossier Images de l'utilisateur par défaut
        File initialDir = new File(System.getProperty("user.home") + "/Pictures");
        if (!initialDir.exists()) initialDir = new File(System.getProperty("user.home"));
        chooser.setInitialDirectory(initialDir);

        File file = chooser.showOpenDialog(coverImageField.getScene().getWindow());
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();  // chemin COMPLET
            coverImageField.setText(selectedImagePath);
            showImagePreview(selectedImagePath);
        }
    }

    private void showImagePreview(String path) {
        Image img = ImageLoader.load(path);
        if (img != null) {
            imagePreview.setImage(img);
            imagePreview.setFitWidth(200);
            imagePreview.setFitHeight(120);
            imagePreview.setPreserveRatio(true);
            imagePreview.setSmooth(true);
            imagePreviewBox.setVisible(true);
            imagePreviewBox.setManaged(true);
        } else {
            imagePreviewBox.setVisible(false);
            imagePreviewBox.setManaged(false);
        }
    }

    // ── Soumettre ────────────────────────────────────────────────

    @FXML
    private void handleSubmit() {
        if (!validateForm()) return;

        // On sauvegarde le chemin COMPLET (ou ce qui est dans le champ)
        String imagePath = selectedImagePath != null
                ? selectedImagePath
                : coverImageField.getText().trim();

        Library lib = new Library(
                nameField.getText().trim(),
                descriptionField.getText().trim(),
                imagePath,
                Integer.parseInt(minAgeField.getText().trim()),
                Integer.parseInt(maxAgeField.getText().trim()),
                levelCombo.getValue(),
                themeField.getText().trim()
        );

        if (libraryToEdit == null) {
            service.ajouter(lib);
            showFlash("✅  Bibliothèque ajoutée avec succès !", true);
        } else {
            lib = new Library(
                    libraryToEdit.getId(),
                    lib.getName(), lib.getDescription(), lib.getCoverImage(),
                    lib.getMinAge(), lib.getMaxAge(), lib.getLevel(), lib.getTheme()
            );
            service.modifier(lib);
            showFlash("✏️  Bibliothèque mise à jour avec succès !", true);
        }

        if (onSaveCallback != null) onSaveCallback.run();

        new Thread(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(this::retourIndex);
        }).start();
    }

    @FXML private void handleRetour() { retourIndex(); }

    // ── Validation ───────────────────────────────────────────────

    private boolean validateForm() {
        boolean valid = true;

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError(nameError, "Le nom est obligatoire."); valid = false;
        } else {
            if (name.length() < 3 || name.length() > 20) {
                showError(nameError, "Entre 3 et 20 caractères."); valid = false;
            } else if (!name.matches("^[a-zA-ZÀ-ÿ0-9\\s\\-_']+$")) {
                showError(nameError, "Caractères spéciaux non autorisés."); valid = false;
            } else if (libraryToEdit == null && service.existsByName(name)) {
                showError(nameError, "Une bibliothèque avec ce nom existe déjà."); valid = false;
            }
        }

        String desc = descriptionField.getText().trim();
        if (desc.isEmpty()) {
            showError(descriptionError, "La description est obligatoire."); valid = false;
        } else if (desc.length() > 100) {
            showError(descriptionError, "Maximum 100 caractères."); valid = false;
        }

        try {
            int min = Integer.parseInt(minAgeField.getText().trim());
            if (min < 0) { showError(minAgeError, "L'âge ne peut pas être négatif."); valid = false; }
        } catch (NumberFormatException e) {
            showError(minAgeError, "Nombre invalide."); valid = false;
        }

        try {
            int max = Integer.parseInt(maxAgeField.getText().trim());
            if (max > 18) { showError(maxAgeError, "Maximum 18 ans."); valid = false; }
        } catch (NumberFormatException e) {
            showError(maxAgeError, "Nombre invalide."); valid = false;
        }

        try {
            int min = Integer.parseInt(minAgeField.getText().trim());
            int max = Integer.parseInt(maxAgeField.getText().trim());
            if (min >= max) { showError(maxAgeError, "Max doit être > Min."); valid = false; }
        } catch (NumberFormatException ignored) {}

        if (levelCombo.getValue() == null) {
            showError(levelError, "Sélectionnez un niveau."); valid = false;
        }

        String theme = themeField.getText().trim();
        if (theme.isEmpty()) {
            showError(themeError, "Le thème est obligatoire."); valid = false;
        } else {
            if (theme.length() < 2 || theme.length() > 20) {
                showError(themeError, "Entre 2 et 20 caractères."); valid = false;
            } else if (!theme.matches("^[a-zA-ZÀ-ÿ0-9\\s\\-_,]+$")) {
                showError(themeError, "Caractères spéciaux non autorisés."); valid = false;
            }
        }

        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            String ext = selectedImagePath.toLowerCase();
            if (!ext.endsWith(".png") && !ext.endsWith(".jpg") && !ext.endsWith(".jpeg")) {
                showError(imageError, "Format accepté : PNG, JPG, JPEG."); valid = false;
            }
        } else if (libraryToEdit == null) {
            showError(imageError, "L'image de couverture est requise."); valid = false;
        }

        return valid;
    }

    private void showError(Label label, String msg) {
        label.setText("⚠️ " + msg);
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
        dev.eduplay.core.Router.reload("library_index");
    }
}