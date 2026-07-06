-- Sambi Kopi POS SQLite database schema

CREATE TABLE IF NOT EXISTS menu_items (
    menu_name TEXT PRIMARY KEY,
    category TEXT NOT NULL,
    price TEXT NOT NULL,
    ingredients TEXT NOT NULL,
    status TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS inventory_items (
    product TEXT PRIMARY KEY,
    stock INTEGER NOT NULL,
    exp_date TEXT NOT NULL,
    status TEXT NOT NULL,
    notify_status TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS orders (
    order_id TEXT PRIMARY KEY,
    customer TEXT NOT NULL,
    product TEXT NOT NULL,
    creation_date TEXT NOT NULL,
    status TEXT NOT NULL,
    payment_method TEXT NOT NULL,
    total_amount INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS app_settings (
    setting_key TEXT PRIMARY KEY,
    setting_value TEXT NOT NULL
);
