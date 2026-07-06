package interva.sambikopi.model;

import interva.sambikopi.database.DatabaseManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class SambiKopiDataStore {

    private static final ObservableList<CafeMenuItem> MENU_ITEMS = FXCollections.observableArrayList();
    private static final ObservableList<CafeMenuItem> MENU_REVIEW_ITEMS = FXCollections.observableArrayList();
    private static final ObservableList<InventoryItem> INVENTORY_ITEMS = FXCollections.observableArrayList();
    private static final ObservableList<InventoryItem> STOCK_REVIEW_ITEMS = FXCollections.observableArrayList();
    private static final ObservableList<OrderItem> ORDERS = FXCollections.observableArrayList();

    private static int generatedOrderCounter = 230;
    private static int baristaNotificationCount = 0;
    private static int ownerStockNotificationCount = 0;
    private static int ownerMenuNotificationCount = 0;

    static {
        DatabaseManager.initializeDatabase();
        seedDefaultDataIfNeeded();
        loadAllFromDatabase();
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

    public static void reloadAllFromDatabase() {
        loadAllFromDatabase();
    }

    public static void addMenuForReview(CafeMenuItem item) {
        if (item == null) {
            return;
        }
        item.setStatus("Pending Approval");
        upsertMenuItem(item);
        MENU_ITEMS.removeIf(existing -> existing.getMenuName().equalsIgnoreCase(item.getMenuName()));
        MENU_REVIEW_ITEMS.removeIf(existing -> existing.getMenuName().equalsIgnoreCase(item.getMenuName()));
        MENU_ITEMS.add(item);
        MENU_REVIEW_ITEMS.add(item);
        ownerMenuNotificationCount++;
    }

    public static void approveMenuItem(CafeMenuItem item) {
        if (item == null) {
            return;
        }
        item.setStatus("Approved");
        updateMenuStatus(item.getMenuName(), "Approved");
        MENU_REVIEW_ITEMS.remove(item);
    }

    public static void rejectMenuItem(CafeMenuItem item) {
        if (item == null) {
            return;
        }
        deleteMenuItem(item);
    }

    public static void deleteMenuItem(CafeMenuItem item) {
        if (item == null) {
            return;
        }
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM menu_items WHERE menu_name = ?")) {
            statement.setString(1, item.getMenuName());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete menu item", e);
        }
        MENU_REVIEW_ITEMS.remove(item);
        MENU_ITEMS.remove(item);
    }

    public static void deleteAllMenuItems() {
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM menu_items");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete all menu items", e);
        }
        MENU_REVIEW_ITEMS.clear();
        MENU_ITEMS.clear();
    }

    public static boolean notifyOwnerStock(InventoryItem item) {
        if (item == null) {
            return false;
        }
        item.setNotifyStatus("Notified");
        updateInventoryNotifyStatus(item.getProduct(), "Notified");
        if (!STOCK_REVIEW_ITEMS.contains(item)) {
            STOCK_REVIEW_ITEMS.add(item);
            ownerStockNotificationCount++;
        }
        return true;
    }

    public static void markStockReviewed(InventoryItem item) {
        if (item == null) {
            return;
        }
        item.setNotifyStatus("Reviewed");
        updateInventoryNotifyStatus(item.getProduct(), "Reviewed");
        STOCK_REVIEW_ITEMS.remove(item);
    }

    public static OrderItem createCashierOrder(String customer, String productSummary, String paymentMethod, int totalAmount) {
        generatedOrderCounter++;
        String orderId = String.format("#%04d", generatedOrderCounter);
        String creationDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        OrderItem order = new OrderItem(orderId, customer, productSummary, creationDate, "Waiting", paymentMethod, totalAmount);
        insertOrder(order);
        ORDERS.add(order);
        baristaNotificationCount++;
        return order;
    }

    public static void updateOrderStatus(OrderItem order, String status) {
        if (order == null || status == null) {
            return;
        }
        order.setStatus(status);
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE orders SET status = ? WHERE order_id = ?")) {
            statement.setString(1, status);
            statement.setString(2, order.getOrderId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order status", e);
        }
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

    private static void loadAllFromDatabase() {
        MENU_ITEMS.clear();
        MENU_REVIEW_ITEMS.clear();
        INVENTORY_ITEMS.clear();
        STOCK_REVIEW_ITEMS.clear();
        ORDERS.clear();

        try (Connection connection = DatabaseManager.getConnection()) {
            loadMenuItems(connection);
            loadInventoryItems(connection);
            loadOrders(connection);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load database data", e);
        }
    }

    private static void loadMenuItems(Connection connection) throws SQLException {
        String sql = "SELECT menu_name, category, price, ingredients, status FROM menu_items ORDER BY menu_name";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                CafeMenuItem item = new CafeMenuItem(
                        resultSet.getString("menu_name"),
                        resultSet.getString("category"),
                        resultSet.getString("price"),
                        resultSet.getString("ingredients"),
                        resultSet.getString("status")
                );
                MENU_ITEMS.add(item);
                if ("Pending Approval".equalsIgnoreCase(item.getStatus())) {
                    MENU_REVIEW_ITEMS.add(item);
                }
            }
        }
    }

    private static void loadInventoryItems(Connection connection) throws SQLException {
        String sql = "SELECT product, stock, exp_date, status, notify_status FROM inventory_items ORDER BY product";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                InventoryItem item = new InventoryItem(
                        resultSet.getString("product"),
                        resultSet.getInt("stock"),
                        resultSet.getString("exp_date"),
                        resultSet.getString("status"),
                        resultSet.getString("notify_status")
                );
                INVENTORY_ITEMS.add(item);
                if ("Notified".equalsIgnoreCase(item.getNotifyStatus())) {
                    STOCK_REVIEW_ITEMS.add(item);
                }
            }
        }
    }

    private static void loadOrders(Connection connection) throws SQLException {
        String sql = "SELECT order_id, customer, product, creation_date, status, payment_method, total_amount FROM orders ORDER BY order_id";
        int maxOrderNumber = 230;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                OrderItem order = new OrderItem(
                        resultSet.getString("order_id"),
                        resultSet.getString("customer"),
                        resultSet.getString("product"),
                        resultSet.getString("creation_date"),
                        resultSet.getString("status"),
                        resultSet.getString("payment_method"),
                        resultSet.getInt("total_amount")
                );
                ORDERS.add(order);
                maxOrderNumber = Math.max(maxOrderNumber, parseOrderNumber(order.getOrderId()));
            }
        }
        generatedOrderCounter = maxOrderNumber;
    }

    private static int parseOrderNumber(String orderId) {
        if (orderId == null) {
            return 0;
        }
        String digits = orderId.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return 0;
        }
        return Integer.parseInt(digits);
    }

    private static void upsertMenuItem(CafeMenuItem item) {
        String sql = """
                INSERT INTO menu_items(menu_name, category, price, ingredients, status)
                VALUES(?, ?, ?, ?, ?)
                ON CONFLICT(menu_name) DO UPDATE SET
                    category = excluded.category,
                    price = excluded.price,
                    ingredients = excluded.ingredients,
                    status = excluded.status
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getMenuName());
            statement.setString(2, item.getCategory());
            statement.setString(3, item.getPrice());
            statement.setString(4, item.getIngredients());
            statement.setString(5, item.getStatus());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save menu item", e);
        }
    }

    private static void updateMenuStatus(String menuName, String status) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE menu_items SET status = ? WHERE menu_name = ?")) {
            statement.setString(1, status);
            statement.setString(2, menuName);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update menu status", e);
        }
    }

    private static void updateInventoryNotifyStatus(String product, String notifyStatus) {
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE inventory_items SET notify_status = ? WHERE product = ?")) {
            statement.setString(1, notifyStatus);
            statement.setString(2, product);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update stock notification", e);
        }
    }

    private static void insertOrder(OrderItem order) {
        String sql = """
                INSERT INTO orders(order_id, customer, product, creation_date, status, payment_method, total_amount)
                VALUES(?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, order.getOrderId());
            statement.setString(2, order.getCustomer());
            statement.setString(3, order.getProduct());
            statement.setString(4, order.getCreationDate());
            statement.setString(5, order.getStatus());
            statement.setString(6, order.getPaymentMethod());
            statement.setInt(7, order.getTotalAmount());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save order", e);
        }
    }

    private static void seedDefaultDataIfNeeded() {
        try (Connection connection = DatabaseManager.getConnection()) {
            if (isDefaultDataAlreadySeeded(connection)) {
                return;
            }

            insertSeedMenu(connection, new CafeMenuItem("Kopi Milk Gula Aren", "Coffee", "Rp 18.000", "Milk, Coffee, Palm Sugar", "Pending Approval"));
            insertSeedMenu(connection, new CafeMenuItem("Americano", "Coffee", "Rp 15.000", "Espresso, Water", "Approved"));
            insertSeedMenu(connection, new CafeMenuItem("Matcha Latte", "Non-Coffee", "Rp 22.000", "Matcha, Milk, Sugar", "Approved"));
            insertSeedMenu(connection, new CafeMenuItem("Almond Croissant", "Pastry", "Rp 25.000", "Flour, Butter, Almond", "Pending Approval"));

            insertSeedInventory(connection, new InventoryItem("Fresh Milk", 12, "12/08/2026", "Available", "Not notified"));
            insertSeedInventory(connection, new InventoryItem("Coffee Beans", 5, "01/09/2026", "Low Stock", "Not notified"));
            insertSeedInventory(connection, new InventoryItem("Matcha Powder", 18, "20/10/2026", "Available", "Not notified"));
            insertSeedInventory(connection, new InventoryItem("Chocolate Syrup", 2, "15/07/2026", "Critical", "Not notified"));
            insertSeedInventory(connection, new InventoryItem("Palm Sugar", 7, "10/09/2026", "Low Stock", "Not notified"));

            insertSeedOrder(connection, new OrderItem("#0228", "Thomas Tejo", "Espresso Con Panna", "16 June 2026", "Complete", "Card", 28000));
            insertSeedOrder(connection, new OrderItem("#0229", "Miks Yermolay", "Milkshake Vanilla", "16 June 2026", "Assigned", "Cash", 32000));
            insertSeedOrder(connection, new OrderItem("#0230", "Neon Sylvon", "Mineral Water", "16 June 2026", "Waiting", "QRIS", 10000));

            markDefaultDataSeeded(connection);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to seed default database data", e);
        }
    }

    private static boolean isDefaultDataAlreadySeeded(Connection connection) throws SQLException {
        String sql = "SELECT setting_value FROM app_settings WHERE setting_key = 'default_data_seeded'";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && "true".equalsIgnoreCase(resultSet.getString("setting_value"));
        }
    }

    private static void markDefaultDataSeeded(Connection connection) throws SQLException {
        String sql = "INSERT INTO app_settings(setting_key, setting_value) VALUES('default_data_seeded', 'true') "
                + "ON CONFLICT(setting_key) DO UPDATE SET setting_value = 'true'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        }
    }

    private static void insertSeedMenu(Connection connection, CafeMenuItem item) throws SQLException {
        String sql = "INSERT OR IGNORE INTO menu_items(menu_name, category, price, ingredients, status) VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getMenuName());
            statement.setString(2, item.getCategory());
            statement.setString(3, item.getPrice());
            statement.setString(4, item.getIngredients());
            statement.setString(5, item.getStatus());
            statement.executeUpdate();
        }
    }

    private static void insertSeedInventory(Connection connection, InventoryItem item) throws SQLException {
        String sql = "INSERT OR IGNORE INTO inventory_items(product, stock, exp_date, status, notify_status) VALUES(?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getProduct());
            statement.setInt(2, item.getStock());
            statement.setString(3, item.getExpDate());
            statement.setString(4, item.getStatus());
            statement.setString(5, item.getNotifyStatus());
            statement.executeUpdate();
        }
    }

    private static void insertSeedOrder(Connection connection, OrderItem order) throws SQLException {
        String sql = "INSERT OR IGNORE INTO orders(order_id, customer, product, creation_date, status, payment_method, total_amount) VALUES(?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, order.getOrderId());
            statement.setString(2, order.getCustomer());
            statement.setString(3, order.getProduct());
            statement.setString(4, order.getCreationDate());
            statement.setString(5, order.getStatus());
            statement.setString(6, order.getPaymentMethod());
            statement.setInt(7, order.getTotalAmount());
            statement.executeUpdate();
        }
    }
}
