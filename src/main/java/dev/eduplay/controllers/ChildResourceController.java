package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.Library;
import dev.eduplay.entities.Resource;
import dev.eduplay.services.ResourceService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
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
    
    private Library currentLibrary;
    private final ResourceService resourceService = new ResourceService();

    @FXML
    public void initialize() {
        Object data = Router.getTransitData();
        if (data instanceof Library) {
            currentLibrary = (Library) data;
            loadLibraryData();
            loadResources();
        } else {
            lblLibraryName.setText("Erreur : Bibliothèque introuvable.");
        }
    }

    private void loadLibraryData() {
        lblLibraryNameNav.setText(currentLibrary.getName());
        lblLibraryName.setText(currentLibrary.getName());
        lblTheme.setText("💡 " + currentLibrary.getTheme());
        lblAge.setText("👶 " + currentLibrary.getMinAge() + "-" + currentLibrary.getMaxAge() + " ans");
        lblLevel.setText("🎓 " + currentLibrary.getLevel());
        lblLibraryDesc.setText(currentLibrary.getDescription() != null ? currentLibrary.getDescription() : "");
    }

    private void loadResources() {
        resourceContainer.getChildren().clear();
        List<Resource> resources = resourceService.afficherParLibrairie(currentLibrary.getId());
        
        lblResourceCount.setText(resources.size() + " ressources disponibles");
        lblResourceCountAlt.setText(resources.size() + " ressources");

        for (Resource res : resources) {
            resourceContainer.getChildren().add(createResourceCard(res));
        }
    }

    private VBox createResourceCard(Resource res) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        card.setPrefWidth(280);

        // Header Top
        HBox header = new HBox();
        Label typeBadge = new Label(res.getType() != null ? res.getType().toUpperCase() : "LIVRE");
        typeBadge.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 12;");
        
        Label ageBadge = new Label(res.getMinAge() + "-" + res.getMaxAge() + " ans");
        ageBadge.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 12;");
        
        Label langBadge = new Label(res.getLanguage() != null ? res.getLanguage().toUpperCase() : "FR");
        langBadge.setStyle("-fx-background-color: #E0E7FF; -fx-text-fill: #4338CA; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 12;");
        
        HBox badges = new HBox(6, typeBadge, ageBadge, langBadge);
        
        // Title
        Label title = new Label(res.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1E293B;");
        title.setWrapText(true);

        Label author = new Label("Par " + (res.getAuthor() != null ? res.getAuthor() : "Inconnu"));
        author.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
        
        // Image Placeholder (or real image)
        VBox imgBox = new VBox();
        imgBox.setPrefHeight(140);
        imgBox.setStyle("-fx-background-color: linear-gradient(to bottom, #DBEAFE, #ECFEFF); -fx-background-radius: 8;");
        
        // Summary
        Label summary = new Label(res.getSummary());
        summary.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");
        summary.setPrefHeight(40);
        summary.setWrapText(true);
        
        // Action Buttons
        HBox actions = new HBox(12);
        Button btnRead = new Button("📖 Lire");
        btnRead.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
        HBox.setHgrow(btnRead, Priority.ALWAYS);
        btnRead.setMaxWidth(Double.MAX_VALUE);
        
        Button btnListen = new Button("🎧 Écouter");
        btnListen.setStyle("-fx-background-color: transparent; -fx-border-color: #CBD5E1; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-border-radius: 8; -fx-cursor: hand; -fx-padding: 8 16;");
        HBox.setHgrow(btnListen, Priority.ALWAYS);
        btnListen.setMaxWidth(Double.MAX_VALUE);
        
        actions.getChildren().addAll(btnRead, btnListen);
        
        btnRead.setOnAction(e -> {
            System.out.println("Opening resource: " + res.getPdfFile());
        });

        card.getChildren().addAll(badges, title, author, imgBox, summary, actions);
        return card;
    }

    @FXML
    private void goBackToLibraries() {
        Router.go("child_library");
    }
}
