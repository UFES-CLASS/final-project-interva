package interva.sambikopi.controller;

import interva.sambikopi.App;
import interva.sambikopi.model.CafeMenuItem;
import interva.sambikopi.model.SambiKopiDataStore;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MenuController {

    @FXML private Button dashboardButton;
    @FXML private Button menuButton;
    @FXML private Button menuListButton;
    @FXML private Button inventoryButton;
    @FXML private Button ordersButton;

    @FXML private HBox messageBox;
    @FXML private Label messageLabel;
    @FXML private TextField menuNameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField priceField;
    @FXML private TextField ingredientsField;
    @FXML private Button saveMenuButton;
    @FXML private Button clearButton;

    @FXML
    private void initialize() {
        categoryComboBox.setItems(FXCollections.observableArrayList(
                "Beverage - Coffee",
                "Beverage - Non Coffee",
                "Pastry",
                "Snack",
                "Dessert"
        ));

        setMessage("Input a new menu item here. Saved data will appear on the Menu List page.", false);
    }

    @FXML
    private void handleSaveMenu() {
        String menuName = menuNameField.getText().trim();
        String category = categoryComboBox.getValue();
        String price = priceField.getText().trim();
        String ingredients = ingredientsField.getText().trim();

        if (menuName.isEmpty()) {
            setMessage("Menu name is required.", true);
            return;
        }

        if (category == null || category.isEmpty()) {
            setMessage("Category is required.", true);
            return;
        }

        if (!isValidPrice(price)) {
            setMessage("Price must be a valid IDR amount, for example 18000 or Rp 18.000.", true);
            return;
        }

        if (ingredients.isEmpty()) {
            setMessage("Ingredients must be filled before the menu can be saved.", true);
            return;
        }

        String formattedPrice = formatToIdr(price);
        CafeMenuItem newItem = new CafeMenuItem(menuName, category, formattedPrice, ingredients, "Pending Approval");
        SambiKopiDataStore.getMenuItems().add(newItem);

        clearForm();
        setMessage("Menu saved: " + menuName + ". Open Menu List to see the table data.", false);
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        setMessage("Form cleared.", false);
    }

    @FXML
    private void handleOpenDashboard() {
        showInfo("Dashboard", "Dashboard page is still under development.");
    }

    @FXML
    private void handleOpenMenu() throws IOException {
        App.setRoot("menu.fxml");
    }

    @FXML
    private void handleOpenMenuList() throws IOException {
        App.setRoot("menu_list.fxml");
    }

    @FXML
    private void handleOpenInventory() throws IOException {
        App.setRoot("inventory.fxml");
    }

    @FXML
    private void handleOpenOrders() throws IOException {
        App.setRoot("orders.fxml");
    }

    private void clearForm() {
        menuNameField.clear();
        categoryComboBox.getSelectionModel().clearSelection();
        priceField.clear();
        ingredientsField.clear();
    }

    private boolean isValidPrice(String price) {
        try {
            return parseIdrAmount(price) >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private long parseIdrAmount(String price) {
        String clean = price == null ? "" : price.trim()
                .replace("Rp", "")
                .replace("rp", "")
                .replace("IDR", "")
                .replace("idr", "")
                .replace(".", "")
                .replace(",", "")
                .replace(" ", "");

        if (clean.isEmpty()) {
            throw new NumberFormatException("Price is empty");
        }

        return Long.parseLong(clean);
    }

    private String formatToIdr(String price) {
        long amount = parseIdrAmount(price);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');

        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        return "Rp " + formatter.format(amount);
    }

    private void setMessage(String message, boolean error) {
        messageLabel.setText(message);
        messageBox.setStyle(error
                ? "-fx-background-color:#FBE4DE; -fx-border-color:#E9C1B6; -fx-border-radius:10; -fx-background-radius:10;"
                : "-fx-background-color:#EEF7EA; -fx-border-color:#D7EBCF; -fx-border-radius:10; -fx-background-radius:10;");
        messageLabel.setStyle(error
                ? "-fx-text-fill:#B04A34; -fx-font-weight:bold; -fx-alignment:center;"
                : "-fx-text-fill:#4F8B45; -fx-font-weight:bold; -fx-alignment:center;");
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
