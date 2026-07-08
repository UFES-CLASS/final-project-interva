package interva.sambikopi.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:sambi_kopi.db";

    private DatabaseManager() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void initializeDatabase() {
        try {
            Class.forName("org.sqlite.JDBC");
            try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");

                statement.execute("""
                        CREATE TABLE IF NOT EXISTS menu_items (
                            menu_name TEXT PRIMARY KEY,
                            category TEXT NOT NULL,
                            price TEXT NOT NULL,
                            ingredients TEXT NOT NULL,
                            status TEXT NOT NULL,
                            image_path TEXT NOT NULL DEFAULT 'assets/menu/default.png'
                        )
                        """);

                statement.execute("""
                        CREATE TABLE IF NOT EXISTS inventory_items (
                            product TEXT PRIMARY KEY,
                            stock INTEGER NOT NULL,
                            exp_date TEXT NOT NULL,
                            status TEXT NOT NULL,
                            notify_status TEXT NOT NULL
                        )
                        """);

                statement.execute("""
                        CREATE TABLE IF NOT EXISTS menu_ingredients (
                            menu_name TEXT NOT NULL,
                            stock_product TEXT NOT NULL,
                            quantity INTEGER NOT NULL,
                            PRIMARY KEY(menu_name, stock_product),
                            FOREIGN KEY(menu_name) REFERENCES menu_items(menu_name) ON DELETE CASCADE,
                            FOREIGN KEY(stock_product) REFERENCES inventory_items(product) ON UPDATE CASCADE
                        )
                        """);

                statement.execute("""
                        CREATE TABLE IF NOT EXISTS orders (
                            order_id TEXT PRIMARY KEY,
                            customer TEXT NOT NULL,
                            product TEXT NOT NULL,
                            creation_date TEXT NOT NULL,
                            status TEXT NOT NULL,
                            payment_method TEXT NOT NULL,
                            total_amount INTEGER NOT NULL
                        )
                        """);

                statement.execute("""
                        CREATE TABLE IF NOT EXISTS app_settings (
                            setting_key TEXT PRIMARY KEY,
                            setting_value TEXT NOT NULL
                        )
                        """);

                addColumnIfMissing(connection, "menu_items", "image_path", "TEXT NOT NULL DEFAULT 'assets/menu/default.png'");
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void addColumnIfMissing(Connection connection, String tableName, String columnName, String columnDefinition) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (resultSet.next()) {
                if (columnName.equalsIgnoreCase(resultSet.getString("name"))) {
                    return;
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
        }
    }
}
