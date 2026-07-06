package interva.sambikopi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("orders"), 1200, 700);
        stage.setTitle("Sambi Kopi POS");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxmlName) throws IOException {
        scene.setRoot(loadFXML(cleanFxmlName(fxmlName)));
    }

    private static Parent loadFXML(String fxmlName) throws IOException {
        String cleanName = cleanFxmlName(fxmlName);
        String resourcePath = "/interva/sambikopi/view/" + cleanName + ".fxml";
        URL fxmlUrl = App.class.getResource(resourcePath);

        if (fxmlUrl == null) {
            throw new IOException("FXML file not found: " + resourcePath);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        return loader.load();
    }

    private static String cleanFxmlName(String fxmlName) {
        if (fxmlName == null || fxmlName.isBlank()) {
            return "menu";
        }
        return fxmlName.endsWith(".fxml")
                ? fxmlName.substring(0, fxmlName.length() - 5)
                : fxmlName;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
