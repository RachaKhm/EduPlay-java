package dev.eduplay.controllers;

import dev.eduplay.entities.Book;
import dev.eduplay.entities.Library;
import dev.eduplay.services.BookService;
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
import java.util.List;

public class BookFormController {

    @FXML private Label    headerTitle, headerSubtitle;
    @FXML private Button   submitBtn;
    @FXML private TextField titleField, authorField, minAgeField, maxAgeField, coverImageField, pdfFileField;
    @FXML private TextArea  summaryField;
    @FXML private ComboBox<String>  typeCombo, languageCombo;
    @FXML private ComboBox<String>  libraryCombo;   // affiche "Nom (id)"
    @FXML private ImageView imagePreview;
    @FXML private VBox      imagePreviewBox, pdfExistingBox;
    @FXML private Label titleError, authorError, typeError, languageError,
            minAgeError, maxAgeError, libraryError;
    @FXML private HBox  flashBox;
    @FXML private Label flashLabel;

    private final BookService    bookService    = new BookService();
    private final LibraryService libraryService = new LibraryService();

    private Book   bookToEdit        = null;
    private Runnable onSaveCallback  = null;
    private String selectedImagePath = null;
    private String selectedPdfPath   = null;
    private List<Library> libraries;

    @FXML
    public void initialize() {
        typeCombo.setItems(FXCollections.observableArrayList("Livre","Magazine","Journal","Article"));
        languageCombo.setItems(FXCollections.observableArrayList("Français","Anglais","Arabe","Espagnol","Autre"));

        // Charger les bibliothèques
        libraries = libraryService.afficher();
        libraryCombo.setItems(FXCollections.observableArrayList(
                libraries.stream().map(l -> l.getName() + " (id:" + l.getId() + ")").toList()
        ));

        // Clear errors on change
        titleField.textProperty().addListener((o,v,n) -> hideError(titleError));
        authorField.textProperty().addListener((o,v,n) -> hideError(authorError));
        minAgeField.textProperty().addListener((o,v,n) -> hideError(minAgeError));
        maxAgeField.textProperty().addListener((o,v,n) -> hideError(maxAgeError));
        typeCombo.valueProperty().addListener((o,v,n) -> hideError(typeError));
        languageCombo.valueProperty().addListener((o,v,n) -> hideError(languageError));
        libraryCombo.valueProperty().addListener((o,v,n) -> hideError(libraryError));
    }

    public void setBookToEdit(Book book) {
        this.bookToEdit = book;
        if (book != null) {
            headerTitle.setText("✏️  Modifier la ressource");
            headerSubtitle.setText("Modifiez les informations de « " + book.getTitle() + " »");
            submitBtn.setText("💾  Mettre à jour");

            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            summaryField.setText(book.getSummary() != null ? book.getSummary() : "");
            typeCombo.setValue(book.getType());
            languageCombo.setValue(book.getLanguage());
            minAgeField.setText(String.valueOf(book.getMinAge()));
            maxAgeField.setText(String.valueOf(book.getMaxAge()));

            // Pré-sélectionner la bibliothèque
            libraries.stream()
                    .filter(l -> l.getId() == book.getLibraryId())
                    .findFirst()
                    .ifPresent(l -> libraryCombo.setValue(l.getName() + " (id:" + l.getId() + ")"));

            // Image existante
            if (book.getCoverImage() != null && !book.getCoverImage().isBlank()) {
                selectedImagePath = book.getCoverImage();
                coverImageField.setText(book.getCoverImage());
                showImagePreview(book.getCoverImage());
            }

            // PDF existant
            if (book.getPdfFile() != null && !book.getPdfFile().isBlank()) {
                selectedPdfPath = book.getPdfFile();
                pdfFileField.setText(book.getPdfFile());
                pdfExistingBox.setVisible(true); pdfExistingBox.setManaged(true);
            }
        }
    }

    public void setOnSaveCallback(Runnable r) { this.onSaveCallback = r; }

    @FXML private void handleChooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir une image de couverture");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images","*.png","*.jpg","*.jpeg","*.gif","*.webp"));
        File f = fc.showOpenDialog(coverImageField.getScene().getWindow());
        if (f != null) { selectedImagePath = f.getAbsolutePath(); coverImageField.setText(selectedImagePath); showImagePreview(selectedImagePath); }
    }

    @FXML private void handleChoosePdf() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choisir un fichier PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF","*.pdf"));
        File f = fc.showOpenDialog(pdfFileField.getScene().getWindow());
        if (f != null) { selectedPdfPath = f.getAbsolutePath(); pdfFileField.setText(selectedPdfPath); pdfExistingBox.setVisible(false); pdfExistingBox.setManaged(false); }
    }

    @FXML private void handleSubmit() {
        if (!validateForm()) return;

        int libraryId = getSelectedLibraryId();
        String imgPath = selectedImagePath != null ? selectedImagePath : coverImageField.getText().trim();
        String pdfPath = selectedPdfPath  != null ? selectedPdfPath  : pdfFileField.getText().trim();

        if (bookToEdit == null) {
            bookService.ajouter(new Book(libraryId, titleField.getText().trim(),
                    authorField.getText().trim(), summaryField.getText().trim(),
                    imgPath, pdfPath, typeCombo.getValue(),
                    Integer.parseInt(minAgeField.getText().trim()),
                    Integer.parseInt(maxAgeField.getText().trim()),
                    languageCombo.getValue()));
            showFlash("✅  Ressource ajoutée avec succès !", true);
        } else {
            bookService.modifier(new Book(bookToEdit.getId(), libraryId,
                    titleField.getText().trim(), authorField.getText().trim(),
                    summaryField.getText().trim(), imgPath, pdfPath,
                    typeCombo.getValue(),
                    Integer.parseInt(minAgeField.getText().trim()),
                    Integer.parseInt(maxAgeField.getText().trim()),
                    languageCombo.getValue()));
            showFlash("✏️  Ressource mise à jour avec succès !", true);
        }

        if (onSaveCallback != null) onSaveCallback.run();
        new Thread(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(this::retourIndex);
        }).start();
    }

    @FXML private void handleRetour() { retourIndex(); }

    private void showImagePreview(String path) {
        Image img = ImageLoader.load(path);
        if (img != null) {
            imagePreview.setImage(img);
            imagePreview.setFitWidth(200); imagePreview.setFitHeight(120);
            imagePreview.setPreserveRatio(true); imagePreview.setSmooth(true);
            imagePreviewBox.setVisible(true); imagePreviewBox.setManaged(true);
        }
    }

    private boolean validateForm() {
        boolean ok = true;
        String title = titleField.getText().trim();
        if (title.isEmpty() || title.length() < 3 || title.length() > 100) { showError(titleError, "Titre requis (3-100 car.)"); ok = false; }
        String author = authorField.getText().trim();
        if (author.isEmpty() || author.length() < 3) { showError(authorError, "Auteur requis (min. 3 car.)"); ok = false; }
        if (typeCombo.getValue() == null) { showError(typeError, "Sélectionnez un type."); ok = false; }
        if (languageCombo.getValue() == null) { showError(languageError, "Sélectionnez une langue."); ok = false; }
        if (libraryCombo.getValue() == null) { showError(libraryError, "Sélectionnez une bibliothèque."); ok = false; }
        try { int v = Integer.parseInt(minAgeField.getText().trim()); if (v < 3) { showError(minAgeError, "Min. 3 ans."); ok = false; } }
        catch (NumberFormatException e) { showError(minAgeError, "Nombre invalide."); ok = false; }
        try { int v = Integer.parseInt(maxAgeField.getText().trim()); if (v > 18) { showError(maxAgeError, "Max. 18 ans."); ok = false; } }
        catch (NumberFormatException e) { showError(maxAgeError, "Nombre invalide."); ok = false; }
        return ok;
    }

    private int getSelectedLibraryId() {
        String val = libraryCombo.getValue();
        if (val == null) return 0;
        int start = val.lastIndexOf("(id:") + 4;
        int end   = val.lastIndexOf(")");
        try { return Integer.parseInt(val.substring(start, end)); }
        catch (Exception e) { return 0; }
    }

    private void showError(Label l, String msg) { l.setText("⚠️ " + msg); l.setVisible(true); l.setManaged(true); }
    private void hideError(Label l) { l.setVisible(false); l.setManaged(false); }

    private void showFlash(String msg, boolean success) {
        flashLabel.setText(msg);
        flashBox.setStyle(success
                ? "-fx-background-color: #f0fdf4; -fx-padding: 10 32; -fx-border-color: #22c55e; -fx-border-width: 0 0 0 4;"
                : "-fx-background-color: #fef2f2; -fx-padding: 10 32; -fx-border-color: #ef4444; -fx-border-width: 0 0 0 4;");
        flashLabel.setStyle(success ? "-fx-text-fill: #15803d; -fx-font-weight: bold; -fx-font-size: 13px;"
                : "-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 13px;");
        flashBox.setVisible(true); flashBox.setManaged(true);
    }

    private void retourIndex() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/BookIndex.fxml"));
            Stage stage = (Stage) titleField.getScene().getWindow();
            stage.setScene(new Scene(root, 1050, 700));
            stage.setTitle("EduPlay – Gestion des Ressources");
        } catch (IOException e) { e.printStackTrace(); }
    }
}
