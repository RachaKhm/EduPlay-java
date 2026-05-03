package dev.eduplay.controllers;

import dev.eduplay.services.QuizService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuizController {

    @FXML private Label lblTitle;
    @FXML private ProgressIndicator loader;
    
    @FXML private VBox quizContainer;
    @FXML private Label lblQuestionCount;
    @FXML private Label lblQuestion;
    @FXML private VBox optionsBox;
    
    @FXML private VBox resultContainer;
    @FXML private Label lblScore;

    private QuizService quizService;
    private List<QuizQuestion> questions;
    private int currentQuestionIndex = 0;
    private int score = 0;
    
    public void initData(String bookTitle, String bookText) {
        quizService = new QuizService();
        lblTitle.setText("Préparation du Quiz pour : " + bookTitle);
        
        Thread t = new Thread(() -> {
            String jsonArray = quizService.generateQuizJson(bookText);
            Platform.runLater(() -> {
                loader.setVisible(false);
                loader.setManaged(false);
                if (jsonArray != null) {
                    if (jsonArray.startsWith("ERROR:") || jsonArray.startsWith("EXCEPTION:")) {
                        lblTitle.setText("Erreur : " + jsonArray);
                    } else {
                        questions = parseQuizJson(jsonArray);
                        if (questions != null && !questions.isEmpty()) {
                            lblTitle.setText("Quiz : " + bookTitle);
                            startQuiz();
                        } else {
                            lblTitle.setText("Impossible de générer le quiz. 😕");
                        }
                    }
                } else {
                    lblTitle.setText("Erreur de connexion. 😕");
                }
            });
        });
        t.setDaemon(true);
        t.start();
    }
    
    private void startQuiz() {
        quizContainer.setVisible(true);
        quizContainer.setManaged(true);
        currentQuestionIndex = 0;
        score = 0;
        showQuestion();
    }
    
    private void showQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            endQuiz();
            return;
        }
        
        QuizQuestion q = questions.get(currentQuestionIndex);
        lblQuestionCount.setText("Question " + (currentQuestionIndex + 1) + " / " + questions.size());
        lblQuestion.setText(q.question);
        
        optionsBox.getChildren().clear();
        for (String option : q.options) {
            Button btnOption = new Button(option);
            btnOption.setMaxWidth(Double.MAX_VALUE);
            btnOption.setStyle("-fx-background-color: linear-gradient(to bottom, #FFFFFF, #F8FAFC); -fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 20px; -fx-font-family: 'Comic Sans MS'; -fx-padding: 15 20; -fx-background-radius: 16; -fx-cursor: hand; -fx-border-color: #E2E8F0; -fx-border-radius: 16; -fx-border-width: 3; -fx-alignment: center-left;");
            
            btnOption.setOnAction(e -> handleAnswer(btnOption, q.answer));
            
            // Hover effect
            btnOption.setOnMouseEntered(e -> {
                if (!btnOption.isDisabled()) {
                    btnOption.setStyle("-fx-background-color: linear-gradient(to bottom, #F0F9FF, #E0F2FE); -fx-text-fill: #0369A1; -fx-font-weight: bold; -fx-font-size: 20px; -fx-font-family: 'Comic Sans MS'; -fx-padding: 15 20; -fx-background-radius: 16; -fx-cursor: hand; -fx-border-color: #BAE6FD; -fx-border-radius: 16; -fx-border-width: 3; -fx-alignment: center-left; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 3);");
                }
            });
            btnOption.setOnMouseExited(e -> {
                if (!btnOption.isDisabled()) {
                    btnOption.setStyle("-fx-background-color: linear-gradient(to bottom, #FFFFFF, #F8FAFC); -fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 20px; -fx-font-family: 'Comic Sans MS'; -fx-padding: 15 20; -fx-background-radius: 16; -fx-cursor: hand; -fx-border-color: #E2E8F0; -fx-border-radius: 16; -fx-border-width: 3; -fx-alignment: center-left;");
                }
            });
            
            optionsBox.getChildren().add(btnOption);
        }
    }
    
    private void handleAnswer(Button selectedBtn, String correctAnswer) {
        // Disable all buttons
        for (javafx.scene.Node node : optionsBox.getChildren()) {
            node.setDisable(true);
            Button btn = (Button) node;
            if (btn.getText().equals(correctAnswer)) {
                btn.setStyle("-fx-background-color: linear-gradient(to bottom, #34D399, #10B981); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-font-family: 'Comic Sans MS'; -fx-padding: 15 20; -fx-background-radius: 16; -fx-border-color: #059669; -fx-border-radius: 16; -fx-border-width: 3; -fx-alignment: center-left;");
            }
        }
        
        if (selectedBtn.getText().equals(correctAnswer)) {
            score++;
        } else {
            selectedBtn.setStyle("-fx-background-color: linear-gradient(to bottom, #F87171, #EF4444); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 20px; -fx-font-family: 'Comic Sans MS'; -fx-padding: 15 20; -fx-background-radius: 16; -fx-border-color: #B91C1C; -fx-border-radius: 16; -fx-border-width: 3; -fx-alignment: center-left;");
        }
        
        // Wait 1.5s then next question
        Thread t = new Thread(() -> {
            try { Thread.sleep(1500); } catch (Exception ignored) {}
            Platform.runLater(() -> {
                currentQuestionIndex++;
                showQuestion();
            });
        });
        t.setDaemon(true);
        t.start();
    }
    
    private void endQuiz() {
        quizContainer.setVisible(false);
        quizContainer.setManaged(false);
        
        resultContainer.setVisible(true);
        resultContainer.setManaged(true);
        
        lblScore.setText("Ton score : " + score + " / " + questions.size());
    }
    
    @FXML
    private void closeQuiz() {
        Stage stage = (Stage) lblTitle.getScene().getWindow();
        stage.close();
    }
    
    private List<QuizQuestion> parseQuizJson(String json) {
        List<QuizQuestion> list = new ArrayList<>();
        try {
            json = unescapeUnicode(json);
            // Replace newlines inside json so regex matching works easier
            json = json.replaceAll("\\r?\\n", " ");
            
            Pattern objPattern = Pattern.compile("\\{([^{}]+)\\}");
            Matcher objMatcher = objPattern.matcher(json);
            while (objMatcher.find()) {
                String obj = objMatcher.group(1);
                
                QuizQuestion q = new QuizQuestion();
                q.options = new ArrayList<>();
                
                Pattern qPattern = Pattern.compile("\"question\"\\s*:\\s*\"(.*?)\"");
                Matcher qm = qPattern.matcher(obj);
                if (qm.find()) q.question = qm.group(1);
                
                Pattern aPattern = Pattern.compile("\"answer\"\\s*:\\s*\"(.*?)\"");
                Matcher am = aPattern.matcher(obj);
                if (am.find()) q.answer = am.group(1);
                
                Pattern optArrPattern = Pattern.compile("\"options\"\\s*:\\s*\\[(.*?)\\]");
                Matcher oam = optArrPattern.matcher(obj);
                if (oam.find()) {
                    String arrStr = oam.group(1);
                    Pattern optPattern = Pattern.compile("\"(.*?)\"");
                    Matcher om = optPattern.matcher(arrStr);
                    while (om.find()) {
                        q.options.add(om.group(1));
                    }
                }
                
                if (q.question != null && !q.options.isEmpty() && q.answer != null) {
                    list.add(q);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    
    private String unescapeUnicode(String s) {
        Pattern pattern = Pattern.compile("\\\\u([0-9A-Fa-f]{4})");
        Matcher matcher = pattern.matcher(s);
        StringBuilder sb = new StringBuilder(s.length());
        while (matcher.find()) {
            matcher.appendReplacement(sb, String.valueOf((char) Integer.parseInt(matcher.group(1), 16)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
    
    private static class QuizQuestion {
        String question;
        List<String> options;
        String answer;
    }
}
