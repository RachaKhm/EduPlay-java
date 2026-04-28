package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.Library;
import dev.eduplay.entities.Resource;
import dev.eduplay.services.ResourceService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;

import dev.eduplay.controllers.QuizController;
import dev.eduplay.controllers.ReadingController;
import dev.eduplay.tools.ImageLoader;
import dev.eduplay.services.GroqService;

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
    
    @FXML private VBox chatWindow;
    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatMessagesContainer;
    @FXML private TextField txtChatMessage;
    @FXML private Button btnToggleChat;
    
    private Library currentLibrary;
    private final ResourceService resourceService = new ResourceService();
    private GroqService groqService;
    private List<Resource> currentResources;

    @FXML
    public void initialize() {
        groqService = new GroqService();
        addChatMessage("Salut ! Quel âge as-tu et quel genre d'histoires aimes-tu ? Je peux te recommander des super livres !", false);

        Object transit = Router.getTransitData();
        if (transit instanceof Library) {
            this.currentLibrary = (Library) transit;
            loadLibraryData();
            loadResources();
        } else {
            lblLibraryNameNav.setText("Erreur : Bibliothèque introuvable.");
        }
    }

    private void loadLibraryData() {
        lblLibraryNameNav.setText(currentLibrary.getName());
        lblLibraryName.setText(currentLibrary.getName());
        lblTheme.setText("💡 " + currentLibrary.getTheme());
        lblAge.setText("👶 " + currentLibrary.getMinAge() + "-" + currentLibrary.getMaxAge() + " ans");
        lblLevel.setText("🎓 " + currentLibrary.getLevel());
        lblLibraryDesc.setText(currentLibrary.getDescription() != null ? currentLibrary.getDescription() : "");
        
        if (libraryImageContainer != null) {
            libraryImageContainer.getChildren().clear();
            if (currentLibrary.getCoverImage() != null && !currentLibrary.getCoverImage().trim().isEmpty()) {
                javafx.scene.image.Image image = ImageLoader.load(currentLibrary.getCoverImage());
                if (image != null) {
                    javafx.scene.image.ImageView img = new javafx.scene.image.ImageView(image);
                    img.setFitWidth(200);
                    img.setFitHeight(260);
                    img.setStyle("-fx-background-radius: 16;");
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
        currentResources = resourceService.afficherParLibrairie(currentLibrary.getId());
        
        int count = currentResources.size();
        lblResourceCount.setText(count + " livres");
        lblResourceCountAlt.setText(count + " livres");
        
        // Injecter le contexte au chatbot
        if (groqService != null && currentResources != null) {
            StringBuilder context = new StringBuilder();
            for (Resource r : currentResources) {
                context.append("- ").append(r.getTitle());
                if (r.getAuthor() != null && !r.getAuthor().trim().isEmpty()) {
                    context.append(" (par ").append(r.getAuthor()).append(")");
                }
                context.append(". Âge recommandé: ").append(r.getMinAge()).append(" à ").append(r.getMaxAge()).append(" ans. ");
                if (r.getSummary() != null && !r.getSummary().trim().isEmpty()) {
                    context.append("Résumé: ").append(r.getSummary());
                }
                context.append("\\n");
            }
            if (context.length() == 0) {
                context.append("Aucun livre dans cette bibliothèque pour le moment.");
            }
            groqService.setLibraryContext(context.toString());
        }
        
        resourceContainer.getChildren().clear();
        
        if (currentResources.isEmpty()) {
            Label empty = new Label("Aucune ressource disponible pour cette bibliothèque.");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748B; -fx-padding: 20;");
            resourceContainer.getChildren().add(empty);
            return;
        }

        for (Resource res : currentResources) {
            resourceContainer.getChildren().add(createResourceCard(res));
        }
    }

    private VBox createResourceCard(Resource res) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        card.setPrefWidth(280);
        
        // Header (Type badge + Date)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label typeBadge = new Label(res.getType() != null ? res.getType() : "Livre");
        typeBadge.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 12;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label language = new Label(res.getLanguage() != null ? res.getLanguage() : "FR");
        language.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px; -fx-font-weight: bold;");
        
        header.getChildren().addAll(typeBadge, spacer, language);
        
        // Text info
        Label title = new Label(res.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1E293B;");
        title.setWrapText(true);
        
        Label author = new Label(res.getAuthor() != null ? "Par " + res.getAuthor() : "");
        author.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
        
        // Image Placeholder (or real image)
        StackPane imgBox = new StackPane();
        imgBox.setPrefHeight(180);
        imgBox.setStyle("-fx-background-color: linear-gradient(to bottom right, #EFF6FF, #F5F3FF); -fx-background-radius: 8;");
        if (res.getCoverImage() != null && !res.getCoverImage().trim().isEmpty()) {
            javafx.scene.image.Image image = ImageLoader.load(res.getCoverImage());
            if (image != null) {
                javafx.scene.image.ImageView img = new javafx.scene.image.ImageView(image);
                img.setFitWidth(246);
                img.setFitHeight(180);
                img.setStyle("-fx-background-radius: 8;");
                imgBox.getChildren().add(img);
            }
        } else {
            Label placeholder = new Label("📚");
            placeholder.setStyle("-fx-font-size: 48px;");
            imgBox.getChildren().add(placeholder);
        }
        
        // Summary
        Label summary = new Label(res.getSummary() != null ? res.getSummary() : "");
        summary.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");
        summary.setPrefHeight(60);
        summary.setWrapText(true);
        
        // Action Buttons
        HBox actions = new HBox(12);
        
        Button btnRead = new Button("👀 Lire");
        if (res.getPdfFile() == null || res.getPdfFile().trim().isEmpty()) {
            btnRead.setText("Pas de PDF");
            btnRead.setDisable(true);
            btnRead.setStyle("-fx-background-color: #9CA3AF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16;");
        } else {
            btnRead.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
            btnRead.setOnAction(e -> openPdf(res));
        }
        HBox.setHgrow(btnRead, Priority.ALWAYS);
        btnRead.setMaxWidth(Double.MAX_VALUE);
        
        Button btnListen = new Button("🎧 Écouter");
        if (res.getPdfFile() == null || res.getPdfFile().trim().isEmpty()) {
            btnListen.setDisable(true);
            btnListen.setStyle("-fx-background-color: #9CA3AF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16;");
        } else {
            btnListen.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
            btnListen.setOnAction(e -> listenToPdf(res));
        }
        HBox.setHgrow(btnListen, Priority.ALWAYS);
        btnListen.setMaxWidth(Double.MAX_VALUE);
        
        Button btnQuiz = new Button("🧠 Quiz");
        if (res.getPdfFile() == null || res.getPdfFile().trim().isEmpty()) {
            btnQuiz.setDisable(true);
            btnQuiz.setStyle("-fx-background-color: #9CA3AF; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 16;");
        } else {
            btnQuiz.setStyle("-fx-background-color: #8B5CF6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
            btnQuiz.setOnAction(e -> startQuizFromCard(res));
        }
        HBox.setHgrow(btnQuiz, Priority.ALWAYS);
        btnQuiz.setMaxWidth(Double.MAX_VALUE);
        
        actions.getChildren().addAll(btnRead, btnListen, btnQuiz);
        
        card.getChildren().addAll(header, title, author, imgBox, summary, actions);
        
        return card;
    }

    private void openPdf(Resource res) {
        if (res.getPdfFile() != null && !res.getPdfFile().trim().isEmpty()) {
            java.io.File pdfFile = new java.io.File(res.getPdfFile());
            if (pdfFile.exists()) {
                try {
                    java.awt.Desktop.getDesktop().open(pdfFile);
                } catch (Exception e) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible d'ouvrir le document PDF : " + e.getMessage());
                    alert.showAndWait();
                }
            } else {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Fichier introuvable");
                alert.setHeaderText(null);
                alert.setContentText("Le livre numérique (PDF) n'a pas été trouvé sur le disque.");
                alert.showAndWait();
            }
        }
    }

    private void listenToPdf(Resource res) {
        if (res.getPdfFile() != null && !res.getPdfFile().trim().isEmpty()) {
            java.io.File pdfFile = new java.io.File(res.getPdfFile());
            if (pdfFile.exists()) {
                try {
                    // Extraire le texte page par page
                    org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.pdmodel.PDDocument.load(pdfFile);
                    org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
                    java.util.List<String> pagesText = new java.util.ArrayList<>();
                    
                    int totalPages = document.getNumberOfPages();
                    for (int i = 1; i <= totalPages; i++) {
                        stripper.setStartPage(i);
                        stripper.setEndPage(i);
                        String pageText = stripper.getText(document);
                        if (pageText != null && !pageText.trim().isEmpty()) {
                            pagesText.add(pageText.trim());
                        }
                    }
                    document.close();

                    if (pagesText.isEmpty()) {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                        alert.setTitle("Texte introuvable");
                        alert.setHeaderText(null);
                        alert.setContentText("Le PDF ne contient pas de texte lisible.");
                        alert.showAndWait();
                        return;
                    }

                    // Ouvrir la vue de lecture
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/child/ReadingView.fxml"));
                    javafx.scene.Parent root = loader.load();
                    
                    ReadingController controller = loader.getController();
                    controller.initData(res.getTitle(), pagesText);

                    javafx.stage.Stage stage = new javafx.stage.Stage();
                    stage.setTitle("Lecture - " + res.getTitle());
                    stage.setScene(new javafx.scene.Scene(root, 800, 600));
                    stage.show();

                } catch (Exception e) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Erreur lors de la lecture du PDF : " + e.getMessage());
                    alert.showAndWait();
                }
            } else {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Fichier introuvable");
                alert.setHeaderText(null);
                alert.setContentText("Le livre numérique (PDF) n'a pas été trouvé sur le disque.");
                alert.showAndWait();
            }
        }
    }

    private void startQuizFromCard(Resource res) {
        if (res.getPdfFile() != null && !res.getPdfFile().trim().isEmpty()) {
            java.io.File pdfFile = new java.io.File(res.getPdfFile());
            if (pdfFile.exists()) {
                try {
                    org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.pdmodel.PDDocument.load(pdfFile);
                    org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
                    String fullText = stripper.getText(document);
                    document.close();

                    if (fullText == null || fullText.trim().isEmpty()) {
                        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                        alert.setTitle("Texte introuvable");
                        alert.setHeaderText(null);
                        alert.setContentText("Le PDF ne contient pas de texte pour générer le quiz.");
                        alert.showAndWait();
                        return;
                    }

                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/child/QuizView.fxml"));
                    javafx.scene.Parent root = loader.load();
                    
                    QuizController controller = loader.getController();
                    controller.initData(res.getTitle(), fullText);

                    javafx.stage.Stage stage = new javafx.stage.Stage();
                    stage.setTitle("Quiz - " + res.getTitle());
                    stage.setScene(new javafx.scene.Scene(root, 800, 600));
                    stage.show();

                } catch (Exception e) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Erreur lors de la lecture du PDF : " + e.getMessage());
                    alert.showAndWait();
                }
            } else {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Fichier introuvable");
                alert.setHeaderText(null);
                alert.setContentText("Le livre numérique (PDF) n'a pas été trouvé sur le disque.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void goBackToLibraries() {
        Router.go("child_library");
    }

    @FXML
    private void handleVoiceSearch() {
        lblVoiceStatus.setText("Préparation du micro...");
        btnVoiceSearch.setDisable(true);

        Thread t = new Thread(() -> {
            try {
                String pythonPath = "C:\\Users\\user\\anaconda3\\python.exe";
                String scriptPath = "src/main/resources/tools/voice_search.py";
                
                ProcessBuilder pb = new ProcessBuilder(pythonPath, scriptPath);
                pb.redirectErrorStream(false);
                Process process = pb.start();

                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
                java.io.BufferedReader errorReader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getErrorStream()));
                
                // Read stderr to catch the "READY" signal
                new Thread(() -> {
                    try {
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            if (line.contains("READY")) {
                                javafx.application.Platform.runLater(() -> lblVoiceStatus.setText("🎤 Allez-y, je vous écoute !"));
                            }
                        }
                    } catch (Exception ignored) {}
                }).start();

                String result = "";
                String line;
                while ((line = reader.readLine()) != null) {
                    result += line;
                }
                process.waitFor();
                
                String finalResult = result.trim();
                javafx.application.Platform.runLater(() -> {
                    btnVoiceSearch.setDisable(false);
                    if (finalResult.equals("TIMEOUT")) {
                        lblVoiceStatus.setText("Temps écoulé. Réessayez.");
                    } else if (finalResult.equals("INCONNU")) {
                        lblVoiceStatus.setText("Je n'ai pas compris. Répétez ?");
                    } else if (finalResult.equals("ERREUR") || finalResult.isEmpty()) {
                        lblVoiceStatus.setText("Dis le nom d'un livre pour le trouver ! (Erreur micro)");
                    } else {
                        lblVoiceStatus.setText("Résultat : " + finalResult);
                        filterByVoice(finalResult);
                    }
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    btnVoiceSearch.setDisable(false);
                    lblVoiceStatus.setText("Erreur système.");
                });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void filterByVoice(String text) {
        String query = text.toLowerCase();
        resourceContainer.getChildren().clear();
        int count = 0;
        
        for (Resource res : currentResources) {
            boolean match = false;
            // On vérifie si un mot de la requête est dans le titre ou si le titre est dans la requête
            if (res.getTitle() != null) {
                String title = res.getTitle().toLowerCase();
                if (title.contains(query) || query.contains(title)) {
                    match = true;
                } else {
                    // Match mots simples
                    for (String word : query.split("\\s+")) {
                        if (word.length() > 3 && title.contains(word)) {
                            match = true;
                            break;
                        }
                    }
                }
            }
            if (match) {
                resourceContainer.getChildren().add(createResourceCard(res));
                count++;
            }
        }
        
        lblResourceCount.setText(count + " livre" + (count > 1 ? "s" : ""));
        lblResourceCountAlt.setText(count + " livre" + (count > 1 ? "s" : ""));
        
        if (count == 0) {
            Label empty = new Label("Aucun livre ne correspond à la recherche vocale : \"" + text + "\"");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748B; -fx-padding: 20;");
            resourceContainer.getChildren().add(empty);
        }
    }

    @FXML
    private void toggleChat() {
        boolean isVisible = chatWindow.isVisible();
        chatWindow.setVisible(!isVisible);
        chatWindow.setManaged(!isVisible);
        
        if (!isVisible) {
            // Scroll to bottom when opening
            javafx.application.Platform.runLater(() -> {
                chatScrollPane.setVvalue(1.0);
                txtChatMessage.requestFocus();
            });
        }
    }

    @FXML
    private void sendChatMessage() {
        String text = txtChatMessage.getText().trim();
        if (text.isEmpty()) return;

        txtChatMessage.clear();
        addChatMessage(text, true);

        // Appel asynchrone
        Thread t = new Thread(() -> {
            String response = groqService.sendMessage(text);
            javafx.application.Platform.runLater(() -> {
                addChatMessage(response, false);
            });
        });
        t.setDaemon(true);
        t.start();
    }

    private void addChatMessage(String text, boolean isUser) {
        Label msgLabel = new Label(text);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(250);
        msgLabel.setStyle("-fx-padding: 10; -fx-background-radius: 12; -fx-font-size: 14px; " + 
                          (isUser ? "-fx-background-color: #8B5CF6; -fx-text-fill: white;" 
                                  : "-fx-background-color: #F1F5F9; -fx-text-fill: #334155;"));

        HBox row = new HBox();
        row.getChildren().add(msgLabel);
        if (isUser) {
            row.setAlignment(Pos.CENTER_RIGHT);
        } else {
            row.setAlignment(Pos.CENTER_LEFT);
        }

        chatMessagesContainer.getChildren().add(row);
        
        // Auto-scroll to bottom
        javafx.application.Platform.runLater(() -> {
            chatScrollPane.setVvalue(1.0);
        });
    }
}
