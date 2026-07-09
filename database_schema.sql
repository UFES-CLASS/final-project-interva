-- Sambi Kopi POS SQLite database schema

CREATE TABLE IF NOT EXISTS menu_items (
    menu_name TEXT PRIMARY KEY,
    category TEXT NOT NULL,
    price TEXT NOT NULL,
    ingredients TEXT NOT NULL,
    status TEXT NOT NULL,
    image_path TEXT NOT NULL DEFAULT 'assets/menu/default.png'
);

CREATE TABLE IF NOT EXISTS inventory_items (
    product TEXT PRIMARY KEY,
    stock INTEGER NOT NULL,
    exp_date TEXT NOT NULL,
    status TEXT NOT NULL,
    notify_status TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS menu_ingredients (
    menu_name TEXT NOT NULL,
    stock_product TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    unit TEXT NOT NULL DEFAULT 'portion',
    PRIMARY KEY(menu_name, stock_product),
    FOREIGN KEY(menu_name) REFERENCES menu_items(menu_name) ON DELETE CASCADE,
    FOREIGN KEY(stock_product) REFERENCES inventory_items(product) ON UPDATE CASCADE
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
