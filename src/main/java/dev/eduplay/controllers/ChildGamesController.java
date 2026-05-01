package dev.eduplay.controllers;
import dev.eduplay.services.OllamaServiceGame;
import dev.eduplay.entities.Game;
import dev.eduplay.entities.Level;
import dev.eduplay.services.GameService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import dev.eduplay.services.TranslationServiceGame;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ChildGamesController implements Initializable {

    @FXML private TextField searchField;
    @FXML private TilePane gamesGrid;
    @FXML private Label statusLabel;

    private GameService gameService;
    private ObservableList<Game> gameList;
    private FilteredList<Game> filteredList;

    private TranslationServiceGame translationService;
    private OllamaServiceGame ollamaServiceGame;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            gameService = new GameService();
            translationService = new TranslationServiceGame();
            ollamaServiceGame = new OllamaServiceGame();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        loadGames();
        setupSearchFilter();
    }

    private void loadGames() {
        try {
            gameList = FXCollections.observableArrayList(gameService.getAll());
            filteredList = new FilteredList<>(gameList, p -> true);
            displayGames();
            updateStatusLabel();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les jeux");
        }
    }

    private void setupSearchFilter() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                filteredList.setPredicate(p -> true);
            } else {
                String lowerFilter = newVal.toLowerCase();
                filteredList.setPredicate(game ->
                        game.getName().toLowerCase().contains(lowerFilter) ||
                                game.getType().toLowerCase().contains(lowerFilter) ||
                                game.getDescription().toLowerCase().contains(lowerFilter)
                );
            }
            displayGames();
            updateStatusLabel();
        });
    }

    private void displayGames() {
        gamesGrid.getChildren().clear();

        for (Game game : filteredList) {
            VBox gameCard = createGameCard(game);
            gamesGrid.getChildren().add(gameCard);
        }
    }

    private VBox createGameCard(Game game) {
        VBox card = new VBox();
        card.setAlignment(Pos.TOP_CENTER);
        card.setSpacing(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(250);
        card.setPrefHeight(300);

        // Style de la carte
        card.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                "-fx-cursor: hand;");

        // Effet hover
        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color: #f8f9fa; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 8); " +
                        "-fx-cursor: hand;")
        );
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: white; " +
                        "-fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                        "-fx-cursor: hand;")
        );

        // Conteneur pour l'image
        StackPane imageContainer = createImageContainer(game);

        // Type badge
        Label typeBadge = new Label(" " + game.getType() + " ");
        typeBadge.setStyle("-fx-background-color: " + getTypeColor(game.getType()) + "; " +
                "-fx-text-fill: white; -fx-font-size: 10px; -fx-font-weight: bold; " +
                "-fx-background-radius: 12; -fx-padding: 4 10;");

        // Nom du jeu
        Label nameLabel = new Label(game.getName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1A1A2E;");
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setMaxWidth(220);

        // Description courte
        String shortDesc = game.getDescription();
        if (shortDesc != null && shortDesc.length() > 80) {
            shortDesc = shortDesc.substring(0, 77) + "...";
        }
        Label descLabel = new Label(shortDesc != null ? shortDesc : "");
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #777799;");
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);
        descLabel.setMaxWidth(220);

        // ========== AJOUTER LES BOUTONS DE TRADUCTION ICI ==========
        // Boutons de traduction
        HBox translationButtons = new HBox(10);
        translationButtons.setAlignment(Pos.CENTER);
        translationButtons.setPadding(new Insets(5, 0, 5, 0));

        Button arabicBtn = new Button("🇸🇦 عربي");
        arabicBtn.setStyle("-fx-background-color: #2E9E6E; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 15;");
        arabicBtn.setOnAction(e -> translateDescription(game, "ar"));

        Button englishBtn = new Button("🇬🇧 English");
        englishBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 15;");
        englishBtn.setOnAction(e -> translateDescription(game, "en"));

        translationButtons.getChildren().addAll(arabicBtn, englishBtn);
        // ===========================================================

        // Bouton IA pour simplifier la description
        Button simplifyBtn = new Button("🤖 IA + simple");
        simplifyBtn.setStyle("-fx-background-color: #9B59B6; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-padding: 5 10; -fx-background-radius: 15;");
        simplifyBtn.setOnAction(e -> simplifyDescription(game));

        // ========== GROUPER LES BOUTONS DANS UN HBox ==========
        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(5, 0, 5, 0));
        actionButtons.getChildren().addAll(arabicBtn, englishBtn, simplifyBtn);
        // =======================================================
        // Niveau
        Level level = game.getId_level();
        Label levelLabel = new Label("🎓 " + (level != null ? level.getName() : "Débutant") + " " + getStars(level));
        levelLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #E94560; -fx-font-weight: bold;");

        // Bouton Jouer
        Button playBtn = new Button("Jouer maintenant 🎮");
        playBtn.setStyle("-fx-background-color: linear-gradient(to right, #E94560, #FF6B6B); " +
                "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-padding: 8 16; -fx-background-radius: 20; -fx-cursor: hand;");
        playBtn.setMaxWidth(Double.MAX_VALUE);
        playBtn.setOnAction(e -> playGame(game));

        card.getChildren().addAll(imageContainer, typeBadge, nameLabel, descLabel,
                actionButtons, levelLabel, playBtn);
        return card;
    }

    private StackPane createImageContainer(Game game) {
        StackPane container = new StackPane();
        container.setAlignment(Pos.CENTER);
        container.setPrefWidth(200);
        container.setPrefHeight(120);

        // Rectangle de fond coloré
        Rectangle background = new Rectangle(200, 120);
        background.setArcWidth(15);
        background.setArcHeight(15);
        background.setFill(getTypeColorPaint(game.getType()));

        // Emoji au centre
        Label emojiLabel = new Label(getGameEmoji(game.getType()));
        emojiLabel.setStyle("-fx-font-size: 50px;");

        // Essayer de charger une image locale si spécifiée
        if (game.getImage() != null && !game.getImage().isEmpty()) {
            try {
                String imagePath = "/images/games/" + game.getImage();
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                if (image != null && !image.isError()) {
                    ImageView imageView = new ImageView(image);
                    imageView.setFitWidth(180);
                    imageView.setFitHeight(100);
                    imageView.setPreserveRatio(true);
                    container.getChildren().addAll(imageView);
                    return container;
                }
            } catch (Exception e) {
                // Ignorer, utiliser l'emoji par défaut
            }
        }

        container.getChildren().addAll(background, emojiLabel);
        return container;
    }

    private Color getTypeColorPaint(String type) {
        switch (type) {
            case "Quiz": return Color.web("#2E9E6E");
            case "Puzzle": return Color.web("#F39C12");
            case "Memory": return Color.web("#3498DB");
            case "Aventure": return Color.web("#9B59B6");
            default: return Color.web("#E94560");
        }
    }

    private String getTypeColor(String type) {
        switch (type) {
            case "Quiz": return "#2E9E6E";
            case "Puzzle": return "#F39C12";
            case "Memory": return "#3498DB";
            case "Aventure": return "#9B59B6";
            default: return "#E94560";
        }
    }

    private String getGameEmoji(String type) {
        switch (type) {
            case "Quiz": return "❓";
            case "Puzzle": return "🧩";
            case "Memory": return "🧠";
            case "Aventure": return "🗺️";
            default: return "🎮";
        }
    }

    private String getStars(Level level) {
        if (level == null) return "⭐";
        switch (level.getDifficulty()) {
            case 1: return "⭐";
            case 2: return "⭐⭐";
            case 3: return "⭐⭐⭐";
            case 4: return "⭐⭐⭐⭐";
            default: return "⭐";
        }
    }

    private void playGame(Game game) {
        try {
            FXMLLoader loader;

            if (game.getType().equals("Memory")) {
                loader = new FXMLLoader(getClass().getResource("/views/child/game/MemoryGameView.fxml"));
            } else if (game.getType().equals("Quiz")) {
                loader = new FXMLLoader(getClass().getResource("/views/child/game/QuizGameView.fxml"));

            } else if (game.getType().equals("BrickBreaker")) {
                loader = new FXMLLoader(getClass().getResource("/views/child/game/BrickBreakerView.fxml"));
            }else {
                showAlert("Jeu", "Le jeu '" + game.getName() + "' va commencer !");
                return;
            }

            Parent root = loader.load();
            Stage gameStage = new Stage();
            gameStage.setTitle(game.getName());
            gameStage.setScene(new Scene(root, 600, 600));
            gameStage.show();

        } catch (IOException e) {
            showAlert("Erreur", "Impossible de lancer le jeu");
            e.printStackTrace();
        }
    }

    private void updateStatusLabel() {
        int size = filteredList.size();
        statusLabel.setText(size + " jeu(x) disponible(s)");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void translateDescription(Game game, String targetLang) {
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Traduction en cours");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("⏳ Traduction en cours...");
        loadingAlert.show();

        new Thread(() -> {
            String translatedText;
            if ("ar".equals(targetLang)) {
                translatedText = translationService.translateToArabic(game.getDescription());
            } else {
                translatedText = translationService.translateToEnglish(game.getDescription());
            }

            final String finalText = translatedText;
            String langName = "ar".equals(targetLang) ? "Arabe" : "Anglais";

            Platform.runLater(() -> {
                loadingAlert.close();

                Alert translationAlert = new Alert(Alert.AlertType.INFORMATION);
                translationAlert.setTitle("Traduction en " + langName);
                translationAlert.setHeaderText(game.getName());
                translationAlert.setContentText(finalText);
                translationAlert.showAndWait();
            });
        }).start();
    }

    private void simplifyDescription(Game game) {
        int childAge = getChildAge(); // Récupérer l'âge de l'enfant connecté

        // Vérifier si Ollama est disponible
        if (!ollamaServiceGame.isOllamaRunning()) {
            showAlert("IA indisponible", "Le service Ollama n'est pas démarré.\n" +
                    "Veuillez lancer Ollama avec la commande : ollama serve");
            return;
        }

        // Afficher un indicateur de chargement
        Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
        loadingAlert.setTitle("Génération en cours");
        loadingAlert.setHeaderText(null);
        loadingAlert.setContentText("🤖 L'IA simplifie la description pour toi...\n⏳ Cela peut prendre quelques secondes");
        loadingAlert.show();

        new Thread(() -> {
            String simplified = ollamaServiceGame.simplifyDescription(game.getDescription(), childAge);

            Platform.runLater(() -> {
                loadingAlert.close();

                // Afficher la description simplifiée
                Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
                resultAlert.setTitle("📖 Description simplifiée");
                resultAlert.setHeaderText(game.getName());
                resultAlert.setContentText(simplified);

                DialogPane dialogPane = resultAlert.getDialogPane();
                dialogPane.setPrefWidth(450);
                dialogPane.setStyle("-fx-background-color: #F8F9FA;");

                resultAlert.showAndWait();
            });
        }).start();
    }

    private int getChildAge() {
        // À adapter selon comment vous stockez l'âge de l'enfant
        // Exemple: return AppContext.getCurrentUser().getAge();
        // Ou depuis la base de données
        return 8; // Valeur par défaut pour test
    }


}