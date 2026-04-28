import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.net.URL;

public class TestLoad {
    public static void main(String[] args) {
        Platform.startup(() -> {
            try {
                URL fxml = new java.io.File("src/main/resources/views/child/ChildResourceView.fxml").toURI().toURL();
                System.out.println("Loading " + fxml);
                FXMLLoader loader = new FXMLLoader(fxml);
                Parent root = loader.load();
                System.out.println("SUCCESS!");
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
