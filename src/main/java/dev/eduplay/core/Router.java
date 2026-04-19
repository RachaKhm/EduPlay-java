package dev.eduplay.core;

import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.EventResource;
import dev.eduplay.entities.SchoolEvent;
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

    private static final Map<String, Object> routeParams = new HashMap<>();

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
        routes.put("statistics_index",     "/StatisticsView.fxml");

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

        routes.put("child_resource",   "/views/child/ChildResourceView.fxml");
        routes.put("child_library",    "/views/child/ChildLibraryView.fxml");
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
    }

    public static void go(String route, Object... params) {
        System.out.println("=== Router.go avec paramètres ===");
        System.out.println("Route: " + route);
        for (int i = 0; i < params.length; i++) {
            System.out.println("param" + i + ": " + params[i]);
        }

        if (!routes.containsKey(route)) {
            System.err.println("[Router] Route inconnue : " + route);
            return;
        }

        routeParams.clear();
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                routeParams.put("param" + i, params[i]);
            }
        }

        go(route);
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
                    FXMLLoader loader = new FXMLLoader(resource);
                    view = loader.load();

                    Object controller = loader.getController();
                    if (controller != null) {
                        // Pour l'ajout d'événement (pas de paramètre)
                        if ("add_event".equals(route)) {
                            // Rien à faire, c'est un ajout
                        }
                        // Pour la modification d'événement
                        if ("edit_event".equals(route) && routeParams.containsKey("param0")) {
                            try {
                                Object param = routeParams.get("param0");
                                if (param instanceof SchoolEvent) {
                                    controller.getClass().getMethod("setEvent", SchoolEvent.class)
                                            .invoke(controller, param);
                                }
                            } catch (Exception e) {
                                System.err.println("Erreur setEvent: " + e.getMessage());
                            }
                        }
                        // Pour les détails d'événement
                        if ("event_detail".equals(route) && routeParams.containsKey("param0")) {
                            try {
                                controller.getClass().getMethod("setEventId", int.class)
                                        .invoke(controller, (int) routeParams.get("param0"));
                            } catch (Exception e) {
                                System.err.println("Erreur setEventId: " + e.getMessage());
                            }
                        }
                        // Pour event_resource
                        if ("event_resource".equals(route)) {
                            if (routeParams.containsKey("param0")) {
                                try {
                                    controller.getClass().getMethod("setEventId", int.class, String.class)
                                            .invoke(controller, (int) routeParams.get("param0"), (String) routeParams.get("param1"));
                                } catch (Exception e) {
                                    System.err.println("Erreur setEventId event_resource: " + e.getMessage());
                                }
                            }
                        }
                        // Pour add_resource (AJOUT)
                        if ("add_resource".equals(route)) {
                            if (routeParams.containsKey("param0")) {
                                try {
                                    controller.getClass().getMethod("setEventId", int.class)
                                            .invoke(controller, (int) routeParams.get("param0"));
                                } catch (Exception e) {
                                    System.err.println("Erreur setEventId add_resource: " + e.getMessage());
                                }
                            }
                            if (routeParams.containsKey("param1")) {
                                try {
                                    controller.getClass().getMethod("setEventTitle", String.class)
                                            .invoke(controller, (String) routeParams.get("param1"));
                                } catch (Exception e) {
                                    System.err.println("Erreur setEventTitle add_resource: " + e.getMessage());
                                }
                            }
                        }
                        // Pour edit_resource (MODIFICATION)
                        if ("edit_resource".equals(route)) {
                            if (routeParams.containsKey("param0")) {
                                try {
                                    controller.getClass().getMethod("setEventId", int.class)
                                            .invoke(controller, (int) routeParams.get("param0"));
                                } catch (Exception e) {
                                    System.err.println("Erreur setEventId edit_resource: " + e.getMessage());
                                }
                            }
                            if (routeParams.containsKey("param1")) {
                                try {
                                    controller.getClass().getMethod("setEventTitle", String.class)
                                            .invoke(controller, (String) routeParams.get("param1"));
                                } catch (Exception e) {
                                    System.err.println("Erreur setEventTitle edit_resource: " + e.getMessage());
                                }
                            }
                            if (routeParams.containsKey("param2")) {
                                try {
                                    Object param = routeParams.get("param2");
                                    if (param instanceof EventResource) {
                                        controller.getClass().getMethod("setResource", EventResource.class)
                                                .invoke(controller, param);
                                    }
                                } catch (Exception e) {
                                    System.err.println("Erreur setResource edit_resource: " + e.getMessage());
                                }
                            }
                        }
                        // Pour resource_detail
                        if ("resource_detail".equals(route)) {
                            if (routeParams.containsKey("param0")) {
                                try {
                                    controller.getClass().getMethod("setEventId", int.class)
                                            .invoke(controller, (int) routeParams.get("param0"));
                                } catch (Exception e) {
                                    System.err.println("Erreur setEventId resource_detail: " + e.getMessage());
                                }
                            }
                            if (routeParams.containsKey("param1")) {
                                try {
                                    controller.getClass().getMethod("setEventTitle", String.class)
                                            .invoke(controller, (String) routeParams.get("param1"));
                                } catch (Exception e) {
                                    System.err.println("Erreur setEventTitle resource_detail: " + e.getMessage());
                                }
                            }
                            if (routeParams.containsKey("param2")) {
                                try {
                                    Object param = routeParams.get("param2");
                                    if (param instanceof EventResource) {
                                        controller.getClass().getMethod("setResource", EventResource.class)
                                                .invoke(controller, param);
                                    }
                                } catch (Exception e) {
                                    System.err.println("Erreur setResource resource_detail: " + e.getMessage());
                                }
                            }
                        }
                        // Pour les détails d'inscription
                        if ("registration_detail".equals(route) && routeParams.containsKey("param0")) {
                            try {
                                Object param = routeParams.get("param0");
                                if (param instanceof Integer) {
                                    controller.getClass().getMethod("setRegistrationId", int.class)
                                            .invoke(controller, (int) param);
                                } else if (param instanceof EventRegistration) {
                                    controller.getClass().getMethod("setRegistration", EventRegistration.class)
                                            .invoke(controller, param);
                                }
                            } catch (Exception e) {
                                System.err.println("Erreur setRegistration: " + e.getMessage());
                            }
                        }
                        // Pour la modification d'inscription
                        if ("edit_registration".equals(route) && routeParams.containsKey("param0")) {
                            try {
                                Object param = routeParams.get("param0");
                                if (param instanceof Integer) {
                                    controller.getClass().getMethod("setRegistrationId", int.class)
                                            .invoke(controller, (int) param);
                                } else if (param instanceof EventRegistration) {
                                    controller.getClass().getMethod("setRegistration", EventRegistration.class)
                                            .invoke(controller, param);
                                }
                            } catch (Exception e) {
                                System.err.println("Erreur setRegistration edit: " + e.getMessage());
                            }
                        }
                    }
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
    public static void clearCache() { viewCache.clear(); currentRoute = ""; routeParams.clear(); }
}