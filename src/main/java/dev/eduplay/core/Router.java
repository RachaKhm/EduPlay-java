package dev.eduplay.core;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Router {

    private static StackPane container;
    private static String currentRoute = "";

    private static final Map<String, Node>   viewCache = new HashMap<>();
    private static final Map<String, String> routes    = new HashMap<>();
    private static Consumer<String> onRouteChange;

    private static Object transitData;

    public static void init(StackPane contentArea) {
        container = contentArea;
        viewCache.clear();
        registerRoutes();
    }

    private static void registerRoutes() {
        // Admin
        routes.put("admin_dashboard", "/views/admin/DashboardView.fxml");
        routes.put("users",           "/views/admin/UserListView.fxml");
        routes.put("teachers",        "/views/admin/UserListView.fxml");
        routes.put("parents",         "/views/admin/UserListView.fxml");
        routes.put("library_index",   "/LibraryIndex.fxml");
        routes.put("library_form",    "/LibraryForm.fxml");
        routes.put("library_show",    "/LibraryShow.fxml");
        routes.put("admin_resource_index", "/ResourceIndex.fxml");
        routes.put("admin_resource_form",  "/ResourceForm.fxml");
        routes.put("admin_resource_show",  "/ResourceShow.fxml");
        routes.put("book_requests_index",  "/BookRequestIndex.fxml");

        // Enseignant — FXML à créer par le collègue module Cours
        routes.put("teacher_dashboard", "/views/teacher/TeacherDashboardView.fxml");
        routes.put("teacher_courses",   "/views/teacher/CoursesView.fxml");
        routes.put("teacher_students",  "/views/teacher/StudentsView.fxml");

        // Parent — FXML à créer par le collègue module Événements
        routes.put("parent_dashboard", "/views/parent/ParentDashboardView.fxml");
        routes.put("parent_children",  "/views/parent/ChildrenView.fxml");
        routes.put("parent_events",    "/views/parent/EventsView.fxml");

        // Enfant — FXML à créer par le collègue module Jeux/Cours
        routes.put("child_dashboard",  "/views/child/ChildDashboardView.fxml");
        routes.put("child_courses",    "/views/child/MyCoursesView.fxml");
        routes.put("child_games",      "/views/child/GamesView.fxml");


        // Commun
        routes.put("profile", "/views/shared/ProfileView.fxml");
    }

    public static void go(String route) {
        if (!routes.containsKey(route)) {
            System.err.println("[Router] Route inconnue : " + route);
            return;
        }
        if (route.equals(currentRoute)) return;

        try {
            Node view = viewCache.get(route);

            if (view == null) {
                URL resource = Router.class.getResource(routes.get(route));
                if (resource == null) {
                    view = makePlaceholder(route);
                } else {
                    view = new FXMLLoader(resource).load();
                }
                viewCache.put(route, view);
            }

            container.getChildren().setAll(view);
            currentRoute = route;
            if (onRouteChange != null) onRouteChange.accept(route);

        } catch (IOException e) {
            System.err.println("[Router] Erreur '" + route + "' : " + e.getMessage());
            container.getChildren().setAll(makePlaceholder(route));
        }
    }

    private static Node makePlaceholder(String route) {
        Label title = new Label("Vue en cours de développement");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #9999BB;");
        Label sub = new Label("Route : " + route + "  —  FXML à intégrer par votre collègue");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #BBBBCC;");
        VBox box = new VBox(12, title, sub);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #F8F9FA;");
        return box;
    }

    public static void reload(String route)                  { viewCache.remove(route); currentRoute = ""; go(route); }

    public static void reload(String route, Object data) {
        transitData = data;
        reload(route);
    }

    public static Object getTransitData() {
        Object data = transitData;
        transitData = null; // consommer la donnée
        return data;
    }

    public static String getCurrentRoute()                   { return currentRoute; }
    public static void setOnRouteChange(Consumer<String> l) { onRouteChange = l; }
    public static void clearCache()                         { viewCache.clear(); currentRoute = ""; }
}