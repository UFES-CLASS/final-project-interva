````md
# Sambi Kopi POS - Code Explanation Documentation

This document explains the main code structure, important files, database logic, OOP concepts, and possible questions that may be asked during the project presentation.

---

## 1. Project Overview

Sambi Kopi POS is a JavaFX desktop application designed to support simple cafe operations.

The application is divided into three role-based tabs:

- Barista
- Cashier
- Owner

Each role has different responsibilities and features. The application uses JavaFX for the user interface, FXML for layout design, Maven for dependency management, and SQLite as the local database.

---

## 2. Technologies Used

This project uses:

- Java
- JavaFX
- FXML
- Maven
- SQLite
- Scene Builder

---

## 3. Project Structure

Main Java package:

```text
src/main/java/interva/sambikopi
````

Main folders:

```text
controller/
model/
database/
```

FXML files are stored in:

```text
src/main/resources/interva/sambikopi
```

Menu images are stored in:

```text
assets/menu/
```

---

## 4. Important Files

### App.java

`App.java` is the main entry point of the JavaFX application.

It extends the JavaFX `Application` class:

```java
public class App extends Application
```

The `Application` class comes from JavaFX:

```java
import javafx.application.Application;
```

The important method is:

```java
@Override
public void start(Stage stage) throws Exception
```

This method is called automatically when the JavaFX application starts. It is used to load the main FXML file and show the application window.

---

### MainController.java

`MainController.java` controls the main role navigation between:

* Barista
* Cashier
* Owner

It also handles Owner login.

The default Owner password is stored in this file:

```java
private static final String OWNER_PASSWORD = "owner123";
```

If the wrong password is entered, the application shows:

```text
Wrong password, Please try again
```

Owner login can also be submitted using the ENTER key.

---

### DatabaseManager.java

`DatabaseManager.java` is responsible for database setup and database connection.

It is located in:

```text
src/main/java/interva/sambikopi/database/DatabaseManager.java
```

The class uses SQLite as the local database:

```java
private static final String DB_URL = "jdbc:sqlite:sambi_kopi.db";
```

This means the database file is stored locally as:

```text
sambi_kopi.db
```

---

## 5. DatabaseManager.java Explanation

### Package

```java
package interva.sambikopi.database;
```

This shows that `DatabaseManager` belongs to the `database` package.

---

### Imports

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
```

These imports are used to connect Java with SQLite and execute SQL commands.

---

### Final Class

```java
public final class DatabaseManager
```

The class is declared as `final`, which means it cannot be inherited by another class.

This is suitable because `DatabaseManager` is used as a utility class for database connection and setup.

---

### Private Constructor

```java
private DatabaseManager() {}
```

The constructor is private so no object can be created from this class.

This means the class is intended to be used through static methods only.

Example:

```java
DatabaseManager.initializeDatabase();
DatabaseManager.getConnection();
```

---

### Database URL

```java
private static final String DB_URL = "jdbc:sqlite:sambi_kopi.db";
```

This line defines the database location.

The database used is SQLite, and the file name is:

```text
sambi_kopi.db
```

---

### getConnection Method

```java
public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(DB_URL);
}
```

This method creates and returns a connection to the SQLite database.

Other classes can call this method whenever they need to access the database.

---

### initializeDatabase Method

```java
public static void initializeDatabase()
```

This method initializes the database when the application starts.

It does several things:

* Loads the SQLite JDBC driver
* Opens a database connection
* Enables foreign key support
* Creates database tables if they do not exist
* Adds missing columns if the database structure is updated

---

### Loading SQLite Driver

```java
Class.forName("org.sqlite.JDBC");
```

This line loads the SQLite JDBC driver so Java can communicate with the SQLite database.

---

### Try-With-Resources

```java
try (Connection connection = getConnection(); Statement statement = connection.createStatement())
```

This uses try-with-resources.

The benefit is that `Connection` and `Statement` will be closed automatically after they are used.

---

### Enable Foreign Keys

```java
statement.execute("PRAGMA foreign_keys = ON");
```

This enables foreign key support in SQLite.

It is important because some tables are connected to each other, such as:

* `menu_items`
* `inventory_items`
* `menu_ingredients`

---

## 6. Database Tables

### menu_items Table

```sql
CREATE TABLE IF NOT EXISTS menu_items (
    menu_name TEXT PRIMARY KEY,
    category TEXT NOT NULL,
    price TEXT NOT NULL,
    ingredients TEXT NOT NULL,
    status TEXT NOT NULL,
    image_path TEXT NOT NULL DEFAULT 'assets/menu/default.png'
)
```

This table stores menu data.

Columns:

* `menu_name` stores the menu name and acts as the primary key
* `category` stores the menu category
* `price` stores the menu price
* `ingredients` stores ingredient information
* `status` stores menu status such as Pending Approval or Approved
* `image_path` stores the image path for the menu photo

The `image_path` column allows the Cashier Menu page to display menu images.

---

### inventory_items Table

```sql
CREATE TABLE IF NOT EXISTS inventory_items (
    product TEXT PRIMARY KEY,
    stock INTEGER NOT NULL,
    exp_date TEXT NOT NULL,
    status TEXT NOT NULL,
    notify_status TEXT NOT NULL
)
```

This table stores stock or inventory data.

Columns:

* `product` stores the stock product name
* `stock` stores the stock amount
* `exp_date` stores the expiration date
* `status` stores the stock status, such as Available, Low Stock, or Critical
* `notify_status` stores whether the stock has been notified to the Owner

---

### menu_ingredients Table

```sql
CREATE TABLE IF NOT EXISTS menu_ingredients (
    menu_name TEXT NOT NULL,
    stock_product TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    unit TEXT NOT NULL DEFAULT 'portion',
    PRIMARY KEY(menu_name, stock_product),
    FOREIGN KEY(menu_name) REFERENCES menu_items(menu_name) ON DELETE CASCADE,
    FOREIGN KEY(stock_product) REFERENCES inventory_items(product) ON UPDATE CASCADE
)
```

This table connects menu items with stock ingredients.

Columns:

* `menu_name` stores the menu name
* `stock_product` stores the stock product used as an ingredient
* `quantity` stores how much stock is needed
* `unit` stores the unit, such as portion, gram, ml, pcs, scoop, or bottle

This table is important because it connects the menu system with the stock system.

Example:

```text
Americano
- Coffee Beans x1 scoop
- Water x1 portion
```

When an order is completed, the system can reduce stock based on the ingredients stored in this table.

---

### orders Table

```sql
CREATE TABLE IF NOT EXISTS orders (
    order_id TEXT PRIMARY KEY,
    customer TEXT NOT NULL,
    product TEXT NOT NULL,
    creation_date TEXT NOT NULL,
    status TEXT NOT NULL,
    payment_method TEXT NOT NULL,
    total_amount INTEGER NOT NULL
)
```

This table stores cashier orders.

Columns:

* `order_id` stores the unique order ID
* `customer` stores the customer name
* `product` stores the ordered menu item
* `creation_date` stores the order date
* `status` stores the order status
* `payment_method` stores the payment method
* `total_amount` stores the order total

---

### app_settings Table

```sql
CREATE TABLE IF NOT EXISTS app_settings (
    setting_key TEXT PRIMARY KEY,
    setting_value TEXT NOT NULL
)
```

This table stores application settings.

It can be used to save small configuration values for the application.

---

## 7. addColumnIfMissing Method

```java
private static void addColumnIfMissing(Connection connection, String tableName, String columnName, String columnDefinition) throws SQLException
```

This method checks whether a column already exists in a table.

If the column does not exist, it adds the column using:

```sql
ALTER TABLE
```

This is useful when the database structure changes, but the old database file already exists.

Example:

```java
addColumnIfMissing(connection, "menu_items", "image_path", "TEXT NOT NULL DEFAULT 'assets/menu/default.png'");
addColumnIfMissing(connection, "menu_ingredients", "unit", "TEXT NOT NULL DEFAULT 'portion'");
```

These lines make sure the database has the latest required columns.

---

## 8. Why addColumnIfMissing Is Needed

If the application already has an old `sambi_kopi.db` file, new columns will not automatically appear just because the Java code has changed.

The `addColumnIfMissing` method solves this by checking and updating the table structure.

For example:

* If `image_path` does not exist in `menu_items`, it will be added
* If `unit` does not exist in `menu_ingredients`, it will be added

This helps prevent database errors when the project is updated.

---

## 9. Barista Features

### Menu Page

The Barista Menu page is used to submit new menu items.

Features:

* Add new menu item
* Upload menu photo
* Select category
* Input price
* Select ingredients from stock
* Set ingredient quantity and unit
* Submit menu to Owner Review
* Automatically format input text using title case

New menu items must be approved by the Owner before they appear in the Cashier Menu.

---

### Menu List Page

The Menu List page displays menu data.

Features:

* View menu items in TableView
* Search menu items
* Delete selected menu item
* Delete all menu items
* View menu status

---

### Stock Page

The Stock page is used to manage inventory.

Features:

* View stock data
* Add new stock
* Edit stock
* Select expiration date using DatePicker
* Notify Owner about low or critical stock

The Notify Owner button sends stock notification data to the Owner.

---

### Orders Page

The Orders page is used to manage orders from the Cashier.

Features:

* View incoming orders
* Assign order
* Complete order
* Cancel order
* Search orders
* Refresh order data

When an order is completed, the stock will be reduced based on the required ingredients.

---

## 10. Cashier Features

### Cashier Menu Page

The Cashier Menu page is used to create customer orders.

Features:

* Input customer name
* View approved menu items only
* View menu images
* Filter menu by category
* Add menu item to current order
* Select payment method
* Receive payment
* Send order to Barista Orders

Only approved menus can appear in the Cashier Menu.

---

### Cashier Orders Page

The Cashier Orders page displays cashier order history.

Features:

* View customer name
* View ordered product
* View order status
* View payment method

---

## 11. Owner Features

### Owner Login

The Owner tab requires a password before accessing Owner features.

Default password:

```text
owner123
```

Wrong password message:

```text
Wrong password, Please try again
```

---

### Review Stock Page

The Owner can review stock notifications from the Barista.

Features:

* View stock notifications
* Mark stock notifications as reviewed

---

### Review New Menu Page

The Owner can review new menu submissions.

Features:

* View submitted menu items
* Approve menu item
* Reject menu item

If the Owner approves a menu, it appears in the Cashier Menu.

If the Owner rejects a menu, it does not appear in the Cashier Menu.

---

### View Report Page

The Owner Report page displays report data using charts.

Features:

* View total orders
* View total sales
* View approved menus
* View low stock items
* View order status chart
* View payment method chart

---

## 12. CRUD Explanation

The project implements CRUD operations.

### Create

Examples:

* Add new menu item
* Add new stock item
* Create cashier order
* Create stock notification

### Read

Examples:

* Display menu data in TableView
* Display stock data in TableView
* Display orders in TableView
* Display owner review data
* Display report data

### Update

Examples:

* Approve menu item
* Reject menu item
* Update order status
* Mark stock notification as reviewed
* Edit stock data
* Reduce stock after order completion

### Delete

Examples:

* Delete selected menu item
* Delete all menu items

---

## 13. OOP Concept Highlight

### Inheritance

Inheritance is used in `App.java`.

```java
public class App extends Application
```

`App` inherits from JavaFX `Application`.

This is required because JavaFX applications must extend the `Application` class to start the JavaFX lifecycle.

---

### Method Overriding

Method overriding is used in `App.java`.

```java
@Override
public void start(Stage stage) throws Exception
```

The `start()` method comes from JavaFX `Application`.

The project overrides this method to define what happens when the application starts.

---

### Method Overloading

In the current version, method overloading is not a main concept used in the project.

Most methods use different names based on their function, such as:

* add menu
* update order status
* approve menu
* reject menu
* save stock notification

Method overloading is not strongly used in this project because most methods have different responsibilities. I used specific method names such as add menu, add stock, update order status, approve menu, and reject menu to make the code easier to read and maintain.

---

### Encapsulation

Encapsulation is used in model classes.

Examples:

* CafeMenuItem
* InventoryItem
* OrderItem
* MenuIngredient

Model classes usually store data in private fields and provide getter and setter methods.

Example:

```java
private String menuName;

public String getMenuName() {
    return menuName;
}

public void setMenuName(String menuName) {
    this.menuName = menuName;
}
```

Encapsulation helps protect data and makes the code easier to maintain.

---

### Abstraction

Abstraction is used in `DatabaseManager.java`.

Controllers do not need to directly know the full database setup process.

They can use simple methods such as:

```java
DatabaseManager.getConnection();
DatabaseManager.initializeDatabase();
```

The database connection and table creation details are hidden inside `DatabaseManager`.

---

### Polymorphism

Polymorphism appears through JavaFX UI components and event handling.

Examples of JavaFX components:

* Button
* TextField
* ComboBox
* TableView
* DatePicker
* Chart

Different components can respond to events using the JavaFX event system.

Example:

```java
button.setOnAction(event -> handleAction());
```

---

### Object Composition

Object composition is used in the menu ingredient system.

A menu item can have multiple ingredients.

Example:

```text
Matcha Latte
- Matcha Powder x1 scoop
- Fresh Milk x1 bottle
```

The relationship between menu and stock ingredients is stored in:

```text
menu_ingredients
```

This means a menu item is composed of several stock items.

---

## 14. MVC Pattern

The project follows the MVC pattern.

### Model

Model classes represent application data.

Examples:

* CafeMenuItem
* InventoryItem
* OrderItem
* MenuIngredient

### View

FXML files represent the user interface.

Examples:

* menu.fxml
* menu_list.fxml
* stock.fxml
* orders.fxml
* cashier.fxml
* owner_report.fxml

### Controller

Controller classes connect the UI with application logic.

Examples:

* MainController.java
* MenuController.java
* MenuListController.java
* StockController.java
* OrdersController.java
* CashierController.java

The controller receives user input from the UI, processes the logic, and communicates with the database.

---

## 15. Data Structure Explanation

The project uses data structures such as:

```java
ObservableList
ArrayList
List
```

These are used to store and display data in JavaFX.

Examples:

* menu items
* stock items
* order data
* ingredient lists
* TableView data

`ObservableList` is useful because it works well with JavaFX TableView.

---

## 16. TableView Explanation

The project uses TableView to display database data in rows and columns.

Examples:

* Menu List table
* Stock table
* Orders table
* Owner Review Stock table
* Owner Review New Menu table

TableView is useful because the data is easier to read, search, and manage.

---

## 17. Chart Explanation

The Owner Report page uses charts to visualize data.

Examples:

* Order status chart
* Payment method chart

Charts help users understand report data more quickly.

---

## 18. Menu Image Explanation

Each menu item can have an image.

Images are stored in:

```text
assets/menu/
```

The database stores the image path in the `image_path` column.

This is better than storing the image directly in the database because it keeps the database smaller and easier to manage.

---

## 19. Stock and Ingredient Logic

Ingredients are connected to stock.

When the Barista creates a menu item, ingredients can be selected from existing stock data.

Each ingredient has:

* stock item
* quantity
* unit

When the Barista completes an order, the system checks whether the stock is enough.

If stock is enough:

* the order becomes Complete
* stock is reduced

If stock is not enough:

* the order cannot be completed
* the system shows an error message

---

## 20. Responsive UI

The UI is designed to be flexible for different window sizes.

Examples:

* Cashier menu cards adjust to screen width
* TableView expands in fullscreen
* search fields can grow
* form layout adjusts to window size
* role tabs are compact

---

## 21. Possible Presentation Questions

### Q: What is the main purpose of this application?

A: To help manage cafe operations such as menu management, cashier ordering, stock monitoring, owner review, and reports.

### Q: What roles are available?

A: Barista, Cashier, and Owner.

### Q: What database is used?

A: SQLite.

### Q: What is the function of DatabaseManager?

A: It manages database connection, creates tables, enables foreign keys, and updates missing database columns.

### Q: What happens if the database file does not exist?

A: The application will create `sambi_kopi.db` automatically.

### Q: What happens if an old database is missing a new column?

A: The `addColumnIfMissing` method checks the table and adds the missing column.

### Q: What happens when a new menu is added?

A: It is submitted to Owner Review. If the Owner approves it, it appears in the Cashier Menu.

### Q: What happens when an order is completed?

A: The order status becomes Complete and the required stock ingredients are reduced.

### Q: What happens if stock is not enough?

A: The order cannot be completed and the application shows an error message.

### Q: Where is inheritance used?

A: In `App.java`, where `App` extends JavaFX `Application`.

### Q: Where is overriding used?

A: In `App.java`, where the `start(Stage stage)` method overrides the method from `Application`.

### Q: Is overloading used?

A: In the current final version, overloading is not a main concept used. The project focuses more on inheritance, overriding, encapsulation, abstraction, composition, MVC, CRUD, database, TableView, and Chart.

### Q: Why use SQLite?

A: SQLite is lightweight, local, easy to use, and suitable for a desktop application.

---

## 22. Summary

Sambi Kopi POS is a JavaFX and SQLite-based cafe management system with three roles:

* Barista
* Cashier
* Owner

The project includes:

* TableView
* Chart
* SQLite database
* CRUD operations
* data structures
* role-based UI
* stock management
* menu approval system
* cashier order workflow
* owner report dashboard
* menu image support
* ingredient and stock connection
* OOP concepts such as inheritance, overriding, encapsulation, abstraction, polymorphism, composition, and MVC