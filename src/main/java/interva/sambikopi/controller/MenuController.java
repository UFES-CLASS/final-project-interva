package interva.sambikopi.controller;

import interva.sambikopi.App;
import interva.sambikopi.model.CafeMenuItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class MenuController {

    @FXML private Button dashboardButton;
    @FXML private Button menuButton;
    @FXML private Button inventoryButton;
    @FXML private Button ordersButton;

    @FXML private HBox messageBox;
    @FXML private Label messageLabel;
    @FXML private TextField menuNameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField priceField;
    @FXML private TextField ingredientsField;
    @FXML private Button confirmAgainButton;
    @FXML private Button cancelButton;
    @FXML private Button deleteMenuButton;

    @FXML private TextField menuSearchField;
    @FXML private Button menuSearchButton;
    @FXML private Button refreshMenuButton;
    @FXML private TableView<CafeMenuItem> menuTable;
    @FXML private TableColumn<CafeMenuItem, String> menuNameColumn;
    @FXML private TableColumn<CafeMenuItem, String> categoryColumn;
    @FXML private TableColumn<CafeMenuItem, String> priceColumn;
    @FXML private TableColumn<CafeMenuItem, String> ingredientsColumn;
    @FXML private TableColumn<CafeMenuItem, String> statusColumn;

    private final ObservableList<CafeMenuItem> masterMenuData = FXCollections.observableArrayList();
    private FilteredList<CafeMenuItem> filteredMenuData;

    @FXML
    private void initialize() {
        categoryComboBox.setItems(FXCollections.observableArrayList(
                "Beverage - Coffee",
                "Beverage - Non Coffee",
                "Pastry",
                "Snack",
                "Dessert"
        ));

        menuNameColumn.setCellValueFactory(new PropertyValueFactory<>("menuName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        ingredientsColumn.setCellValueFactory(new PropertyValueFactory<>("ingredients"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        masterMenuData.addAll(
                new CafeMenuItem("Kopi Milk Gula Aren", "Beverage - Coffee", formatToIdr("18000"), "Milk, Coffee, Palm Sugar", "Pending Approval"),
                new CafeMenuItem("Americano", "Beverage - Coffee", formatToIdr("15000"), "Espresso, Water", "Approved"),
                new CafeMenuItem("Matcha Latte", "Beverage - Non Coffee", formatToIdr("22000"), "Matcha, Milk, Sugar", "Approved"),
                new CafeMenuItem("Almond Croissant", "Pastry", formatToIdr("25000"), "Flour, Butter, Almond", "Pending Approval")
        );

        filteredMenuData = new FilteredList<>(masterMenuData, item -> true);
        menuTable.setItems(filteredMenuData);

        menuTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selected) -> {
            if (selected != null) {
                fillForm(selected);
            }
        });

        setMessage("Fill the menu data, then confirm again.", false);
    }

    @FXML
    private void handleSearchMenu() {
        String keyword = menuSearchField.getText().trim().toLowerCase();

        filteredMenuData.setPredicate(item -> {
            if (keyword.isEmpty()) {
                return true;
            }

            return item.getMenuName().toLowerCase().contains(keyword)
                    || item.getCategory().toLowerCase().contains(keyword)
                    || item.getPrice().toLowerCase().contains(keyword)
                    || item.getStatus().toLowerCase().contains(keyword);
        });

        setMessage(keyword.isEmpty() ? "Showing all menu items." : "Search result for: " + keyword, false);
    }

    @FXML
    private void handleRefreshMenu() {
        menuSearchField.clear();
        filteredMenuData.setPredicate(item -> true);
        menuTable.getSelectionModel().clearSelection();
        clearForm();
        setMessage("Menu list refreshed.", false);
    }

    @FXML
    private void handleConfirmAgain() {
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

        price = formatToIdr(price);

        if (ingredients.isEmpty()) {
            setMessage("Incomplete data. Ingredients wajib diisi minimal 1 item sebelum menu bisa divalidasi.", true);
            return;
        }

        CafeMenuItem selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            masterMenuData.add(new CafeMenuItem(menuName, category, price, ingredients, "Pending Approval"));
            setMessage("New menu added: " + menuName, false);
        } else {
            selected.setMenuName(menuName);
            selected.setCategory(category);
            selected.setPrice(price);
            selected.setIngredients(ingredients);
            selected.setStatus("Pending Approval");
            menuTable.refresh();
            setMessage("Menu updated: " + menuName, false);
        }

        clearForm();
        menuTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleCancel() {
        clearForm();
        menuTable.getSelectionModel().clearSelection();
        setMessage("Input cancelled.", false);
    }

    @FXML
    private void handleDeleteMenu() {
        CafeMenuItem selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setMessage("Select a menu item from the table first.", true);
            return;
        }

        masterMenuData.remove(selected);
        clearForm();
        setMessage("Menu deleted: " + selected.getMenuName(), false);
    }

    @FXML
    private void handleOpenDashboard() {
        showInfo("Dashboard", "Dashboard page is not created yet.");
    }

    @FXML
    private void handleOpenMenu() throws IOException {
        App.setRoot("menu.fxml");
    }

    @FXML
    private void handleOpenInventory() throws IOException {
        App.setRoot("inventory.fxml");
    }

    @FXML
    private void handleOpenOrders() throws IOException {
        App.setRoot("orders.fxml");
    }

    private void fillForm(CafeMenuItem item) {
        menuNameField.setText(item.getMenuName());
        categoryComboBox.setValue(item.getCategory());
        priceField.setText(item.getPrice());
        ingredientsField.setText(item.getIngredients());
        setMessage("Editing selected menu: " + item.getMenuName(), false);
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
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }
}
