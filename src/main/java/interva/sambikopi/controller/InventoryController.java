package interva.sambikopi.controller;

import interva.sambikopi.App;
import interva.sambikopi.model.InventoryItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class InventoryController {

    @FXML private Button dashboardButton;
    @FXML private Button inventoryButton;
    @FXML private Button menuButton;
    @FXML private Button menuListButton;
    @FXML private Button ordersButton;

    @FXML private TextField inventorySearchField;
    @FXML private Button inventorySearchButton;
    @FXML private Button checkStockButton;
    @FXML private Button addStockButton;
    @FXML private Button notifyOwnerButton;
    @FXML private Label inventoryStatusLabel;

    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, String> productColumn;
    @FXML private TableColumn<InventoryItem, Number> stockColumn;
    @FXML private TableColumn<InventoryItem, String> expDateColumn;
    @FXML private TableColumn<InventoryItem, String> statusColumn;

    private final ObservableList<InventoryItem> masterInventoryData = FXCollections.observableArrayList();
    private FilteredList<InventoryItem> filteredInventoryData;

    @FXML
    private void initialize() {
        productColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        expDateColumn.setCellValueFactory(new PropertyValueFactory<>("expDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        masterInventoryData.addAll(
                new InventoryItem("Fresh Milk", 12, "12/08/2026", "Available"),
                new InventoryItem("Coffee Beans", 5, "01/09/2026", "Low Stock"),
                new InventoryItem("Matcha Powder", 18, "20/10/2026", "Available"),
                new InventoryItem("Chocolate Syrup", 2, "15/07/2026", "Critical"),
                new InventoryItem("Palm Sugar", 7, "10/11/2026", "Low Stock")
        );

        filteredInventoryData = new FilteredList<>(masterInventoryData, item -> true);
        inventoryTable.setItems(filteredInventoryData);
        setStatus("Inventory ready.", false);
    }

    @FXML
    private void handleInventorySearch() {
        String keyword = inventorySearchField.getText().trim().toLowerCase();

        filteredInventoryData.setPredicate(item -> {
            if (keyword.isEmpty()) {
                return true;
            }

            return item.getProductName().toLowerCase().contains(keyword)
                    || item.getExpDate().toLowerCase().contains(keyword)
                    || item.getStatus().toLowerCase().contains(keyword)
                    || String.valueOf(item.getStock()).contains(keyword);
        });

        setStatus(keyword.isEmpty() ? "Showing all inventory items." : "Search result for: " + keyword, false);
    }

    @FXML
    private void handleCheckStock() {
        InventoryItem selected = inventoryTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            long lowOrCritical = masterInventoryData.stream()
                    .filter(item -> item.getStock() <= 5)
                    .count();
            setStatus("Stock checked. Low or critical items: " + lowOrCritical, lowOrCritical > 0);
            return;
        }

        setStatus(selected.getProductName() + " has " + selected.getStock() + " stock. Status: " + selected.getStatus(),
                selected.getStock() <= 5);
    }

    @FXML
    private void handleAddStock() {
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Add Stock");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField productField = new TextField();
        productField.setPromptText("Product name");

        TextField stockField = new TextField();
        stockField.setPromptText("Stock amount");

        TextField expDateField = new TextField();
        expDateField.setPromptText("Exp date, for example 12/08/2026");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.setItems(FXCollections.observableArrayList("Available", "Low Stock", "Critical"));
        statusBox.setPromptText("Status");

        VBox form = new VBox(8,
                new Label("Product:"), productField,
                new Label("Stock:"), stockField,
                new Label("Exp Date:"), expDateField,
                new Label("Status:"), statusBox
        );
        form.setPadding(new Insets(12));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                String product = productField.getText().trim();
                String stockText = stockField.getText().trim();
                String expDate = expDateField.getText().trim();
                String status = statusBox.getValue();

                if (product.isEmpty() || stockText.isEmpty() || expDate.isEmpty() || status == null) {
                    return null;
                }

                try {
                    int stock = Integer.parseInt(stockText);
                    return new InventoryItem(product, stock, expDate, status);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresentOrElse(item -> {
            masterInventoryData.add(item);
            setStatus("Stock added: " + item.getProductName(), false);
        }, () -> setStatus("Add stock cancelled or invalid input.", true));
    }

    @FXML
    private void handleNotifyOwner() {
        StringBuilder message = new StringBuilder();

        masterInventoryData.stream()
                .filter(item -> item.getStock() <= 5 || item.getStatus().equalsIgnoreCase("Critical"))
                .forEach(item -> message.append("- ")
                        .append(item.getProductName())
                        .append(" | Stock: ")
                        .append(item.getStock())
                        .append(" | Status: ")
                        .append(item.getStatus())
                        .append("\n"));

        if (message.isEmpty()) {
            showInfo("Notify Owner", "No critical stock to report.");
            setStatus("No owner notification needed.", false);
            return;
        }

        showInfo("Notify Owner", "Owner notification preview:\n\n" + message);
        setStatus("Owner notification prepared for low or critical stock.", false);
    }

    @FXML
    private void handleOpenDashboard() {
        showInfo("Dashboard", "Dashboard page is not created yet.");
    }

    @FXML
    private void handleOpenInventory() throws IOException {
        App.setRoot("inventory.fxml");
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
    private void handleOpenOrders() throws IOException {
        App.setRoot("orders.fxml");
    }

    private void setStatus(String message, boolean error) {
        inventoryStatusLabel.setText(message);
        inventoryStatusLabel.setStyle(error
                ? "-fx-text-fill:#B04A34; -fx-font-weight:bold;"
                : "-fx-text-fill:#5E8B5A; -fx-font-weight:bold;");
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
