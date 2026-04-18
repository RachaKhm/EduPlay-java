package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.Map;

/**
 * MainController
 * ─────────────────────────────────────────────────────────────
 * Controller du shell principal (MainView.fxml).
 * Responsabilités :
 *   • Initialiser le Router avec le contentArea
 *   • Mettre à jour le titre de la topbar à chaque navigation
 *   • Afficher le nom de l'utilisateur connecté dans la topbar
 *
 * Ce controller ne gère AUCUNE navigation directe.
 * Toute navigation passe par Router.go("route").
 * ─────────────────────────────────────────────────────────────
 */
public class MainController {

    /* ── FXML bindings ─────────────────────────────────────── */

    @FXML private StackPane contentArea;
    // (Ancien topbarTitle enlevé car déplacé dans le design du content ou inutile)
    @FXML private Label     pillInitials;
    @FXML private Label     pillName;
    @FXML private Label     pillRole;
    @FXML private void showProfile()        { Router.go("profile"); }

    /* ── Titres de la topbar par route ─────────────────────── */

    private static final Map<String, String> ROUTE_TITLES = Map.ofEntries(
            Map.entry("admin_dashboard",   "Tableau de bord"),
            Map.entry("users",             "Gestion des utilisateurs"),
            Map.entry("teachers",          "Enseignants"),
            Map.entry("parents",           "Parents"),
            Map.entry("teacher_dashboard", "Tableau de bord"),
            Map.entry("teacher_courses",   "Mes cours"),
            Map.entry("teacher_students",  "Mes élèves"),
            Map.entry("parent_dashboard",  "Tableau de bord"),
            Map.entry("parent_children",   "Mes enfants"),
            Map.entry("parent_events",     "Événements"),
            Map.entry("child_dashboard",   "Tableau de bord"),
            Map.entry("child_courses",     "Mes cours"),
            Map.entry("child_games",       "Jeux"),
            Map.entry("profile",           "Mon profil")
    );

    /* ── Initialisation ────────────────────────────────────── */

    @FXML
    public void initialize() {
        // Mettre à jour le profil (Pill)
        if (pillName != null) {
            pillName.setText(capitalize(AppContext.getFullName()));
        }
        if (pillRole != null) {
            pillRole.setText(capitalize(AppContext.getRole()));
        }
        if (pillInitials != null) {
            String fullName = AppContext.getFullName();
            String init = "";
            if (fullName != null && !fullName.isBlank()) {
                String[] parts = fullName.split(" ");
                if (parts.length > 0 && !parts[0].isEmpty()) init += parts[0].charAt(0);
                if (parts.length > 1 && !parts[1].isEmpty()) init += parts[1].charAt(0);
            } else {
                init = "U";
            }
            pillInitials.setText(init.toUpperCase());
        }

        // Enregistrer le contentArea dans le Router
        Router.init(contentArea);

        // Synchroniser le titre de la topbar à chaque changement de route
        Router.setOnRouteChange(route -> {
            // Optionnel: on pourrait émettre un évènement si certaines pages ont besoin de ce titre,
            // mais l'UI moderne (de l'image) n'affiche plus le titre global en haut, 
            // c'est affiché directement dans le content "Inscriptions par évènement"
        });

        // Naviguer vers la page d'accueil du rôle connecté
        Router.go(AppContext.getDefaultRoute());
    }

    @FXML
    public void openProfileDropdown() {
        // En vrai: ouvrir une popup pour Settings/Profile/Logout
        // Pour l'instant on navigue direct au profil
        Router.go("profile");
    }

    /* ── Utilitaires ───────────────────────────────────────── */

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}