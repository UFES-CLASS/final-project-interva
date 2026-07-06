package interva.sambikopi.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public final class SambiKopiDataStore {

    private static final ObservableList<CafeMenuItem> MENU_ITEMS = FXCollections.observableArrayList(
            new CafeMenuItem("Kopi Milk Gula Aren", "Beverage - Coffee", "Rp 18.000", "Milk, Coffee, Palm Sugar", "Pending Approval"),
            new CafeMenuItem("Americano", "Beverage - Coffee", "Rp 15.000", "Espresso, Water", "Approved"),
            new CafeMenuItem("Matcha Latte", "Beverage - Non Coffee", "Rp 22.000", "Matcha, Milk, Sugar", "Approved"),
            new CafeMenuItem("Almond Croissant", "Pastry", "Rp 25.000", "Flour, Butter, Almond", "Pending Approval")
    );

    private static final ObservableList<OrderItem> ORDERS = FXCollections.observableArrayList(
            new OrderItem("00978-AB", "Thomas Tejo", "Espresso Con Panna", "16 January 2028", "Complete"),
            new OrderItem("94741-AB", "Miks Yermolay", "Milkshake Vanilla", "16 January 2028", "Assigned"),
            new OrderItem("09213-AB", "Neon Sylvon", "Mineral Water", "16 January 2028", "Waiting"),
            new OrderItem("81220-AC", "Fade Nightmare", "Kopi Milk Gula Aren", "17 January 2028", "Assigned"),
            new OrderItem("73014-BC", "Chamber Vamos", "Matcha Latte", "17 January 2028", "Waiting")
    );

    private static final String[] SAMPLE_CUSTOMERS = {
            "Neon Sylvon", "Yoru Natsuki", "Iso Yang", "Jett Kim", "Omen Octopus", "Killjoy", "Cypher Lineup"
    };

    private static final String[] SAMPLE_PRODUCTS = {
            "Mineral Water", "Kopi Milk Gula Aren", "Americano", "Matcha Latte", "Almond Croissant", "Milkshake Vanilla"
    };

    private static final Random RANDOM = new Random();
    private static int generatedOrderCounter = 214;

    private SambiKopiDataStore() {
    }

    public static ObservableList<CafeMenuItem> getMenuItems() {
        return MENU_ITEMS;
    }

    public static ObservableList<OrderItem> getOrders() {
        return ORDERS;
    }

    public static OrderItem createIncomingOrder() {
        generatedOrderCounter++;
        String orderId = String.format("%05d-AB", generatedOrderCounter);
        String customer = SAMPLE_CUSTOMERS[RANDOM.nextInt(SAMPLE_CUSTOMERS.length)];
        String product = SAMPLE_PRODUCTS[RANDOM.nextInt(SAMPLE_PRODUCTS.length)];
        String creationDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
        return new OrderItem(orderId, customer, product, creationDate, "Waiting");
    }
}
