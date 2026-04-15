package dev.eduplay.controllers;

import dev.eduplay.entities.Book;
import dev.eduplay.services.BookService;
import dev.eduplay.tools.ImageLoader;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class BookShowController {

    @FXML private Label titleLabel, authorLabel;
    @FXML private Label typeBadgeLabel, ageBadgeLabel, languageBadgeLabel;
    @FXML private VBox  imageContainer, imageCardContainer, pdfCard;
    @FXML private ImageView coverImageView, coverImageCard;
    @FXML private Label summaryLabel;
    @FXML private Label authorInfoLabel, ageRangeLabel, minAgeLabel, maxAgeLabel;
    @FXML private Region ageProgressBar;
    @FXML private Label typeInfoLabel, typeEmojiLabel, languageInfoLabel, libraryInfoLabel;

    private Book book;
    private String libraryName;
    private final BookService bookService = new BookService();

    public void setBook(Book b, String libName) {
        this.book = b;
        this.libraryName = libName;
        populate();
    }

    private void populate() {
        titleLabel.setText(book.getTitle());
        authorLabel.setText("par " + book.getAuthor());

        typeBadgeLabel.setText(book.getType());
        typeBadgeLabel.setStyle(getTypeBadgeStyle(book.getType()));
        ageBadgeLabel.setText("⏱  " + book.getMinAge() + " - " + book.getMaxAge() + " ans");
        languageBadgeLabel.setText("🌍  " + book.getLanguage());

        // Image
        Image img = ImageLoader.load(book.getCoverImage());
        if (img != null) {
            coverImageView.setImage(img);
            coverImageView.setFitWidth(980); coverImageView.setFitHeight(240);
            coverImageView.setPreserveRatio(false); coverImageView.setSmooth(true);
            imageContainer.setVisible(true); imageContainer.setManaged(true);

            coverImageCard.setImage(img);
            imageCardContainer.setVisible(true); imageCardContainer.setManaged(true);
        }

        // Résumé
        summaryLabel.setText((book.getSummary() != null && !book.getSummary().isBlank())
                ? book.getSummary() : "Aucun résumé disponible.");

        // Infos
        authorInfoLabel.setText(book.getAuthor());
        ageRangeLabel.setText(book.getMinAge() + " - " + book.getMaxAge() + " ans");
        minAgeLabel.setText(book.getMinAge() + " ans");
        maxAgeLabel.setText(book.getMaxAge() + " ans");
        double ratio = (double)(book.getMaxAge() - book.getMinAge()) / 18.0;
        ageProgressBar.setPrefWidth(Math.min(260.0, Math.max(20.0, 260.0 * ratio)));

        typeInfoLabel.setText(book.getType());
        typeEmojiLabel.setText(getTypeEmoji(book.getType()));
        languageInfoLabel.setText(book.getLanguage());
        libraryInfoLabel.setText(libraryName);

        // PDF
        if (book.getPdfFile() != null && !book.getPdfFile().isBlank()) {
            pdfCard.setVisible(true); pdfCard.setManaged(true);
        }
    }

    @FXML private void handleRetour() { navigateTo("/BookIndex.fxml", 1050, 700, "EduPlay – Ressources"); }

    @FXML private void handleModifier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BookForm.fxml"));
            Parent root = loader.load();
            BookFormController ctrl = loader.getController();
            ctrl.setBookToEdit(book);
            ctrl.setOnSaveCallback(() -> {});
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 750));
            stage.setTitle("Modifier : " + book.getTitle());
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleSupprimer() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer \u00ab " + book.getTitle() + " \u00bb ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                bookService.supprimer(book.getId());
                navigateTo("/BookIndex.fxml", 1050, 700, "EduPlay – Ressources");
            }
        });
    }

    @FXML private void handleOpenPdf() {
        if (book.getPdfFile() == null || book.getPdfFile().isBlank()) return;
        File f = new File(book.getPdfFile());
        if (f.exists()) {
            try { Desktop.getDesktop().open(f); }
            catch (IOException e) { e.printStackTrace(); }
        } else {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("PDF introuvable");
            a.setContentText("Le fichier PDF est introuvable : " + book.getPdfFile());
            a.showAndWait();
        }
    }

    private void navigateTo(String fxml, double w, double h, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) titleLabel.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private String getTypeBadgeStyle(String type) {
        String base = "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 5 14;";
        return switch (type == null ? "" : type) {
            case "Livre"    -> base + "-fx-background-color: linear-gradient(to right,#3b82f6,#6366f1);";
            case "Magazine" -> base + "-fx-background-color: linear-gradient(to right,#10b981,#059669);";
            case "Journal"  -> base + "-fx-background-color: linear-gradient(to right,#f59e0b,#d97706);";
            case "Article"  -> base + "-fx-background-color: linear-gradient(to right,#ec4899,#be185d);";
            default -> base + "-fx-background-color: linear-gradient(to right,#6366f1,#4f46e5);";
        };
    }

    private String getTypeEmoji(String type) {
        return switch (type == null ? "" : type) {
            case "Livre" -> "📖"; case "Magazine" -> "📰";
            case "Journal" -> "📓"; default -> "📄";
        };
    }
}
