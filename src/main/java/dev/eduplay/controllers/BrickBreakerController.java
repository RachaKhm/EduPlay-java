package dev.eduplay.controllers;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class BrickBreakerController implements Initializable {

    @FXML private Pane gamePane;
    @FXML private Label scoreLabel;
    @FXML private Label livesLabel;
    @FXML private Label levelLabel;

    private Rectangle paddle;
    private Circle ball;
    private List<Rectangle> bricks = new ArrayList<>();

    private double ballVelocityX = 3;
    private double ballVelocityY = -3;
    private int score = 0;
    private int lives = 3;
    private int currentLevel = 1;
    private int bricksCount = 0;

    private AnimationTimer gameLoop;
    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean gameRunning = true;
    private Scene scene;

    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 12;
    private static final int BALL_SIZE = 8; // Légèrement réduit pour plus de précision
    private static final int BRICK_WIDTH = 70;
    private static final int BRICK_HEIGHT = 25;

    private Random random = new Random();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // CRITIQUE : On attend que le layout soit calculé pour placer les objets
        Platform.runLater(() -> {
            setupGame();
            setupKeyboardControls();
            startGameLoop();
        });

        gamePane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                scene = newScene;
                setupKeyboardControls(); // Ré-attacher au cas où
            }
        });
    }

    private void setupGame() {
        gamePane.getChildren().clear();

        paddle = new Rectangle(PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setFill(Color.web("#E94560"));
        paddle.setArcWidth(10);
        paddle.setArcHeight(10);

        // Positionnement basé sur la largeur réelle maintenant disponible
        paddle.setX((gamePane.getWidth() - PADDLE_WIDTH) / 2);
        paddle.setY(gamePane.getHeight() - 50);
        gamePane.getChildren().add(paddle);

        ball = new Circle(BALL_SIZE);
        ball.setFill(Color.web("#FF6B6B"));
        resetBall();
        gamePane.getChildren().add(ball);

        createLevel();
    }

    private void createLevel() {
        // Supprimer les anciennes briques sans supprimer la raquette et la balle
        if (bricks != null) {
            gamePane.getChildren().removeAll(bricks);
            bricks.clear();
        }

        int rows = Math.min(3 + currentLevel, 6);
        int cols = 8;
        bricksCount = 0;

        Color[] colors = { Color.web("#E94560"), Color.web("#F39C12"), Color.web("#2E9E6E"), Color.web("#3498DB") };

        double spacing = 5;
        double totalWidth = (cols * BRICK_WIDTH) + ((cols - 1) * spacing);
        double startX = (gamePane.getWidth() - totalWidth) / 2;
        double startY = 60;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Rectangle brick = new Rectangle(BRICK_WIDTH, BRICK_HEIGHT);
                brick.setFill(colors[row % colors.length]);
                brick.setX(startX + col * (BRICK_WIDTH + spacing));
                brick.setY(startY + row * (BRICK_HEIGHT + spacing));
                brick.setUserData(10); // Points

                gamePane.getChildren().add(brick);
                bricks.add(brick);
                bricksCount++;
            }
        }
        levelLabel.setText("Niveau: " + currentLevel);
    }

    private void setupKeyboardControls() {
        if (gamePane.getScene() == null) return;
        scene = gamePane.getScene();

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.Q) leftPressed = true;
            if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) rightPressed = true;
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.Q) leftPressed = false;
            if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) rightPressed = false;
        });
    }

    private void startGameLoop() {
        if (gameLoop != null) gameLoop.stop();
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameRunning) update();
            }
        };
        gameLoop.start();
    }

    private void update() {
        // Mouvement Paddle
        if (leftPressed && paddle.getX() > 0) paddle.setX(paddle.getX() - 8);
        if (rightPressed && paddle.getX() < gamePane.getWidth() - PADDLE_WIDTH) paddle.setX(paddle.getX() + 8);

        // Mouvement Balle
        ball.setCenterX(ball.getCenterX() + ballVelocityX);
        ball.setCenterY(ball.getCenterY() + ballVelocityY);

        // Rebond murs
        if (ball.getCenterX() <= BALL_SIZE || ball.getCenterX() >= gamePane.getWidth() - BALL_SIZE) {
            ballVelocityX *= -1;
        }
        if (ball.getCenterY() <= BALL_SIZE) {
            ballVelocityY *= -1;
        }

        // Perte de vie
        if (ball.getCenterY() >= gamePane.getHeight()) {
            lives--;
            livesLabel.setText("❤️ Vies: " + lives);
            if (lives <= 0) gameOver();
            else resetBall();
            return;
        }

        // Collision Paddle
        if (ball.getBoundsInParent().intersects(paddle.getBoundsInParent())) {
            ballVelocityY = -Math.abs(ballVelocityY); // Toujours vers le haut
            // Ajouter un peu d'effet selon l'endroit où on touche le paddle
            double diff = ball.getCenterX() - (paddle.getX() + PADDLE_WIDTH/2);
            ballVelocityX = diff * 0.15;
            ball.setCenterY(paddle.getY() - BALL_SIZE - 1);
        }

        // Collision Briques
        Rectangle brickHit = null;
        for (Rectangle brick : bricks) {
            if (ball.getBoundsInParent().intersects(brick.getBoundsInParent())) {
                brickHit = brick;
                ballVelocityY *= -1; // Rebond simple
                score += (int) brick.getUserData();
                scoreLabel.setText("Score: " + score);
                break;
            }
        }

        if (brickHit != null) {
            gamePane.getChildren().remove(brickHit);
            bricks.remove(brickHit);
            bricksCount--;
            if (bricksCount == 0) nextLevel();
        }
    }

    private void resetBall() {
        ball.setCenterX(gamePane.getWidth() / 2);
        ball.setCenterY(gamePane.getHeight() - 100);
        ballVelocityY = -4;
        ballVelocityX = (random.nextBoolean() ? 3 : -3);
    }

    private void nextLevel() {
        currentLevel++;
        createLevel();
        resetBall();
    }

    private void gameOver() {
        gameRunning = false;
        gameLoop.stop();
        Platform.runLater(() -> {
            showMessage("GAME OVER", "Score final: " + score);
            closeGame();
        });
    }

    private void showMessage(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void closeGame() {
        gameRunning = false;
        if (gameLoop != null) gameLoop.stop();
        Stage stage = (Stage) gamePane.getScene().getWindow();
        stage.close();
    }
}