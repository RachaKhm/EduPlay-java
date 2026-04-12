package dev.eduplay.controllers;

import dev.eduplay.entities.Library;
import dev.eduplay.services.LibraryService;
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
import java.io.IOException;

public class LibraryShowController {

    @FXML private Label   libraryNameLabel;
    @FXML private Label   ageBadgeLabel;
    @FXML private Label   levelBadgeLabel;
    @FXML private Label   themeBadgeLabel;
    @FXML private VBox    imageContainer;
    @FXML private ImageView coverImageView;
    @FXML private Label   descriptionLabel;
    @FXML private Label   ageRangeLabel;
    @FXML private Label   minAgeBarLabel;
    @FXML private Label   maxAgeBarLabel;
    @FXML private Region  ageProgressBar;
    @FXML private Label   levelDetailLabel;
    @FXML private Label   levelEmojiLabel;
    @FXML private Label   themeDetailLabel;

    private Library library;
    private final LibraryService service = new LibraryService();

    public void setLibrary(Library lib) {
        this.library = lib;
        populate();
    }

    private void populate() {
        libraryNameLabel.setText(library.getName());
        ageBadgeLabel.setText("\u23f1  " + library.getMinAge() + " - " + library.getMaxAge() + " ans");
        levelBadgeLabel.setText(library.getLevel());
        levelBadgeLabel.setStyle(getLevelBadgeStyle(library.getLevel()));
        themeBadgeLabel.setText(library.getTheme());

        // Image universelle
        Image img = ImageLoader.load(library.getCoverImage());
        if (img != null) {
            coverImageView.setImage(img);
            coverImageView.setFitWidth(980);
            coverImageView.setFitHeight(220);
            coverImageView.setPreserveRatio(false);
            coverImageView.setSmooth(true);
            imageContainer.setVisible(true);
            imageContainer.setManaged(true);
        } else {
            imageContainer.setVisible(false);
            imageContainer.setManaged(false);
        }

        String desc = library.getDescription();
        descriptionLabel.setText((desc != null && !desc.isBlank()) ? desc : "Aucune description disponible.");

        ageRangeLabel.setText(library.getMinAge() + " - " + library.getMaxAge() + " ans");
        minAgeBarLabel.setText(library.getMinAge() + " ans");
        maxAgeBarLabel.setText(library.getMaxAge() + " ans");

        double ratio = (double)(library.getMaxAge() - library.getMinAge()) / 18.0;
        ageProgressBar.setPrefWidth(Math.min(260.0, Math.max(20.0, 260.0 * ratio)));

        levelDetailLabel.setText(library.getLevel());
        levelEmojiLabel.setText(getLevelEmoji(library.getLevel()));
        themeDetailLabel.setText(library.getTheme());
    }

    @FXML private void handleRetour() {
        navigateTo("/LibraryIndex.fxml", 1050, 700, "EduPlay");
    }

    @FXML private void handleModifier() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LibraryForm.fxml"));
            Parent root = loader.load();
            LibraryFormController ctrl = loader.getController();
            ctrl.setLibraryToEdit(library);
            ctrl.setOnSaveCallback(() -> {});
            Stage stage = (Stage) libraryNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 680));
            stage.setTitle("Modifier : " + library.getName());
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleSupprimer() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer \u00ab " + library.getName() + " \u00bb ?");
        confirm.setContentText("Cette action est irr\u00e9versible.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                service.supprimer(library.getId());
                navigateTo("/LibraryIndex.fxml", 1050, 700, "EduPlay");
            }
        });
    }

    private void navigateTo(String fxml, double w, double h, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) libraryNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root, w, h));
            stage.setTitle(title);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private String getLevelBadgeStyle(String level) {
        String base = "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;" +
                "-fx-background-radius: 20; -fx-padding: 5 14;";
        return switch (level == null ? "" : level.toLowerCase()) {
            case "beginner"     -> base + "-fx-background-color: linear-gradient(to right,#10b981,#059669);";
            case "intermediate" -> base + "-fx-background-color: linear-gradient(to right,#f59e0b,#d97706);";
            case "advanced"     -> base + "-fx-background-color: linear-gradient(to right,#ef4444,#dc2626);";
            case "expert"       -> base + "-fx-background-color: linear-gradient(to right,#ec4899,#be185d);";
            default -> base + "-fx-background-color: linear-gradient(to right,#6366f1,#4f46e5);";
        };
    }

    private String getLevelEmoji(String level) {
        return switch (level == null ? "" : level.toLowerCase()) {
            case "beginner"     -> "\ud83c\udf31";
            case "intermediate" -> "\u26a1";
            case "advanced"     -> "\ud83d\udd25";
            case "expert"       -> "\ud83c\udfc6";
            default -> "\ud83d\udcda";
        };
    }
}