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
    @FXML private Label     topbarTitle;
    @FXML private Label     topbarUser;

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
        // Afficher le nom de l'utilisateur connecté dans la topbar
        if (topbarTitle != null) {
            topbarTitle.setText(AppContext.getFullName());
        }
        if (topbarUser != null) {
            topbarUser.setText(buildInitials(AppContext.getFullName()));
        }

        // Enregistrer le contentArea dans le Router
        Router.init(contentArea);

        // Synchroniser le titre de la topbar (Non utilisé visuellement mais permet d'éviter l'erreur)
        Router.setOnRouteChange(route -> {
            // (Optionnel) Ici on pourrait mettre à jour un autre label si l'on voulait afficher la route
        });

        // Naviguer vers la page d'accueil du rôle connecté
        Router.go(AppContext.getDefaultRoute());
    }

    private String buildInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    /* ── Utilitaires ───────────────────────────────────────── */

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}