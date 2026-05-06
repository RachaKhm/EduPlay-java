package dev.eduplay.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class SlidingPuzzleController implements Initializable {

    @FXML private GridPane gridPane;
    @FXML private Label scoreLabel;
    @FXML private Label movesLabel;
    @FXML private Label timeLabel;
    @FXML private Label difficultyLabel;

    private Button[][] buttons;
    private int[][] numbers;
    private int emptyRow, emptyCol;
    private int moves = 0;
    private int score = 0;
    private int size = 4; // 4x4 grille
    private int minutes = 0;
    private int seconds = 0;
    private Timeline timeline;
    private boolean gameRunning = true;

    private final String[] colors = {
            "#E94560", "#FF6B6B", "#F39C12", "#2E9E6E",
            "#3498DB", "#9B59B6", "#1ABC9C", "#E67E22"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startNewGame();
    }

    private void startNewGame() {
        // Afficher l'objectif au début
        showObjective();
        gridPane.getChildren().clear();
        buttons = new Button[size][size];
        numbers = new int[size][size];

        // Initialiser les nombres 1 à 15 et case vide (0)
        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i < size * size; i++) {
            nums.add(i);
        }
        nums.add(0); // Case vide
        Collections.shuffle(nums);

        // Remplir la grille
        int index = 0;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                numbers[row][col] = nums.get(index++);
                if (numbers[row][col] == 0) {
                    emptyRow = row;
                    emptyCol = col;
                }
                createButton(row, col);
            }
        }

        // Vérifier si le puzzle est soluble
        if (!isSolvable(numbers)) {
            // Échanger deux cases pour rendre soluble
            for (int row = 0; row < size; row++) {
                for (int col = 0; col < size; col++) {
                    if (numbers[row][col] != 0 && numbers[row][col] != 1) {
                        int temp = numbers[row][col];
                        numbers[row][col] = numbers[0][0];
                        numbers[0][0] = temp;
                        if (numbers[0][0] == 0) {
                            emptyRow = 0;
                            emptyCol = 0;
                        }
                        createButton(row, col);
                        createButton(0, 0);
                        return;
                    }
                }
            }
        }

        moves = 0;
        score = 0;
        updateLabels();
        startTimer();
    }

    private void createButton(int row, int col) {
        Button btn = new Button();
        btn.setPrefSize(80, 80);
        btn.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");

        if (numbers[row][col] != 0) {
            btn.setText(String.valueOf(numbers[row][col]));
            int colorIndex = (numbers[row][col] - 1) % colors.length;
            btn.setStyle("-fx-background-color: " + colors[colorIndex] + "; " +
                    "-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; " +
                    "-fx-background-radius: 10; -fx-cursor: hand;");
        } else {
            btn.setText("");
            btn.setStyle("-fx-background-color: #1A1A2E; -fx-background-radius: 10; -fx-cursor: hand;");
        }

        final int r = row, c = col;
        btn.setOnAction(e -> onButtonClick(r, c));
        buttons[row][col] = btn;
        gridPane.add(btn, col, row);
    }

    private void onButtonClick(int row, int col) {
        if (!gameRunning) return;

        // Vérifier si la case cliquée est adjacente à la case vide
        if ((Math.abs(row - emptyRow) + Math.abs(col - emptyCol)) == 1) {
            // Échanger les cases
            numbers[emptyRow][emptyCol] = numbers[row][col];
            numbers[row][col] = 0;

            createButton(emptyRow, emptyCol);
            createButton(row, col);

            emptyRow = row;
            emptyCol = col;

            moves++;
            score = Math.max(0, 1000 - (moves * 2));
            updateLabels();

            // Vérifier si le puzzle est résolu
            if (isSolved()) {
                endGame(true);
            }
        }
    }

    private boolean isSolvable(int[][] puzzle) {
        // Compter les inversions
        int[] flat = new int[size * size];
        int index = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                flat[index++] = puzzle[i][j];
            }
        }

        int inversions = 0;
        for (int i = 0; i < flat.length - 1; i++) {
            for (int j = i + 1; j < flat.length; j++) {
                if (flat[i] != 0 && flat[j] != 0 && flat[i] > flat[j]) {
                    inversions++;
                }
            }
        }

        // Pour une grille 4x4, la solvabilité dépend des inversions
        return inversions % 2 == 0;
    }

    private boolean isSolved() {
        int expected = 1;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                if (row == size - 1 && col == size - 1) {
                    return numbers[row][col] == 0;
                }
                if (numbers[row][col] != expected) {
                    return false;
                }
                expected++;
            }
        }
        return true;
    }

    private void startTimer() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (gameRunning) {
                seconds++;
                if (seconds == 60) {
                    minutes++;
                    seconds = 0;
                }
                updateTimerDisplay();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTimerDisplay() {
        if (timeLabel != null) {
            timeLabel.setText(String.format("⏱️ %02d:%02d", minutes, seconds));
        }
    }

    private void updateLabels() {
        if (scoreLabel != null) scoreLabel.setText("🎯 Score: " + score);
        if (movesLabel != null) movesLabel.setText("🔄 Mouvements: " + moves);
    }

    private void endGame(boolean won) {
        gameRunning = false;
        if (timeline != null) {
            timeline.stop();
        }

        String title = won ? "🎉 FÉLICITATIONS ! 🎉" : "💀 GAME OVER 💀";
        String message;

        if (won) {
            int timeBonus = calculateTimeBonus();
            int moveBonus = calculateMoveBonus();
            int totalBonus = timeBonus + moveBonus;
            int finalScore = score + totalBonus;

            message = "🎯 Score final: " + finalScore + "\n" +
                    "🔄 Mouvements: " + moves + "\n" +
                    "⏱️ Temps: " + String.format("%02d:%02d", minutes, seconds) + "\n\n" +
                    "✨ Bonus temps: +" + timeBonus + "\n" +
                    "✨ Bonus mouvements: +" + moveBonus + "\n" +
                    "🏆 Bonus total: +" + totalBonus;
        } else {
            message = "Dommage ! Réessaie pour faire mieux !";
        }

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Fin du jeu");
        alert.setHeaderText(title);
        alert.setContentText(message);

        javafx.scene.control.ButtonType replay = new javafx.scene.control.ButtonType("🔄 Rejouer", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        javafx.scene.control.ButtonType quit = new javafx.scene.control.ButtonType("❌ Quitter", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(replay, quit);

        alert.showAndWait().ifPresent(response -> {
            if (response == replay) {
                resetGame();
            } else {
                closeGame();
            }
        });
    }

    private int calculateTimeBonus() {
        int totalSeconds = minutes * 60 + seconds;
        if (totalSeconds < 30) return 200;
        if (totalSeconds < 60) return 150;
        if (totalSeconds < 90) return 100;
        if (totalSeconds < 120) return 50;
        return 20;
    }

    private int calculateMoveBonus() {
        if (moves < 50) return 200;
        if (moves < 100) return 150;
        if (moves < 150) return 100;
        if (moves < 200) return 50;
        return 20;
    }

    @FXML
    private void resetGame() {
        if (timeline != null) {
            timeline.stop();
        }
        minutes = 0;
        seconds = 0;
        gameRunning = true;
        startNewGame();
    }

    @FXML
    private void closeGame() {
        if (timeline != null) {
            timeline.stop();
        }
        Stage stage = (Stage) gridPane.getScene().getWindow();
        stage.close();
    }
    private void showObjective() {
        // Créer une nouvelle fenêtre pour montrer l'objectif
        Stage objectiveStage = new Stage();
        objectiveStage.setTitle("🎯 Objectif du jeu - À atteindre !");
        objectiveStage.setResizable(false);

        GridPane objectiveGrid = new GridPane();
        objectiveGrid.setAlignment(javafx.geometry.Pos.CENTER);
        objectiveGrid.setHgap(8);
        objectiveGrid.setVgap(8);
        objectiveGrid.setPadding(new javafx.geometry.Insets(20));

        // Créer l'état final (grille gagnante)
        int[][] target = {
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12},
                {13, 14, 15, 0}
        };

        String[] colors = {"#E94560", "#FF6B6B", "#F39C12", "#2E9E6E",
                "#3498DB", "#9B59B6", "#1ABC9C", "#E67E22"};

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                Button btn = new Button();
                btn.setPrefSize(70, 70);
                btn.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-radius: 10;");

                if (target[row][col] != 0) {
                    btn.setText(String.valueOf(target[row][col]));
                    int colorIndex = (target[row][col] - 1) % colors.length;
                    btn.setStyle("-fx-background-color: " + colors[colorIndex] + "; " +
                            "-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; " +
                            "-fx-background-radius: 10;");
                } else {
                    btn.setText("");
                    btn.setStyle("-fx-background-color: #1A1A2E; -fx-background-radius: 10;");
                }
                objectiveGrid.add(btn, col, row);
            }
        }

        // Ajouter un label explicatif
        VBox objectiveBox = new VBox(15);
        objectiveBox.setAlignment(javafx.geometry.Pos.CENTER);
        objectiveBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1A1A2E, #16213E); -fx-padding: 20;");

        Label titleLabel = new Label("🎯 OBJECTIF DU JEU 🎯");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #E94560;");

        Label descLabel = new Label("Remettez les nombres dans cet ordre !\nLa case vide doit être en bas à droite.");
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-text-alignment: center;");
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        Label instructionLabel = new Label("💡 Conseil : Résolvez ligne par ligne du haut vers le bas");
        instructionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #F39C12;");

        Button startBtn = new Button("🚀 COMMENCER LE JEU 🚀");
        startBtn.setStyle("-fx-background-color: #2E9E6E; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 20; -fx-cursor: hand;");
        startBtn.setOnAction(e -> objectiveStage.close());

        objectiveBox.getChildren().addAll(titleLabel, objectiveGrid, descLabel, instructionLabel, startBtn);

        Scene objectiveScene = new Scene(objectiveBox, 450, 550);
        objectiveStage.setScene(objectiveScene);
        objectiveStage.showAndWait();
    }
}