package interva.sambikopi.controller;

import interva.sambikopi.App;
import interva.sambikopi.model.CafeMenuItem;
import interva.sambikopi.model.SambiKopiDataStore;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;

public class MenuListController {

    @FXML private Button dashboardButton;
    @FXML private Button menuButton;
    @FXML private Button menuListButton;
    @FXML private Button inventoryButton;
    @FXML private Button ordersButton;

    @FXML private Label menuListStatusLabel;
    @FXML private TableView<CafeMenuItem> menuTable;
    @FXML private TableColumn<CafeMenuItem, String> menuNameColumn;
    @FXML private TableColumn<CafeMenuItem, String> categoryColumn;
    @FXML private TableColumn<CafeMenuItem, String> priceColumn;
    @FXML private TableColumn<CafeMenuItem, String> ingredientsColumn;
    @FXML private TableColumn<CafeMenuItem, String> statusColumn;

    @FXML
    private void initialize() {
        menuNameColumn.setCellValueFactory(new PropertyValueFactory<>("menuName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        ingredientsColumn.setCellValueFactory(new PropertyValueFactory<>("ingredients"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        menuTable.setItems(SambiKopiDataStore.getMenuItems());
        menuListStatusLabel.setText("Saved menu data: " + SambiKopiDataStore.getMenuItems().size() + " items.");
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

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
