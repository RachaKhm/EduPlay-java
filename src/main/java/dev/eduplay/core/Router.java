package dev.eduplay.core;

import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.EventResource;
import dev.eduplay.entities.SchoolEvent;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.lang.reflect.Method;
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

        // Enseignant
        routes.put("teacher_dashboard", "/views/teacher/TeacherDashboardView.fxml");
        routes.put("teacher_courses",   "/views/teacher/CoursesView.fxml");
        routes.put("teacher_students",  "/views/teacher/StudentsView.fxml");

        // Parent
        routes.put("parent_dashboard", "/views/parent/ParentDashboardView.fxml");
        routes.put("parent_children",  "/views/parent/ChildrenView.fxml");
        routes.put("parent_events",    "/views/parent/EventsView.fxml");

        // Enfant
        routes.put("child_dashboard",  "/views/child/ChildDashboardView.fxml");
        routes.put("child_courses",    "/views/child/MyCoursesView.fxml");
        routes.put("child_games",      "/views/child/GamesView.fxml");

        // Routes pour Events
        routes.put("event_list",        "/views/event/event_list.fxml");
        routes.put("add_event",         "/views/event/add_event.fxml");
        routes.put("edit_event",        "/views/event/edit_event.fxml");
        routes.put("event_detail",      "/views/event/event_detail.fxml");
        routes.put("event_resource",    "/views/event/event_resource.fxml");
        routes.put("add_resource",      "/views/event/add_resource.fxml");
        routes.put("edit_resource",     "/views/event/edit_resource.fxml");
        routes.put("resource_detail",   "/views/event/resource_detail.fxml");
        routes.put("registration_list", "/views/registration/registration_list.fxml");
        routes.put("registration_detail", "/views/registration/registration_detail.fxml");
        routes.put("edit_registration", "/views/registration/edit_registration.fxml");

        // Commun
        routes.put("profile", "/views/shared/ProfileView.fxml");
        routes.put("forgot-password", "/views/auth/forgot-password.fxml");
        routes.put("reset-password",  "/views/auth/reset-password.fxml");
        routes.put("face-login", "/views/auth/face-login.fxml");
        routes.put("login", "/views/auth/LoginView.fxml");
    }

    public static void go(String route, Object... params) {
        if (!routes.containsKey(route)) {
            System.err.println("[Router] Route inconnue : " + route);
            return;
        }

        // On ne court-circuite que si container != null et même route
        if (container != null && route.equals(currentRoute)) return;

        try {
            // Pas de cache pour les vues avec paramètres pour éviter les collisions d'état
            boolean useCache = (container != null) && (params == null || params.length == 0);
            Node view = useCache ? viewCache.get(route) : null;

            if (view == null) {
                URL resource = Router.class.getResource(routes.get(route));
                if (resource == null) {
                    view = makePlaceholder(route);
                } else {
                    FXMLLoader loader = new FXMLLoader(resource);
                    view = loader.load();
                    Object controller = loader.getController();

                    if (controller != null && params != null && params.length > 0) {
                        injectParameters(controller, params);
                        handleLegacySpecialRoutes(route, controller, params);
                    }
                }
                if (useCache) viewCache.put(route, view);
            }

            if (container != null) {
                container.getChildren().setAll(view);
            } else {
                Node finalView = view;
                Platform.runLater(() -> {
                    Stage stage = (Stage) Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
                    if (stage != null) {
                        Scene scene = stage.getScene();
                        if (scene == null) {
                            stage.setScene(new Scene((Parent) finalView));
                        } else {
                            scene.setRoot((Parent) finalView);
                        }
                        stage.show();
                    }
                });
            }

            currentRoute = route;
            if (onRouteChange != null) onRouteChange.accept(route);

        } catch (IOException e) {
            System.err.println("[Router] Erreur '" + route + "' : " + e.getMessage());
            if (container != null) container.getChildren().setAll(makePlaceholder(route));
        }
    }

    public static void go(String route) {
        go(route, (Object[]) null);
    }

    private static void injectParameters(Object controller, Object[] params) {
        if (params.length > 0 && params[0] instanceof String) {
            try {
                Method setToken = controller.getClass().getMethod("setToken", String.class);
                setToken.invoke(controller, params[0]);
            } catch (NoSuchMethodException ignored) {
            } catch (Exception e) {
                System.err.println("Erreur injection token: " + e.getMessage());
            }
        }
    }

    private static void handleLegacySpecialRoutes(String route, Object controller, Object[] params) {
        try {
            if ("edit_event".equals(route) && params[0] instanceof SchoolEvent) {
                controller.getClass().getMethod("setEvent", SchoolEvent.class).invoke(controller, params[0]);
            } else if ("event_detail".equals(route) && params.length > 0) {
                controller.getClass().getMethod("setEventId", int.class).invoke(controller, params[0]);
            } else if ("event_resource".equals(route) && params.length > 1) {
                controller.getClass().getMethod("setEventId", int.class, String.class).invoke(controller, params[0], params[1]);
            } else if ("add_resource".equals(route)) {
                if (params.length > 0) controller.getClass().getMethod("setEventId", int.class).invoke(controller, params[0]);
                if (params.length > 1) controller.getClass().getMethod("setEventTitle", String.class).invoke(controller, params[1]);
            } else if ("edit_resource".equals(route) || "resource_detail".equals(route)) {
                if (params.length > 0) controller.getClass().getMethod("setEventId", int.class).invoke(controller, params[0]);
                if (params.length > 1) controller.getClass().getMethod("setEventTitle", String.class).invoke(controller, params[1]);
                if (params.length > 2 && params[2] instanceof EventResource) controller.getClass().getMethod("setResource", EventResource.class).invoke(controller, params[2]);
            } else if (("registration_detail".equals(route) || "edit_registration".equals(route)) && params.length > 0) {
                Object p = params[0];
                if (p instanceof Integer) controller.getClass().getMethod("setRegistrationId", int.class).invoke(controller, p);
                else if (p instanceof EventRegistration) controller.getClass().getMethod("setRegistration", EventRegistration.class).invoke(controller, p);
            }
        } catch (Exception e) {
            // Ignoré
        }
    }

    public static void handleDeepLink(String url) {
        System.out.println("[Router] Deep Link reçu: " + url);
        if (url == null || !url.contains("://")) return;

        String path = url.split("://")[1];
        if (path.startsWith("reset-password")) {
            String token = "";
            if (path.contains("token=")) {
                token = path.substring(path.indexOf("token=") + 6).split("&")[0];
            }
            final String finalToken = token;
            Platform.runLater(() -> go("reset-password", finalToken));
        }
    }

    private static Node makePlaceholder(String route) {
        Label title = new Label("Vue en cours de développement");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #9999BB;");
        Label sub = new Label("Route : " + route + "  —  FXML à intégrer");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #BBBBCC;");
        VBox box = new VBox(12, title, sub);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #F8F9FA;");
        return box;
    }

    public static void reload(String route) { viewCache.remove(route); currentRoute = ""; go(route); }
    public static String getCurrentRoute() { return currentRoute; }
    public static void setOnRouteChange(Consumer<String> l) { onRouteChange = l; }
    public static void clearCache() { viewCache.clear(); currentRoute = ""; }
}