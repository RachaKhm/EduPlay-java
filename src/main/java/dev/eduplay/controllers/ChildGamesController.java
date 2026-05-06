package dev.eduplay.controllers;
import dev.eduplay.core.AppContext;
import dev.eduplay.entities.User;
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
import java.time.LocalDate;
import java.time.Period;
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
        card.setPrefHeight(480);

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


        // ========== BOUTONS ==========
        VBox buttonsBox = new VBox(8);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(5, 0, 5, 0));

        // Bouton Voir description (AJOUTÉ)
        Button detailsBtn = new Button("📖 Voir description");
        detailsBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; " +
                "-fx-font-size: 11px; -fx-padding: 6 12; -fx-background-radius: 15; -fx-cursor: hand;");
        detailsBtn.setMaxWidth(Double.MAX_VALUE);
        detailsBtn.setOnAction(e -> showGameDetails(game));

        // Bouton Jouer maintenant (existant)
        Button playBtn = new Button("Jouer maintenant 🎮");
        playBtn.setStyle("-fx-background-color: linear-gradient(to right, #E94560, #FF6B6B); " +
                "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-padding: 8 16; -fx-background-radius: 20; -fx-cursor: hand;");
        playBtn.setMaxWidth(Double.MAX_VALUE);
        playBtn.setOnAction(e -> playGame(game));

        buttonsBox.getChildren().addAll(detailsBtn, playBtn);

        // ... ajouter buttonsBox dans card.getChildren()
        card.getChildren().addAll(imageContainer, typeBadge, nameLabel, descLabel,
                translationButtons, levelLabel, buttonsBox);

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
            }else if (game.getType().equals("Puzzle")) {
                loader = new FXMLLoader(getClass().getResource("/views/child/game/SlidingPuzzleView.fxml"));
            }else if (game.getType().equals("Aventure") ) {
                loader = new FXMLLoader(getClass().getResource("/views/child/game/AdventureGameView.fxml"));
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
        // Récupérer l'utilisateur connecté depuis AppContext
        User currentUser = AppContext.getCurrentUser();

        if (currentUser != null && currentUser.getBirthDate() != null) {
            // Calculer l'âge à partir de la date de naissance
            LocalDate birthDate = currentUser.getBirthDate();
            LocalDate today = LocalDate.now();
            int age = Period.between(birthDate, today).getYears();
            System.out.println("Âge de l'enfant: " + age);
            return age;
        }

        System.out.println("Âge non trouvé, valeur par défaut: 8");
        return 8; // Valeur par défaut
    }

    private void showGameDetails(Game game) {
        Level level = game.getId_level();
        int childAge = getChildAge();

        Stage detailStage = new Stage();
        detailStage.setTitle("📖 Détails du jeu - " + game.getName());
        detailStage.setResizable(true);
        detailStage.setMinWidth(550);
        detailStage.setMinHeight(650);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setFitToWidth(true);

        VBox container = new VBox(15);
        container.setAlignment(Pos.TOP_CENTER);
        container.setStyle("-fx-background-color: linear-gradient(to bottom, #1A1A2E, #16213E); -fx-padding: 25;");

        // Titre
        Label titleLabel = new Label(game.getName());
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #E94560;");

        // Type
        HBox typeBox = new HBox(10);
        typeBox.setAlignment(Pos.CENTER_LEFT);
        Label typeIcon = new Label(getGameEmoji(game.getType()));
        typeIcon.setStyle("-fx-font-size: 20px;");
        Label typeLabel = new Label("Type: " + game.getType());
        typeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #F39C12; -fx-font-weight: bold;");
        typeBox.getChildren().addAll(typeIcon, typeLabel);

        // Niveau
        HBox levelBox = new HBox(10);
        levelBox.setAlignment(Pos.CENTER_LEFT);
        Label levelIcon = new Label("🎓");
        levelIcon.setStyle("-fx-font-size: 20px;");
        String levelName = level != null ? level.getName() : "Non défini";
        String stars = getStars(level);
        Label levelLabel = new Label("Niveau: " + levelName + " " + stars);
        levelLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2E9E6E; -fx-font-weight: bold;");
        levelBox.getChildren().addAll(levelIcon, levelLabel);

        // Âge recommandé
        HBox ageBox = new HBox(10);
        ageBox.setAlignment(Pos.CENTER_LEFT);
        Label ageIcon = new Label("📅");
        ageIcon.setStyle("-fx-font-size: 20px;");
        String ageRange = "";
        if (level != null) {
            ageRange = level.getMinAge() + " - " + level.getMaxAge() + " ans";
        } else {
            ageRange = "Non spécifié";
        }
        Label ageLabel = new Label("Âge recommandé: " + ageRange);
        ageLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3498DB; -fx-font-weight: bold;");
        ageBox.getChildren().addAll(ageIcon, ageLabel);

        // Difficulté
        HBox diffBox = new HBox(10);
        diffBox.setAlignment(Pos.CENTER_LEFT);
        Label diffIcon = new Label("🎯");
        diffIcon.setStyle("-fx-font-size: 20px;");
        String difficulty = level != null ? getDifficultyString(level.getDifficulty()) : "Moyen";
        Label diffLabel = new Label("Difficulté: " + difficulty);
        diffLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #9B59B6; -fx-font-weight: bold;");
        diffBox.getChildren().addAll(diffIcon, diffLabel);

        // Description originale
        Label descTitle = new Label("📝 Description originale:");
        descTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #E94560; -fx-padding: 10 0 5 0;");

        TextArea descArea = new TextArea(game.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefHeight(100);
        descArea.setStyle("-fx-background-color: #0F0F23; -fx-text-fill: white; -fx-font-size: 13px; " +
                "-fx-control-inner-background: #0F0F23; -fx-border-color: #E94560; -fx-border-radius: 10;");

        // ========== SECTION TRADUCTION ==========
        Label translateTitle = new Label("🌐 Traduire la description:");
        translateTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2E9E6E; -fx-padding: 15 0 10 0;");

        HBox translateBox = new HBox(15);
        translateBox.setAlignment(Pos.CENTER);

        TextArea translationArea = new TextArea();
        translationArea.setWrapText(true);
        translationArea.setEditable(false);
        translationArea.setPrefHeight(80);
        translationArea.setPromptText("La traduction apparaîtra ici...");
        translationArea.setStyle("-fx-background-color: #0F0F23; -fx-text-fill: white; -fx-font-size: 13px; " +
                "-fx-control-inner-background: #0F0F23; -fx-border-color: #2E9E6E; -fx-border-radius: 10;");

        TranslationServiceGame translationService = new TranslationServiceGame();



        Button englishBtn = new Button("🇬🇧 Traduire en Anglais");
        englishBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15; -fx-background-radius: 15; -fx-cursor: hand;");
        englishBtn.setOnAction(e -> {
            translationArea.setText("⏳ Traduction en cours...");
            new Thread(() -> {
                String translated = translationService.translateToEnglish(game.getDescription());
                Platform.runLater(() -> translationArea.setText(translated));
            }).start();
        });

        translateBox.getChildren().addAll( englishBtn);

        // ========== SECTION IA SIMPLIFICATION ==========
        Label aiTitle = new Label("🤖 IA - Simplifier la description:");
        aiTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #9B59B6; -fx-padding: 15 0 10 0;");

        TextArea aiArea = new TextArea();
        aiArea.setWrapText(true);
        aiArea.setEditable(false);
        aiArea.setPrefHeight(80);
        aiArea.setPromptText("La description simplifiée apparaîtra ici...");
        aiArea.setStyle("-fx-background-color: #0F0F23; -fx-text-fill: #F39C12; -fx-font-size: 13px; " +
                "-fx-control-inner-background: #0F0F23; -fx-border-color: #9B59B6; -fx-border-radius: 10;");

        OllamaServiceGame ollamaService = new OllamaServiceGame();

        Button simplifyBtn = new Button("✨ Simplifier pour mon âge (" + childAge + " ans)");
        simplifyBtn.setStyle("-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 8 15; -fx-background-radius: 15; -fx-cursor: hand;");
        simplifyBtn.setOnAction(e -> {
            if (!ollamaService.isOllamaRunning()) {
                aiArea.setText("❌ Service Ollama non démarré. Lancez 'ollama serve'");
                return;
            }
            aiArea.setText("🤖 Génération en cours...");
            new Thread(() -> {
                String simplified = ollamaService.simplifyDescription(game.getDescription(), childAge);
                Platform.runLater(() -> aiArea.setText(simplified));
            }).start();
        });

        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #E94560;");

        // Objectif pédagogique
        Label objectiveTitle = new Label("🎯 Objectif pédagogique:");
        objectiveTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #F39C12; -fx-padding: 10 0 5 0;");

        Label objectiveLabel = new Label();
        if (level != null && level.getPedagGoal() != null && !level.getPedagGoal().isEmpty()) {
            objectiveLabel.setText(level.getPedagGoal());
        } else {
            objectiveLabel.setText("Développer les compétences cognitives et la logique à travers le jeu");
        }
        objectiveLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #AAAAAA; -fx-wrap-text: true;");
        objectiveLabel.setWrapText(true);

        // Boutons Jouer et Fermer
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button playBtn = new Button("🎮 Jouer maintenant");
        playBtn.setStyle("-fx-background-color: linear-gradient(to right, #E94560, #FF6B6B); -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 25; -fx-background-radius: 25; -fx-cursor: hand;");
        playBtn.setOnAction(e -> {
            detailStage.close();
            playGame(game);
        });

        Button closeBtn = new Button("🔒 Fermer");
        closeBtn.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; -fx-font-size: 14px; " +
                "-fx-padding: 10 25; -fx-background-radius: 25; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> detailStage.close());

        buttonBox.getChildren().addAll(playBtn, closeBtn);

        // Ajouter tous les éléments
        container.getChildren().addAll(
                titleLabel, typeBox, levelBox, ageBox, diffBox,
                descTitle, descArea,
                translateTitle, translateBox, translationArea,
                aiTitle, simplifyBtn, aiArea,
                separator, objectiveTitle, objectiveLabel,
                buttonBox
        );

        scrollPane.setContent(container);
        Scene scene = new Scene(scrollPane, 580, 750);
        detailStage.setScene(scene);
        detailStage.showAndWait();
    }

    private String getDifficultyString(int difficulty) {
        switch (difficulty) {
            case 1: return "Facile ⭐";
            case 2: return "Moyen ⭐⭐";
            case 3: return "Difficile ⭐⭐⭐";
            case 4: return "Expert ⭐⭐⭐⭐";
            default: return "Moyen ⭐⭐";
        }
    }



}