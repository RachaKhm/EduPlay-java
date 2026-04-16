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

    @FXML
    public void initialize() {
        Object data = dev.eduplay.core.Router.getTransitData();
        if (data instanceof Library) {
            setLibrary((Library) data);
        }
    }

    @FXML private void handleRetour() {
        dev.eduplay.core.Router.reload("library_index");
    }

    @FXML private void handleModifier() {
        dev.eduplay.core.Router.reload("library_form", library);
    }

    @FXML private void handleSupprimer() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer cette bibliotheque ?");

        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                service.supprimer(library.getId());
                dev.eduplay.core.Router.reload("library_index");
            }
        });
    }

    private String getLevelBadgeStyle(String level) {
        String base = "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;" +
                "-fx-background-radius: 20; -fx-padding: 5 14;";

        switch (level.toLowerCase()) {
            case "debutant":
                return base + "-fx-background-color: #22c55e;";
            case "intermediaire":
                return base + "-fx-background-color: #f59e0b;";
            case "avance":
                return base + "-fx-background-color: #ef4444;";
            default:
                return base + "-fx-background-color: #64748b;";
        }
    }

    private String getLevelEmoji(String level) {
        switch (level.toLowerCase()) {
            case "debutant": return "🌱";
            case "intermediaire": return "⭐";
            case "avance": return "🔥";
            default: return "📌";
        }
    }
}