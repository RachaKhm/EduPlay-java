package dev.eduplay.controllers;

import dev.eduplay.entities.Book;
import dev.eduplay.entities.Library;
import dev.eduplay.services.BookService;
import dev.eduplay.services.LibraryService;
import dev.eduplay.tools.ImageLoader;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class BookIndexController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterType;
    @FXML private ComboBox<String> filterLanguage;
    @FXML private Label countLabel;
    @FXML private FlowPane cardsPane;
    @FXML private VBox emptyState;
    @FXML private HBox flashBox;
    @FXML private Label flashLabel;

    private final BookService bookService = new BookService();
    private final LibraryService libraryService = new LibraryService();
    private List<Book> allBooks;

    @FXML
    public void initialize() {
        filterType.setItems(FXCollections.observableArrayList("Tous","Livre","Magazine","Journal","Article"));
        filterType.setValue("Tous");
        filterLanguage.setItems(FXCollections.observableArrayList("Tous","Français","Anglais","Arabe","Espagnol","Autre"));
        filterLanguage.setValue("Tous");
        loadBooks();
    }

    private void loadBooks() {
        allBooks = bookService.afficher();
        renderCards(allBooks);
    }

    private void renderCards(List<Book> list) {
        cardsPane.getChildren().clear();
        if (list.isEmpty()) {
            emptyState.setVisible(true); emptyState.setManaged(true);
            cardsPane.setVisible(false); countLabel.setText("");
        } else {
            emptyState.setVisible(false); emptyState.setManaged(false);
            cardsPane.setVisible(true);
            int n = list.size();
            countLabel.setText(n + " ressource" + (n > 1 ? "s" : "") + " trouvée" + (n > 1 ? "s" : ""));
            for (Book b : list) cardsPane.getChildren().add(createCard(b));
        }
    }

    private VBox createCard(Book book) {
        VBox card = new VBox();
        card.setPrefWidth(290); card.setMaxWidth(290);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16;" +
                "-fx-border-radius: 16; -fx-border-color: #e2e8f0; -fx-border-width: 2;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),12,0,0,4);");

        card.getChildren().addAll(buildImagePane(book), buildBody(book));
        return card;
    }

    private StackPane buildImagePane(Book book) {
        StackPane pane = new StackPane();
        pane.setPrefHeight(160); pane.setMinHeight(160); pane.setMaxHeight(160);
        Rectangle clip = new Rectangle(290, 160);
        clip.setArcWidth(28); clip.setArcHeight(28);
        pane.setClip(clip);

        Region bg = new Region();
        bg.setPrefSize(290, 160);
        bg.setStyle(getTypeGradient(book.getType()));

        Image img = ImageLoader.load(book.getCoverImage());
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(290); iv.setFitHeight(160);
            iv.setPreserveRatio(false); iv.setSmooth(true);
            pane.getChildren().addAll(bg, iv);
        } else {
            Label icon = new Label(getTypeEmoji(book.getType()));
            icon.setStyle("-fx-font-size: 48px;");
            pane.getChildren().addAll(bg, icon);
        }

        Label typeBadge = new Label(book.getType());
        typeBadge.setStyle(getTypeBadgeStyle(book.getType()));
        StackPane.setAlignment(typeBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(typeBadge, new Insets(10, 10, 0, 0));
        pane.getChildren().add(typeBadge);
        return pane;
    }

    private VBox buildBody(Book book) {
        VBox body = new VBox(10);
        body.setPadding(new Insets(16));

        Label title = new Label(book.getTitle());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        title.setWrapText(true);

        Label author = new Label("👤  " + book.getAuthor());
        author.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        HBox badges = new HBox(8);
        badges.setAlignment(Pos.CENTER_LEFT);
        Label ageBadge = new Label("⏱  " + book.getMinAge() + "-" + book.getMaxAge() + " ans");
        ageBadge.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-font-size: 11px;" +
                "-fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 10;");
        Label langBadge = new Label(book.getLanguage());
        langBadge.setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #15803d; -fx-font-size: 11px;" +
                "-fx-font-weight: bold; -fx-background-radius: 20; -fx-padding: 4 10;");
        badges.getChildren().addAll(ageBadge, langBadge);

        // Description résumé tronqué
        String sum = (book.getSummary() != null && !book.getSummary().isBlank())
                ? book.getSummary() : "Aucun résumé.";
        if (sum.length() > 70) sum = sum.substring(0, 70) + "...";
        Label sumLabel = new Label(sum);
        sumLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        sumLabel.setWrapText(true);

        // Boutons
        HBox buttons = new HBox(8);
        buttons.setAlignment(Pos.CENTER);

        Button voirBtn = new Button("👁  Voir");
        voirBtn.setMaxWidth(Double.MAX_VALUE);
        voirBtn.setStyle("-fx-background-color: white; -fx-text-fill: #374151; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 12;" +
                "-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-border-radius: 10; -fx-cursor: hand;");
        voirBtn.setOnAction(e -> ouvrirDetail(book));

        Button modifBtn = new Button("✏  Modifier");
        modifBtn.setMaxWidth(Double.MAX_VALUE);
        modifBtn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-font-size: 12px;" +
                "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 12;" +
                "-fx-border-color: #c7d2fe; -fx-border-width: 1; -fx-border-radius: 10; -fx-cursor: hand;");
        modifBtn.setOnAction(e -> ouvrirFormulaire(book));

        Button suppBtn = new Button("🗑");
        suppBtn.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #dc2626; -fx-font-size: 13px;" +
                "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 8 14;" +
                "-fx-border-color: #fecaca; -fx-border-width: 1; -fx-border-radius: 10; -fx-cursor: hand;");
        suppBtn.setOnAction(e -> handleSupprimer(book));

        HBox.setHgrow(voirBtn, Priority.ALWAYS);
        HBox.setHgrow(modifBtn, Priority.ALWAYS);
        buttons.getChildren().addAll(voirBtn, modifBtn, suppBtn);

        body.getChildren().addAll(title, author, badges, sumLabel, buttons);
        return body;
    }

    @FXML private void handleSearch() {
        String kw = searchField.getText().toLowerCase().trim();
        String type = filterType.getValue();
        String lang = filterLanguage.getValue();
        List<Book> filtered = allBooks.stream().filter(b -> {
            boolean k = kw.isEmpty() || b.getTitle().toLowerCase().contains(kw)
                    || b.getAuthor().toLowerCase().contains(kw)
                    || (b.getSummary() != null && b.getSummary().toLowerCase().contains(kw));
            boolean t = type == null || type.equals("Tous") || b.getType().equalsIgnoreCase(type);
            boolean l = lang == null || lang.equals("Tous") || b.getLanguage().equalsIgnoreCase(lang);
            return k && t && l;
        }).collect(Collectors.toList());
        renderCards(filtered);
    }

    @FXML private void handleReset() {
        searchField.clear();
        filterType.setValue("Tous");
        filterLanguage.setValue("Tous");
        renderCards(allBooks);
    }

    @FXML private void handleNouveauLivre() { ouvrirFormulaire(null); }

    private void ouvrirDetail(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BookShow.fxml"));
            Parent root = loader.load();
            BookShowController ctrl = loader.getController();
            ctrl.setBook(book, getLibraryName(book.getLibraryId()));
            Stage stage = (Stage) cardsPane.getScene().getWindow();
            stage.setScene(new Scene(root, 1050, 700));
            stage.setTitle("Détails : " + book.getTitle());
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void ouvrirFormulaire(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/BookForm.fxml"));
            Parent root = loader.load();
            BookFormController ctrl = loader.getController();
            ctrl.setBookToEdit(book);
            ctrl.setOnSaveCallback(this::loadBooks);
            Stage stage = (Stage) cardsPane.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 750));
            stage.setTitle(book == null ? "Nouvelle Ressource" : "Modifier : " + book.getTitle());
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleSupprimer(Book book) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer \u00ab " + book.getTitle() + " \u00bb ?");
        confirm.setContentText("Cette action est irréversible.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                bookService.supprimer(book.getId());
                showFlash("\u2705  \u00ab " + book.getTitle() + " \u00bb supprimé avec succès !", true);
                loadBooks();
            }
        });
    }

    private String getLibraryName(int libraryId) {
        return libraryService.afficher().stream()
                .filter(l -> l.getId() == libraryId)
                .map(Library::getName)
                .findFirst().orElse("Bibliothèque #" + libraryId);
    }

    public void showFlash(String msg, boolean success) {
        flashLabel.setText(msg);
        flashBox.setStyle(success
                ? "-fx-background-color: #f0fdf4; -fx-padding: 10 32; -fx-border-color: #22c55e; -fx-border-width: 0 0 0 4;"
                : "-fx-background-color: #fef2f2; -fx-padding: 10 32; -fx-border-color: #ef4444; -fx-border-width: 0 0 0 4;");
        flashLabel.setStyle(success ? "-fx-text-fill: #15803d; -fx-font-weight: bold; -fx-font-size: 13px;"
                : "-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 13px;");
        flashBox.setVisible(true); flashBox.setManaged(true);
        new Thread(() -> {
            try { Thread.sleep(4000); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> { flashBox.setVisible(false); flashBox.setManaged(false); });
        }).start();
    }

    private String getTypeGradient(String type) {
        String r = "-fx-background-radius: 14 14 0 0; ";
        return switch (type == null ? "" : type) {
            case "Livre" -> r + "-fx-background-color: linear-gradient(to right,#dbeafe,#a5b4fc);";
            case "Magazine" -> r + "-fx-background-color: linear-gradient(to right,#d1fae5,#6ee7b7);";
            case "Journal" -> r + "-fx-background-color: linear-gradient(to right,#fef3c7,#fcd34d);";
            case "Article" -> r + "-fx-background-color: linear-gradient(to right,#fce7f3,#f9a8d4);";
            default -> r + "-fx-background-color: linear-gradient(to right,#e0e7ff,#a5b4fc);";
        };
    }

    private String getTypeBadgeStyle(String type) {
        String base = "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-radius: 20; -fx-padding: 4 12;";
        return switch (type == null ? "" : type) {
            case "Livre"    -> base + "-fx-background-color: linear-gradient(to right,#3b82f6,#6366f1);";
            case "Magazine" -> base + "-fx-background-color: linear-gradient(to right,#10b981,#059669);";
            case "Journal"  -> base + "-fx-background-color: linear-gradient(to right,#f59e0b,#d97706);";
            case "Article"  -> base + "-fx-background-color: linear-gradient(to right,#ec4899,#be185d);";
            default -> base + "-fx-background-color: linear-gradient(to right,#6366f1,#4f46e5);";
        };
    }

    private String getTypeEmoji(String type) {
        return switch (type == null ? "" : type) {
            case "Livre"    -> "📖";
            case "Magazine" -> "📰";
            case "Journal"  -> "📓";
            case "Article"  -> "📄";
            default -> "📚";
        };
    }
}
