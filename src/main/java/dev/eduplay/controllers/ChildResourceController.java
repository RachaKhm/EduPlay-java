package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.BookRequest;
import dev.eduplay.entities.Library;
import dev.eduplay.entities.Resource;
import dev.eduplay.services.BookRequestService;
import dev.eduplay.services.ResourceService;
import dev.eduplay.tools.ImageLoader;
import dev.eduplay.services.GroqService;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ChildResourceController {

    @FXML private Label lblLibraryNameNav;
    @FXML private Label lblLibraryName;
    @FXML private Label lblTheme;
    @FXML private Label lblAge;
    @FXML private Label lblLevel;
    @FXML private Label lblLibraryDesc;
    @FXML private Label lblResourceCount;
    @FXML private Label lblResourceCountAlt;
    @FXML private FlowPane resourceContainer;
    @FXML private VBox libraryImageContainer;
    
    @FXML private Button btnVoiceSearch;
    @FXML private Label lblVoiceStatus;
    
    // Notifications & Requests
    @FXML private Label lblWelcomeRequest;
    @FXML private Label lblChildName;
    @FXML private TextField txtRequestTitle;
    @FXML private VBox notificationContainer;
    
    @FXML private VBox chatWindow;
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatMessagesContainer;
    @FXML private TextField txtChatMessage;
    @FXML private Button btnToggleChat;
    
    private Library currentLibrary;
    private final ResourceService resourceService = new ResourceService();
    private final BookRequestService bookRequestService = new BookRequestService();
    private GroqService groqService;
    private List<Resource> currentResources;

    @FXML
    public void initialize() {
        groqService = new GroqService();
        addChatMessage("Salut ! Quel âge as-tu et quel genre d'histoires aimes-tu ? Je peux te recommander des super livres !", false);

        String fullName = AppContext.getFullName();
        if (lblChildName != null) lblChildName.setText(fullName);
        if (lblWelcomeRequest != null) lblWelcomeRequest.setText("Bonjour " + fullName + " ! Demande ton livre ici 👇");

        Object transit = Router.getTransitData();
        if (transit instanceof Library) {
            this.currentLibrary = (Library) transit;
            loadLibraryData();
            loadResources();
            loadNotifications();
        } else {
            if (lblLibraryNameNav != null) lblLibraryNameNav.setText("Erreur : Bibliothèque introuvable.");
        }
    }

    private void loadNotifications() {
        if (notificationContainer == null) return;
        notificationContainer.getChildren().clear();
        
        List<BookRequest> list = bookRequestService.getNotificationsNonLues(AppContext.getUserId());
        if (list.isEmpty()) {
            Label empty = new Label("Aucune nouvelle notification.");
            empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px; -fx-padding: 15; -fx-font-style: italic;");
            notificationContainer.getChildren().add(empty);
            return;
        }

        for (BookRequest br : list) {
            HBox item = new HBox(12);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setStyle("-fx-background-color: white; -fx-padding: 12 16; -fx-background-radius: 12; -fx-border-color: #F1F5F9; -fx-border-width: 1; -fx-border-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.02), 5, 0, 0, 2);");
            
            StackPane iconBox = new StackPane(new Label("📚"));
            iconBox.setStyle("-fx-background-color: #F0FDF4; -fx-padding: 8; -fx-background-radius: 10;");
            
            VBox info = new VBox(2);
            Label title = new Label(br.getBookTitle());
            title.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F172A; -fx-font-size: 14px;");
            
            String dateStr = (br.getRequestedAt() != null) ? br.getRequestedAt().format(DateTimeFormatter.ofPattern("dd/MM")) : "N/A";
            Label sub = new Label("Disponible depuis le " + dateStr + " ✨");
            sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #16A34A; -fx-font-weight: bold;");
            info.getChildren().addAll(title, sub);
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Button btnRead = new Button("Lire");
            btnRead.setStyle("-fx-background-color: #22C55E; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 6 20; -fx-cursor: hand;");
            btnRead.setOnAction(e -> {
                bookRequestService.marquerCommeNotifie(br.getId());
                loadNotifications();
                showPremiumPopup("Bonne lecture !", "Le livre « " + br.getBookTitle() + " » t'attend ! 📖", "#10B981");
            });
            
            item.getChildren().addAll(iconBox, info, spacer, btnRead);
            notificationContainer.getChildren().add(item);
        }
    }

    @FXML
    private void handleQuickRequest() {
        String title = txtRequestTitle.getText().trim();
        if (title.isEmpty()) return;

        BookRequest br = new BookRequest();
        br.setBookTitle(title);
        br.setEnfantId(AppContext.getUserId());
        br.setRequestedAt(LocalDateTime.now());
        br.setAvailable(false);
        br.setNotified(false);

        bookRequestService.ajouter(br);
        txtRequestTitle.clear();
        
        showPremiumPopup("Demande envoyée !", "On te préviendra dès que le livre sera prêt ! 🚀", "#F97316");
    }

    private void showPremiumPopup(String title, String message, String colorHex) {
        VBox popup = new VBox(12);
        popup.setAlignment(Pos.CENTER);
        popup.setMaxSize(350, 180);
        popup.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-padding: 24; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 30, 0, 0, 10); -fx-border-color: " + colorHex + "; -fx-border-width: 2; -fx-border-radius: 20;");
        
        Label icon = new Label("✨");
        icon.setStyle("-fx-font-size: 32px;");
        
        Label lblTitle = new Label(title);
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        
        Label lblMsg = new Label(message);
        lblMsg.setStyle("-fx-text-fill: #64748B; -fx-font-size: 14px; -fx-text-alignment: center;");
        lblMsg.setWrapText(true);
        
        popup.getChildren().addAll(icon, lblTitle, lblMsg);
        
        StackPane.setAlignment(popup, Pos.CENTER);
        if (Router.getContainer() != null) {
            StackPane root = Router.getContainer();
            root.getChildren().add(popup);
            
            // Animation
            popup.setOpacity(0);
            popup.setTranslateY(20);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), popup);
            fadeIn.setToValue(1);
            
            TranslateTransition moveUp = new TranslateTransition(Duration.millis(300), popup);
            moveUp.setToY(0);
            
            fadeIn.play();
            moveUp.play();
            
            // Auto close after 3s
            new Thread(() -> {
                try { Thread.sleep(3000); } catch (Exception e) {}
                Platform.runLater(() -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), popup);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(e -> root.getChildren().remove(popup));
                    fadeOut.play();
                });
            }).start();
        }
    }

    private void loadLibraryData() {
        if (currentLibrary == null) return;
        if (lblLibraryNameNav != null) lblLibraryNameNav.setText(currentLibrary.getName());
        if (lblLibraryName != null) lblLibraryName.setText(currentLibrary.getName());
        if (lblTheme != null) lblTheme.setText("💡 " + currentLibrary.getTheme());
        if (lblAge != null) lblAge.setText("👶 " + currentLibrary.getMinAge() + "-" + currentLibrary.getMaxAge() + " ans");
        if (lblLevel != null) lblLevel.setText("🎓 " + currentLibrary.getLevel());
        if (lblLibraryDesc != null) lblLibraryDesc.setText(currentLibrary.getDescription() != null ? currentLibrary.getDescription() : "");
        
        if (libraryImageContainer != null) {
            libraryImageContainer.getChildren().clear();
            if (currentLibrary.getCoverImage() != null && !currentLibrary.getCoverImage().trim().isEmpty()) {
                javafx.scene.image.Image image = ImageLoader.load(currentLibrary.getCoverImage());
                if (image != null) {
                    javafx.scene.image.ImageView img = new javafx.scene.image.ImageView(image);
                    img.setFitWidth(200); img.setFitHeight(260); img.setStyle("-fx-background-radius: 16;");
                    libraryImageContainer.getChildren().add(img);
                }
            } else {
                Label placeholder = new Label("📚");
                placeholder.setStyle("-fx-font-size: 80px; -fx-text-fill: #93C5FD;");
                libraryImageContainer.getChildren().add(placeholder);
            }
        }
    }

    private void loadResources() {
        if (currentLibrary == null) return;
        currentResources = resourceService.afficherParLibrairie(currentLibrary.getId());
        
        int count = currentResources.size();
        if (lblResourceCount != null) lblResourceCount.setText(count + " livres");
        if (lblResourceCountAlt != null) lblResourceCountAlt.setText(count + " livres");
        
        if (groqService != null && currentResources != null) {
            StringBuilder context = new StringBuilder();
            for (Resource r : currentResources) {
                context.append("- TITRE: ").append(r.getTitle());
                context.append(" | AUTEUR: ").append(r.getAuthor() != null ? r.getAuthor() : "Inconnu");
                context.append(" | ÂGE: ").append(r.getMinAge()).append(" à ").append(r.getMaxAge()).append(" ans");
                context.append(" | TYPE: ").append(r.getType());
                if (r.getSummary() != null && !r.getSummary().trim().isEmpty()) {
                    String sum = r.getSummary().length() > 100 ? r.getSummary().substring(0, 97) + "..." : r.getSummary();
                    context.append(" | RÉSUMÉ: ").append(sum);
                }
                context.append("\\n");
            }
            groqService.setLibraryContext(context.toString());
        }
        
        if (resourceContainer != null) {
            resourceContainer.getChildren().clear();
            if (currentResources.isEmpty()) {
                Label empty = new Label("Aucune ressource disponible.");
                empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748B; -fx-padding: 20;");
                resourceContainer.getChildren().add(empty);
                return;
            }

            for (Resource res : currentResources) {
                resourceContainer.getChildren().add(createResourceCard(res));
            }
        }
    }

    private VBox createResourceCard(Resource res) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-border-color: #F1F5F9; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        card.setPrefWidth(280);
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label typeBadge = new Label(res.getType() != null ? res.getType() : "Livre");
        typeBadge.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 12;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label language = new Label(res.getLanguage() != null ? res.getLanguage() : "FR");
        language.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");
        header.getChildren().addAll(typeBadge, spacer, language);
        
        Label title = new Label(res.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0F172A;");
        title.setWrapText(true);
        
        Label author = new Label(res.getAuthor() != null ? "Par " + res.getAuthor() : "");
        author.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

        Label ageRange = new Label("👶 " + res.getMinAge() + "-" + res.getMaxAge() + " ans");
        ageRange.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 8;");
        
        StackPane imgBox = new StackPane();
        imgBox.setPrefHeight(180);
        imgBox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8;");
        if (res.getCoverImage() != null && !res.getCoverImage().trim().isEmpty()) {
            javafx.scene.image.Image image = ImageLoader.load(res.getCoverImage());
            if (image != null) {
                javafx.scene.image.ImageView img = new javafx.scene.image.ImageView(image);
                img.setFitWidth(246); img.setFitHeight(180); img.setStyle("-fx-background-radius: 8;");
                imgBox.getChildren().add(img);
            }
        } else {
            Label p = new Label("📚"); p.setStyle("-fx-font-size: 48px;"); imgBox.getChildren().add(p);
        }
        
        Label summary = new Label(res.getSummary() != null ? res.getSummary() : "");
        summary.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");
        summary.setPrefHeight(60); summary.setWrapText(true);
        
        // Buttons
        Button btnRead = new Button("👀 Lire");
        btnRead.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 10 16;");
        btnRead.setOnAction(e -> openPdf(res));
        HBox.setHgrow(btnRead, Priority.ALWAYS); btnRead.setMaxWidth(2000);

        Button btnListen = new Button("🎧 Écouter");
        btnListen.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 10 16;");
        btnListen.setOnAction(e -> listenToPdf(res));
        HBox.setHgrow(btnListen, Priority.ALWAYS); btnListen.setMaxWidth(2000);

        Button btnQuiz = new Button("🧠 Quiz");
        btnQuiz.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 10 16;");
        btnQuiz.setOnAction(e -> startQuizFromCard(res));
        btnQuiz.setMaxWidth(2000);
        VBox.setVgrow(btnQuiz, Priority.ALWAYS);

        HBox row1 = new HBox(12);
        row1.getChildren().addAll(btnRead, btnListen);
        
        VBox actions = new VBox(12);
        actions.getChildren().addAll(row1, btnQuiz);
        
        card.getChildren().addAll(header, title, author, ageRange, imgBox, summary, actions);
        return card;
    }

    private void openPdf(Resource res) {
        if (res.getPdfFile() != null && !res.getPdfFile().trim().isEmpty()) {
            File pdfFile = new File(res.getPdfFile());
            if (pdfFile.exists()) {
                try { java.awt.Desktop.getDesktop().open(pdfFile); } catch (Exception e) {}
            }
        }
    }

    private void listenToPdf(Resource res) {
        if (res.getPdfFile() != null && !res.getPdfFile().trim().isEmpty()) {
            File pdfFile = new File(res.getPdfFile());
            if (pdfFile.exists()) {
                try {
                    PDDocument document = PDDocument.load(pdfFile);
                    PDFTextStripper stripper = new PDFTextStripper();
                    List<String> pagesText = new ArrayList<>();
                    for (int i = 1; i <= document.getNumberOfPages(); i++) {
                        stripper.setStartPage(i); stripper.setEndPage(i);
                        String pageText = stripper.getText(document);
                        if (pageText != null && !pageText.trim().isEmpty()) pagesText.add(pageText.trim());
                    }
                    document.close();

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/child/ReadingView.fxml"));
                    Parent root = loader.load();
                    ReadingController controller = loader.getController();
                    controller.initData(res.getTitle(), pagesText);
                    Stage stage = new Stage();
                    stage.setTitle("Lecture - " + res.getTitle());
                    stage.setScene(new Scene(root, 800, 600));
                    stage.show();
                } catch (Exception e) {}
            }
        }
    }

    private void startQuizFromCard(Resource res) {
        if (res.getPdfFile() != null && !res.getPdfFile().trim().isEmpty()) {
            File pdfFile = new File(res.getPdfFile());
            if (pdfFile.exists()) {
                try {
                    PDDocument document = PDDocument.load(pdfFile);
                    PDFTextStripper stripper = new PDFTextStripper();
                    String fullText = stripper.getText(document);
                    document.close();
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/child/QuizView.fxml"));
                    Parent root = loader.load();
                    QuizController controller = loader.getController();
                    controller.initData(res.getTitle(), fullText);
                    Stage stage = new Stage();
                    stage.setTitle("Quiz - " + res.getTitle());
                    stage.setScene(new Scene(root, 800, 600));
                    stage.show();
                } catch (Exception e) {}
            }
        }
    }

    @FXML
    private void goBackToLibraries() { Router.go("child_library"); }

    @FXML
    private void handleVoiceSearch() {
        lblVoiceStatus.setText("Préparation du micro...");
        btnVoiceSearch.setDisable(true);
        new Thread(() -> {
            try {
                String pythonPath = "C:\\Users\\user\\anaconda3\\python.exe";
                String scriptPath = "src/main/resources/tools/voice_search.py";
                ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath);
                Process process = pb.start();
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
                String result = reader.readLine();
                process.waitFor();
                Platform.runLater(() -> {
                    btnVoiceSearch.setDisable(false);
                    if (result != null && !result.trim().isEmpty()) {
                        lblVoiceStatus.setText("Résultat : " + result);
                        filterByVoice(result);
                    } else {
                        lblVoiceStatus.setText("Je n'ai pas compris.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> { btnVoiceSearch.setDisable(false); lblVoiceStatus.setText("Erreur micro."); });
            }
        }).start();
    }

    private void filterByVoice(String text) {
        String query = text.toLowerCase();
        if (resourceContainer != null) {
            resourceContainer.getChildren().clear();
            for (Resource res : currentResources) {
                if (res.getTitle().toLowerCase().contains(query)) {
                    resourceContainer.getChildren().add(createResourceCard(res));
                }
            }
        }
    }

    @FXML
    private void toggleChat() {
        if (chatWindow != null) {
            boolean v = chatWindow.isVisible();
            chatWindow.setVisible(!v); chatWindow.setManaged(!v);
            if (!v) Platform.runLater(() -> txtChatMessage.requestFocus());
        }
    }

    @FXML
    private void sendChatMessage() {
        String t = txtChatMessage.getText().trim();
        if (t.isEmpty()) return;
        txtChatMessage.clear(); addChatMessage(t, true);
        new Thread(() -> {
            String r = groqService.sendMessage(t);
            Platform.runLater(() -> addChatMessage(r, false));
        }).start();
    }

    private void addChatMessage(String text, boolean isUser) {
        if (chatMessagesContainer == null) return;
        Label msg = new Label(text); msg.setWrapText(true); msg.setMaxWidth(250);
        msg.setStyle("-fx-padding: 12 16; -fx-background-radius: 15; -fx-font-size: 14px; " + 
                          (isUser ? "-fx-background-color: #8B5CF6; -fx-text-fill: white;" 
                                  : "-fx-background-color: #F1F5F9; -fx-text-fill: #1E293B;"));
        HBox row = new HBox(msg); row.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        chatMessagesContainer.getChildren().add(row);
        if (chatScrollPane != null) Platform.runLater(() -> chatScrollPane.setVvalue(1.0));
    }
}
