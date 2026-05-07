package dev.eduplay.controllers;
import javafx.application.Platform;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

public class AdventureGameController implements Initializable {

    @FXML private Pane gamePane;
    @FXML private Label scoreLabel, levelLabel, livesLabel, speedLabel;
    @FXML private ProgressBar healthBar;

    private Rectangle player;
    private List<Rectangle> obstacles = new ArrayList<>();
    private List<Rectangle> powerUps = new ArrayList<>();
    private List<Rectangle> groundTiles = new ArrayList<>();

    private double playerVelocityY = 0;
    private boolean isJumping = false;
    private boolean gameRunning = true;
    private boolean isInvulnerable = false;

    private int score = 0, lives = 3, level = 1;
    private double speed = 5, boost = 0;
    private Random random = new Random();

    private static final int GROUND_Y = 400;
    private static final int PLAYER_SIZE = 40;

    private List<Question> questionBank = new ArrayList<>(Arrays.asList(
            new Question("7 + 5 ?", new String[]{"10", "11", "12", "13"}, 2),
            new Question("Capitale de la France ?", new String[]{"Lyon", "Berlin", "Paris", "Marseille"}, 2),
            new Question("9 x 3 ?", new String[]{"24", "27", "30", "33"}, 1),
            new Question("2 + 2 x 2 ?", new String[]{"8", "6", "4", "10"}, 1)
    ));

    private static class Question {
        String text; String[] options; int correct;
        Question(String t, String[] o, int c) { this.text = t; this.options = o; this.correct = c; }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupGame();

        // Attendre que la scène soit chargée pour attacher les contrôles
        Platform.runLater(() -> {
            if (gamePane.getScene() != null) {
                gamePane.getScene().setOnKeyPressed(e -> {
                    if ((e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.UP) && !isJumping && gameRunning) {
                        playerVelocityY = -13;
                        isJumping = true;
                    }
                });
            }
        });

        startGameLoop();
    }

    private void setupGame() {
        // Sol défilant
        for (int i = 0; i < 2; i++) {
            Rectangle g = new Rectangle(i * 800, GROUND_Y, 801, 100);
            g.setFill(Color.web("#2E9E6E"));
            gamePane.getChildren().add(g);
            groundTiles.add(g);
        }

        // Joueur
        player = new Rectangle(50, GROUND_Y - PLAYER_SIZE, PLAYER_SIZE, PLAYER_SIZE);
        player.setFill(Color.web("#E94560"));
        player.setArcWidth(10); player.setArcHeight(10);
        gamePane.getChildren().add(player);

        updateUI();
    }

    private void startGameLoop() {
        new AnimationTimer() {
            long lastObstacle = 0, lastPowerUp = 0;
            @Override
            public void handle(long now) {
                if (!gameRunning) return;

                updatePhysics();
                updateGround();

                // Spawn des entités
                if (now - lastObstacle > 1_500_000_000 / (1 + speed/10)) {
                    spawnObstacle();
                    lastObstacle = now;
                }
                if (now - lastPowerUp > 7_000_000_000L) {
                    spawnPowerUp();
                    lastPowerUp = now;
                }

                updateEntities();
            }
        }.start();
    }

    private void updatePhysics() {
        playerVelocityY += 0.6; // Pesanteur
        player.setY(player.getY() + playerVelocityY);

        if (player.getY() >= GROUND_Y - PLAYER_SIZE) {
            player.setY(GROUND_Y - PLAYER_SIZE);
            playerVelocityY = 0;
            isJumping = false;
        }
    }

    private void updateGround() {
        for (Rectangle g : groundTiles) {
            g.setX(g.getX() - (speed + boost));
            if (g.getX() <= -800) g.setX(798);
        }
    }

    private void updateEntities() {
        double currentSpeed = speed + boost;
        boost = Math.max(0, boost - 0.02);

        // Gestion obstacles
        Iterator<Rectangle> obsIt = obstacles.iterator();
        while (obsIt.hasNext()) {
            Rectangle obs = obsIt.next();
            obs.setX(obs.getX() - currentSpeed);

            if (player.getBoundsInParent().intersects(obs.getBoundsInParent()) && !isInvulnerable) {
                takeDamage();
                gamePane.getChildren().remove(obs);
                obsIt.remove();
            } else if (obs.getX() < -50) {
                score += 10;
                updateUI();
                gamePane.getChildren().remove(obs);
                obsIt.remove();
            }
        }

        // Gestion PowerUps
        Iterator<Rectangle> pIt = powerUps.iterator();
        while (pIt.hasNext()) {
            Rectangle p = pIt.next();
            p.setX(p.getX() - currentSpeed);

            if (player.getBoundsInParent().intersects(p.getBoundsInParent())) {
                showQuestionUI();
                gamePane.getChildren().remove(p);
                pIt.remove();
            } else if (p.getX() < -50) {
                gamePane.getChildren().remove(p);
                pIt.remove();
            }
        }

        if (score >= level * 200) levelUp();
    }

    private void takeDamage() {
        lives--;
        updateUI();
        if (lives <= 0) {
            gameOver();
        } else {
            isInvulnerable = true;
            FadeTransition ft = new FadeTransition(Duration.millis(200), player);
            ft.setFromValue(1.0); ft.setToValue(0.2);
            ft.setCycleCount(6); ft.setAutoReverse(true);
            ft.setOnFinished(e -> isInvulnerable = false);
            ft.play();
        }
    }

    private void showQuestionUI() {
        gameRunning = false;
        Question q = questionBank.get(random.nextInt(questionBank.size()));

        VBox qBox = new VBox(15);
        qBox.setAlignment(Pos.CENTER);
        qBox.setStyle("-fx-background-color: rgba(20, 20, 45, 0.95); -fx-padding: 25; -fx-background-radius: 15; -fx-border-color: #F39C12; -fx-border-width: 2;");
        qBox.setLayoutX(250); qBox.setLayoutY(100);
        qBox.setPrefWidth(300);

        Text title = new Text("QUESTION !");
        title.setFill(Color.web("#F39C12"));
        title.setFont(Font.font("System", FontWeight.BOLD, 18));

        Text qTxt = new Text(q.text);
        qTxt.setFill(Color.WHITE);
        qTxt.setFont(Font.font(16));

        qBox.getChildren().addAll(title, qTxt);

        for (int i = 0; i < q.options.length; i++) {
            int idx = i;
            Button btn = new Button(q.options[i]);
            btn.setPrefWidth(200);
            btn.setOnAction(e -> {
                if (idx == q.correct) {
                    score += 50; boost = 6;
                    showMessage("Gagné ! +50 pts + Boost", "#2E9E6E");
                } else {
                    showMessage("Perdu ! La réponse était : " + q.options[q.correct], "#E94560");
                }
                gamePane.getChildren().remove(qBox);
                gameRunning = true;
                updateUI();
            });
            qBox.getChildren().add(btn);
        }
        gamePane.getChildren().add(qBox);
    }

    private void spawnObstacle() {
        Rectangle o = new Rectangle(850, GROUND_Y - 40, 30, 40);
        o.setFill(Color.web("#E94560"));
        obstacles.add(o);
        gamePane.getChildren().add(o);
    }

    private void spawnPowerUp() {
        Rectangle p = new Rectangle(850, GROUND_Y - 100, 25, 25);
        p.setFill(Color.web("#F39C12"));
        p.setRotate(45);
        powerUps.add(p);
        gamePane.getChildren().add(p);
    }

    private void levelUp() {
        level++; speed += 0.5;
        showMessage("NIVEAU " + level, "#9B59B6");
    }

    private void showMessage(String msg, String color) {
        Text t = new Text(msg);
        t.setFont(Font.font("System", FontWeight.BOLD, 20));
        t.setFill(Color.web(color));
        t.setX(250); t.setY(80);
        gamePane.getChildren().add(t);

        FadeTransition ft = new FadeTransition(Duration.seconds(2), t);
        ft.setToValue(0);
        ft.setOnFinished(e -> gamePane.getChildren().remove(t));
        ft.play();
    }

    private void updateUI() {
        scoreLabel.setText("🏆 " + score);
        levelLabel.setText("🎮 Lvl: " + level);
        livesLabel.setText("❤️ " + lives);
        speedLabel.setText("⚡ " + String.format("%.1f", speed + boost));
        healthBar.setProgress(lives / 3.0);
    }

    private void gameOver() {
        gameRunning = false;
        VBox over = new VBox(20);
        over.setAlignment(Pos.CENTER);
        over.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 50;");
        over.setPrefSize(800, 500);

        Text t = new Text("GAME OVER\nScore: " + score);
        t.setFill(Color.WHITE);
        t.setFont(Font.font("System", FontWeight.BOLD, 40));

        Button btn = new Button("Fermer");
        btn.setOnAction(e -> closeGame());

        over.getChildren().addAll(t, btn);
        gamePane.getChildren().add(over);
    }

    @FXML private void closeGame() {
        ((Stage) gamePane.getScene().getWindow()).close();
    }
}