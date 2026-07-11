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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SambiKopiDataStore {

    public static final String DEFAULT_MENU_IMAGE = "assets/menu/default.png";

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

    public static ObservableList<CafeMenuItem> getMenuItems() { return MENU_ITEMS; }
    public static ObservableList<CafeMenuItem> getMenuReviewItems() { return MENU_REVIEW_ITEMS; }
    public static ObservableList<InventoryItem> getInventoryItems() { return INVENTORY_ITEMS; }
    public static ObservableList<InventoryItem> getStockReviewItems() { return STOCK_REVIEW_ITEMS; }
    public static ObservableList<OrderItem> getOrders() { return ORDERS; }

    public static void reloadAllFromDatabase() { loadAllFromDatabase(); }

    public static void addMenuForReview(CafeMenuItem item) {
        addMenuForReview(item, parseIngredientText(item == null ? "" : item.getMenuName(), item == null ? "" : item.getIngredients()));
    }

    public static void addMenuForReview(CafeMenuItem item, List<MenuIngredient> ingredients) {
        if (item == null) {
            return;
        }
        item.setStatus("Pending Approval");
        upsertMenuItem(item);
        saveMenuIngredients(item.getMenuName(), ingredients);
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

    public static void updateMenuItem(CafeMenuItem item) {
        if (item == null) {
            return;
        }
        upsertMenuItem(item);
        CafeMenuItem existing = findMenuByName(item.getMenuName());
        if (existing != null) {
            existing.setCategory(item.getCategory());
            existing.setPrice(item.getPrice());
            existing.setIngredients(item.getIngredients());
            existing.setStatus(item.getStatus());
            existing.setImagePath(item.getImagePath());
        }
        if ("Pending Approval".equalsIgnoreCase(item.getStatus())) {
            if (!MENU_REVIEW_ITEMS.contains(existing != null ? existing : item)) {
                MENU_REVIEW_ITEMS.add(existing != null ? existing : item);
            }
        } else {
            MENU_REVIEW_ITEMS.remove(existing != null ? existing : item);
        }
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

    public static void addInventoryItem(InventoryItem item) {
        if (item == null) {
            return;
        }

        item.setStatus(getStatusFromStock(item.getStock()));
        upsertInventoryItem(item);
        INVENTORY_ITEMS.removeIf(existing -> existing.getProduct().equalsIgnoreCase(item.getProduct()));
        STOCK_REVIEW_ITEMS.removeIf(existing -> existing.getProduct().equalsIgnoreCase(item.getProduct()));
        INVENTORY_ITEMS.add(item);

        if ("Notified".equalsIgnoreCase(item.getNotifyStatus())) {
            STOCK_REVIEW_ITEMS.add(item);
            ownerStockNotificationCount++;
        }
    }

    public static void updateInventoryItem(InventoryItem item) {
        if (item == null) {
            return;
        }
        item.setStatus(getStatusFromStock(item.getStock()));
        upsertInventoryItem(item);
        InventoryItem existing = findInventoryByProduct(item.getProduct());
        if (existing != null) {
            existing.setStock(item.getStock());
            existing.setExpDate(item.getExpDate());
            existing.setStatus(item.getStatus());
            existing.setNotifyStatus(item.getNotifyStatus());
        } else {
            INVENTORY_ITEMS.add(item);
        }
        if ("Notified".equalsIgnoreCase(item.getNotifyStatus())) {
            InventoryItem target = existing != null ? existing : item;
            if (!STOCK_REVIEW_ITEMS.contains(target)) {
                STOCK_REVIEW_ITEMS.add(target);
            }
        } else {
            STOCK_REVIEW_ITEMS.remove(existing != null ? existing : item);
        }
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

    public static StockDeductionResult validateStockAvailabilityForOrder(String productSummary) {
        Map<String, Integer> menuQuantities = parseOrderProductSummary(productSummary);
        Map<String, Integer> stockRequirements = new HashMap<>();
        Map<String, String> stockUnits = new HashMap<>();

        for (Map.Entry<String, Integer> menuEntry : menuQuantities.entrySet()) {
            CafeMenuItem menuItem = findMenuByName(menuEntry.getKey());
            if (menuItem == null) {
                continue;
            }

            List<MenuIngredient> ingredients = getMenuIngredients(menuItem.getMenuName());
            for (MenuIngredient ingredient : ingredients) {
                int required = ingredient.getQuantity() * menuEntry.getValue();
                stockRequirements.merge(ingredient.getStockProduct(), required, Integer::sum);
                stockUnits.putIfAbsent(ingredient.getStockProduct(), ingredient.getUnit());
            }
        }

        for (Map.Entry<String, Integer> requirement : stockRequirements.entrySet()) {
            InventoryItem stockItem = findInventoryByProduct(requirement.getKey());
            if (stockItem == null) {
                return StockDeductionResult.failed("Stock item not found: " + requirement.getKey());
            }
            if (stockItem.getStock() < requirement.getValue()) {
                String unit = stockUnits.getOrDefault(stockItem.getProduct(), "portion");
                return StockDeductionResult.failed("Cannot create order. " + stockItem.getProduct()
                        + " stock is not enough. Needed " + requirement.getValue() + " " + unit
                        + ", available " + stockItem.getStock() + " " + unit + ".");
            }
        }

        return StockDeductionResult.success("Stock is available for this order.");
    }

    public static StockDeductionResult completeOrderAndDeductStock(OrderItem order) {
        if (order == null) {
            return StockDeductionResult.failed("Select an order first.");
        }
        if ("Complete".equalsIgnoreCase(order.getStatus())) {
            return StockDeductionResult.failed("This order is already complete.");
        }
        if ("Cancelled".equalsIgnoreCase(order.getStatus())) {
            return StockDeductionResult.failed("Cancelled orders cannot be completed.");
        }

        Map<String, Integer> menuQuantities = parseOrderProductSummary(order.getProduct());
        Map<String, Integer> stockRequirements = new HashMap<>();
        Map<String, String> stockUnits = new HashMap<>();

        for (Map.Entry<String, Integer> menuEntry : menuQuantities.entrySet()) {
            CafeMenuItem menuItem = findMenuByName(menuEntry.getKey());
            if (menuItem == null) {
                continue;
            }

            List<MenuIngredient> ingredients = getMenuIngredients(menuItem.getMenuName());
            for (MenuIngredient ingredient : ingredients) {
                int required = ingredient.getQuantity() * menuEntry.getValue();
                stockRequirements.merge(ingredient.getStockProduct(), required, Integer::sum);
                stockUnits.putIfAbsent(ingredient.getStockProduct(), ingredient.getUnit());
            }
        }

        if (stockRequirements.isEmpty()) {
            updateOrderStatus(order, "Complete");
            return StockDeductionResult.success("Order completed. No linked stock ingredients were found for deduction.");
        }

        for (Map.Entry<String, Integer> requirement : stockRequirements.entrySet()) {
            InventoryItem stockItem = findInventoryByProduct(requirement.getKey());
            if (stockItem == null) {
                return StockDeductionResult.failed("Stock item not found: " + requirement.getKey());
            }
            if (stockItem.getStock() < requirement.getValue()) {
                String unit = stockUnits.getOrDefault(stockItem.getProduct(), "portion");
                return StockDeductionResult.failed("Cannot complete order. " + stockItem.getProduct()
                        + " stock is not enough. Needed " + requirement.getValue() + " " + unit
                        + ", available " + stockItem.getStock() + " " + unit + ".");
            }
        }

        for (Map.Entry<String, Integer> requirement : stockRequirements.entrySet()) {
            InventoryItem stockItem = findInventoryByProduct(requirement.getKey());
            int newStock = stockItem.getStock() - requirement.getValue();
            updateInventoryStock(stockItem, newStock);
        }

        updateOrderStatus(order, "Complete");
        return StockDeductionResult.success("Order completed and stock deducted.");
    }

    public static List<MenuIngredient> getMenuIngredients(String menuName) {
        List<MenuIngredient> ingredients = new ArrayList<>();
        if (menuName == null || menuName.isBlank()) {
            return ingredients;
        }

        String sql = "SELECT menu_name, stock_product, quantity, unit FROM menu_ingredients WHERE menu_name = ? ORDER BY stock_product";
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, menuName);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ingredients.add(new MenuIngredient(
                            resultSet.getString("menu_name"),
                            resultSet.getString("stock_product"),
                            resultSet.getInt("quantity"),
                            resultSet.getString("unit")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load menu ingredients", e);
        }
        return ingredients;
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

    public static String formatTitleCase(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return "";
        }

        String normalized = rawValue.trim().replaceAll("\\s+", " ");
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (int i = 0; i < normalized.length(); i++) {
            char c = normalized.charAt(i);

            if (Character.isLetter(c)) {
                result.append(capitalizeNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
                capitalizeNext = Character.isWhitespace(c)
                        || c == '-'
                        || c == '/'
                        || c == ','
                        || c == '.'
                        || c == '(';
            }
        }

        return result.toString();
    }

    public static String buildIngredientDisplay(List<MenuIngredient> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return "";
        }
        return ingredients.stream()
                .map(MenuIngredient::toDisplayText)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    public static String getStatusFromStock(int stock) {
        if (stock <= 3) {
            return "Critical";
        }
        if (stock <= 8) {
            return "Low Stock";
        }
        return "Available";
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
        String sql = "SELECT menu_name, category, price, ingredients, status, image_path FROM menu_items ORDER BY menu_name";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                CafeMenuItem item = new CafeMenuItem(
                        resultSet.getString("menu_name"),
                        resultSet.getString("category"),
                        resultSet.getString("price"),
                        resultSet.getString("ingredients"),
                        resultSet.getString("status"),
                        resultSet.getString("image_path")
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
                INSERT INTO menu_items(menu_name, category, price, ingredients, status, image_path)
                VALUES(?, ?, ?, ?, ?, ?)
                ON CONFLICT(menu_name) DO UPDATE SET
                    category = excluded.category,
                    price = excluded.price,
                    ingredients = excluded.ingredients,
                    status = excluded.status,
                    image_path = excluded.image_path
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getMenuName());
            statement.setString(2, item.getCategory());
            statement.setString(3, item.getPrice());
            statement.setString(4, item.getIngredients());
            statement.setString(5, item.getStatus());
            statement.setString(6, item.getImagePath());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save menu item", e);
        }
    }

    private static void saveMenuIngredients(String menuName, List<MenuIngredient> ingredients) {
        if (menuName == null || menuName.isBlank()) {
            return;
        }
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement delete = connection.prepareStatement("DELETE FROM menu_ingredients WHERE menu_name = ?")) {
                delete.setString(1, menuName);
                delete.executeUpdate();
            }
            if (ingredients == null || ingredients.isEmpty()) {
                return;
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO menu_ingredients(menu_name, stock_product, quantity, unit) VALUES(?, ?, ?, ?)")) {
                for (MenuIngredient ingredient : ingredients) {
                    insert.setString(1, menuName);
                    insert.setString(2, ingredient.getStockProduct());
                    insert.setInt(3, Math.max(1, ingredient.getQuantity()));
                    insert.setString(4, ingredient.getUnit());
                    insert.addBatch();
                }
                insert.executeBatch();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save menu ingredients", e);
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

    private static void upsertInventoryItem(InventoryItem item) {
        String sql = """
                INSERT INTO inventory_items(product, stock, exp_date, status, notify_status)
                VALUES(?, ?, ?, ?, ?)
                ON CONFLICT(product) DO UPDATE SET
                    stock = excluded.stock,
                    exp_date = excluded.exp_date,
                    status = excluded.status,
                    notify_status = excluded.notify_status
                """;
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getProduct());
            statement.setInt(2, item.getStock());
            statement.setString(3, item.getExpDate());
            statement.setString(4, item.getStatus());
            statement.setString(5, item.getNotifyStatus());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save stock item", e);
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

    private static void updateInventoryStock(InventoryItem item, int newStock) {
        item.setStock(newStock);
        item.setStatus(getStatusFromStock(newStock));
        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE inventory_items SET stock = ?, status = ? WHERE product = ?")) {
            statement.setInt(1, item.getStock());
            statement.setString(2, item.getStatus());
            statement.setString(3, item.getProduct());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update stock quantity", e);
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

    private static CafeMenuItem findMenuByName(String menuName) {
        return MENU_ITEMS.stream()
                .filter(item -> item.getMenuName().equalsIgnoreCase(menuName))
                .findFirst()
                .orElse(null);
    }

    private static InventoryItem findInventoryByProduct(String product) {
        return INVENTORY_ITEMS.stream()
                .filter(item -> item.getProduct().equalsIgnoreCase(product))
                .findFirst()
                .orElse(null);
    }

    private static Map<String, Integer> parseOrderProductSummary(String summary) {
        Map<String, Integer> result = new HashMap<>();
        if (summary == null || summary.isBlank()) {
            return result;
        }
        Pattern pattern = Pattern.compile("(\\d+)x\\s+(.+)", Pattern.CASE_INSENSITIVE);
        for (String part : summary.split(",")) {
            String trimmed = part.trim();
            Matcher matcher = pattern.matcher(trimmed);
            if (matcher.matches()) {
                int quantity = Integer.parseInt(matcher.group(1));
                String menuName = matcher.group(2).trim();
                result.merge(menuName, quantity, Integer::sum);
            } else if (!trimmed.isBlank()) {
                result.merge(trimmed, 1, Integer::sum);
            }
        }
        return result;
    }

    private static List<MenuIngredient> parseIngredientText(String menuName, String ingredientsText) {
        List<MenuIngredient> ingredients = new ArrayList<>();
        if (ingredientsText == null || ingredientsText.isBlank()) {
            return ingredients;
        }

        Pattern dashPattern = Pattern.compile("(.+)\\s+-\\s+(\\d+)\\s+(.+)$", Pattern.CASE_INSENSITIVE);
        Pattern xPattern = Pattern.compile("(.+)\\s+x(\\d+)$", Pattern.CASE_INSENSITIVE);

        for (String part : ingredientsText.split(",")) {
            String trimmed = part.trim();
            if (trimmed.isBlank()) {
                continue;
            }

            Matcher dashMatcher = dashPattern.matcher(trimmed);
            Matcher xMatcher = xPattern.matcher(trimmed);

            if (dashMatcher.matches()) {
                ingredients.add(new MenuIngredient(
                        menuName,
                        dashMatcher.group(1).trim(),
                        Integer.parseInt(dashMatcher.group(2)),
                        dashMatcher.group(3).trim()));
            } else if (xMatcher.matches()) {
                ingredients.add(new MenuIngredient(menuName, xMatcher.group(1).trim(), Integer.parseInt(xMatcher.group(2)), "portion"));
            } else {
                ingredients.add(new MenuIngredient(menuName, trimmed, 1, "portion"));
            }
        }
        return ingredients;
    }

    private static void seedDefaultDataIfNeeded() {
        try (Connection connection = DatabaseManager.getConnection()) {
            if (!isDefaultDataAlreadySeeded(connection)) {
                insertSeedInventory(connection, new InventoryItem("Fresh Milk", 12, "12/08/2026", "Available", "Not notified"));
                insertSeedInventory(connection, new InventoryItem("Coffee Beans", 15, "01/09/2026", "Available", "Not notified"));
                insertSeedInventory(connection, new InventoryItem("Matcha Powder", 18, "20/10/2026", "Available", "Not notified"));
                insertSeedInventory(connection, new InventoryItem("Chocolate Syrup", 6, "15/07/2026", "Low Stock", "Not notified"));
                insertSeedInventory(connection, new InventoryItem("Palm Sugar", 10, "10/09/2026", "Available", "Not notified"));
                insertSeedInventory(connection, new InventoryItem("Flour", 20, "22/10/2026", "Available", "Not notified"));
                insertSeedInventory(connection, new InventoryItem("Butter", 14, "30/09/2026", "Available", "Not notified"));
                insertSeedInventory(connection, new InventoryItem("Almond", 8, "18/10/2026", "Low Stock", "Not notified"));
                insertSeedInventory(connection, new InventoryItem("Water", 50, "31/12/2026", "Available", "Not notified"));
                insertSeedInventory(connection, new InventoryItem("Sugar", 30, "31/12/2026", "Available", "Not notified"));

                insertSeedMenu(connection, new CafeMenuItem("Kopi Susu Gula Aren", "Coffee", "Rp 18.000", "Fresh Milk - 1 portion, Coffee Beans - 1 portion, Palm Sugar - 1 portion", "Pending Approval", "assets/menu/kopi_milk_gula_aren.png"));
                insertSeedMenu(connection, new CafeMenuItem("Americano", "Coffee", "Rp 15.000", "Coffee Beans - 1 portion, Water - 1 portion", "Approved", "assets/menu/americano.png"));
                insertSeedMenu(connection, new CafeMenuItem("Matcha Latte", "Non-Coffee", "Rp 22.000", "Matcha Powder - 1 portion, Fresh Milk - 1 portion, Sugar - 1 portion", "Approved", "assets/menu/matcha_latte.png"));
                insertSeedMenu(connection, new CafeMenuItem("Almond Croissant", "Pastry", "Rp 25.000", "Flour - 1 portion, Butter - 1 portion, Almond - 1 portion", "Pending Approval", "assets/menu/almond_croissant.png"));

                insertSeedOrder(connection, new OrderItem("#0228", "Thomas Tejo", "1x Americano", "16 January 2026", "Complete", "Card", 15000));
                insertSeedOrder(connection, new OrderItem("#0229", "Miks Yermolay", "1x Matcha Latte", "16 January 2026", "Assigned", "Cash", 22000));
                insertSeedOrder(connection, new OrderItem("#0230", "Neon Sylvon", "1x Americano", "16 January 2026", "Waiting", "QRIS", 15000));

                markDefaultDataSeeded(connection);
            }

            ensureDefaultMenuAssetsAndIngredients(connection);
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
        String sql = "INSERT OR IGNORE INTO menu_items(menu_name, category, price, ingredients, status, image_path) VALUES(?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, item.getMenuName());
            statement.setString(2, item.getCategory());
            statement.setString(3, item.getPrice());
            statement.setString(4, item.getIngredients());
            statement.setString(5, item.getStatus());
            statement.setString(6, item.getImagePath());
            statement.executeUpdate();
        }
        saveSeedIngredients(connection, item.getMenuName(), parseIngredientText(item.getMenuName(), item.getIngredients()));
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

    private static void ensureDefaultMenuAssetsAndIngredients(Connection connection) throws SQLException {
        insertSeedInventory(connection, new InventoryItem("Fresh Milk", 12, "12/08/2026", "Available", "Not notified"));
        insertSeedInventory(connection, new InventoryItem("Coffee Beans", 15, "01/09/2026", "Available", "Not notified"));
        insertSeedInventory(connection, new InventoryItem("Matcha Powder", 18, "20/10/2026", "Available", "Not notified"));
        insertSeedInventory(connection, new InventoryItem("Chocolate Syrup", 6, "15/07/2026", "Low Stock", "Not notified"));
        insertSeedInventory(connection, new InventoryItem("Palm Sugar", 10, "10/09/2026", "Available", "Not notified"));
        insertSeedInventory(connection, new InventoryItem("Flour", 20, "22/10/2026", "Available", "Not notified"));
        insertSeedInventory(connection, new InventoryItem("Butter", 14, "30/09/2026", "Available", "Not notified"));
        insertSeedInventory(connection, new InventoryItem("Almond", 8, "18/10/2026", "Low Stock", "Not notified"));
        insertSeedInventory(connection, new InventoryItem("Water", 50, "31/12/2026", "Available", "Not notified"));
        insertSeedInventory(connection, new InventoryItem("Sugar", 30, "31/12/2026", "Available", "Not notified"));

        updateSeedMenuImage(connection, "Americano", "assets/menu/americano.png");
        updateSeedMenuImage(connection, "Kopi Milk Gula Aren", "assets/menu/kopi_milk_gula_aren.png");
        updateSeedMenuImage(connection, "Matcha Latte", "assets/menu/matcha_latte.png");
        updateSeedMenuImage(connection, "Almond Croissant", "assets/menu/almond_croissant.png");

        saveSeedIngredients(connection, "Americano", List.of(
                new MenuIngredient("Americano", "Coffee Beans", 1),
                new MenuIngredient("Americano", "Water", 1)));
        saveSeedIngredients(connection, "Kopi Milk Gula Aren", List.of(
                new MenuIngredient("Kopi Milk Gula Aren", "Fresh Milk", 1),
                new MenuIngredient("Kopi Milk Gula Aren", "Coffee Beans", 1),
                new MenuIngredient("Kopi Milk Gula Aren", "Palm Sugar", 1)));
        saveSeedIngredients(connection, "Matcha Latte", List.of(
                new MenuIngredient("Matcha Latte", "Matcha Powder", 1),
                new MenuIngredient("Matcha Latte", "Fresh Milk", 1),
                new MenuIngredient("Matcha Latte", "Sugar", 1)));
        saveSeedIngredients(connection, "Almond Croissant", List.of(
                new MenuIngredient("Almond Croissant", "Flour", 1),
                new MenuIngredient("Almond Croissant", "Butter", 1),
                new MenuIngredient("Almond Croissant", "Almond", 1)));
    }

    private static void updateSeedMenuImage(Connection connection, String menuName, String imagePath) throws SQLException {
        String sql = "UPDATE menu_items SET image_path = ? WHERE menu_name = ? AND (image_path IS NULL OR image_path = '' OR image_path = 'assets/menu/default.png')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, imagePath);
            statement.setString(2, menuName);
            statement.executeUpdate();
        }
    }

    private static void saveSeedIngredients(Connection connection, String menuName, List<MenuIngredient> ingredients) throws SQLException {
        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }
        try (PreparedStatement countStatement = connection.prepareStatement("SELECT COUNT(*) FROM menu_ingredients WHERE menu_name = ?")) {
            countStatement.setString(1, menuName);
            try (ResultSet resultSet = countStatement.executeQuery()) {
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    return;
                }
            }
        }
        try (PreparedStatement statement = connection.prepareStatement("INSERT OR IGNORE INTO menu_ingredients(menu_name, stock_product, quantity, unit) VALUES(?, ?, ?, ?)")) {
            for (MenuIngredient ingredient : ingredients) {
                statement.setString(1, menuName);
                statement.setString(2, ingredient.getStockProduct());
                statement.setInt(3, ingredient.getQuantity());
                statement.setString(4, ingredient.getUnit());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public static final class StockDeductionResult {
        private final boolean success;
        private final String message;

        private StockDeductionResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static StockDeductionResult success(String message) {
            return new StockDeductionResult(true, message);
        }

        public static StockDeductionResult failed(String message) {
            return new StockDeductionResult(false, message);
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
