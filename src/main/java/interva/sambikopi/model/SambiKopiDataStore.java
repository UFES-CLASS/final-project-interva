package interva.sambikopi.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class SambiKopiDataStore {

    private static final NumberFormat IDR_FORMAT = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private static final ObservableList<CafeMenuItem> MENU_ITEMS = FXCollections.observableArrayList(
            new CafeMenuItem("Kopi Milk Gula Aren", "Coffee", "Rp 18.000", "Milk, Coffee, Palm Sugar", "Pending Approval"),
            new CafeMenuItem("Americano", "Coffee", "Rp 15.000", "Espresso, Water", "Approved"),
            new CafeMenuItem("Matcha Latte", "Non-Coffee", "Rp 22.000", "Matcha, Milk, Sugar", "Approved"),
            new CafeMenuItem("Almond Croissant", "Pastry", "Rp 25.000", "Flour, Butter, Almond", "Pending Approval")
    );

    private static final ObservableList<CafeMenuItem> MENU_REVIEW_ITEMS = FXCollections.observableArrayList();

    private static final ObservableList<InventoryItem> INVENTORY_ITEMS = FXCollections.observableArrayList(
            new InventoryItem("Fresh Milk", 12, "12/08/2026", "Available", "Not notified"),
            new InventoryItem("Coffee Beans", 5, "01/09/2026", "Low Stock", "Not notified"),
            new InventoryItem("Matcha Powder", 18, "20/10/2026", "Available", "Not notified"),
            new InventoryItem("Chocolate Syrup", 2, "15/07/2026", "Critical", "Not notified"),
            new InventoryItem("Palm Sugar", 7, "10/09/2026", "Low Stock", "Not notified")
    );

    private static final ObservableList<InventoryItem> STOCK_REVIEW_ITEMS = FXCollections.observableArrayList();

    private static final ObservableList<OrderItem> ORDERS = FXCollections.observableArrayList(
            new OrderItem("#0228", "Thomas Tejo", "1x Espresso Con Panna", "16 May 2026", "Complete", "Card", 28000),
            new OrderItem("#0229", "Miks Yermolay", "1x Milkshake Vanilla", "16 May 2026", "Assigned", "Cash", 32000),
            new OrderItem("#0230", "Neon Sylvon", "1x Mineral Water", "16 May 2026", "Waiting", "QRIS", 10000)
    );

    private static int generatedOrderCounter = 230;
    private static int baristaNotificationCount = 0;
    private static int ownerStockNotificationCount = 0;
    private static int ownerMenuNotificationCount = 0;

    static {
        MENU_REVIEW_ITEMS.addAll(MENU_ITEMS.filtered(item -> item.getStatus().equalsIgnoreCase("Pending Approval")));
    }

    private SambiKopiDataStore() {}

    public static ObservableList<CafeMenuItem> getMenuItems() {
        return MENU_ITEMS;
    }

    public static ObservableList<CafeMenuItem> getMenuReviewItems() {
        return MENU_REVIEW_ITEMS;
    }

    public static ObservableList<InventoryItem> getInventoryItems() {
        return INVENTORY_ITEMS;
    }

    public static ObservableList<InventoryItem> getStockReviewItems() {
        return STOCK_REVIEW_ITEMS;
    }

    public static ObservableList<OrderItem> getOrders() {
        return ORDERS;
    }

    public static void addMenuForReview(CafeMenuItem item) {
        MENU_ITEMS.add(item);
        MENU_REVIEW_ITEMS.add(item);
        ownerMenuNotificationCount++;
    }

    public static void approveMenuItem(CafeMenuItem item) {
        if (item == null) {
            return;
        }
        item.setStatus("Approved");
        MENU_REVIEW_ITEMS.remove(item);
    }

    public static void rejectMenuItem(CafeMenuItem item) {
        if (item == null) {
            return;
        }
        MENU_REVIEW_ITEMS.remove(item);
        MENU_ITEMS.remove(item);
    }

    public static boolean notifyOwnerStock(InventoryItem item) {
        if (item == null) {
            return false;
        }
        if (!STOCK_REVIEW_ITEMS.contains(item)) {
            STOCK_REVIEW_ITEMS.add(item);
            ownerStockNotificationCount++;
        }
        item.setNotifyStatus("Notified");
        return true;
    }

    public static void markStockReviewed(InventoryItem item) {
        if (item == null) {
            return;
        }
        item.setNotifyStatus("Reviewed");
        STOCK_REVIEW_ITEMS.remove(item);
    }

    public static OrderItem createCashierOrder(String customer, String productSummary, String paymentMethod, int totalAmount) {
        generatedOrderCounter++;
        String orderId = String.format("#%04d", generatedOrderCounter);
        String creationDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        OrderItem order = new OrderItem(orderId, customer, productSummary, creationDate, "Waiting", paymentMethod, totalAmount);
        ORDERS.add(order);
        baristaNotificationCount++;
        return order;
    }

    public static int consumeBaristaNotificationCount() {
        int count = baristaNotificationCount;
        baristaNotificationCount = 0;
        return count;
    }

    public static int consumeOwnerStockNotificationCount() {
        int count = ownerStockNotificationCount;
        ownerStockNotificationCount = 0;
        return count;
    }

    public static int consumeOwnerMenuNotificationCount() {
        int count = ownerMenuNotificationCount;
        ownerMenuNotificationCount = 0;
        return count;
    }

    public static String formatIdr(int value) {
        // Force a readable IDR format: Rp 10.000 instead of Rp10.000.
        return "Rp " + String.format(new Locale("id", "ID"), "%,d", value).replace(',', '.');
    }

    public static int parseIdr(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return 0;
        }
        String digits = rawValue.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return 0;
        }
        return Integer.parseInt(digits);
    }
}
