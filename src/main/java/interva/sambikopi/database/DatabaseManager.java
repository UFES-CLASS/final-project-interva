package interva.sambikopi.database;

import java.sql.Connection;
import java.sql.DriverManager;
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
                            status TEXT NOT NULL
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
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
}
