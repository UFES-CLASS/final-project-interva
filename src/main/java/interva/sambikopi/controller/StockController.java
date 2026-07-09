package interva.sambikopi.controller;

import interva.sambikopi.model.InventoryItem;
import interva.sambikopi.model.SambiKopiDataStore;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StockController {

    private static final DateTimeFormatter EXP_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TextField stockSearchField;
    @FXML private Label stockStatusLabel;
    @FXML private TableView<InventoryItem> stockTable;
    @FXML private TableColumn<InventoryItem, String> colProduct;
    @FXML private TableColumn<InventoryItem, Number> colStock;
    @FXML private TableColumn<InventoryItem, String> colExpDate;
    @FXML private TableColumn<InventoryItem, String> colStatus;
    @FXML private TableColumn<InventoryItem, String> colNotifyStatus;

    private FilteredList<InventoryItem> filteredStockData;

    @FXML
    private void initialize() {
        colProduct.setCellValueFactory(new PropertyValueFactory<>("product"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colExpDate.setCellValueFactory(new PropertyValueFactory<>("expDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colNotifyStatus.setCellValueFactory(new PropertyValueFactory<>("notifyStatus"));

        filteredStockData = new FilteredList<>(SambiKopiDataStore.getInventoryItems(), item -> true);
        stockTable.setItems(filteredStockData);
        setStatus("Check stock levels and notify Owner if needed.", false);
    }

    @FXML
    private void handleSearchStock() {
        String keyword = stockSearchField.getText().trim().toLowerCase();
        filteredStockData.setPredicate(item -> keyword.isEmpty()
                || item.getProduct().toLowerCase().contains(keyword)
                || item.getExpDate().toLowerCase().contains(keyword)
                || item.getStatus().toLowerCase().contains(keyword)
                || item.getNotifyStatus().toLowerCase().contains(keyword));
        setStatus(keyword.isEmpty() ? "Showing all stock items." : "Search result for: " + keyword, false);
    }

    @FXML
    private void handleRefreshStock() {
        stockSearchField.clear();
        filteredStockData.setPredicate(item -> true);
        stockTable.getSelectionModel().clearSelection();
        stockTable.refresh();
        setStatus("Stock data refreshed.", false);
    }

    @FXML
    private void handleCheckStock() {
        InventoryItem selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a stock item first.", true);
            return;
        }

        if (selected.getStock() <= 3 || selected.getStatus().equalsIgnoreCase("Critical")) {
            setStatus(selected.getProduct() + " is critical. Notify owner immediately.", true);
        } else if (selected.getStock() <= 8 || selected.getStatus().equalsIgnoreCase("Low Stock")) {
            setStatus(selected.getProduct() + " is low stock. Owner notification is recommended.", false);
        } else {
            setStatus(selected.getProduct() + " stock is still available.", false);
        }
    }

    @FXML
    private void handleAddStock() {
        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Add Stock");
        dialog.setHeaderText("Add or update stock item");

        ButtonType saveButtonType = new ButtonType("Save Stock", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField productField = new TextField();
        productField.setPromptText("Product name");

        TextField stockField = new TextField();
        stockField.setPromptText("Stock amount, e.g. 12");

        DatePicker expDatePicker = new DatePicker();
        expDatePicker.setPromptText("Select expiration date");
        expDatePicker.setPrefWidth(260);

        VBox form = new VBox(8,
                new Label("Product:"), productField,
                new Label("Stock:"), stockField,
                new Label("Exp Date:"), expDatePicker);
        form.setPadding(new Insets(12));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                String product = SambiKopiDataStore.formatTitleCase(productField.getText());
                LocalDate expDateValue = expDatePicker.getValue();
                int stock;

                try {
                    stock = Integer.parseInt(stockField.getText().trim());
                } catch (NumberFormatException e) {
                    setStatus("Stock must be a number.", true);
                    return null;
                }

                if (product.isEmpty() || expDateValue == null) {
                    setStatus("Product and exp date cannot be empty.", true);
                    return null;
                }

                String expDate = expDateValue.format(EXP_DATE_FORMATTER);
                String status = getStatusFromStock(stock);
                return new InventoryItem(product, stock, expDate, status, "Not notified");
            }
            return null;
        });

        dialog.showAndWait().ifPresent(item -> {
            SambiKopiDataStore.addInventoryItem(item);
            filteredStockData.setPredicate(filteredStockData.getPredicate());
            stockTable.getSelectionModel().select(item);
            stockTable.refresh();
            setStatus("Stock item saved: " + item.getProduct(), false);
        });
    }

    @FXML
    private void handleEditStock() {
        InventoryItem selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a stock item first before editing.", true);
            return;
        }

        Dialog<InventoryItem> dialog = new Dialog<>();
        dialog.setTitle("Edit Stock");
        dialog.setHeaderText("Update stock item: " + selected.getProduct());

        ButtonType saveButtonType = new ButtonType("Save Changes", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        TextField productField = new TextField(selected.getProduct());
        productField.setEditable(false);

        TextField stockField = new TextField(String.valueOf(selected.getStock()));

        DatePicker expDatePicker = new DatePicker();
        try {
            expDatePicker.setValue(LocalDate.parse(selected.getExpDate(), EXP_DATE_FORMATTER));
        } catch (Exception e) {
            expDatePicker.setValue(LocalDate.now());
        }
        expDatePicker.setPrefWidth(260);

        VBox form = new VBox(8,
                new Label("Product:"), productField,
                new Label("Stock:"), stockField,
                new Label("Exp Date:"), expDatePicker);
        form.setPadding(new Insets(12));
        dialog.getDialogPane().setContent(form);

        dialog.setResultConverter(button -> {
            if (button == saveButtonType) {
                int stock;
                try {
                    stock = Integer.parseInt(stockField.getText().trim());
                } catch (NumberFormatException e) {
                    setStatus("Stock must be a number.", true);
                    return null;
                }

                if (stock < 0 || expDatePicker.getValue() == null) {
                    setStatus("Stock cannot be negative and exp date cannot be empty.", true);
                    return null;
                }

                selected.setStock(stock);
                selected.setExpDate(expDatePicker.getValue().format(EXP_DATE_FORMATTER));
                selected.setStatus(getStatusFromStock(stock));
                return selected;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(item -> {
            SambiKopiDataStore.updateInventoryItem(item);
            stockTable.refresh();
            setStatus("Updated stock item: " + item.getProduct(), false);
        });
    }

    private String getStatusFromStock(int stock) {
        if (stock <= 3) {
            return "Critical";
        }
        if (stock <= 8) {
            return "Low Stock";
        }
        return "Available";
    }

    @FXML
    private void handleNotifyOwner() {
        InventoryItem selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a stock item before notifying owner.", true);
            return;
        }
        SambiKopiDataStore.notifyOwnerStock(selected);
        stockTable.refresh();
        setStatus("Owner notified about stock item: " + selected.getProduct(), false);
    }

    private void setStatus(String message, boolean error) {
        stockStatusLabel.setText(message);
        stockStatusLabel.setStyle(error
                ? "-fx-text-fill:#B04A34; -fx-font-weight:bold;"
                : "-fx-text-fill:#4F6448; -fx-font-weight:bold;");
    }
}
