package interva.sambikopi.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
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
    private static final Map<String, List<MenuIngredient>> MENU_INGREDIENTS = new HashMap<>();

    private static int generatedOrderCounter = 230;
    private static int baristaNotificationCount = 0;
    private static int ownerStockNotificationCount = 0;
    private static int ownerMenuNotificationCount = 0;

    static { seedDefaultData(); }
    private SambiKopiDataStore() {}

    public static ObservableList<CafeMenuItem> getMenuItems() { return MENU_ITEMS; }
    public static ObservableList<CafeMenuItem> getMenuReviewItems() { return MENU_REVIEW_ITEMS; }
    public static ObservableList<InventoryItem> getInventoryItems() { return INVENTORY_ITEMS; }
    public static ObservableList<InventoryItem> getStockReviewItems() { return STOCK_REVIEW_ITEMS; }
    public static ObservableList<OrderItem> getOrders() { return ORDERS; }
    public static void reloadAllFromDatabase() { }

    public static void addMenuForReview(CafeMenuItem item) { addMenuForReview(item, parseIngredientText(item == null ? "" : item.getMenuName(), item == null ? "" : item.getIngredients())); }
    public static void addMenuForReview(CafeMenuItem item, List<MenuIngredient> ingredients) {
        if (item == null) return;
        item.setStatus("Pending Approval");
        putMenuItem(item);
        saveMenuIngredients(item.getMenuName(), ingredients);
        if (!MENU_REVIEW_ITEMS.contains(item)) MENU_REVIEW_ITEMS.add(item);
        ownerMenuNotificationCount++;
    }
    public static void approveMenuItem(CafeMenuItem item) { if (item != null) { item.setStatus("Approved"); MENU_REVIEW_ITEMS.remove(item); } }
    public static void rejectMenuItem(CafeMenuItem item) { deleteMenuItem(item); }
    public static void updateMenuItem(CafeMenuItem item) {
        if (item == null) return;
        putMenuItem(item);
        if ("Pending Approval".equalsIgnoreCase(item.getStatus())) {
            if (!MENU_REVIEW_ITEMS.contains(item)) MENU_REVIEW_ITEMS.add(item);
        } else {
            MENU_REVIEW_ITEMS.remove(item);
        }
    }
    public static void deleteMenuItem(CafeMenuItem item) {
        if (item == null) return;
        String key = normalizeKey(item.getMenuName());
        MENU_ITEMS.removeIf(e -> normalizeKey(e.getMenuName()).equals(key));
        MENU_REVIEW_ITEMS.removeIf(e -> normalizeKey(e.getMenuName()).equals(key));
        MENU_INGREDIENTS.remove(key);
    }
    public static void deleteAllMenuItems() { MENU_ITEMS.clear(); MENU_REVIEW_ITEMS.clear(); MENU_INGREDIENTS.clear(); }

    public static void addInventoryItem(InventoryItem item) { updateInventoryItem(null, item); }
    public static void updateInventoryItem(InventoryItem item) { updateInventoryItem(item == null ? null : item.getProduct(), item); }
    public static void updateInventoryItem(String oldProductName, InventoryItem item) {
        if (item == null) return;
        item.setProduct(formatTitleCase(item.getProduct()));
        item.setStatus(getStatusFromStock(item.getStock()));
        if (item.getNotifyStatus() == null || item.getNotifyStatus().isBlank()) item.setNotifyStatus("Not notified");
        String oldKey = normalizeKey(oldProductName);
        String newKey = normalizeKey(item.getProduct());
        if (!oldKey.isEmpty() && !oldKey.equals(newKey)) {
            InventoryItem editedItem = item;
            INVENTORY_ITEMS.removeIf(e -> e != editedItem && normalizeKey(e.getProduct()).equals(oldKey));
            STOCK_REVIEW_ITEMS.removeIf(e -> e != editedItem && normalizeKey(e.getProduct()).equals(oldKey));
            renameIngredientReferences(oldProductName, item.getProduct());
        }
        InventoryItem found = findInventoryByProduct(item.getProduct());
        if (found != null && found != item) {
            found.setStock(item.getStock());
            found.setExpDate(item.getExpDate());
            found.setStatus(item.getStatus());
            found.setNotifyStatus(item.getNotifyStatus());
            item = found;
        } else if (!INVENTORY_ITEMS.contains(item)) {
            INVENTORY_ITEMS.add(item);
        }
        InventoryItem finalItem = item;
        STOCK_REVIEW_ITEMS.removeIf(e -> normalizeKey(e.getProduct()).equals(newKey) && e != finalItem);
        if ("Notified".equalsIgnoreCase(item.getNotifyStatus())) {
            if (!STOCK_REVIEW_ITEMS.contains(item)) STOCK_REVIEW_ITEMS.add(item);
        } else {
            STOCK_REVIEW_ITEMS.remove(item);
        }
    }
    public static boolean notifyOwnerStock(InventoryItem item) {
        if (item == null) return false;
        item.setNotifyStatus("Notified");
        updateInventoryItem(item.getProduct(), item);
        if (!STOCK_REVIEW_ITEMS.contains(item)) { STOCK_REVIEW_ITEMS.add(item); ownerStockNotificationCount++; }
        return true;
    }
    public static void markStockReviewed(InventoryItem item) { if (item != null) { item.setNotifyStatus("Reviewed"); STOCK_REVIEW_ITEMS.remove(item); } }

    public static OrderItem createCashierOrder(String customer, String productSummary, String paymentMethod, int totalAmount) {
        generatedOrderCounter++;
        OrderItem order = new OrderItem(String.format("#%04d", generatedOrderCounter), customer, productSummary, LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")), "Waiting", paymentMethod, totalAmount);
        ORDERS.add(order); baristaNotificationCount++; return order;
    }

    public static Queue<OrderItem> getActiveOrderQueue() {
        Queue<OrderItem> queue = new LinkedList<>();
        for (OrderItem order : ORDERS) {
            if (isActiveOrder(order)) {
                queue.offer(order);
            }
        }
        return queue;
    }

    public static OrderItem peekNextOrderInQueue() {
        return getActiveOrderQueue().peek();
    }

    public static boolean isFirstActiveOrder(OrderItem order) {
        return order != null && order == peekNextOrderInQueue();
    }

    public static boolean isActiveOrder(OrderItem order) {
        if (order == null) return false;
        return !"Complete".equalsIgnoreCase(order.getStatus())
                && !"Cancelled".equalsIgnoreCase(order.getStatus());
    }

    public static void updateOrderStatus(OrderItem order, String status) { if (order != null && status != null) order.setStatus(status); }
    public static StockDeductionResult validateStockAvailabilityForOrder(String productSummary) {
        for (Map.Entry<String, Integer> requirement : buildStockRequirements(productSummary).entrySet()) {
            InventoryItem stockItem = findInventoryByProduct(requirement.getKey());
            if (stockItem == null) return StockDeductionResult.failed("Stock item not found: " + requirement.getKey());
            if (stockItem.getStock() < requirement.getValue()) return StockDeductionResult.failed("Cannot create order. " + stockItem.getProduct() + " stock is not enough. Needed " + requirement.getValue() + ", available " + stockItem.getStock() + ".");
        }
        return StockDeductionResult.success("Stock is available for this order.");
    }
    public static StockDeductionResult completeOrderAndDeductStock(OrderItem order) {
        if (order == null) return StockDeductionResult.failed("Select an order first.");
        if ("Complete".equalsIgnoreCase(order.getStatus())) return StockDeductionResult.failed("This order is already complete.");
        if ("Cancelled".equalsIgnoreCase(order.getStatus())) return StockDeductionResult.failed("Cancelled orders cannot be completed.");
        OrderItem queueFront = peekNextOrderInQueue();
        if (queueFront != null && order != queueFront) {
            return StockDeductionResult.failed("Queue rule: process " + queueFront.getOrderId() + " first before handling " + order.getOrderId() + ".");
        }
        Map<String, Integer> requirements = buildStockRequirements(order.getProduct());
        for (Map.Entry<String, Integer> requirement : requirements.entrySet()) {
            InventoryItem stockItem = findInventoryByProduct(requirement.getKey());
            if (stockItem == null) return StockDeductionResult.failed("Stock item not found: " + requirement.getKey());
            if (stockItem.getStock() < requirement.getValue()) return StockDeductionResult.failed("Cannot complete order. " + stockItem.getProduct() + " stock is not enough. Needed " + requirement.getValue() + ", available " + stockItem.getStock() + ".");
        }
        for (Map.Entry<String, Integer> requirement : requirements.entrySet()) {
            InventoryItem stockItem = findInventoryByProduct(requirement.getKey());
            stockItem.setStock(stockItem.getStock() - requirement.getValue());
            stockItem.setStatus(getStatusFromStock(stockItem.getStock()));
        }
        updateOrderStatus(order, "Complete");
        return requirements.isEmpty() ? StockDeductionResult.success("Order completed. No linked stock ingredients were found for deduction.") : StockDeductionResult.success("Order completed and stock deducted.");
    }
    public static List<MenuIngredient> getMenuIngredients(String menuName) { return new ArrayList<>(MENU_INGREDIENTS.getOrDefault(normalizeKey(menuName), List.of())); }
    public static int consumeBaristaNotificationCount() { int c = baristaNotificationCount; baristaNotificationCount = 0; return c; }
    public static int consumeOwnerStockNotificationCount() { int c = ownerStockNotificationCount; ownerStockNotificationCount = 0; return c; }
    public static int consumeOwnerMenuNotificationCount() { int c = ownerMenuNotificationCount; ownerMenuNotificationCount = 0; return c; }
    public static String formatIdr(int value) { return "Rp " + String.format(new Locale("id", "ID"), "%,d", value).replace(',', '.'); }
    public static int parseIdr(String rawValue) { if (rawValue == null || rawValue.isBlank()) return 0; String digits = rawValue.replaceAll("[^0-9]", ""); return digits.isBlank() ? 0 : Integer.parseInt(digits); }
    public static String formatTitleCase(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) return "";
        String normalized = rawValue.trim().replaceAll("\\s+", " ");
        StringBuilder result = new StringBuilder(); boolean cap = true;
        for (int i=0; i<normalized.length(); i++) { char ch=normalized.charAt(i); if (Character.isLetter(ch)) { result.append(cap ? Character.toUpperCase(ch) : Character.toLowerCase(ch)); cap=false; } else { result.append(ch); cap = Character.isWhitespace(ch) || ch=='-' || ch=='/' || ch==',' || ch=='.' || ch=='('; } }
        return result.toString();
    }
    public static String buildIngredientDisplay(List<MenuIngredient> ingredients) { if (ingredients == null || ingredients.isEmpty()) return ""; return ingredients.stream().map(MenuIngredient::toDisplayText).reduce((a,b)->a+", "+b).orElse(""); }
    public static String getStatusFromStock(int stock) { if (stock <= 3) return "Critical"; if (stock <= 8) return "Low Stock"; return "Available"; }

    private static void putMenuItem(CafeMenuItem item) {
        CafeMenuItem found = findMenuByName(item.getMenuName());
        if (found != null && found != item) { found.setCategory(item.getCategory()); found.setPrice(item.getPrice()); found.setIngredients(item.getIngredients()); found.setStatus(item.getStatus()); found.setImagePath(item.getImagePath()); }
        else if (!MENU_ITEMS.contains(item)) MENU_ITEMS.add(item);
    }
    private static void saveMenuIngredients(String menuName, List<MenuIngredient> ingredients) {
        List<MenuIngredient> copy = new ArrayList<>();
        if (ingredients != null) for (MenuIngredient ing : ingredients) copy.add(new MenuIngredient(menuName, ing.getStockProduct(), Math.max(1, ing.getQuantity()), ing.getUnit()));
        MENU_INGREDIENTS.put(normalizeKey(menuName), copy);
    }
    private static Map<String, Integer> buildStockRequirements(String productSummary) {
        Map<String, Integer> menuQuantities = parseOrderProductSummary(productSummary);
        Map<String, Integer> requirements = new HashMap<>();
        for (Map.Entry<String, Integer> entry : menuQuantities.entrySet()) for (MenuIngredient ing : getMenuIngredients(entry.getKey())) requirements.merge(ing.getStockProduct(), ing.getQuantity() * entry.getValue(), Integer::sum);
        return requirements;
    }
    private static Map<String, Integer> parseOrderProductSummary(String productSummary) {
        Map<String, Integer> result = new HashMap<>();
        if (productSummary == null || productSummary.isBlank()) return result;
        Matcher matcher = Pattern.compile("(\\d+)x\\s*([^,]+)").matcher(productSummary);
        while (matcher.find()) result.merge(matcher.group(2).trim(), Integer.parseInt(matcher.group(1)), Integer::sum);
        if (result.isEmpty()) result.put(productSummary.trim(), 1);
        return result;
    }
    private static List<MenuIngredient> parseIngredientText(String menuName, String ingredientsText) {
        List<MenuIngredient> list = new ArrayList<>();
        if (ingredientsText == null || ingredientsText.isBlank()) return list;
        for (String part : ingredientsText.split(",")) {
            String text = part.trim(); if (text.isEmpty()) continue;
            String[] pieces = text.split(" - "); String product = pieces[0].trim(); int qty=1; String unit="portion";
            if (pieces.length > 1) { String[] amount = pieces[1].trim().split("\\s+", 2); try { qty = Integer.parseInt(amount[0]); } catch (Exception ignored) {} if (amount.length > 1) unit = amount[1]; }
            list.add(new MenuIngredient(menuName, product, qty, unit));
        }
        return list;
    }
    private static CafeMenuItem findMenuByName(String menuName) { String key = normalizeKey(menuName); for (CafeMenuItem item : MENU_ITEMS) if (normalizeKey(item.getMenuName()).equals(key)) return item; return null; }
    private static InventoryItem findInventoryByProduct(String product) { String key = normalizeKey(product); for (InventoryItem item : INVENTORY_ITEMS) if (normalizeKey(item.getProduct()).equals(key)) return item; return null; }
    private static String normalizeKey(String value) { return value == null ? "" : value.trim().toLowerCase(Locale.ROOT); }
    private static void renameIngredientReferences(String oldProductName, String newProductName) {
        String oldKey = normalizeKey(oldProductName); if (oldKey.isEmpty() || newProductName == null || newProductName.isBlank()) return;
        for (Map.Entry<String, List<MenuIngredient>> entry : MENU_INGREDIENTS.entrySet()) {
            for (MenuIngredient ing : entry.getValue()) if (normalizeKey(ing.getStockProduct()).equals(oldKey)) ing.setStockProduct(newProductName);
            CafeMenuItem menu = findMenuByName(entry.getKey()); if (menu != null) menu.setIngredients(buildIngredientDisplay(entry.getValue()));
        }
    }
    private static void seedDefaultData() {
        if (!MENU_ITEMS.isEmpty() || !INVENTORY_ITEMS.isEmpty() || !ORDERS.isEmpty()) return;
        addSeedInventory(new InventoryItem("Fresh Milk", 12, "12/08/2026", "Available", "Not notified"));
        addSeedInventory(new InventoryItem("Coffee Beans", 15, "01/09/2026", "Available", "Not notified"));
        addSeedInventory(new InventoryItem("Matcha Powder", 18, "20/10/2026", "Available", "Not notified"));
        addSeedInventory(new InventoryItem("Chocolate Syrup", 6, "15/07/2026", "Low Stock", "Not notified"));
        addSeedInventory(new InventoryItem("Palm Sugar", 10, "10/09/2026", "Available", "Not notified"));
        addSeedInventory(new InventoryItem("Flour", 20, "22/10/2026", "Available", "Not notified"));
        addSeedInventory(new InventoryItem("Butter", 14, "30/09/2026", "Available", "Not notified"));
        addSeedInventory(new InventoryItem("Almond", 8, "18/10/2026", "Low Stock", "Not notified"));
        addSeedInventory(new InventoryItem("Water", 50, "31/12/2026", "Available", "Not notified"));
        addSeedInventory(new InventoryItem("Sugar", 30, "31/12/2026", "Available", "Not notified"));
        addSeedMenu(new CafeMenuItem("Kopi Milk Gula Aren", "Coffee", "Rp 18.000", "Fresh Milk - 1 portion, Coffee Beans - 1 portion, Palm Sugar - 1 portion", "Pending Approval", "assets/menu/kopi_milk_gula_aren.png"));
        addSeedMenu(new CafeMenuItem("Americano", "Coffee", "Rp 15.000", "Coffee Beans - 1 portion, Water - 1 portion", "Approved", "assets/menu/americano.png"));
        addSeedMenu(new CafeMenuItem("Matcha Latte", "Non-Coffee", "Rp 22.000", "Matcha Powder - 1 portion, Fresh Milk - 1 portion, Sugar - 1 portion", "Approved", "assets/menu/matcha_latte.png"));
        addSeedMenu(new CafeMenuItem("Almond Croissant", "Pastry", "Rp 25.000", "Flour - 1 portion, Butter - 1 portion, Almond - 1 portion", "Pending Approval", "assets/menu/almond_croissant.png"));
        ORDERS.add(new OrderItem("#0228", "Thomas Tejo", "1x Americano", "16 January 2026", "Complete", "Card", 15000));
        ORDERS.add(new OrderItem("#0229", "Miks Yermolay", "1x Matcha Latte", "16 January 2026", "Assigned", "Cash", 22000));
        ORDERS.add(new OrderItem("#0230", "Neon Sylvon", "1x Americano", "16 January 2026", "Waiting", "QRIS", 15000));
    }
    private static void addSeedInventory(InventoryItem item) { item.setStatus(getStatusFromStock(item.getStock())); INVENTORY_ITEMS.add(item); }
    private static void addSeedMenu(CafeMenuItem item) { MENU_ITEMS.add(item); if ("Pending Approval".equalsIgnoreCase(item.getStatus())) MENU_REVIEW_ITEMS.add(item); saveMenuIngredients(item.getMenuName(), parseIngredientText(item.getMenuName(), item.getIngredients())); }

    public static final class StockDeductionResult {
        private final boolean success; private final String message;
        private StockDeductionResult(boolean success, String message) { this.success = success; this.message = message; }
        public static StockDeductionResult success(String message) { return new StockDeductionResult(true, message); }
        public static StockDeductionResult failed(String message) { return new StockDeductionResult(false, message); }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
