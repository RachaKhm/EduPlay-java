package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.Map;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label     topbarTitle;
    @FXML private Label     topbarUser;

    // Titres affichés dans la topbar selon la route
    private static final Map<String, String> ROUTE_TITLES = Map.of(
            "dashboard", "Tableau de bord",
            "users",     "Gestion des utilisateurs",
            "teachers",  "Enseignants",
            "parents",   "Parents",
            "profile",   "Mon profil"
    );

    @FXML
    public void initialize() {
        // Afficher le nom de l'utilisateur dans la topbar
        topbarUser.setText(AppContext.getFullName());

        // Initialiser le Router avec le contentArea
        Router.init(contentArea);

        // Mettre à jour le titre de la topbar à chaque navigation
        Router.setOnRouteChange(route -> {
            String title = ROUTE_TITLES.getOrDefault(route, "EduPlay");
            topbarTitle.setText(title);
        });

        // Naviguer vers la route par défaut du rôle connecté
        Router.go(AppContext.getDefaultRoute());
    }
}