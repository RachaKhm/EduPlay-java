package dev.eduplay.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;

public class QuizGameController implements Initializable {

    @FXML private Label questionLabel;
    @FXML private Button option1, option2, option3, option4;
    @FXML private Label scoreLabel;
    @FXML private Label questionCounter;

    private List<Question> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int totalQuestions = 5;

    // Classe interne pour les questions
    private static class Question {
        String text;
        String[] options;
        int correctAnswer;

        Question(String text, String[] options, int correctAnswer) {
            this.text = text;
            this.options = options;
            this.correctAnswer = correctAnswer;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadQuestions();
        displayQuestion();

        // Assigner les actions aux boutons
        option1.setOnAction(e -> checkAnswer(0));
        option2.setOnAction(e -> checkAnswer(1));
        option3.setOnAction(e -> checkAnswer(2));
        option4.setOnAction(e -> checkAnswer(3));
    }

    private void loadQuestions() {
        questions = new ArrayList<>();

        // Question 1 - Mathématiques
        questions.add(new Question(
                "Combien font 7 × 8 ?",
                new String[]{"48", "56", "64", "72"},
                1
        ));

        // Question 2 - Géographie
        questions.add(new Question(
                "Quelle est la capitale de la France ?",
                new String[]{"Londres", "Berlin", "Paris", "Madrid"},
                2
        ));

        // Question 3 - Science
        questions.add(new Question(
                "Quel est le plus grand animal du monde ?",
                new String[]{"Éléphant", "Girafe", "Baleine bleue", "Requin blanc"},
                2
        ));

        // Question 4 - Culture générale
        questions.add(new Question(
                "Quelle couleur obtient-on en mélangeant du bleu et du jaune ?",
                new String[]{"Rouge", "Vert", "Orange", "Violet"},
                1
        ));

        // Question 5 - Animaux
        questions.add(new Question(
                "Quel animal est connu pour son long cou ?",
                new String[]{"Zèbre", "Girafe", "Lion", "Éléphant"},
                1
        ));
    }

    private void displayQuestion() {
        if (currentQuestionIndex < totalQuestions) {
            Question q = questions.get(currentQuestionIndex);
            questionLabel.setText(q.text);
            option1.setText(q.options[0]);
            option2.setText(q.options[1]);
            option3.setText(q.options[2]);
            option4.setText(q.options[3]);

            // Réinitialiser les couleurs des boutons
            resetButtonColors();

            // Mettre à jour le compteur de questions
            questionCounter.setText("Question " + (currentQuestionIndex + 1) + "/" + totalQuestions);
        }
    }

    private void resetButtonColors() {
        String defaultStyle = "-fx-background-color: #2E9E6E; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 10;";
        option1.setStyle("-fx-background-color: #2E9E6E; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 10;");
        option2.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 10;");
        option3.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 10;");
        option4.setStyle("-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 10;");
    }

    private void checkAnswer(int selectedIndex) {
        Question q = questions.get(currentQuestionIndex);
        Button selectedButton = getButtonByIndex(selectedIndex);

        if (selectedIndex == q.correctAnswer) {
            // Bonne réponse
            score += 10;
            scoreLabel.setText("Score: " + score);
            selectedButton.setStyle("-fx-background-color: #2E9E6E; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(46,158,110,0.5), 10, 0, 0, 0);");

            // Message de félicitations
            showFeedback("✅ Bonne réponse ! +10 points", "success");
        } else {
            // Mauvaise réponse
            selectedButton.setStyle("-fx-background-color: #E94560; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(233,69,96,0.5), 10, 0, 0, 0);");

            // Montrer la bonne réponse
            Button correctButton = getButtonByIndex(q.correctAnswer);
            correctButton.setStyle("-fx-background-color: #2E9E6E; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10; -fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(46,158,110,0.5), 10, 0, 0, 0);");

            showFeedback("❌ Mauvaise réponse ! La bonne réponse était : " + q.options[q.correctAnswer], "error");
        }

        // Désactiver les boutons temporairement
        disableButtons(true);

        // Passer à la question suivante après 1.5 secondes
        new java.util.Timer().schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                javafx.application.Platform.runLater(() -> {
                    currentQuestionIndex++;

                    if (currentQuestionIndex < totalQuestions) {
                        displayQuestion();
                        disableButtons(false);
                    } else {
                        endGame();
                    }
                });
            }
        }, 1500);
    }

    private Button getButtonByIndex(int index) {
        switch (index) {
            case 0: return option1;
            case 1: return option2;
            case 2: return option3;
            case 3: return option4;
            default: return option1;
        }
    }

    private void disableButtons(boolean disable) {
        option1.setDisable(disable);
        option2.setDisable(disable);
        option3.setDisable(disable);
        option4.setDisable(disable);
    }

    private void showFeedback(String message, String type) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Résultat");
        alert.setHeaderText(null);
        alert.setContentText(message);

        if ("success".equals(type)) {
            alert.getDialogPane().setStyle("-fx-background-color: #F8F9FA;");
            alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: #2E9E6E; -fx-font-weight: bold;");
        } else {
            alert.getDialogPane().setStyle("-fx-background-color: #F8F9FA;");
            alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: #E94560; -fx-font-weight: bold;");
        }

        alert.showAndWait();
    }

    private void endGame() {
        String message;
        String emoji;

        if (score >= 40) {
            message = "🏆 Excellent ! Tu es un champion ! 🏆";
            emoji = "🎉";
        } else if (score >= 30) {
            message = "🌟 Très bien ! Continue comme ça ! 🌟";
            emoji = "👍";
        } else if (score >= 20) {
            message = "📚 Bon travail ! Révise un peu et tu feras mieux ! 📚";
            emoji = "💪";
        } else {
            message = "🎯 Ne baisse pas les bras ! Réessaie pour t'améliorer ! 🎯";
            emoji = "⭐";
        }

        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Fin du jeu");
        alert.setHeaderText(emoji + " Jeu terminé ! " + emoji);
        alert.setContentText(message + "\n\n📊 Ton score final : " + score + "/" + (totalQuestions * 10));

        alert.showAndWait();
        closeGame();
    }

    @FXML
    private void closeGame() {
        Stage stage = (Stage) questionLabel.getScene().getWindow();
        stage.close();
    }
}