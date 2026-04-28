package dev.eduplay.controllers;

import dev.eduplay.services.TTSService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.control.ComboBox;
import javafx.stage.Popup;
import javafx.scene.Cursor;
import javafx.geometry.Pos;
import dev.eduplay.services.GroqService;
import javafx.geometry.Bounds;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;

public class ReadingController {

    @FXML private Label lblTitle;
    @FXML private TextFlow textFlow;
    @FXML private Slider sliderSpeed;
    @FXML private Button btnPrev;
    @FXML private Button btnNext;
    @FXML private Button btnPause;
    @FXML private Button btnResume;
    @FXML private Button btnStop;
    @FXML private Button btnQuiz;
    @FXML private Label lblPageInfo;
    @FXML private ComboBox<String> comboLanguage;

    private TTSService ttsService;
    private GroqService groqService;
    private List<String> pages;
    private int currentPageIndex = 0;
    private Popup translationPopup;
    private String currentTitle;
    private boolean isProgrammaticChange = false;

    @FXML
    public void initialize() {
        ttsService = new TTSService();
        
        // Options listeners
        sliderSpeed.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (pages != null && !sliderSpeed.isValueChanging()) replayPage();
        });
        
        sliderSpeed.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
            if (!isChanging && pages != null) {
                replayPage();
            }
        });
    }

    public void initData(String title, List<String> pagesText) {
        this.currentTitle = title;
        this.pages = pagesText;
        this.currentPageIndex = 0;
        this.ttsService = new TTSService();
        this.groqService = new GroqService();

        lblTitle.setText(title);
        
        if (comboLanguage != null) {
            comboLanguage.getItems().addAll("Anglais", "Français", "Arabe", "Espagnol", "Allemand");
            comboLanguage.getSelectionModel().selectFirst();
        }

        translationPopup = new Popup();
        translationPopup.setAutoHide(true);
        
        Platform.runLater(() -> {
            Stage stage = (Stage) lblTitle.getScene().getWindow();
            stage.setOnCloseRequest(event -> stopReading());
        });

        loadPage();
    }

    private void loadPage() {
        if (pages == null || pages.isEmpty()) return;

        btnPrev.setDisable(currentPageIndex == 0);
        btnNext.setDisable(currentPageIndex == pages.size() - 1);
        lblPageInfo.setText("Page " + (currentPageIndex + 1) + " sur " + pages.size());

        if (currentPageIndex == pages.size() - 1 && btnQuiz != null) {
            btnQuiz.setVisible(true);
            btnQuiz.setManaged(true);
        } else if (btnQuiz != null) {
            btnQuiz.setVisible(false);
            btnQuiz.setManaged(false);
        }

        // Nettoyer le texte pour qu'il soit parfaitement aligné avec les index de PowerShell
        // On remplace tous les sauts de ligne et espaces multiples par un seul espace
        String text = pages.get(currentPageIndex).replaceAll("\\s+", " ").trim();
        pages.set(currentPageIndex, text); // On sauvegarde le texte nettoyé
        
        textFlow.getChildren().clear();
        for (int i = 0; i < text.length(); i++) {
            Text t = new Text(String.valueOf(text.charAt(i)));
            t.setFont(Font.font("Comic Sans MS", FontWeight.NORMAL, 32));
            t.setFill(Color.web("#334155"));
            
            // Interaction pour la traduction
            final int charIndex = i;
            t.setCursor(Cursor.HAND);
            t.setOnMouseEntered(e -> t.setFill(Color.web("#8B5CF6")));
            t.setOnMouseExited(e -> {
                if (!t.isUnderline()) t.setFill(Color.web("#334155"));
            });
            t.setOnMouseClicked(e -> handleWordClick(text, charIndex, t, e));
            
            textFlow.getChildren().add(t);
        }

        readCurrentPageText(text);
    }

    private void handleWordClick(String fullText, int charIndex, Text sourceNode, javafx.scene.input.MouseEvent event) {
        if (translationPopup.isShowing()) {
            translationPopup.hide();
        }

        // Trouver le début du mot
        int start = charIndex;
        while (start > 0 && Character.isLetterOrDigit(fullText.charAt(start - 1))) {
            start--;
        }
        
        // Trouver la fin du mot
        int end = charIndex;
        while (end < fullText.length() - 1 && Character.isLetterOrDigit(fullText.charAt(end + 1))) {
            end++;
        }
        
        String word = fullText.substring(start, end + 1).trim();
        if (word.isEmpty()) return;

        String targetLanguage = comboLanguage != null ? comboLanguage.getValue() : "Anglais";
        
        // Afficher un "Chargement..."
        showTranslationPopup(sourceNode, word, "...");

        Thread t = new Thread(() -> {
            String translated = groqService.translateWord(word, targetLanguage);
            Platform.runLater(() -> {
                if (translationPopup.isShowing()) {
                    showTranslationPopup(sourceNode, word, translated);
                }
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void showTranslationPopup(Text sourceNode, String originalWord, String translatedWord) {
        VBox box = new VBox(5);
        box.setStyle("-fx-background-color: #1E293B; -fx-padding: 10 15; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5); -fx-border-color: #38BDF8; -fx-border-width: 2; -fx-border-radius: 6;");
        box.setAlignment(Pos.CENTER);
        
        Label lblOrig = new Label(originalWord);
        lblOrig.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px; -fx-font-weight: bold;");
        
        Label lblTrans = new Label(translatedWord);
        lblTrans.setStyle("-fx-text-fill: #38BDF8; -fx-font-size: 18px; -fx-font-weight: bold;");
        
        box.getChildren().addAll(lblOrig, lblTrans);
        
        translationPopup.getContent().clear();
        translationPopup.getContent().add(box);
        
        Bounds boundsInScreen = sourceNode.localToScreen(sourceNode.getBoundsInLocal());
        if (boundsInScreen != null) {
            translationPopup.show(sourceNode, boundsInScreen.getMinX() - 20, boundsInScreen.getMinY() - 60);
        }
    }

    private void readCurrentPageText(String text) {
        int speed = (int) sliderSpeed.getValue();

        if (btnPause != null) btnPause.setDisable(false);
        if (btnResume != null) btnResume.setDisable(true);
        if (btnStop != null) btnStop.setDisable(false);

        ttsService.readText(text, null, speed, (start, length) -> {
            // Réinitialiser
            for (javafx.scene.Node node : textFlow.getChildren()) {
                if (node instanceof Text) {
                    ((Text) node).setFill(Color.web("#334155"));
                    ((Text) node).setFont(Font.font("Comic Sans MS", FontWeight.NORMAL, 32));
                    ((Text) node).setUnderline(false);
                }
            }
            // Surligner le mot en couleur vive
            for (int i = start; i < start + length && i < textFlow.getChildren().size(); i++) {
                Text t = (Text) textFlow.getChildren().get(i);
                t.setFill(Color.web("#F59E0B")); // Orange vif / Doré
                t.setFont(Font.font("Comic Sans MS", FontWeight.BOLD, 40)); 
                t.setUnderline(true);
            }
        }, () -> {
            // Fin
            for (javafx.scene.Node node : textFlow.getChildren()) {
                if (node instanceof Text) {
                    ((Text) node).setFill(Color.web("#334155"));
                    ((Text) node).setFont(Font.font("Comic Sans MS", FontWeight.NORMAL, 32));
                    ((Text) node).setUnderline(false);
                }
            }
            if (btnPause != null) btnPause.setDisable(true);
            if (btnResume != null) btnResume.setDisable(true);
            if (btnStop != null) btnStop.setDisable(true);
        });
    }

    @FXML
    private void pauseReading() {
        if (ttsService != null) {
            ttsService.pause();
            if (btnPause != null) btnPause.setDisable(true);
            if (btnResume != null) btnResume.setDisable(false);
        }
    }

    @FXML
    private void resumeReading() {
        if (ttsService != null) {
            ttsService.resume();
            if (btnPause != null) btnPause.setDisable(false);
            if (btnResume != null) btnResume.setDisable(true);
        }
    }

    @FXML
    private void stopCurrentReading() {
        if (ttsService != null) {
            ttsService.stop();
            if (btnPause != null) btnPause.setDisable(true);
            if (btnResume != null) btnResume.setDisable(true);
            if (btnStop != null) btnStop.setDisable(true);
            
            // Réinitialiser le texte
            for (javafx.scene.Node node : textFlow.getChildren()) {
                if (node instanceof Text) {
                    ((Text) node).setFill(Color.web("#334155"));
                    ((Text) node).setFont(Font.font("Comic Sans MS", FontWeight.NORMAL, 32));
                    ((Text) node).setUnderline(false);
                }
            }
        }
    }

    @FXML
    private void prevPage() {
        if (currentPageIndex > 0) {
            currentPageIndex--;
            loadPage();
        }
    }

    @FXML
    private void nextPage() {
        if (currentPageIndex < pages.size() - 1) {
            currentPageIndex++;
            loadPage();
        }
    }

    @FXML
    private void replayPage() {
        loadPage();
    }

    @FXML
    private void stopReading() {
        if (ttsService != null) {
            ttsService.stop();
        }
        Stage stage = (Stage) lblTitle.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void openQuiz() {
        if (ttsService != null) {
            ttsService.stop();
        }
        
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/child/QuizView.fxml"));
            javafx.scene.Parent root = loader.load();
            
            QuizController controller = loader.getController();
            controller.initData(currentTitle, String.join(" ", pages));
            
            Stage stage = new Stage();
            stage.setTitle("Quiz - " + currentTitle);
            stage.setScene(new javafx.scene.Scene(root, 800, 600));
            stage.show();
            
            // Fermer la vue de lecture
            Stage currentStage = (Stage) lblTitle.getScene().getWindow();
            currentStage.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
