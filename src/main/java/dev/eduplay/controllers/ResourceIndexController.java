package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.Resource;
import dev.eduplay.services.ResourceService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public class ResourceIndexController {

    @FXML private FlowPane gridResources;
    @FXML private Label lblResourceCountAlt;
    @FXML private TextField txtSearchTitle;
    @FXML private TextField txtSearchAuthor;
    @FXML private ComboBox<String> comboSearchType;

    private final ResourceService resourceService = new ResourceService();
    private List<Resource> allResources;

    @FXML
    public void initialize() {
        comboSearchType.getItems().addAll("Tous", "Livre", "Magazine", "Journal", "Article");
        comboSearchType.getSelectionModel().selectFirst();
        
        loadResources();
        
        // Setup instant search filters
        txtSearchTitle.textProperty().addListener((obs, oldV, newV) -> filterResources());
        txtSearchAuthor.textProperty().addListener((obs, oldV, newV) -> filterResources());
        comboSearchType.valueProperty().addListener((obs, oldV, newV) -> filterResources());
    }

    private void loadResources() {
        allResources = resourceService.afficher();
        filterResources();
    }

    @FXML
    private void handleSearch() {
        filterResources();
    }

    private void filterResources() {
        String titleQ = txtSearchTitle.getText() == null ? "" : txtSearchTitle.getText().toLowerCase();
        String authorQ = txtSearchAuthor.getText() == null ? "" : txtSearchAuthor.getText().toLowerCase();
        String typeQ = comboSearchType.getValue();

        List<Resource> filtered = allResources.stream().filter(r -> {
            boolean matchTitle = r.getTitle() != null && r.getTitle().toLowerCase().contains(titleQ);
            boolean matchAuthor = r.getAuthor() != null && r.getAuthor().toLowerCase().contains(authorQ);
            boolean matchType = "Tous".equals(typeQ) || (r.getType() != null && r.getType().equalsIgnoreCase(typeQ));
            return matchTitle && matchAuthor && matchType;
        }).collect(Collectors.toList());

        lblResourceCountAlt.setText(filtered.size() + " résultats");
        gridResources.getChildren().clear();

        for (Resource res : filtered) {
            gridResources.getChildren().add(createResourceCard(res));
        }
    }

    private VBox createResourceCard(Resource res) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");
        card.setPrefWidth(300);

        // Header
        HBox header = new HBox(8);
        String type = res.getType() != null ? res.getType().toUpperCase() : "DOC";
        Label badge = new Label(type);
        badge.setStyle("-fx-background-color: #EEF2FF; -fx-text-fill: #4F46E5; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 12;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label age = new Label("🤖 " + res.getMinAge() + "-" + res.getMaxAge() + " ans");
        age.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 12;");
        header.getChildren().addAll(badge, spacer, age);

        // Image placeholder or actual image
        VBox imgbox = new VBox();
        imgbox.setPrefHeight(150);
        imgbox.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-alignment: center;");
        
        boolean hasImage = false;
        if (res.getCoverImage() != null && !res.getCoverImage().trim().isEmpty()) {
            try {
                javafx.scene.image.Image img = dev.eduplay.tools.ImageLoader.load(res.getCoverImage());
                if (img != null && !img.isError()) {
                    javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                    imgView.setFitWidth(300);
                    imgView.setFitHeight(150);
                    imgView.setPreserveRatio(true);
                    imgbox.getChildren().add(imgView);
                    hasImage = true;
                }
            } catch (Exception e) {
                System.out.println("Image loading error: " + e.getMessage());
            }
        }
        
        if (!hasImage) {
            Label imgPlaceholder = new Label(type.equals("LIVRE") ? "📘" : (type.equals("MAGAZINE") ? "📰" : "📄"));
            imgPlaceholder.setStyle("-fx-font-size: 40px;");
            imgbox.getChildren().add(imgPlaceholder);
        }

        // Content
        Label title = new Label(res.getTitle());
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        title.setWrapText(true);

        Label author = new Label("👤 " + (res.getAuthor() != null ? res.getAuthor() : "Inconnu"));
        author.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

        // Action Buttons
        HBox actions = new HBox(8);
        Button btnVoir = new Button("Voir");
        btnVoir.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-cursor: hand;");
        btnVoir.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnVoir, Priority.ALWAYS);
        btnVoir.setOnAction(e -> Router.reload("admin_resource_show", res)); // Passing object to show view!

        Button btnEditer = new Button("Éditer");
        btnEditer.setStyle("-fx-background-color: #EEF2FF; -fx-border-color: #C7D2FE; -fx-border-radius: 8; -fx-text-fill: #4F46E5; -fx-font-weight: bold; -fx-cursor: hand;");
        btnEditer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnEditer, Priority.ALWAYS);
        btnEditer.setOnAction(e -> Router.reload("admin_resource_form", res));

        Button btnDel = new Button("🗑");
        btnDel.setStyle("-fx-background-color: #FEF2F2; -fx-border-color: #FECACA; -fx-border-radius: 8; -fx-text-fill: #DC2626; -fx-cursor: hand;");
        btnDel.setOnAction(e -> supprimerResource(res));

        actions.getChildren().addAll(btnVoir, btnEditer, btnDel);

        card.getChildren().addAll(header, imgbox, title, author, actions);
        return card;
    }

    private void supprimerResource(Resource r) {
        resourceService.supprimer(r.getId());
        loadResources();
    }

    @FXML
    private void showBookRequests() {
        Router.go("admin_book_requests");
    }

    @FXML
    private void addResource() {
        Router.reload("admin_resource_form", null); // Edit form with null = create new
    }
}
