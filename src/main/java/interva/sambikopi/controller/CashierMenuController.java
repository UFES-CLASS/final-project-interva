package interva.sambikopi.controller;

import interva.sambikopi.model.CafeMenuItem;
import interva.sambikopi.model.OrderItem;
import interva.sambikopi.model.SambiKopiDataStore;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CashierMenuController {

    @FXML private Button allFilterButton;
    @FXML private Button coffeeFilterButton;
    @FXML private Button nonCoffeeFilterButton;
    @FXML private Button pastryFilterButton;

    @FXML private FlowPane menuCardsPane;

    @FXML private Button cashButton;
    @FXML private Button cardButton;
    @FXML private Button qrisButton;

    @FXML private TextField customerNameField;
    @FXML private Label orderIdLabel;
    @FXML private Label orderDateLabel;
    @FXML private Label orderTimeLabel;
    @FXML private Label cashierNameLabel;
    @FXML private Label paymentMethodLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private Label cashierStatusLabel;
    @FXML private VBox receiptItemsBox;

    @FXML private VBox confirmationOverlay;
    @FXML private Label confirmationCustomerLabel;
    @FXML private Label confirmationOrderIdLabel;
    @FXML private Label confirmationDateLabel;
    @FXML private Label confirmationCashierLabel;
    @FXML private Label confirmationTotalLabel;

    private final Map<String, OrderLine> currentOrder = new LinkedHashMap<>();
    private final Set<CafeMenuItem> observedMenuItems = Collections.newSetFromMap(new IdentityHashMap<>());
    private String paymentMethod = "Card";
    private String selectedCategory = "All";
    private int displayedOrderNumber = 231;

    @FXML
    private void initialize() {
        resetOrderInfo();
        setPaymentMethod("Card");
        observeMenuItems();
        loadApprovedMenuCards();
        updateReceipt();
        setStatus("Cashier Menu ready. Only Owner-approved menu items can be ordered.", false);
    }

    @FXML private void handleAllFilter() { selectFilter(allFilterButton, "All", "Showing all approved menu items."); }
    @FXML private void handleCoffeeFilter() { selectFilter(coffeeFilterButton, "Coffee", "Filter selected: Coffee."); }
    @FXML private void handleNonCoffeeFilter() { selectFilter(nonCoffeeFilterButton, "Non-Coffee", "Filter selected: Non-Coffee."); }
    @FXML private void handlePastryFilter() { selectFilter(pastryFilterButton, "Pastry", "Filter selected: Pastry."); }

    @FXML private void handlePaymentCash() { setPaymentMethod("Cash"); }
    @FXML private void handlePaymentCard() { setPaymentMethod("Card"); }
    @FXML private void handlePaymentQris() { setPaymentMethod("QRIS"); }

    @FXML
    private void handleReceivePayment() {
        String customerName = customerNameField.getText().trim();

        if (customerName.isEmpty()) {
            setStatus("Customer name is required before receiving payment.", true);
            customerNameField.requestFocus();
            return;
        }

        if (currentOrder.isEmpty()) {
            setStatus("Add at least one approved menu item before receiving payment.", true);
            return;
        }

        String summary = currentOrder.values().stream()
                .map(line -> line.quantity + "x " + line.name)
                .collect(Collectors.joining(", "));

        int total = calculateTotal();
        OrderItem savedOrder = SambiKopiDataStore.createCashierOrder(customerName, summary, paymentMethod, total);

        confirmationCustomerLabel.setText(savedOrder.getCustomer());
        confirmationOrderIdLabel.setText(savedOrder.getOrderId());
        confirmationDateLabel.setText(orderDateLabel.getText() + ", " + orderTimeLabel.getText());
        confirmationCashierLabel.setText(cashierNameLabel.getText());
        confirmationTotalLabel.setText(SambiKopiDataStore.formatIdr(total));

        confirmationOverlay.setVisible(true);
        confirmationOverlay.setManaged(true);
        setStatus("Payment received. Order sent to Barista Orders page for " + customerName + ".", false);
    }

    @FXML
    private void handlePrintReceipt() {
        showInfo("Print Receipt", "Receipt printing is simulated for this prototype.");
    }

    @FXML
    private void handleNewOrder() {
        confirmationOverlay.setVisible(false);
        confirmationOverlay.setManaged(false);
        currentOrder.clear();
        customerNameField.clear();
        displayedOrderNumber++;
        resetOrderInfo();
        updateReceipt();
        setStatus("New cashier order started.", false);
    }

    private void observeMenuItems() {
        SambiKopiDataStore.getMenuItems().forEach(this::observeMenuItem);
        SambiKopiDataStore.getMenuItems().addListener((ListChangeListener<CafeMenuItem>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(this::observeMenuItem);
                }
            }
            loadApprovedMenuCards();
        });
    }

    private void observeMenuItem(CafeMenuItem item) {
        if (item == null || observedMenuItems.contains(item)) {
            return;
        }
        observedMenuItems.add(item);
        ChangeListener<String> reloadWhenStatusChanges = (observable, oldValue, newValue) -> loadApprovedMenuCards();
        item.statusProperty().addListener(reloadWhenStatusChanges);
        item.categoryProperty().addListener((observable, oldValue, newValue) -> loadApprovedMenuCards());
        item.menuNameProperty().addListener((observable, oldValue, newValue) -> loadApprovedMenuCards());
        item.priceProperty().addListener((observable, oldValue, newValue) -> loadApprovedMenuCards());
    }

    private void loadApprovedMenuCards() {
        menuCardsPane.getChildren().clear();

        for (CafeMenuItem item : SambiKopiDataStore.getMenuItems()) {
            if (!"Approved".equalsIgnoreCase(item.getStatus())) {
                continue;
            }
            if (!selectedCategory.equals("All") && !selectedCategory.equalsIgnoreCase(item.getCategory())) {
                continue;
            }
            menuCardsPane.getChildren().add(createMenuCard(item));
        }

        if (menuCardsPane.getChildren().isEmpty()) {
            Label emptyLabel = new Label("No approved menu items available for this category.");
            emptyLabel.setWrapText(true);
            emptyLabel.setPrefWidth(520);
            emptyLabel.setStyle("-fx-background-color:white; -fx-background-radius:14; -fx-padding:22; -fx-text-fill:#8A8178; -fx-font-size:14px; -fx-font-weight:bold;");
            menuCardsPane.getChildren().add(emptyLabel);
        }
    }

    private VBox createMenuCard(CafeMenuItem item) {
        VBox card = new VBox(8);
        card.setPrefWidth(165);
        card.setPrefHeight(210);
        card.setStyle("-fx-background-color:white; -fx-background-radius:14; -fx-effect:dropshadow(three-pass-box, rgba(45,27,20,0.10), 10, 0, 0, 3);");

        VBox cardTop = new VBox();
        cardTop.setPrefHeight(85);
        cardTop.setPadding(new Insets(10));
        cardTop.setStyle("-fx-background-color:#EFE4D1; -fx-background-radius:14 14 0 0;");

        Label categoryLabel = new Label(item.getCategory());
        categoryLabel.setStyle("-fx-background-color:#5B514B; -fx-text-fill:white; -fx-background-radius:10; -fx-padding:3 10 3 10; -fx-font-size:11px; -fx-font-weight:bold;");
        cardTop.getChildren().add(categoryLabel);

        VBox cardBody = new VBox(4);
        cardBody.setPadding(new Insets(10));

        Label nameLabel = new Label(item.getMenuName());
        nameLabel.setWrapText(true);
        nameLabel.setStyle("-fx-font-size:15px; -fx-font-weight:bold; -fx-text-fill:#2D1B14;");

        Label priceLabel = new Label(item.getPrice());
        priceLabel.setStyle("-fx-text-fill:#8A8178;");

        Button addButton = new Button("+ Add");
        addButton.setPrefWidth(90);
        addButton.setStyle("-fx-background-color:white; -fx-border-color:#D8B89A; -fx-text-fill:#C96B43; -fx-font-weight:bold; -fx-background-radius:16; -fx-border-radius:16;");
        addButton.setOnAction(event -> addItem(item.getMenuName(), SambiKopiDataStore.parseIdr(item.getPrice())));

        cardBody.getChildren().addAll(nameLabel, priceLabel, addButton);
        card.getChildren().addAll(cardTop, cardBody);
        return card;
    }

    private void addItem(String name, int price) {
        if (price <= 0) {
            setStatus("This menu item has an invalid price.", true);
            return;
        }
        OrderLine line = currentOrder.get(name);
        if (line == null) {
            currentOrder.put(name, new OrderLine(name, price, 1));
        } else {
            line.quantity++;
        }
        updateReceipt();
        setStatus("Added: " + name, false);
    }

    private void updateReceipt() {
        receiptItemsBox.getChildren().clear();

        if (currentOrder.isEmpty()) {
            Label emptyLabel = new Label("No items added yet.");
            emptyLabel.setStyle("-fx-text-fill:#8A8178; -fx-font-size:13px;");
            receiptItemsBox.getChildren().add(emptyLabel);
        } else {
            for (OrderLine line : currentOrder.values()) {
                Label itemLabel = new Label(line.quantity + "  " + line.name + "     " + SambiKopiDataStore.formatIdr(line.getTotal()));
                itemLabel.setStyle("-fx-text-fill:#2D1B14; -fx-font-weight:bold; -fx-font-size:14px;");
                receiptItemsBox.getChildren().add(itemLabel);
            }
        }

        int subtotal = calculateSubtotal();
        int tax = calculateTax();
        int total = calculateTotal();

        subtotalLabel.setText(SambiKopiDataStore.formatIdr(subtotal));
        taxLabel.setText(SambiKopiDataStore.formatIdr(tax));
        totalLabel.setText(SambiKopiDataStore.formatIdr(total));
    }

    private int calculateSubtotal() {
        return currentOrder.values().stream().mapToInt(OrderLine::getTotal).sum();
    }

    private int calculateTax() {
        return (int) Math.round(calculateSubtotal() * 0.10);
    }

    private int calculateTotal() {
        return calculateSubtotal() + calculateTax();
    }

    private void setPaymentMethod(String method) {
        paymentMethod = method;
        paymentMethodLabel.setText(method);

        String inactive = "-fx-background-color:white; -fx-border-color:#E5D8C8; -fx-text-fill:#6E655C; -fx-font-weight:bold; -fx-background-radius:8; -fx-border-radius:8;";
        String active = "-fx-background-color:white; -fx-border-color:#C96B43; -fx-border-width:2; -fx-text-fill:#2D1B14; -fx-font-weight:bold; -fx-background-radius:8; -fx-border-radius:8;";

        cashButton.setStyle(method.equals("Cash") ? active : inactive);
        cardButton.setStyle(method.equals("Card") ? active : inactive);
        qrisButton.setStyle(method.equals("QRIS") ? active : inactive);
    }

    private void selectFilter(Button activeButton, String category, String message) {
        selectedCategory = category;
        String inactive = "-fx-background-color:white; -fx-text-fill:#6E655C; -fx-font-weight:bold; -fx-background-radius:18;";
        String active = "-fx-background-color:#C96B43; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:18;";
        allFilterButton.setStyle(inactive);
        coffeeFilterButton.setStyle(inactive);
        nonCoffeeFilterButton.setStyle(inactive);
        pastryFilterButton.setStyle(inactive);
        activeButton.setStyle(active);
        loadApprovedMenuCards();
        setStatus(message, false);
    }

    private void resetOrderInfo() {
        orderIdLabel.setText(String.format("#%04d", displayedOrderNumber));
        orderDateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        orderTimeLabel.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        cashierNameLabel.setText("Nabila");
    }

    private void setStatus(String message, boolean error) {
        cashierStatusLabel.setText(message);
        cashierStatusLabel.setStyle(error
                ? "-fx-text-fill:#B04A34; -fx-font-weight:bold;"
                : "-fx-text-fill:#4F6448; -fx-font-weight:bold;");
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private static class OrderLine {
        private final String name;
        private final int price;
        private int quantity;

        private OrderLine(String name, int price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }

        private int getTotal() {
            return price * quantity;
        }
    }
}
