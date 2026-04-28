package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.Library;
import dev.eduplay.services.LibraryService;
import dev.eduplay.tools.ImageLoader;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.stream.Collectors;

public class ChildLibraryController {

    @FXML private TextField searchField;
    @FXML private FlowPane libraryContainer;
    @FXML private FlowPane themeFilterContainer;
    @FXML private FlowPane recentContainer;
    
    private final LibraryService libraryService = new LibraryService();
    private List<Library> allLibraries;
    private String currentTheme = "all";

    @FXML
    public void initialize() {
        allLibraries = libraryService.afficher();
        afficherBibliotheques(allLibraries);
        afficherThemes();
        afficherRecents();

        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filtrerBibliotheques(newValue, currentTheme);
            });
        }
    }

    private void afficherThemes() {
        if (themeFilterContainer == null) return;
        themeFilterContainer.getChildren().clear();
        
        List<String> themes = allLibraries.stream()
                .map(Library::getTheme)
                .filter(t -> t != null && !t.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        Label btnAll = createThemePill("Tous", true);
        btnAll.setOnMouseClicked(e -> updateThemeFilter("all", btnAll));
        themeFilterContainer.getChildren().add(btnAll);

        for (String theme : themes) {
            Label btnTheme = createThemePill(theme, false);
            btnTheme.setOnMouseClicked(e -> updateThemeFilter(theme, btnTheme));
            themeFilterContainer.getChildren().add(btnTheme);
        }
    }
    
    private void updateThemeFilter(String theme, Label clickedBtn) {
        currentTheme = theme;
        // update UI classes
        themeFilterContainer.getChildren().forEach(n -> {
            if (n instanceof Label) {
                ((Label) n).setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand;");
            }
        });
        clickedBtn.setStyle("-fx-background-color: #2B7CEE; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand;");
        
        filtrerBibliotheques(searchField != null ? searchField.getText() : "", currentTheme);
    }

    private Label createThemePill(String text, boolean active) {
        Label lbl = new Label(text);
        if (active) {
            lbl.setStyle("-fx-background-color: #2B7CEE; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand;");
        } else {
            lbl.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 8 20; -fx-background-radius: 20; -fx-cursor: hand;");
        }
        return lbl;
    }

    private void afficherRecents() {
        if (recentContainer == null) return;
        recentContainer.getChildren().clear();
        
        // Take last 3 items, assuming the list is ordered by creation time.
        // If not, we just take the first 3 for now.
        List<Library> recents = allLibraries.stream()
                .limit(3)
                .collect(Collectors.toList());

        for (Library lib : recents) {
            HBox card = new HBox(12);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setStyle("-fx-background-color: white; -fx-padding: 8 16; -fx-background-radius: 30; -fx-border-color: #E2E8F0; -fx-border-radius: 30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");
            card.setCursor(Cursor.HAND);
            card.setOnMouseClicked(e -> Router.reload("child_resource", lib));

            StackPane imgBox = new StackPane();
            imgBox.setPrefSize(48, 48);
            imgBox.setStyle("-fx-background-color: #E0E7FF; -fx-background-radius: 24;");
            if (lib.getCoverImage() != null && !lib.getCoverImage().trim().isEmpty()) {
                javafx.scene.image.Image image = ImageLoader.load(lib.getCoverImage());
                if (image != null) {
                    ImageView img = new ImageView(image);
                    img.setFitWidth(48); img.setFitHeight(48);
                    javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(24, 24, 24);
                    img.setClip(clip);
                    imgBox.getChildren().add(img);
                }
            } else {
                Label lbl = new Label("📚");
                imgBox.getChildren().add(lbl);
            }

            VBox texts = new VBox(2);
            Label title = new Label(lib.getName());
            title.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827;");
            Label sub = new Label(lib.getTheme());
            sub.setStyle("-fx-font-size: 11px; -fx-text-fill: #9CA3AF;");
            texts.getChildren().addAll(title, sub);

            card.getChildren().addAll(imgBox, texts);
            recentContainer.getChildren().add(card);
        }
    }

    private void filtrerBibliotheques(String query, String theme) {
        String lowerQuery = (query == null) ? "" : query.toLowerCase();
        List<Library> filtered = allLibraries.stream()
                .filter(l -> {
                    boolean matchQuery = lowerQuery.isEmpty() || 
                                         l.getName().toLowerCase().contains(lowerQuery) ||
                                         (l.getDescription() != null && l.getDescription().toLowerCase().contains(lowerQuery));
                    boolean matchTheme = theme.equals("all") || (l.getTheme() != null && l.getTheme().equals(theme));
                    return matchQuery && matchTheme;
                })
                .collect(Collectors.toList());
        afficherBibliotheques(filtered);
    }

    private void afficherBibliotheques(List<Library> libraries) {
        if (libraryContainer == null) return;
        libraryContainer.getChildren().clear();

        if (libraries.isEmpty()) {
            Label emptyLbl = new Label("Aucune bibliothèque trouvée !");
            emptyLbl.setStyle("-fx-font-size: 18px; -fx-text-fill: #9CA3AF; -fx-font-weight: bold;");
            libraryContainer.getChildren().add(emptyLbl);
            return;
        }

        for (Library lib : libraries) {
            libraryContainer.getChildren().add(creerCarteBibliotheque(lib));
        }
    }

    private VBox creerCarteBibliotheque(Library lib) {
        VBox card = new VBox();
        card.setSpacing(16);
        card.setPadding(new Insets(16));
        card.setPrefWidth(280);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: #E5E7EB; -fx-border-width: 1;");
        card.setCursor(Cursor.HAND);
        
        DropShadow shadow = new DropShadow(20, Color.rgb(59, 130, 246, 0.15));
        card.setOnMouseEntered(e -> {
            card.setEffect(shadow);
            card.setTranslateY(-4);
        });
        card.setOnMouseExited(e -> {
            card.setEffect(null);
            card.setTranslateY(0);
        });

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefHeight(200);
        imageContainer.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 12;");
        
        if (lib.getCoverImage() != null && !lib.getCoverImage().trim().isEmpty()) {
            javafx.scene.image.Image image = ImageLoader.load(lib.getCoverImage());
            if (image != null) {
                ImageView img = new ImageView(image);
                img.setFitWidth(248);
                img.setFitHeight(200);
                img.setPreserveRatio(false);
                imageContainer.getChildren().add(img);
            }
        } else {
            Label placeholder = new Label("📚");
            placeholder.setStyle("-fx-font-size: 64px;");
            imageContainer.getChildren().add(placeholder);
        }

        Label themeBadge = new Label(lib.getTheme());
        themeBadge.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-text-fill: #2B7CEE; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10; -fx-background-radius: 12;");
        StackPane.setAlignment(themeBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(themeBadge, new Insets(10));
        imageContainer.getChildren().add(themeBadge);

        VBox infoBox = new VBox(8);
        
        Label ageBadge = new Label(lib.getMinAge() + " - " + lib.getMaxAge() + " ans");
        ageBadge.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #2563EB; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4;");
        
        Label title = new Label(lib.getName());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #111418;");
        title.setWrapText(true);

        Label desc = new Label(lib.getDescription() != null ? lib.getDescription() : "Aucune description...");
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");
        desc.setWrapText(true);
        desc.setPrefHeight(40);
        
        HBox levelBox = new HBox(10);
        levelBox.setAlignment(Pos.CENTER_LEFT);
        String colorLevel = "#DC2626";
        String bgLevel = "#FEE2E2";
        if ("Débutant".equalsIgnoreCase(lib.getLevel())) { colorLevel="#059669"; bgLevel="#D1FAE5"; }
        else if ("Intermédiaire".equalsIgnoreCase(lib.getLevel())) { colorLevel="#D97706"; bgLevel="#FEF3C7"; }
        
        Label levelBadge = new Label(lib.getLevel());
        levelBadge.setStyle("-fx-background-color: " + bgLevel + "; -fx-text-fill: " + colorLevel + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 12;");
        
        Label explIcon = new Label("👀 Bibliothèque");
        explIcon.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 11px;");
        levelBox.getChildren().addAll(levelBadge, explIcon);

        Label exploreBtn = new Label("Commencer l'Exploration ➔");
        exploreBtn.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #2B7CEE; -fx-font-weight: bold; -fx-font-size: 13px; -fx-alignment: center; -fx-padding: 10; -fx-background-radius: 20;");
        exploreBtn.setMaxWidth(Double.MAX_VALUE);
        
        card.setOnMouseClicked(e -> {
            Router.reload("child_resource", lib);
        });

        infoBox.getChildren().addAll(ageBadge, title, desc, levelBox, exploreBtn);
        card.getChildren().addAll(imageContainer, infoBox);

        return card;
    }
}
