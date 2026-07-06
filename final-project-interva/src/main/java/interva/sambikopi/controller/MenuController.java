package interva.sambikopi.controller;

import interva.sambikopi.model.CafeMenuItem;
import interva.sambikopi.model.SambiKopiDataStore;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class MenuController {

    @FXML private TextField menuNameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField priceField;
    @FXML private TextField ingredientsField;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        categoryComboBox.getItems().setAll("Coffee", "Non-Coffee", "Pastry");
        categoryComboBox.getSelectionModel().select("Coffee");
        setStatus("Menu input page ready. Fill the form, then save it to Menu List.", false);
    }

    @FXML
    private void handleSaveMenu() {
        String name = menuNameField.getText().trim();
        String category = categoryComboBox.getValue();
        String priceRaw = priceField.getText().trim();
        String ingredients = ingredientsField.getText().trim();

        if (name.isEmpty() || category == null || priceRaw.isEmpty() || ingredients.isEmpty()) {
            setStatus("Please complete menu name, category, price, and ingredients before saving.", true);
            return;
        }

        int price = SambiKopiDataStore.parseIdr(priceRaw);
        if (price <= 0) {
            setStatus("Price must be a valid number, for example 18000 or Rp 18.000.", true);
            return;
        }

        CafeMenuItem item = new CafeMenuItem(name, category, SambiKopiDataStore.formatIdr(price), ingredients, "Pending Approval");
        SambiKopiDataStore.addMenuForReview(item);
        setStatus("Saved to Menu List and sent to Owner Review: " + name, false);
        clearFormOnly();
    }

    @FXML
    private void handleClearForm() {
        clearFormOnly();
        setStatus("Form cleared.", false);
    }

    private void clearFormOnly() {
        menuNameField.clear();
        priceField.clear();
        ingredientsField.clear();
        categoryComboBox.getSelectionModel().select("Coffee");
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setStyle(error
                ? "-fx-text-fill:#B04A34; -fx-font-weight:bold;"
                : "-fx-text-fill:#4F6448; -fx-font-weight:bold;");
    }
}
