package interva.sambikopi.controller;

import interva.sambikopi.App;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController {

    private static final String OWNER_PASSWORD = "owner123";

    @FXML private StackPane baristaContentPane;
    @FXML private StackPane cashierContentPane;
    @FXML private StackPane ownerContentPane;

    @FXML private VBox ownerLoginPane;
    @FXML private VBox ownerSidebar;
    @FXML private PasswordField ownerPasswordField;

    @FXML private Button baristaMenuButton;
    @FXML private Button baristaMenuListButton;
    @FXML private Button baristaStockButton;
    @FXML private Button baristaOrdersButton;

    @FXML private Button cashierMenuButton;
    @FXML private Button cashierOrdersButton;
    @FXML private Button cashierReportButton;

    @FXML private Button ownerReviewStockButton;
    @FXML private Button ownerReviewMenuButton;
    @FXML private Button ownerReportButton;

    @FXML private Label baristaStatusLabel;
    @FXML private Label cashierStatusLabel;
    @FXML private Label ownerStatusLabel;
    @FXML private Label ownerLoginStatusLabel;

    private static final String ACTIVE_BUTTON = "-fx-background-color:#C96B43; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:10; -fx-font-size:14px;";
    private static final String INACTIVE_BUTTON = "-fx-background-color:transparent; -fx-text-fill:#D8C8BC; -fx-font-size:14px;";

    @FXML
    private void initialize() {
        showBaristaMenu();
        showCashierMenu();
        showOwnerLogin();
    }

    @FXML
    private void showBaristaMenu() {
        loadInto(baristaContentPane, "menu");
        setBaristaActive(baristaMenuButton);
        baristaStatusLabel.setText("Barista Menu");
    }

    @FXML
    private void showBaristaMenuList() {
        loadInto(baristaContentPane, "menu_list");
        setBaristaActive(baristaMenuListButton);
        baristaStatusLabel.setText("Barista Menu List");
    }

    @FXML
    private void showBaristaStock() {
        loadInto(baristaContentPane, "stock");
        setBaristaActive(baristaStockButton);
        baristaStatusLabel.setText("Barista Stock");
    }

    @FXML
    private void showBaristaOrders() {
        loadInto(baristaContentPane, "orders");
        setBaristaActive(baristaOrdersButton);
        baristaStatusLabel.setText("Barista Orders");
    }

    @FXML
    private void showCashierMenu() {
        loadInto(cashierContentPane, "cashier_menu");
        setCashierActive(cashierMenuButton);
        cashierStatusLabel.setText("Cashier Menu");
    }

    @FXML
    private void showCashierOrders() {
        loadInto(cashierContentPane, "cashier_orders");
        setCashierActive(cashierOrdersButton);
        cashierStatusLabel.setText("Cashier Orders");
    }

    @FXML
    private void showCashierReport() {
        loadInto(cashierContentPane, "cashier_report");
        setCashierActive(cashierReportButton);
        cashierStatusLabel.setText("Cashier Report");
    }

    @FXML
    private void handleOwnerLogin() {
        String password = ownerPasswordField.getText().trim();
        if (!OWNER_PASSWORD.equals(password)) {
            ownerLoginStatusLabel.setText("Wrong owner password. Please try again.");
            ownerLoginStatusLabel.setStyle("-fx-text-fill:#B04A34; -fx-font-weight:bold;");
            return;
        }

        ownerLoginPane.setVisible(false);
        ownerLoginPane.setManaged(false);
        ownerSidebar.setVisible(true);
        ownerSidebar.setManaged(true);
        ownerContentPane.setVisible(true);
        ownerContentPane.setManaged(true);
        ownerStatusLabel.setText("Owner logged in");
        showOwnerReviewStock();
    }

    @FXML
    private void handleOwnerLogout() {
        ownerPasswordField.clear();
        ownerContentPane.getChildren().clear();
        showOwnerLogin();
    }

    @FXML
    private void showOwnerReviewStock() {
        loadInto(ownerContentPane, "owner_review_stock");
        setOwnerActive(ownerReviewStockButton);
        ownerStatusLabel.setText("Owner Review Stock");
    }

    @FXML
    private void showOwnerReviewMenu() {
        loadInto(ownerContentPane, "owner_review_menu");
        setOwnerActive(ownerReviewMenuButton);
        ownerStatusLabel.setText("Owner Review New Menu");
    }

    @FXML
    private void showOwnerReport() {
        loadInto(ownerContentPane, "owner_report");
        setOwnerActive(ownerReportButton);
        ownerStatusLabel.setText("Owner Report");
    }

    private void showOwnerLogin() {
        ownerLoginPane.setVisible(true);
        ownerLoginPane.setManaged(true);
        ownerSidebar.setVisible(false);
        ownerSidebar.setManaged(false);
        ownerContentPane.setVisible(false);
        ownerContentPane.setManaged(false);
        ownerLoginStatusLabel.setText("Owner login is required to access review and report pages.");
        ownerLoginStatusLabel.setStyle("-fx-text-fill:#6E655C; -fx-font-weight:bold;");
    }

    private void loadInto(StackPane targetPane, String fxmlName) {
        try {
            Parent content = App.loadFXML(fxmlName);
            targetPane.getChildren().setAll(content);
        } catch (IOException e) {
            Label errorLabel = new Label("Could not load page: " + fxmlName + "\n" + e.getMessage());
            errorLabel.setStyle("-fx-text-fill:#B04A34; -fx-font-weight:bold;");
            targetPane.getChildren().setAll(errorLabel);
        }
    }

    private void setBaristaActive(Button activeButton) {
        baristaMenuButton.setStyle(INACTIVE_BUTTON);
        baristaMenuListButton.setStyle(INACTIVE_BUTTON);
        baristaStockButton.setStyle(INACTIVE_BUTTON);
        baristaOrdersButton.setStyle(INACTIVE_BUTTON);
        activeButton.setStyle(ACTIVE_BUTTON);
    }

    private void setCashierActive(Button activeButton) {
        cashierMenuButton.setStyle(INACTIVE_BUTTON);
        cashierOrdersButton.setStyle(INACTIVE_BUTTON);
        cashierReportButton.setStyle(INACTIVE_BUTTON);
        activeButton.setStyle(ACTIVE_BUTTON);
    }

    private void setOwnerActive(Button activeButton) {
        ownerReviewStockButton.setStyle(INACTIVE_BUTTON);
        ownerReviewMenuButton.setStyle(INACTIVE_BUTTON);
        ownerReportButton.setStyle(INACTIVE_BUTTON);
        activeButton.setStyle(ACTIVE_BUTTON);
    }
}
