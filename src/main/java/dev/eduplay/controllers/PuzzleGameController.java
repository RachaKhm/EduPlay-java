package dev.eduplay.controllers;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class PuzzleGameController implements Initializable {

    @FXML private GridPane gridPane;
    @FXML private Label scoreLabel;
    @FXML private Label movesLabel;
    @FXML private Label timeLabel;
    @FXML private Label pairsLabel;

    private Button[][] cards;
    private String[][] cardValues;
    private boolean[][] cardFlipped;
    private boolean[][] cardMatched;

    private int firstRow = -1, firstCol = -1;
    private int secondRow = -1, secondCol = -1;
    private boolean waiting = false;
    private int pairsFound = 0;
    private int score = 0;
    private int moves = 0;
    private int totalPairs = 8;

    private Timer timer;
    private int seconds = 0;
    private int minutes = 0;

    private final String[] emojis = {
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startNewGame();
    }

    private void startNewGame() {

        gridPane.getChildren().clear();
        cards = new Button[4][4];
        cardValues = new String[4][4];
        cardFlipped = new boolean[4][4];
        cardMatched = new boolean[4][4];
        pairsFound = 0;
        score = 0;
        moves = 0;
        firstRow = -1;
        firstCol = -1;
        secondRow = -1;
        secondCol = -1;
        waiting = false;

        updateLabels();
        startTimer();

        // Mélanger les cartes
        List<String> cardList = new ArrayList<>();
        for (String emoji : emojis) {
            cardList.add(emoji);
            cardList.add(emoji);
        }
        Collections.shuffle(cardList);

        int index = 0;
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                cardValues[row][col] = cardList.get(index++);

                Button btn = new Button("?");
                btn.setPrefSize(90, 90);
                btn.setStyle("-fx-background-color: linear-gradient(to bottom, #2C3E50, #34495E); " +
                        "-fx-background-radius: 12; -fx-font-size: 35px; -fx-font-weight: bold; " +
                        "-fx-text-fill: white; -fx-cursor: hand;");

                final int r = row, c = col;
                btn.setOnAction(e -> onCardClick(r, c));

                cards[row][col] = btn;
                gridPane.add(btn, col, row);
            }
        }
    }

    private void onCardClick(int row, int col) {
        if (waiting) return;
        if (cardMatched[row][col]) return;
        if (cardFlipped[row][col]) return;

        // Révéler la carte
        flipCard(row, col, true);
        moves++;
        updateLabels();

        if (firstRow == -1) {
            firstRow = row;
            firstCol = col;
        } else if (secondRow == -1) {
            secondRow = row;
            secondCol = col;

            // Vérifier si les cartes correspondent
            if (cardValues[firstRow][firstCol].equals(cardValues[secondRow][secondCol])) {
                // Match trouvé
                pairsFound++;
                score += 10;
                moves--;
                updateLabels();

                cardMatched[firstRow][firstCol] = true;
                cardMatched[secondRow][secondCol] = true;

                // Garder les cartes visibles
                cards[firstRow][firstCol].setStyle("-fx-background-color: linear-gradient(to bottom, #2E9E6E, #1B5E3F); " +
                        "-fx-background-radius: 12; -fx-font-size: 35px; -fx-font-weight: bold; " +
                        "-fx-text-fill: white;");
                cards[secondRow][secondCol].setStyle("-fx-background-color: linear-gradient(to bottom, #2E9E6E, #1B5E3F); " +
                        "-fx-background-radius: 12; -fx-font-size: 35px; -fx-font-weight: bold; " +
                        "-fx-text-fill: white;");

                firstRow = -1;
                firstCol = -1;
                secondRow = -1;
                secondCol = -1;

                if (pairsFound == totalPairs) {
                    endGame(true);
                }
            } else {
                // Pas de match
                waiting = true;
                PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(e -> {
                    flipCard(firstRow, firstCol, false);
                    flipCard(secondRow, secondCol, false);

                    firstRow = -1;
                    firstCol = -1;
                    secondRow = -1;
                    secondCol = -1;
                    waiting = false;
                });
                pause.play();
            }
        }
    }

    private void flipCard(int row, int col, boolean show) {
        cardFlipped[row][col] = show;
        Button card = cards[row][col];

        if (show) {
            card.setText(cardValues[row][col]);
            card.setStyle("-fx-background-color: linear-gradient(to bottom, #E94560, #C0392B); " +
                    "-fx-background-radius: 12; -fx-font-size: 35px; -fx-font-weight: bold; " +
                    "-fx-text-fill: white;");
        } else {
            card.setText("?");
            card.setStyle("-fx-background-color: linear-gradient(to bottom, #2C3E50, #34495E); " +
                    "-fx-background-radius: 12; -fx-font-size: 35px; -fx-font-weight: bold; " +
                    "-fx-text-fill: white;");
        }
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    if (!waiting && pairsFound < totalPairs) {
                        seconds++;
                        if (seconds == 60) {
                            minutes++;
                            seconds = 0;
                        }
                        updateTimerDisplay();
                    }
                });
            }
        }, 1000, 1000);
    }

    private void updateTimerDisplay() {
        if (timeLabel != null) {
            timeLabel.setText(String.format("⏱️ %02d:%02d", minutes, seconds));
        }
    }

    private void updateLabels() {
        if (scoreLabel != null) scoreLabel.setText("🎯 Score: " + score);
        if (movesLabel != null) movesLabel.setText("🔄 Mouvements: " + moves);
        if (pairsLabel != null) pairsLabel.setText("🔗 Paires: " + pairsFound + "/" + totalPairs);
    }

    private void endGame(boolean won) {
        if (timer != null) {
            timer.cancel();
        }

        String title = won ? "🎉 Félicitations ! 🎉" : "💀 Partie terminée 💀";
        String message;

        if (won) {
            int bonus = calculateBonus();
            score += bonus;
            updateLabels();
            message = "🎯 Score final: " + score + "\n" +
                    "🔄 Mouvements: " + moves + "\n" +
                    "⏱️ Temps: " + String.format("%02d:%02d", minutes, seconds) + "\n" +
                    "✨ Bonus temps: +" + bonus + " points !";
        } else {
            message = "Dommage ! Réessaie pour faire mieux !";
        }

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Fin du jeu");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();

        // Proposer de rejouer
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

    private int calculateBonus() {
        int bonus = 0;
        if (minutes == 0 && seconds < 30) bonus = 100;
        else if (minutes == 0 && seconds < 60) bonus = 50;
        else if (minutes == 1 && seconds < 30) bonus = 30;
        else if (minutes == 1 && seconds < 60) bonus = 20;
        else bonus = 10;
        return bonus;
    }

    @FXML
    private void resetGame() {
        if (timer != null) {
            timer.cancel();
        }
        minutes = 0;
        seconds = 0;
        startNewGame();
    }

    @FXML
    private void closeGame() {
        if (timer != null) {
            timer.cancel();
        }
        Stage stage = (Stage) gridPane.getScene().getWindow();
        stage.close();
    }
}