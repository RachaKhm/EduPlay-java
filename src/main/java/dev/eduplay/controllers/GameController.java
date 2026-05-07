package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.Game;
import dev.eduplay.entities.Level;
import dev.eduplay.services.EmailServiceGame;
import dev.eduplay.services.GameService;
import dev.eduplay.services.LevelService;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML private Label formTitle;

    // Step 1
    @FXML private Circle step1Circle;
    @FXML private Line stepLine1;

    // Section 1
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;

    // Section 2
    @FXML private ComboBox<String> typeCombo;
    @FXML private ComboBox<Level> levelCombo;
    @FXML private TextField imageField;

    // Statistiques
    @FXML private Label statTotalGames;
    @FXML private Label statQuizGames;
    @FXML private Label statPuzzleGames;
    @FXML private Label statOtherGames;

    @FXML private Label feedbackLabel;
    @FXML private Label feedbackname;
    @FXML private Label feedbackdesc;
    @FXML private Label feedbackimage;
    @FXML private Label feedbackid;
    @FXML private Label feedbacktype;
    // Éléments pour la liste (TableView)
    @FXML private TableView<Game> gameTable;
    @FXML private TableColumn<Game, Integer> colId;
    @FXML private TableColumn<Game, String> colName;
    @FXML private TableColumn<Game, String> colLevel;
    @FXML private TableColumn<Game, String> colType;
    @FXML private TableColumn<Game, String> colDescription;
    @FXML private TableColumn<Game, Void> colActions;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    @FXML private ComboBox<String> sortCombo;
    @FXML private Button sortByNameAsc;
    @FXML private Button sortByTypeAsc;

    private GameService gameService;
    private LevelService levelService;
    private Game editingGame;
    private ObservableList<Game> gameList;
    private FilteredList<Game> filteredList;
    private ObservableList<Level> levelList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            gameService = new GameService();
            levelService = new LevelService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Vérifier si on est en mode formulaire ou liste
        if (nameField != null) {
            // Mode formulaire
            setupTypeCombo();
            setupLevelCombo();
            setupListeners();
        }

        if (gameTable != null) {
            // Mode liste
            setupTableColumns();
            loadGames();
            setupSearchFilter();
            setupSortFilter();
        }
    }

    // ==================== MÉTHODES POUR LE FORMULAIRE ====================

    private void setupTypeCombo() {
        typeCombo.getItems().addAll("Quiz", "Puzzle", "Memory", "Aventure", "Éducatif");
        typeCombo.getSelectionModel().selectFirst();
    }

    private void setupLevelCombo() {
        try {
            levelList = FXCollections.observableArrayList(levelService.getAll());
            levelCombo.setItems(levelList);
            levelCombo.setPromptText("Sélectionner un niveau");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateStep1Validation(!newVal.trim().isEmpty());
        });
    }

    private void updateStep1Validation(boolean isValid) {
        if (isValid) {
            step1Circle.setFill(javafx.scene.paint.Color.valueOf("#2E9E6E"));
        } else {
            step1Circle.setFill(javafx.scene.paint.Color.valueOf("#E94560"));
        }
    }

    @FXML
    private void saveGame() {
        if (!validateForm()) {
            return;
        }

        try {
            Game game = new Game();
            game.setName(nameField.getText().trim());
            game.setDescription(descriptionField.getText());
            game.setType(typeCombo.getValue());
            game.setLevel(levelCombo.getValue());
            game.setImage(imageField.getText());

            if (editingGame == null) {
                gameService.add(game);
                // Envoyer email à tous les parents
                EmailServiceGame emailService = new EmailServiceGame();
                emailService.sendEmailToAllParents(game.getName(), game.getDescription());

                nameField.clear();
                descriptionField.clear();
                imageField.clear();
                typeCombo.getSelectionModel().selectFirst();
            } else {
                game.setId(editingGame.getId());
                gameService.update(game);

                closeForm();
            }
            showSuccessAlert("Jeu \"" + game.getName() + "\" a été sauvegardé avec succès !");
            Router.reload("games_list");
            clearAllFeedbacks();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Effacer tous les feedbacks avant validation
        clearAllFeedbacks();

        // Validation du nom
        String name = nameField.getText().trim();

        if (name.isEmpty()) {
            showFeedback(feedbackname, "Veuillez saisir le nom du jeu", "error");
            isValid = false;
        } else if (!name.matches("[a-zA-Z\\s]+")) {
            showFeedback(feedbackname, "Le nom doit contenir uniquement des lettres et des espaces", "error");
            isValid = false;
        } else if (name.length() < 3) {
            showFeedback(feedbackname, "Le nom doit contenir au moins 3 caractères", "error");
            isValid = false;
        } else {
            showFeedback(feedbackname, "✓ Nom valide", "success");
        }



        // Validation de la description
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            showFeedback(feedbackdesc, "Veuillez saisir la description du jeu", "error");
            isValid = false;
        } else if (description.length() < 10) {
            showFeedback(feedbackdesc, "La description doit contenir au moins 10 caractères", "error");
            isValid = false;
        }
        else if (!description.matches("[a-zA-Z\\s]+")) {
            showFeedback(feedbackdesc, "La description doit contenir uniquement des lettres et des espaces", "error");
            isValid = false;
        }else {
            showFeedback(feedbackdesc, "✓ Description valide", "success");
        }

        // Validation de l'image
        String image = imageField.getText().trim();
        if (image.isEmpty()) {
            showFeedback(feedbackimage, "L'image est obligatoire", "error");
            isValid = false;
        } else if (!image.toLowerCase().endsWith(".png") && !image.toLowerCase().endsWith(".jpg")) {
            showFeedback(feedbackimage, "L'image doit être au format .png ou .jpg", "error");
            isValid = false;
        } else {
            showFeedback(feedbackimage, "✓ Format image valide", "success");
        }

        // Validation du niveau
        if (levelCombo.getValue() == null) {
            showFeedback(feedbackid, "Veuillez sélectionner un niveau", "error");
            isValid = false;
        }
        if (typeCombo.getValue() == null) {
            showFeedback(feedbacktype, "Veuillez sélectionner un type", "error");
            isValid = false;
        }


        return isValid;
    }

    private void clearAllFeedbacks() {
        if (feedbackname != null) feedbackname.setText("");
        if (feedbackdesc != null) feedbackdesc.setText("");
        if (feedbackimage != null) feedbackimage.setText("");
        if (feedbackid != null) feedbackid.setText("");
    }

    private void showFeedback(Label label, String message, String type) {
        if (label != null) {
            label.setText(message);
            if ("error".equals(type)) {
                label.setStyle("-fx-text-fill: #E94560; -fx-font-size: 11px;");
            } else {
                label.setStyle("-fx-text-fill: #2E9E6E; -fx-font-size: 11px;");
            }
        }
    }

    @FXML
    private void closeForm() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    public void setGame(Game game) {
        this.editingGame = game;
        formTitle.setText("Modifier le jeu");

        nameField.setText(game.getName());
        descriptionField.setText(game.getDescription());
        typeCombo.setValue(game.getType());
        levelCombo.setValue(game.getId_level());
        imageField.setText(game.getImage());
    }

    // ==================== MÉTHODES POUR LA LISTE ====================

    private void setupTableColumns() {
        colId.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        colName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        colName.setSortable(true);  // ← AJOUTER CETTE LIGNE

        colLevel.setCellValueFactory(cellData -> {
            Level level = cellData.getValue().getId_level();
            return new SimpleStringProperty(level != null ? level.getName() : "Non défini");
        });
        colLevel.setSortable(true);  // ← AJOUTER CETTE LIGNE

        colType.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getType()));
        colType.setSortable(true);  // ← AJOUTER CETTE LIGNE

        colDescription.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription()));
        colDescription.setSortable(true);  // ← AJOUTER CETTE LIGNE

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox container = new HBox(8, editBtn, deleteBtn);

            {
                editBtn.setStyle("-fx-background-color: #2E9E6E; -fx-text-fill: white; " +
                        "-fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 4;");
                deleteBtn.setStyle("-fx-background-color: #E94560; -fx-text-fill: white; " +
                        "-fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 4;");
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Game game = getTableView().getItems().get(getIndex());
                    editBtn.setOnAction(e -> editGame(game));
                    deleteBtn.setOnAction(e -> deleteGame(game));
                    setGraphic(container);
                }
            }
        });
    }

    private void loadGames() {
        try {
            gameList = FXCollections.observableArrayList(gameService.getAll());
            filteredList = new FilteredList<>(gameList, p -> true);
            gameTable.setItems(filteredList);

            gameTable.setItems(filteredList);
            updateStatusLabel();
            updateStatistics();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les jeux : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSearchFilter() {
        if (searchField != null) {
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
                updateStatusLabel();
            });
        }
    }

    private void updateStatusLabel() {
        if (statusLabel != null) {
            int size = filteredList.size();
            statusLabel.setText(size + " jeu(x)");
        }
    }

    @FXML
    private void onAddGame() {
        openGameForm(null);
    }

    private void editGame(Game game) {
        openGameForm(game);
    }

    private void deleteGame(Game game) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le jeu");
        alert.setContentText("Voulez-vous vraiment supprimer le jeu \"" + game.getName() + "\" ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                gameService.delete(game.getId());
                gameList.remove(game);
                showAlert("Succès", "Jeu supprimé avec succès !");
                updateStatistics();
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de supprimer : " + e.getMessage());
            }
        }
    }

    private void openGameForm(Game game) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/teacher/game/ModifierGame.fxml"));
            Parent root = loader.load();

            GameController controller = loader.getController();
            if (game != null) {
                controller.setGame(game);
            }

            Stage stage = new Stage();
            stage.setTitle(game == null ? "Ajouter un jeu" : "Modifier un jeu");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(gameTable.getScene().getWindow());
            stage.showAndWait();

            // Recharger la liste après fermeture
            loadGames();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible d'ouvrir le formulaire");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #F8F9FA;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #2E9E6E; -fx-font-weight: bold;");

        alert.showAndWait();
    }

    @FXML
    private void Page_Ajout() { Router.go("Ajout_game"); }

    @FXML
    private void showGames() {
        nameField.clear();
        descriptionField.clear();
        imageField.clear();
        clearAllFeedbacks();
        typeCombo.getSelectionModel().selectFirst();
        Router.go("games_list"); }

    private void updateStatistics() {
        try {
            List<Game> allGames = gameService.getAll();
            int total = allGames.size();
            int quiz = 0, puzzle = 0, other = 0;

            for (Game game : allGames) {
                String type = game.getType();
                if ("Quiz".equalsIgnoreCase(type)) quiz++;
                else if ("Puzzle".equalsIgnoreCase(type)) puzzle++;
                else other++;
            }

            statTotalGames.setText(String.valueOf(total));
            statQuizGames.setText(String.valueOf(quiz));
            statPuzzleGames.setText(String.valueOf(puzzle));
            statOtherGames.setText(String.valueOf(other));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSortFilter() {
        if (sortCombo != null) {
            sortCombo.getItems().addAll("Trier par...", "Nom (A-Z)", "Nom (Z-A)", "Type (A-Z)", "Type (Z-A)");
            sortCombo.getSelectionModel().selectFirst();

            sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.equals("Trier par...")) {
                    applySort(newVal);
                }
            });
        }
    }

    private void applySort(String sortType) {
        ObservableList<Game> currentList = filteredList;

        switch (sortType) {
            case "Nom (A-Z)":
                FXCollections.sort(currentList, (g1, g2) ->
                        g1.getName().compareToIgnoreCase(g2.getName()));
                break;
            case "Nom (Z-A)":
                FXCollections.sort(currentList, (g1, g2) ->
                        g2.getName().compareToIgnoreCase(g1.getName()));
                break;
            case "Type (A-Z)":
                FXCollections.sort(currentList, (g1, g2) ->
                        g1.getType().compareToIgnoreCase(g2.getType()));
                break;
            case "Type (Z-A)":
                FXCollections.sort(currentList, (g1, g2) ->
                        g2.getType().compareToIgnoreCase(g1.getType()));
                break;
        }
        gameTable.setItems(currentList);
    }

}