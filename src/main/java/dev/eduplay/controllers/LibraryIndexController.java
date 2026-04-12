package dev.eduplay.controllers;

import dev.eduplay.entities.Library;
import dev.eduplay.services.LibraryService;
import dev.eduplay.tools.ImageLoader;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryIndexController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterLevel;
    @FXML private TextField filterTheme;
    @FXML private Label countLabel;
    @FXML private FlowPane cardsPane;
    @FXML private VBox emptyState;
    @FXML private HBox flashBox;
    @FXML private Label flashLabel;

    private final LibraryService service = new LibraryService();
    private List<Library> allLibraries;

    @FXML
    public void initialize() {
        filterLevel.setItems(FXCollections.observableArrayList(
                "Tous", "Beginner", "Intermediate", "Advanced"
        ));
        filterLevel.setValue("Tous");
        loadLibraries();
    }

    private void loadLibraries() {
        allLibraries = service.afficher();
        renderCards(allLibraries);
    }

    private void renderCards(List<Library> list) {
        cardsPane.getChildren().clear();
        if (list.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            cardsPane.setVisible(false);
            countLabel.setText("");
        } else {
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            cardsPane.setVisible(true);
            int n = list.size();
            countLabel.setText(n + " bibliotheque" + (n > 1 ? "s" : "") + " trouvee" + (n > 1 ? "s" : ""));
            for (Library lib : list) cardsPane.getChildren().add(createCard(lib));
        }
    }

    private VBox createCard(Library lib) {
        VBox card = new VBox();
        card.setPrefWidth(290);
        card.setMaxWidth(290);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16;" +
                "-fx-border-radius: 16; -fx-border-color: #e2e8f0; -fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 12, 0, 0, 4);");

        StackPane imagePane = buildImagePane(lib);
        VBox body = new VBox(10);
        body.setPadding(new Insets(16));

        Label titleLabel = new Label(lib.getName());
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);
        Label ageBadge = new Label("\u23f1  " + lib.getMinAge() + " - " + lib.getMaxAge() + " ans");
        ageBadge.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8;" +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 10;");
        Label themeBadge = new Label(lib.getTheme());
        themeBadge.setStyle("-fx-background-color: #f5f3ff; -fx-text-fill: #7c3aed;" +
                "-fx-font-size: 11px; -fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 10;");
        badges.getChildren().addAll(ageBadge, themeBadge);

        String desc = (lib.getDescription() != null && !lib.getDescription().isBlank())
                ? lib.getDescription() : "Aucune description disponible.";
        if (desc.length() > 80) desc = desc.substring(0, 80) + "...";
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        descLabel.setWrapText(true);

        HBox buttons = buildCardButtons(lib);
        body.getChildren().addAll(titleLabel, badges, descLabel, buttons);
        card.getChildren().addAll(imagePane, body);
        return card;
    }

    private StackPane buildImagePane(Library lib) {
        StackPane pane = new StackPane();
        pane.setPrefHeight(160);
        pane.setMaxHeight(160);
        pane.setMinHeight(160);

        Rectangle clip = new Rectangle(290, 160);
        clip.setArcWidth(28);
        clip.setArcHeight(28);
        pane.setClip(clip);

        Region background = new Region();
        background.setPrefSize(290, 160);
        background.setStyle(getHeaderGradient(lib.getLevel()));

        Image img = ImageLoader.load(lib.getCoverImage());
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(290);
            iv.setFitHeight(160);
            iv.setPreserveRatio(false);
            iv.setSmooth(true);
            pane.getChildren().addAll(background, iv);
        } else {
            Label icon = new Label("\ud83d\udcda");
            icon.setStyle("-fx-font-size: 48px;");
            pane.getChildren().addAll(background, icon);
        }

        Label levelBadge = new Label(lib.getLevel());
        levelBadge.setStyle(getLevelBadgeStyle(lib.getLevel()));
        StackPane.setAlignment(levelBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(levelBadge, new Insets(10, 10, 0, 0));
        pane.getChildren().add(levelBadge);
        return pane;
    }

    private HBox buildCardButtons(Library lib) {
        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER);

        Button voirBtn = new Button("\ud83d\udc41  Voir");
        voirBtn.setMaxWidth(Double.MAX_VALUE);
        voirBtn.setStyle("-fx-background-color: white; -fx-text-fill: #374151; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 12;" +
                "-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-border-radius: 10; -fx-cursor: hand;");
        voirBtn.setOnAction(e -> ouvrirDetail(lib));

        Button modifBtn = new Button("\u270f  Modifier");
        modifBtn.setMaxWidth(Double.MAX_VALUE);
        modifBtn.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 12;" +
                "-fx-border-color: #bfdbfe; -fx-border-width: 1; -fx-border-radius: 10; -fx-cursor: hand;");
        modifBtn.setOnAction(e -> ouvrirFormulaire(lib));

        Button suppBtn = new Button("\ud83d\uddd1");
        suppBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #dc2626; -fx-font-size: 13px;" +
                "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 14;" +
                "-fx-border-color: #fecaca; -fx-border-width: 1; -fx-border-radius: 10; -fx-cursor: hand;");
        suppBtn.setOnAction(e -> handleSupprimer(lib));

        HBox.setHgrow(voirBtn,  Priority.ALWAYS);
        HBox.setHgrow(modifBtn, Priority.ALWAYS);
        buttons.getChildren().addAll(voirBtn, modifBtn, suppBtn);
        return buttons;
    }

    @FXML private void handleSearch() {
        String keyword = searchField.getText().toLowerCase().trim();
        String level   = filterLevel.getValue();
        String theme   = filterTheme.getText().toLowerCase().trim();
        List<Library> filtered = allLibraries.stream().filter(lib -> {
            boolean kw = keyword.isEmpty() || lib.getName().toLowerCase().contains(keyword)
                    || (lib.getDescription() != null && lib.getDescription().toLowerCase().contains(keyword))
                    || lib.getTheme().toLowerCase().contains(keyword);
            boolean lv = level == null || level.equals("Tous") || lib.getLevel().equalsIgnoreCase(level);
            boolean th = theme.isEmpty() || lib.getTheme().toLowerCase().contains(theme);
            return kw && lv && th;
        }).collect(Collectors.toList());
        renderCards(filtered);
    }

    @FXML private void handleReset() {
        searchField.clear();
        filterLevel.setValue("Tous");
        filterTheme.clear();
        renderCards(allLibraries);
    }

    @FXML private void handleNouveauLivre() { ouvrirFormulaire(null); }

    private void ouvrirDetail(Library lib) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LibraryShow.fxml"));
            Parent root = loader.load();
            LibraryShowController ctrl = loader.getController();
            ctrl.setLibrary(lib);
            Stage stage = (Stage) cardsPane.getScene().getWindow();
            stage.setScene(new Scene(root, 1050, 700));
            stage.setTitle("Details : " + lib.getName());
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void ouvrirFormulaire(Library lib) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/LibraryForm.fxml"));
            Parent root = loader.load();
            LibraryFormController ctrl = loader.getController();
            ctrl.setLibraryToEdit(lib);
            ctrl.setOnSaveCallback(this::loadLibraries);
            Stage stage = (Stage) cardsPane.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 680));
            stage.setTitle(lib == null ? "Nouvelle Bibliotheque" : "Modifier : " + lib.getName());
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleSupprimer(Library lib) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer \u00ab " + lib.getName() + " \u00bb ?");
        confirm.setContentText("Cette action est irreversible.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                service.supprimer(lib.getId());
                showFlash("\u2705  \u00ab " + lib.getName() + " \u00bb supprimee avec succes !", true);
                loadLibraries();
            }
        });
    }

    public void showFlash(String message, boolean success) {
        flashLabel.setText(message);
        flashBox.setStyle(success
                ? "-fx-background-color: #f0fdf4; -fx-padding: 10 32; -fx-border-color: #22c55e; -fx-border-width: 0 0 0 4;"
                : "-fx-background-color: #fef2f2; -fx-padding: 10 32; -fx-border-color: #ef4444; -fx-border-width: 0 0 0 4;");
        flashLabel.setStyle(success
                ? "-fx-text-fill: #15803d; -fx-font-weight: bold; -fx-font-size: 13px;"
                : "-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 13px;");
        flashBox.setVisible(true);
        flashBox.setManaged(true);
        new Thread(() -> {
            try { Thread.sleep(4000); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> { flashBox.setVisible(false); flashBox.setManaged(false); });
        }).start();
    }

    private String getHeaderGradient(String level) {
        return switch (level == null ? "" : level.toLowerCase()) {
            case "beginner","debutant" -> "-fx-background-color: linear-gradient(to right,#d1fae5,#6ee7b7); -fx-background-radius: 14 14 0 0;";
            case "intermediate","intermediaire" -> "-fx-background-color: linear-gradient(to right,#fef3c7,#fcd34d); -fx-background-radius: 14 14 0 0;";
            case "advanced","avance" -> "-fx-background-color: linear-gradient(to right,#fee2e2,#fca5a5); -fx-background-radius: 14 14 0 0;";
            default -> "-fx-background-color: linear-gradient(to right,#e0e7ff,#a5b4fc); -fx-background-radius: 14 14 0 0;";
        };
    }

    private String getLevelBadgeStyle(String level) {
        String base = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 4 12;";
        return switch (level == null ? "" : level.toLowerCase()) {
            case "beginner","debutant" -> base + "-fx-background-color: linear-gradient(to right,#10b981,#059669);";
            case "intermediate","intermediaire" -> base + "-fx-background-color: linear-gradient(to right,#f59e0b,#d97706);";
            case "advanced","avance" -> base + "-fx-background-color: linear-gradient(to right,#ef4444,#dc2626);";
            default -> base + "-fx-background-color: linear-gradient(to right,#6366f1,#4f46e5);";
        };
    }
}