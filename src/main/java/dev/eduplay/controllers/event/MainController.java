package dev.eduplay.controllers.event;

import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.EventResource;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.services.SchoolEventService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class MainController {

    @FXML private StackPane contentContainer;
    @FXML private Label menuEvents;
    @FXML private Label menuInscriptions;
    @FXML private Label menuScanner;

    private Stage primaryStage;
    private EventListController eventListController;
    private SchoolEventService eventService;

    @FXML
    public void initialize() {
        System.out.println("=== MainController initialisé ===");
        eventService = new SchoolEventService();

        menuEvents.setOnMouseClicked(e -> goToEventList());
        menuInscriptions.setOnMouseClicked(e -> goToInscriptions());
        menuInscriptions.setOnMouseClicked(e -> goToRegistrationList());
        menuScanner.setOnMouseClicked(e -> goToScanner());

        goToEventList();
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    private void loadView(String fxmlPath, String title) {
        try {
            System.out.println("Chargement de : " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            if (loader.getLocation() == null) {
                System.err.println("Fichier non trouvé: " + fxmlPath);
                showErrorMessage("Fichier " + fxmlPath + " non trouvé");
                return;
            }

            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof EventListController) {
                ((EventListController) controller).setMainController(this);
            } else if (controller instanceof AddEventController) {
                ((AddEventController) controller).setMainController(this);
            } else if (controller instanceof EventDetailController) {
                ((EventDetailController) controller).setMainController(this);
            } else if (controller instanceof EventResourceController) {
                ((EventResourceController) controller).setMainController(this);
            } else if (controller instanceof AddResourceController) {
                ((AddResourceController) controller).setMainController(this);
            } else if (controller instanceof ResourceDetailController) {
                ((ResourceDetailController) controller).setMainController(this);
            } else if (controller instanceof RegistrationListController) {
                ((RegistrationListController) controller).setMainController(this);
                System.out.println("✅ RegistrationListController lié au MainController");
            }

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(root);

            if (primaryStage != null) {
                primaryStage.setTitle("EduPlay - " + title);
            }

        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de " + fxmlPath);
            e.printStackTrace();
            showErrorMessage("Erreur : Impossible de charger " + fxmlPath);
        }
    }

    public void goToEventList() {
        loadView("/event/event_list.fxml", "Gestion des événements");
    }

    public void goToAddEvent() {
        System.out.println("Navigation vers Ajout d'événement");
        loadView("/event/add_event.fxml", "Ajouter un événement");
    }

    public void goToEditEvent(int eventId) {
        try {
            System.out.println("Navigation vers Modification de l'event ID: " + eventId);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/add_event.fxml"));
            Parent root = loader.load();

            AddEventController controller = loader.getController();
            controller.setMainController(this);
            controller.setEventToModify(eventId);

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(root);

            if (primaryStage != null) {
                primaryStage.setTitle("EduPlay - Modifier un événement");
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de la modification");
            e.printStackTrace();
            showErrorMessage("Impossible d'ouvrir le formulaire de modification");
        }
    }

    public void goToEventDetail(int eventId) {
        try {
            System.out.println("Navigation vers Détails de l'event ID: " + eventId);

            SchoolEvent event = eventService.recupererParId(eventId);

            if (event == null) {
                showErrorMessage("Événement non trouvé (ID: " + eventId + ")");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/event_detail.fxml"));
            Parent root = loader.load();

            EventDetailController controller = loader.getController();
            controller.setMainController(this);
            controller.setEvent(event);

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(root);

            if (primaryStage != null) {
                primaryStage.setTitle("EduPlay - Détails - " + event.getTitle());
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement des détails");
            e.printStackTrace();
            showErrorMessage("Impossible d'ouvrir la page des détails");
        } catch (SQLException e) {
            System.err.println("Erreur base de données: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Erreur lors de la récupération de l'événement");
        }
    }

    public void goToEventResources(int eventId, String eventTitle) {
        try {
            System.out.println("Navigation vers Ressources pour: " + eventTitle);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/event_resource.fxml"));
            Parent root = loader.load();

            EventResourceController controller = loader.getController();
            controller.setMainController(this);
            controller.setEventId(eventId, eventTitle);

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(root);

            if (primaryStage != null) {
                primaryStage.setTitle("EduPlay - Ressources - " + eventTitle);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Erreur: " + e.getMessage());
        }
    }

    public void goToAddResource(int eventId, String eventTitle) {
        try {
            System.out.println("Navigation vers Ajout de ressource");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/add_resource.fxml"));
            Parent root = loader.load();
            AddResourceController controller = loader.getController();
            controller.setMainController(this);
            controller.setEventId(eventId, eventTitle);
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(root);
            if (primaryStage != null) {
                primaryStage.setTitle("EduPlay - Ajouter une ressource - " + eventTitle);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Impossible d'ouvrir le formulaire d'ajout de ressource");
        }
    }

    public void goToEditResource(int eventId, String eventTitle, EventResource resource) {
        try {
            System.out.println("Navigation vers Modification de la ressource ID: " + resource.getId());
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/add_resource.fxml"));
            Parent root = loader.load();
            AddResourceController controller = loader.getController();
            controller.setMainController(this);
            controller.setEventId(eventId, eventTitle);
            controller.setResourceToModify(resource);
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(root);
            if (primaryStage != null) {
                primaryStage.setTitle("EduPlay - Modifier une ressource - " + eventTitle);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Impossible d'ouvrir le formulaire de modification de ressource");
        }
    }

    public void refreshEventList() {
        if (eventListController != null) {
            eventListController.refreshList();
        }
        goToEventList();
    }

    private void goToInscriptions() {
        showTemporaryMessage("📋 Page des inscriptions aux événements (à venir)");
    }

    private void goToScanner() {
        showTemporaryMessage("🎫 Scanner de tickets (à venir)");
    }

    private void showTemporaryMessage(String message) {
        Label tempLabel = new Label(message);
        tempLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #4f46e5; -fx-padding: 50; -fx-alignment: center; -fx-font-weight: bold;");
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(tempLabel);
    }

    private void showErrorMessage(String message) {
        Label errorLabel = new Label("❌ " + message);
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-padding: 50; -fx-alignment: center; -fx-font-weight: bold; -fx-wrap-text: true;");
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(errorLabel);
    }


    public void goToResourceDetail(int eventId, String eventTitle, EventResource resource) {
        try {
            System.out.println("Navigation vers Détails de la ressource ID: " + resource.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/resource_detail.fxml"));
            Parent root = loader.load();

            ResourceDetailController controller = loader.getController();
            controller.setMainController(this);
            controller.setEventInfo(eventId, eventTitle);
            controller.setResource(resource);

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(root);

            if (primaryStage != null) {
                primaryStage.setTitle("EduPlay - Détails - " + resource.getTitle());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Impossible d'ouvrir la page des détails");
        }
    }

    public void goToRegistrationList() {
        loadView("/event/registration_list.fxml", "Gestion des inscriptions");
    }

    public void goToRegistrationDetail(EventRegistration registration) {
        try {
            System.out.println("=== goToRegistrationDetail appelé ===");
            System.out.println("ID inscription: " + registration.getId());
            System.out.println("Enfant: " + registration.getChildFullName());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/registration_detail.fxml"));

            if (loader.getLocation() == null) {
                showErrorMessage("Fichier registration_detail.fxml non trouvé");
                return;
            }

            Parent root = loader.load();

            RegistrationDetailController controller = loader.getController();
            controller.setMainController(this);
            controller.setRegistration(registration);

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(root);

            if (primaryStage != null) {
                primaryStage.setTitle("EduPlay - Détails inscription - " + registration.getChildFullName());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Impossible d'ouvrir la page des détails: " + e.getMessage());
        }
    }

    public void goToEditRegistration(EventRegistration registration) {
        try {
            System.out.println("=== goToEditRegistration appelé ===");
            System.out.println("ID inscription: " + registration.getId());

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/event/edit_registration.fxml"));
            Parent root = loader.load();

            EditRegistrationController controller = loader.getController();
            controller.setMainController(this);
            controller.setRegistration(registration);

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(root);

            if (primaryStage != null) {
                primaryStage.setTitle("EduPlay - Modifier inscription - " + registration.getChildFullName());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Impossible d'ouvrir la page de modification: " + e.getMessage());
        }
    }


}