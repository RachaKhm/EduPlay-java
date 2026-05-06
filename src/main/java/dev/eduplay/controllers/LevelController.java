package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.Game;
import dev.eduplay.entities.Level;
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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class LevelController implements Initializable {

    @FXML private Label formTitle;

    // Step 1
    @FXML private Circle step1Circle;
    @FXML private Line stepLine1;

    // Section 1
    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;

    // Section 2
    @FXML private ComboBox<String> difficultyCombo;
    @FXML private Spinner<Integer> minAgeSpinner;
    @FXML private Spinner<Integer> maxAgeSpinner;
    @FXML private TextArea pedagogGoalField;
   //statistiques
    @FXML private Label statTotalLevels;
    @FXML private Label statEasyLevels;
    @FXML private Label statMediumLevels;
    @FXML private Label statHardLevels;

    // Section 3 (simplifiée car pas de prérequis dans votre DB)
    @FXML private CheckBox isPublishedCheck;

    @FXML private Label feedbackLabel;
    @FXML private Label feedbackname;
    @FXML private Label feedbackdesc;
    @FXML private Label feedbackage;
    @FXML private Label feedbackid;
    @FXML private Label feedbackpedag;


    @FXML private Button sortByNameAsc;
    @FXML private Button sortByNameDesc;
    @FXML private Button sortByTypeAsc;
    @FXML private Button sortByTypeDesc;

    // Éléments pour la liste (TableView)
    @FXML private TableView<Level> levelTable;
    @FXML private TableColumn<Level, Integer> colId;
    @FXML private TableColumn<Level, String> colName;
    @FXML private TableColumn<Level, String> colDifficulty;
    @FXML private TableColumn<Level, String> colAgeRange;
    @FXML private TableColumn<Level, String> colPedagGoal;
    @FXML private TableColumn<Level, Void> colActions;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private LevelService levelService;
    private Level editingLevel;
    private ObservableList<Level> levelList;
    private FilteredList<Level> filteredList;

    // Dans GameController
    private ObservableList<Game> gameList;        // ← Pour la liste originale
        // ← Pour la liste filtrée
    @FXML private TableView<Game> gameTable;      // ← Pour le tableau

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            levelService = new LevelService();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Vérifier si on est en mode formulaire ou liste
        if (nameField != null) {
            // Mode formulaire
            setupSpinners();
            setupDifficultyCombo();
            setupListeners();
        }

        if (levelTable != null) {
            // Mode liste
            setupTableColumns();
            loadLevels();
            setupSearchFilter();
        }
    }

    // ==================== MÉTHODES POUR LE FORMULAIRE ====================

    private void setupSpinners() {
        minAgeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 18, 6));
        maxAgeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 18, 12));

        minAgeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal > maxAgeSpinner.getValue()) {
                maxAgeSpinner.getValueFactory().setValue(newVal);
            }
        });

        maxAgeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal < minAgeSpinner.getValue()) {
                minAgeSpinner.getValueFactory().setValue(newVal);
            }
        });
    }

    private void setupDifficultyCombo() {
        difficultyCombo.getItems().addAll("Facile", "Moyen", "Difficile", "Expert");
        difficultyCombo.getSelectionModel().selectFirst();
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
    private void saveLevel() {
        if (!validateForm()) {
            return;
        }

        try {
            Level level = new Level();
            level.setName(nameField.getText().trim());
            level.setDescription(descriptionField.getText());
            level.setDifficulty(getDifficultyValue());
            level.setMinAge(minAgeSpinner.getValue());
            level.setMaxAge(maxAgeSpinner.getValue());
            level.setPedagGoal(pedagogGoalField.getText());

            LocalDateTime now = LocalDateTime.now();
            level.setCreatedAt(now);
            level.setUpdatedAt(now);

            if (editingLevel == null) {
                levelService.add(level);

                nameField.clear();
                descriptionField.clear();
                pedagogGoalField.clear();
            } else {
                level.setId(editingLevel.getId());
                levelService.update(level);

                closeForm();
            }
            showSuccessAlert("Niveau \"" + level.getName() + "\" a été créé avec succès !");

            Router.reload("levels_list");
            clearAllFeedbacks();



        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private int getDifficultyValue() {
        String difficulty = difficultyCombo.getValue();
        if (difficulty == null) return 2;

        switch (difficulty) {
            case "Facile": return 1;
            case "Moyen": return 2;
            case "Difficile": return 3;
            case "Expert": return 4;
            default: return 2;
        }
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Effacer tous les feedbacks
        clearAllFeedbacks();

        // Validation du nom
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showFeedback(feedbackname, "Veuillez saisir le nom du niveau", "error");
            isValid = false;
        } else if (!name.matches("[a-zA-Z]+")) {
            showFeedback(feedbackname, "Le nom doit contenir uniquement des lettres ", "error");
            isValid = false;
        } else if (name.length() < 3) {
            showFeedback(feedbackname, "Le nom doit contenir au moins 3 caractères", "error");
            isValid = false;
        } else {
            showFeedback(feedbackname, "✓ Nom valide", "success");
        }

        // Validation de la difficulté
        if (difficultyCombo.getValue() == null) {
            showFeedback(feedbackid, "Veuillez sélectionner la difficulté", "error");
            isValid = false;
        } else {
            showFeedback(feedbackid, "✓ Difficulté sélectionnée", "success");
        }

        // Validation des âges
        int minAge = minAgeSpinner.getValue();
        int maxAge = maxAgeSpinner.getValue();
        if (minAge > maxAge) {
            showFeedback(feedbackage, "L'âge minimum ne peut pas être supérieur à l'âge maximum", "error");
            isValid = false;
        } else if(minAge < 4 || maxAge > 16) {
            showFeedback(feedbackage, "L'âge doit être entre 4 et 16  ", "error");
            isValid = false;

        }
        else {
            showFeedback(feedbackage, "✓ Tranche d'âge valide (" + minAge + " - " + maxAge + " ans)", "success");
        }

        // Validation de l'objectif pédagogique
        String pedagogGoal = pedagogGoalField.getText().trim();
        if (pedagogGoal.isEmpty()) {
            showFeedback(feedbackpedag, "Veuillez saisir l'objectif pédagogique", "error");
            isValid = false;
        } else if (pedagogGoal.length() < 10) {
            showFeedback(feedbackpedag, "L'objectif doit contenir au moins 10 caractères", "error");
            isValid = false;
        } else if (!pedagogGoal.matches("[a-zA-Z\\s]+")) {
            showFeedback(feedbackpedag, "L'objectif doit contenir uniquement des lettres et des espaces", "error");
            isValid = false;
        } else {
            showFeedback(feedbackpedag, "✓ Objectif pédagogique valide", "success");
        }

        // Validation de la description
        String desc = descriptionField.getText().trim();
        if (desc.isEmpty()) {
            showFeedback(feedbackdesc, "Veuillez saisir la description", "error");
            isValid = false;
        } else if (!desc.matches("[a-zA-Z\\s]+")) {
            showFeedback(feedbackdesc, "La description doit contenir uniquement des lettres et des espaces", "error");
            isValid = false;
        } else if (desc.length() < 10) {
            showFeedback(feedbackdesc, "La description doit contenir au moins 10 caractères", "error");
            isValid = false;
        } else {
            showFeedback(feedbackdesc, "✓ La description valide", "success");
        }

        return isValid;
    }

    private void clearAllFeedbacks() {
        if (feedbackname != null) feedbackname.setText("");
        if (feedbackid != null) feedbackid.setText("");
        if (feedbackage != null) feedbackage.setText("");
        if (feedbackpedag != null) feedbackpedag.setText("");
        if (feedbackdesc != null) feedbackdesc.setText("");
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

    public void setLevel(Level level) {
        this.editingLevel = level;
        formTitle.setText("Modifier le niveau");

        nameField.setText(level.getName());
        descriptionField.setText(level.getDescription());
        difficultyCombo.setValue(getDifficultyString(level.getDifficulty()));
        minAgeSpinner.getValueFactory().setValue(level.getMinAge());
        maxAgeSpinner.getValueFactory().setValue(level.getMaxAge());
        pedagogGoalField.setText(level.getPedagGoal());
    }

    private String getDifficultyString(int difficulty) {
        switch (difficulty) {
            case 1: return "Facile";
            case 2: return "Moyen";
            case 3: return "Difficile";
            case 4: return "Expert";
            default: return "Moyen";
        }
    }

    // ==================== MÉTHODES POUR LA LISTE ====================

    private void setupTableColumns() {
        colId.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        colName.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        colDifficulty.setCellValueFactory(cellData ->
                new SimpleStringProperty(getDifficultyString(cellData.getValue().getDifficulty())));

        colAgeRange.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getMinAge() + " - " +
                        cellData.getValue().getMaxAge() + " ans"));

        colPedagGoal.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getPedagGoal()));

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
                    Level level = getTableView().getItems().get(getIndex());
                    editBtn.setOnAction(e -> editLevel(level));
                    deleteBtn.setOnAction(e -> deleteLevel(level));
                    setGraphic(container);
                }
            }
        });
    }

    private void loadLevels() {
        try {
            levelList = FXCollections.observableArrayList(levelService.getAll());
            filteredList = new FilteredList<>(levelList, p -> true);
            levelTable.setItems(filteredList);
            updateStatusLabel();
            updateStatistics();
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les niveaux : " + e.getMessage());
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
                    filteredList.setPredicate(level ->
                            level.getName().toLowerCase().contains(lowerFilter) ||
                                    level.getPedagGoal().toLowerCase().contains(lowerFilter)
                    );
                }
                updateStatusLabel();
            });
        }
    }

    private void updateStatusLabel() {
        if (statusLabel != null) {
            int size = filteredList.size();
            statusLabel.setText(size + " niveau(x)");
        }
    }

    @FXML
    private void onAddLevel() {
        openLevelForm(null);
    }

    private void editLevel(Level level) {
        openLevelForm(level);
    }

    private void deleteLevel(Level level) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer le niveau");
        alert.setContentText("Voulez-vous vraiment supprimer le niveau \"" + level.getName() + "\" ?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                levelService.delete(level.getId());
                levelList.remove(level);
                showAlert("Succès", "Niveau supprimé avec succès !");
                updateStatistics();
            } catch (Exception e) {
                showAlert("Erreur", "Impossible de supprimer : " + e.getMessage());
            }
        }
    }

    private void openLevelForm(Level level) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/teacher/level/ModiferLevel.fxml"));
            Parent root = loader.load();

            LevelController controller = loader.getController();
            if (level != null) {
                controller.setLevel(level);
            }

            Stage stage = new Stage();
            stage.setTitle(level == null ? "Ajouter un niveau" : "Modifier un niveau");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(levelTable.getScene().getWindow());
            stage.showAndWait();

            // Recharger la liste après fermeture
            loadLevels();

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

        // Ajouter un style personnalisé
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #F8F9FA;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #2E9E6E; -fx-font-weight: bold;");

        alert.showAndWait();
    }

    @FXML private void Page_Ajout()          { Router.go("Ajout_level"); }
    @FXML private void showLevels()          {
        nameField.clear();
        descriptionField.clear();
        pedagogGoalField.clear();
        clearAllFeedbacks();
        Router.go("levels_list"); }


    private void updateStatistics() {
        try {
            List<Level> allLevels = levelService.getAll();
            int total = allLevels.size();
            int easy = 0, medium = 0, hard = 0;

            for (Level level : allLevels) {
                if (level.getDifficulty() == 1) easy++;
                else if (level.getDifficulty() == 2) medium++;
                else if (level.getDifficulty() >= 3) hard++;
            }

            statTotalLevels.setText(String.valueOf(total));
            statEasyLevels.setText(String.valueOf(easy));
            statMediumLevels.setText(String.valueOf(medium));
            statHardLevels.setText(String.valueOf(hard));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setupSortButtons() {
        sortByNameAsc.setOnAction(e -> sortGames("name", "asc"));
        sortByNameDesc.setOnAction(e -> sortGames("name", "desc"));
        sortByTypeAsc.setOnAction(e -> sortGames("type", "asc"));
        sortByTypeDesc.setOnAction(e -> sortGames("type", "desc"));
    }

    private void sortGames(String field, String order) {
        Comparator<Game> comparator = null;

        if ("name".equals(field)) {
            comparator = (g1, g2) -> g1.getName().compareToIgnoreCase(g2.getName());
        } else if ("type".equals(field)) {
            comparator = (g1, g2) -> g1.getType().compareToIgnoreCase(g2.getType());
        }

        if ("desc".equals(order)) {
            comparator = comparator.reversed();
        }

        if (comparator != null) {
            FXCollections.sort(gameList, comparator);
            gameTable.refresh();
        }
    }
}