package interva.sambikopi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(loadFXML("main"), 1280, 760);
        stage.setTitle("Sambi Kopi POS");
        stage.setScene(scene);
        stage.show();
    }

    public static Parent loadFXML(String fxmlName) throws IOException {
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
            return "main";
        }
        return fxmlName.endsWith(".fxml")
                ? fxmlName.substring(0, fxmlName.length() - 5)
                : fxmlName;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
