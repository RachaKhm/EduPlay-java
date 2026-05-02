package dev.eduplay.controllers;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class MemoryGameController implements Initializable {

    @FXML private GridPane gridPane;
    @FXML private Label scoreLabel;
    @FXML private Label turnsLabel;
    @FXML private Label pairsLabel;

    private Button[][] cards;
    private String[][] cardValues;
    private boolean[][] cardFlipped;
    private boolean[][] cardMatched;

    private int firstRow = -1, firstCol = -1;
    private int secondRow = -1, secondCol = -1;
    private boolean waiting = false;
    private int pairsFound = 0;
    private int totalPairs = 8;
    private int turns = 0;
    private int score = 0;

    // Liste des emojis pour les cartes
    private final String[] emojis = {
            "🐶", "🐱", "🐭", "🐹", "🐰", "🦊", "🐻", "🐼",
            "🐨", "🐯", "🦁", "🐮", "🐷", "🐸", "🐵", "🐔"
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupGame();
    }

    private void setupGame() {
        int size = 4; // 4x4 grid = 8 paires
        cards = new Button[size][size];
        cardValues = new String[size][size];
        cardFlipped = new boolean[size][size];
        cardMatched = new boolean[size][size];

        gridPane.getChildren().clear();

        // Créer les paires de cartes
        List<String> cardList = new ArrayList<>();
        for (int i = 0; i < totalPairs; i++) {
            cardList.add(emojis[i]);
            cardList.add(emojis[i]); // Chaque emoji apparaît deux fois
        }
        Collections.shuffle(cardList);

        // Remplir la grille
        int index = 0;
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                cardValues[row][col] = cardList.get(index++);
                cardFlipped[row][col] = false;
                cardMatched[row][col] = false;

                Button card = createCardButton(row, col);
                cards[row][col] = card;
                gridPane.add(card, col, row);
            }
        }

        updateLabels();
    }

    private Button createCardButton(int row, int col) {
        Button btn = new Button();
        btn.setPrefSize(100, 100);
        btn.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 10; -fx-font-size: 36px;");
        btn.setAlignment(Pos.CENTER);
        btn.setText("?");
        btn.setFont(Font.font(30));

        btn.setOnAction(e -> onCardClick(row, col));
        return btn;
    }

    private void onCardClick(int row, int col) {
        if (waiting) return;
        if (cardMatched[row][col]) return;
        if (cardFlipped[row][col]) return;

        // Afficher la carte
        flipCard(row, col, true);

        if (firstRow == -1 && firstCol == -1) {
            // Première carte
            firstRow = row;
            firstCol = col;
        } else if (secondRow == -1 && secondCol == -1) {
            // Deuxième carte
            secondRow = row;
            secondCol = col;
            turns++;
            updateLabels();

            // Vérifier si les cartes correspondent
            if (cardValues[firstRow][firstCol].equals(cardValues[secondRow][secondCol])) {
                // Match trouvé !
                pairsFound++;
                score += 10;
                updateLabels();

                cardMatched[firstRow][firstCol] = true;
                cardMatched[secondRow][secondCol] = true;

                firstRow = -1;
                firstCol = -1;
                secondRow = -1;
                secondCol = -1;

                // Vérifier si le jeu est terminé
                if (pairsFound == totalPairs) {
                    endGame();
                }
            } else {
                // Pas de match, retourner les cartes après 1 seconde
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
            card.setStyle("-fx-background-color: #E94560; -fx-background-radius: 10; -fx-font-size: 36px;");
        } else {
            card.setText("?");
            card.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 10; -fx-font-size: 36px;");
        }
    }

    private void updateLabels() {
        scoreLabel.setText("Score: " + score);
        turnsLabel.setText("Tours: " + turns);
        pairsLabel.setText("Paires: " + pairsFound + "/" + totalPairs);
    }

    private void endGame() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Félicitations !");
        alert.setHeaderText("🎉 Vous avez gagné ! 🎉");
        alert.setContentText("Score final: " + score + "\nTours effectués: " + turns);
        alert.showAndWait();

        // Rejouer?
        ButtonType replay = new ButtonType("Rejouer", ButtonBar.ButtonData.OK_DONE);
        ButtonType quit = new ButtonType("Quitter", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(replay, quit);

        alert.showAndWait().ifPresent(response -> {
            if (response == replay) {
                resetGame();
            } else {
                closeGame();
            }
        });
    }

    @FXML
    private void resetGame() {
        pairsFound = 0;
        turns = 0;
        score = 0;
        firstRow = -1;
        firstCol = -1;
        secondRow = -1;
        secondCol = -1;
        waiting = false;
        setupGame();
    }

    @FXML
    private void closeGame() {
        Stage stage = (Stage) gridPane.getScene().getWindow();
        stage.close();
    }
}