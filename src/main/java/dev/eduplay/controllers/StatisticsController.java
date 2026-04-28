package dev.eduplay.controllers;

import dev.eduplay.entities.Library;
import dev.eduplay.entities.Resource;
import dev.eduplay.services.LibraryService;
import dev.eduplay.services.ResourceService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

import java.util.*;
import java.util.stream.Collectors;

public class StatisticsController {

    // KPI Labels
    @FXML private Label lblTotalLibraries;
    @FXML private Label lblTotalResources;
    @FXML private Label lblAvgResources;

    // Distribution niveaux - counts
    @FXML private Label lblCountDebutant;
    @FXML private Label lblCountIntermediaire;
    @FXML private Label lblCountAvance;
    @FXML private Label lblCountExpert;

    // Distribution niveaux - barres
    @FXML private Region barDebutant;
    @FXML private Region barIntermediaire;
    @FXML private Region barAvance;
    @FXML private Region barExpert;

    // Top bibliothèques
    @FXML private VBox topLibrariesBox;

    // Types de ressources
    @FXML private FlowPane resourceTypePane;

    private final LibraryService libraryService = new LibraryService();
    private final ResourceService resourceService = new ResourceService();

    @FXML
    public void initialize() {
        loadStats();
    }

    @FXML
    private void handleRefresh() {
        loadStats();
    }

    private void loadStats() {
        List<Library>  libraries  = libraryService.afficher();
        List<Resource> resources  = resourceService.afficher();

        int totalLibs = libraries.size();
        int totalRes  = resources.size();
        double avg    = totalLibs > 0 ? (double) totalRes / totalLibs : 0;

        lblTotalLibraries.setText(String.valueOf(totalLibs));
        lblTotalResources.setText(String.valueOf(totalRes));
        lblAvgResources.setText(String.format("%.1f", avg));

        // ── Distribution par niveau ──────────────────────────────────────
        Map<String, Long> levelMap = libraries.stream()
                .collect(Collectors.groupingBy(
                        l -> l.getLevel() == null ? "Autre" : l.getLevel(),
                        Collectors.counting()));

        long countDeb  = getCount(levelMap, "Débutant", "debutant", "Beginner", "beginner");
        long countMed  = getCount(levelMap, "Intermédiaire", "intermediaire", "Intermediate", "intermediate");
        long countAdv  = getCount(levelMap, "Avancé", "avance", "Advanced", "advanced");
        long countExp  = getCount(levelMap, "Expert", "expert");

        lblCountDebutant.setText(String.valueOf(countDeb));
        lblCountIntermediaire.setText(String.valueOf(countMed));
        lblCountAvance.setText(String.valueOf(countAdv));
        lblCountExpert.setText(String.valueOf(countExp));

        long maxLevel = Math.max(1, Math.max(countDeb, Math.max(countMed, Math.max(countAdv, countExp))));
        setBarWidth(barDebutant,       countDeb, maxLevel);
        setBarWidth(barIntermediaire,  countMed, maxLevel);
        setBarWidth(barAvance,         countAdv, maxLevel);
        setBarWidth(barExpert,         countExp, maxLevel);

        // ── Top 5 bibliothèques par nombre de ressources ─────────────────
        topLibrariesBox.getChildren().clear();

        Map<Integer, Long> resCountByLib = resources.stream()
                .collect(Collectors.groupingBy(Resource::getLibraryId, Collectors.counting()));

        libraries.stream()
                .sorted(Comparator.comparingLong(l -> -resCountByLib.getOrDefault(l.getId(), 0L)))
                .limit(5)
                .forEach(lib -> {
                    long count = resCountByLib.getOrDefault(lib.getId(), 0L);
                    long maxCount = resCountByLib.values().stream().mapToLong(v -> v).max().orElse(1);
                    topLibrariesBox.getChildren().add(buildTopLibRow(lib.getName(), count, maxCount));
                });

        if (topLibrariesBox.getChildren().isEmpty()) {
            Label empty = new Label("Aucune bibliothèque trouvée.");
            empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");
            topLibrariesBox.getChildren().add(empty);
        }

        // ── Distribution par type de ressource ───────────────────────────
        resourceTypePane.getChildren().clear();

        Map<String, Long> typeMap = resources.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getType() == null ? "Non défini" : r.getType(),
                        Collectors.counting()));

        String[] colors = {"#6366F1", "#10B981", "#F59E0B", "#EF4444", "#8B5CF6", "#06B6D4", "#EC4899"};
        int[] colorIdx = {0};

        typeMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> {
                    String color = colors[colorIdx[0] % colors.length];
                    colorIdx[0]++;
                    resourceTypePane.getChildren().add(buildTypeBadge(e.getKey(), e.getValue(), color));
                });

        if (resourceTypePane.getChildren().isEmpty()) {
            Label empty = new Label("Aucune ressource trouvée.");
            empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 13px;");
            resourceTypePane.getChildren().add(empty);
        }
    }

    // ── Builders ─────────────────────────────────────────────────────────────

    private HBox buildTopLibRow(String name, long count, long maxCount) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #374151; -fx-min-width: 140; -fx-max-width: 140; -fx-wrap-text: true;");

        StackPane barBg = new StackPane();
        barBg.setStyle("-fx-background-color: #EEF2FF; -fx-background-radius: 6;");
        barBg.setPrefHeight(10);
        HBox.setHgrow(barBg, Priority.ALWAYS);

        double pct = maxCount > 0 ? (double) count / maxCount : 0;
        Region bar = new Region();
        bar.setPrefHeight(10);
        bar.setStyle("-fx-background-color: linear-gradient(to right, #6366F1, #9333EA); -fx-background-radius: 6;");
        bar.setPrefWidth(pct * 200);

        HBox barWrapper = new HBox(bar);
        barWrapper.setAlignment(Pos.CENTER_LEFT);
        barBg.getChildren().add(barWrapper);

        Label countLabel = new Label(count + " res.");
        countLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #6366F1; -fx-min-width: 55;");

        row.getChildren().addAll(nameLabel, barBg, countLabel);
        return row;
    }

    private VBox buildTypeBadge(String type, long count, String color) {
        VBox badge = new VBox(4);
        badge.setAlignment(Pos.CENTER);
        badge.setStyle("-fx-background-color: white; -fx-border-radius: 12; -fx-background-radius: 12; "
                + "-fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-padding: 14 20; "
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 6, 0, 0, 2);");

        Label countLbl = new Label(String.valueOf(count));
        countLbl.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label typeLbl = new Label(type);
        typeLbl.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #64748B;");

        badge.getChildren().addAll(countLbl, typeLbl);
        return badge;
    }

    // ── Utilitaires ───────────────────────────────────────────────────────────

    private long getCount(Map<String, Long> map, String... keys) {
        return Arrays.stream(keys).mapToLong(k -> map.getOrDefault(k, 0L)).sum();
    }

    private void setBarWidth(Region bar, long count, long max) {
        double pct = max > 0 ? (double) count / max : 0;
        bar.setPrefWidth(pct * 260);
    }
}
