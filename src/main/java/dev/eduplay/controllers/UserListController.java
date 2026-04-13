package dev.eduplay.controllers;

import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserListController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String>  colFirstName;
    @FXML private TableColumn<User, String>  colLastName;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colType;
    @FXML private TableColumn<User, Boolean> colActive;
    @FXML private TableColumn<User, Void>    colActions;

    @FXML private TextField      searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Label          statusLabel;

    private final UserService userService = new UserService();
    private ObservableList<User> allUsers = FXCollections.observableArrayList();

    // ─────────────────────────────────────────
    public void initialize() {
        setupColumns();
        setupActionColumn();

        typeFilter.getItems().addAll("Tous", "admin", "enseignant", "parent", "enfant");
        typeFilter.setValue("Tous");
        typeFilter.valueProperty().addListener((obs, o, n) -> applyFilters());

        loadUsers();
    }

    // ── Binding colonnes ──────────────────────
    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Colonne Type : badge coloré selon la valeur
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(type);
                badge.setStyle(getBadgeStyle(type));
                setGraphic(badge);
                setText(null);
            }
        });

        // Colonne Actif : Oui / Non avec couleur
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        colActive.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) { setGraphic(null); return; }
                Label lbl = new Label(active ? "Actif" : "Inactif");
                lbl.setStyle(active
                        ? "-fx-background-color: #D4F7E8; -fx-text-fill: #1A7A4A; " +
                        "-fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px;"
                        : "-fx-background-color: #FFE8E8; -fx-text-fill: #C0304A; " +
                        "-fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px;");
                setGraphic(lbl);
                setText(null);
            }
        });
    }

    // ── CellFactory boutons Actions ───────────
    private void setupActionColumn() {
        colActions.setCellFactory(new Callback<>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> col) {
                return new TableCell<>() {
                    private final Button btnEdit   = new Button("Modifier");
                    private final Button btnDelete = new Button("Suppr.");
                    private final HBox   box       = new HBox(6, btnEdit, btnDelete);

                    {
                        btnEdit.setStyle(
                                "-fx-background-color: #4A90D9; -fx-text-fill: white;" +
                                        "-fx-font-size: 11px; -fx-padding: 4 10;" +
                                        "-fx-background-radius: 4; -fx-cursor: hand;");
                        btnDelete.setStyle(
                                "-fx-background-color: #E94560; -fx-text-fill: white;" +
                                        "-fx-font-size: 11px; -fx-padding: 4 10;" +
                                        "-fx-background-radius: 4; -fx-cursor: hand;");

                        btnEdit.setOnAction(e -> {
                            User u = getTableView().getItems().get(getIndex());
                            openEditForm(u);
                        });

                        btnDelete.setOnAction(e -> {
                            User u = getTableView().getItems().get(getIndex());
                            confirmAndDelete(u);
                        });

                        box.setStyle("-fx-alignment: CENTER;");
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : box);
                    }
                };
            }
        });
    }

    // ── Chargement ────────────────────────────
    private void loadUsers() {
        allUsers.setAll(userService.getAll());
        userTable.setItems(allUsers);
        updateStatus();
    }

    // ── Filtres / Recherche ───────────────────
    @FXML
    public void search() {
        applyFilters();
    }

    private void applyFilters() {
        String kw   = searchField.getText().toLowerCase().trim();
        String type = typeFilter.getValue();

        List<User> filtered = allUsers.stream()
                .filter(u -> "Tous".equals(type) || type.equals(u.getType()))
                .filter(u -> kw.isEmpty()
                        || (u.getFirstName() != null && u.getFirstName().toLowerCase().contains(kw))
                        || (u.getLastName()  != null && u.getLastName().toLowerCase().contains(kw))
                        || (u.getEmail()     != null && u.getEmail().toLowerCase().contains(kw)))
                .collect(Collectors.toList());

        userTable.setItems(FXCollections.observableArrayList(filtered));
        updateStatus();
    }

    // ── Actions tableau ───────────────────────
    @FXML
    public void deleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert(Alert.AlertType.WARNING, "Sélectionnez un utilisateur.");
            return;
        }
        confirmAndDelete(selected);
    }

    private void confirmAndDelete(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer " + user.getFullName() + " définitivement ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userService.supprimer(user);
            allUsers.remove(user);
            applyFilters();
            alert(Alert.AlertType.INFORMATION, "Utilisateur supprimé.");
        }
    }

    @FXML
    public void toggleActiveUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert(Alert.AlertType.WARNING, "Sélectionnez un utilisateur.");
            return;
        }
        selected.setActive(!selected.isActive());
        userService.modifier(selected);
        userTable.refresh();
    }

    @FXML
    public void refreshList() {
        searchField.clear();
        typeFilter.setValue("Tous");
        loadUsers();
    }

    // ── Navigation ────────────────────────────
    @FXML
    public void openAddUserForm() {
        openForm(null);
    }

    @FXML
    public void editSelectedUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            alert(Alert.AlertType.WARNING, "Sélectionnez un utilisateur à modifier.");
            return;
        }
        openEditForm(selected);
    }

    private void openEditForm(User user) {
        openForm(user);
    }

    private void openForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/child/UserFormView.fxml"));
            Parent root = loader.load();

            UserFormController ctrl = loader.getController();
            if (user != null) ctrl.setUser(user);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(user == null ? "Nouvel utilisateur" : "Modifier — " + user.getFullName());
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> loadUsers()); // refresh après fermeture
            stage.show();

        } catch (IOException e) {
            alert(Alert.AlertType.ERROR, "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    // ── Utilitaires ───────────────────────────
    private void updateStatus() {
        int count = userTable.getItems().size();
        statusLabel.setText(count + " utilisateur" + (count > 1 ? "s" : "") + " affiché" + (count > 1 ? "s" : ""));
    }

    private void alert(Alert.AlertType type, String message) {
        new Alert(type, message, ButtonType.OK).showAndWait();
    }

    private String getBadgeStyle(String type) {
        return switch (type) {
            case "admin"      -> "-fx-background-color: #F0E8FF; -fx-text-fill: #6030A0; " +
                    "-fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px;";
            case "enseignant" -> "-fx-background-color: #FFF0E0; -fx-text-fill: #A05010; " +
                    "-fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px;";
            case "parent"     -> "-fx-background-color: #E0F0FF; -fx-text-fill: #105090; " +
                    "-fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px;";
            case "enfant"     -> "-fx-background-color: #E0FFE8; -fx-text-fill: #106030; " +
                    "-fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px;";
            default           -> "-fx-background-color: #F0F0F0; -fx-text-fill: #606060; " +
                    "-fx-padding: 3 10; -fx-background-radius: 12; -fx-font-size: 11px;";
        };
    }
}