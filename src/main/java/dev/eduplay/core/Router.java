package dev.eduplay.core;

import dev.eduplay.controllers.ScannerController;
import dev.eduplay.controllers.event.EditRegistrationController;
import dev.eduplay.controllers.event.RegistrationDetailController;
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
    private static Object currentController;
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
        routes.put("statistics",      "/views/admin/StatisticsView.fxml");
        routes.put("library",         "/views/admin/AdminCoursesView.fxml");
        routes.put("resource",        "/views/admin/AdminSeancesView.fxml");
        routes.put("admin_calendar",  "/views/admin/AdminCalendarView.fxml");
        routes.put("admin_stats",     "/views/admin/AdminStatsView.fxml");
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
        routes.put("teacher_seances",   "/views/teacher/TeacherSeancesView.fxml");

        routes.put("levels_list",       "/views/teacher/level/ListLevel.fxml");
        routes.put("Ajout_level",       "/views/teacher/level/AjoutLevel.fxml");
        routes.put("Modifier_level",    "/views/teacher/level/ModiferLevel.fxml");
        routes.put("games_list",        "/views/teacher/game/ListGame.fxml");
        routes.put("Ajout_game",        "/views/teacher/game/AjoutGame.fxml");
        routes.put("Modifier_game",     "/views/teacher/game/ModifierGame.fxml");

        // ==================== PARENT (Front Office) ====================
        routes.put("parent_dashboard",          "/views/parent/ParentDashboardView.fxml");
        routes.put("parent_children",           "/views/parent/ChildrenView.fxml");
        routes.put("parent_events",             "/views/event/event_list.fxml");
        routes.put("parent_event_list",         "/views/parent/ParentEventList.fxml");
        routes.put("parent_registrations",      "/views/parent/ParentRegistrationsList.fxml");
        routes.put("parent_event_detail",       "/views/parent/ParentEventDetail.fxml");
        routes.put("parent_registration_form",  "/views/parent/ParentRegistrationForm.fxml");
        routes.put("parent_registration_detail","/views/parent/ParentRegistrationDetail.fxml");
        routes.put("parent_courses",            "/views/parent/ParentCoursesView.fxml");
        routes.put("parent_seances",            "/views/parent/ParentSeancesView.fxml");

        // Enfant
        routes.put("child_dashboard",  "/views/child/ChildDashboardView.fxml");
        routes.put("child_courses",    "/views/child/MyCoursesView.fxml");
        routes.put("child_games",      "/views/child/game/ChildGamesView.fxml");
        routes.put("child_library",    "/views/child/ChildLibraryView.fxml");
        routes.put("child_seances",    "/views/child/ChildSeancesView.fxml");
        routes.put("child_resource",   "/views/child/ChildResourceView.fxml");

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

        // ==================== SCANNER QR CODE ====================
        routes.put("scanner", "/views/scanner/ScannerView.fxml");

        // ==================== COMMUN ====================
        routes.put("profile", "/views/shared/ProfileView.fxml");
        routes.put("forgot-password", "/views/auth/forgot-password.fxml");
        routes.put("reset-password",  "/views/auth/reset-password.fxml");
        routes.put("face-login", "/views/auth/face-login.fxml");
        routes.put("login", "/views/auth/LoginView.fxml");
        routes.put("admin_user_form", "/views/admin/UserFormView.fxml");

        // Produits & Marketplace
        routes.put("admin_product_index", "/ProductIndex.fxml");
        routes.put("admin_product_form",  "/ProductForm.fxml");
        routes.put("parent_marketplace",  "/views/parent/ParentMarketplaceView.fxml");
        routes.put("parent_cart",         "/views/parent/ParentCartView.fxml");
        routes.put("parent_orders",       "/views/parent/ParentOrdersView.fxml");
        routes.put("parent_chat",         "/views/parent/ParentChatView.fxml");
        routes.put("parent_order_form",   "/views/parent/ParentOrderForm.fxml");
        routes.put("parent_stripe_payment", "/views/parent/StripePaymentView.fxml");
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

        // Nettoyer le contrôleur précédent
        cleanupCurrentController();

        try {
            boolean isDynamicRoute = route.equals("event_detail") ||
                    route.equals("edit_event") ||
                    route.equals("event_resource") ||
                    route.equals("resource_detail") ||
                    route.equals("registration_detail") ||
                    route.equals("edit_registration") ||
                    route.equals("parent_event_detail") ||
                    route.equals("parent_registration_form") ||
                    route.equals("parent_registration_detail") ||
                    route.equals("add_resource") ||
                    route.equals("edit_resource") ||
                    route.equals("add_event") ||
                    route.equals("parent_stripe_payment") ||
                    route.equals("scanner") ||
                    route.equals("statistics");

            if (isDynamicRoute) {
                viewCache.remove(route);
            }

            Node view = viewCache.get(route);

            if (view == null) {
                String fxmlPath = routes.get(route);
                System.out.println("Chargement FXML: " + fxmlPath);
                URL resource = Router.class.getResource(fxmlPath);

                if (resource == null) {
                    System.err.println("Resource non trouvée: " + fxmlPath);
                    view = makePlaceholder(route);
                    currentController = null;
                } else {
                    FXMLLoader loader = new FXMLLoader(resource);
                    view = loader.load();

                    Object controller = loader.getController();
                    currentController = controller;

                    if (controller != null) {
                        injectParameters(route, controller);
                    }
                }

                if (!isDynamicRoute && view != null) {
                    viewCache.put(route, view);
                }
            }

            if (view != null) {
                container.getChildren().setAll(view);
                currentRoute = route;
                if (onRouteChange != null) onRouteChange.accept(route);
            }

        } catch (IOException e) {
            System.err.println("[Router] Erreur '" + route + "' : " + e.getMessage());
            e.printStackTrace();
            container.getChildren().setAll(makePlaceholder(route));
            currentController = null;
        }
    }

    private static void injectParameters(String route, Object controller) {
        try {
            // ==================== PARENT EVENT DETAIL ====================
            if ("parent_event_detail".equals(route) && routeParams.containsKey("param0")) {
                Object param = routeParams.get("param0");
                System.out.println("=== INJECTION parent_event_detail avec param: " + param);
                if (param instanceof Integer) {
                    controller.getClass().getMethod("setEventId", int.class)
                            .invoke(controller, (int) param);
                }
            }

            // ==================== PARENT REGISTRATION FORM ====================
            if ("parent_registration_form".equals(route) && routeParams.containsKey("param0")) {
                Object param = routeParams.get("param0");
                System.out.println("=== INJECTION parent_registration_form avec param: " + param);
                if (param instanceof Integer) {
                    controller.getClass().getMethod("setEventId", int.class)
                            .invoke(controller, (int) param);
                }
                if (routeParams.containsKey("param1")) {
                    String eventTitle = (String) routeParams.get("param1");
                    controller.getClass().getMethod("setEventTitle", String.class)
                            .invoke(controller, eventTitle);
                }
            }

            // ==================== SCANNER ====================
            if ("scanner".equals(route) && routeParams.containsKey("param0")) {
                Boolean continuousMode = (Boolean) routeParams.get("param0");
                controller.getClass().getMethod("setContinuousMode", boolean.class)
                        .invoke(controller, continuousMode);
            }

            // ==================== REGISTRATION DETAIL (ADMIN) ====================
            if ("registration_detail".equals(route) && routeParams.containsKey("param0")) {
                Object param = routeParams.get("param0");
                if (param instanceof Integer) {
                    controller.getClass().getMethod("setRegistrationId", int.class)
                            .invoke(controller, (int) param);
                } else if (param instanceof EventRegistration) {
                    controller.getClass().getMethod("setRegistration", EventRegistration.class)
                            .invoke(controller, param);
                }
            }

            // ==================== EDIT REGISTRATION (ADMIN) ====================
            if ("edit_registration".equals(route) && routeParams.containsKey("param0")) {
                Object param = routeParams.get("param0");
                if (param instanceof Integer) {
                    controller.getClass().getMethod("setRegistrationId", int.class)
                            .invoke(controller, (int) param);
                } else if (param instanceof EventRegistration) {
                    controller.getClass().getMethod("setRegistration", EventRegistration.class)
                            .invoke(controller, param);
                }
            }

            // ==================== PARENT REGISTRATION DETAIL ====================
            if ("parent_registration_detail".equals(route) && routeParams.containsKey("param0")) {
                Object param = routeParams.get("param0");
                if (param instanceof Integer) {
                    controller.getClass().getMethod("setRegistrationId", int.class)
                            .invoke(controller, (int) param);
                } else if (param instanceof EventRegistration) {
                    controller.getClass().getMethod("setRegistration", EventRegistration.class)
                            .invoke(controller, param);
                }
            }

            // ==================== BACK OFFICE - ÉVÉNEMENTS ====================
            if ("edit_event".equals(route) && routeParams.containsKey("param0")) {
                Object param = routeParams.get("param0");
                if (param instanceof SchoolEvent) {
                    controller.getClass().getMethod("setEvent", SchoolEvent.class)
                            .invoke(controller, param);
                }
            }

            if ("event_detail".equals(route) && routeParams.containsKey("param0")) {
                controller.getClass().getMethod("setEventId", int.class)
                        .invoke(controller, (int) routeParams.get("param0"));
            }

            if ("event_resource".equals(route)) {
                if (routeParams.containsKey("param0")) {
                    controller.getClass().getMethod("setEventId", int.class, String.class)
                            .invoke(controller, (int) routeParams.get("param0"), (String) routeParams.get("param1"));
                }
            }

            if ("add_resource".equals(route)) {
                if (routeParams.containsKey("param0")) {
                    controller.getClass().getMethod("setEventId", int.class)
                            .invoke(controller, (int) routeParams.get("param0"));
                }
                if (routeParams.containsKey("param1")) {
                    controller.getClass().getMethod("setEventTitle", String.class)
                            .invoke(controller, (String) routeParams.get("param1"));
                }
            }

            if ("edit_resource".equals(route)) {
                if (routeParams.containsKey("param0")) {
                    controller.getClass().getMethod("setEventId", int.class)
                            .invoke(controller, (int) routeParams.get("param0"));
                }
                if (routeParams.containsKey("param1")) {
                    controller.getClass().getMethod("setEventTitle", String.class)
                            .invoke(controller, (String) routeParams.get("param1"));
                }
                if (routeParams.containsKey("param2")) {
                    Object param = routeParams.get("param2");
                    if (param instanceof EventResource) {
                        controller.getClass().getMethod("setResource", EventResource.class)
                                .invoke(controller, param);
                    }
                }
            }

            if ("resource_detail".equals(route)) {
                if (routeParams.containsKey("param0")) {
                    controller.getClass().getMethod("setEventId", int.class)
                            .invoke(controller, (int) routeParams.get("param0"));
                }
                if (routeParams.containsKey("param1")) {
                    controller.getClass().getMethod("setEventTitle", String.class)
                            .invoke(controller, (String) routeParams.get("param1"));
                }
                if (routeParams.containsKey("param2")) {
                    Object param = routeParams.get("param2");
                    if (param instanceof EventResource) {
                        controller.getClass().getMethod("setResource", EventResource.class)
                                .invoke(controller, param);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Erreur injection paramètres pour route '" + route + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void cleanupCurrentController() {
        if (currentController != null) {
            if (currentController instanceof ScannerController) {
                try {
                    System.out.println("[Router] Nettoyage du ScannerController...");
                    ((ScannerController) currentController).cleanup();
                } catch (Exception e) {
                    System.err.println("[Router] Erreur nettoyage ScannerController: " + e.getMessage());
                }
            }
            try {
                currentController.getClass().getMethod("cleanup").invoke(currentController);
            } catch (NoSuchMethodException e) {
                // Pas de méthode cleanup, c'est normal
            } catch (Exception e) {
                System.err.println("[Router] Erreur appel cleanup(): " + e.getMessage());
            }
        }
    }

    public static void reload(String route) {
        cleanupCurrentController();
        viewCache.remove(route);
        currentRoute = "";
        go(route);
    }

    public static void reload(String route, Object data) {
        transitData = data;
        reload(route);
    }

    public static Object getTransitData() {
        Object data = transitData;
        transitData = null;
        return data;
    }

    public static StackPane getContainer() { return container; }
    public static String getCurrentRoute() { return currentRoute; }
    public static void setOnRouteChange(Consumer<String> l) { onRouteChange = l; }
    public static void clearCache() {
        viewCache.clear();
        currentRoute = "";
        routeParams.clear();
        currentController = null;
    }
    public static Object getCurrentController() { return currentController; }

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
}