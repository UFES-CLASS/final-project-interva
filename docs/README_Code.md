# Sambi Kopi POS - Code Explanation

This document explains the important code concepts that may be asked during presentation.

## 1. Main Entry Point

The main entry point is:

```text
src/main/java/interva/sambikopi/App.java
```

`App.java` extends JavaFX `Application`:

```java
public class App extends Application
```

This is required by JavaFX. The `start(Stage stage)` method is called when the application starts.

## 2. Core Inheritance

Inheritance is applied in the core model layer, not only in JavaFX.

Parent class:

```text
src/main/java/interva/sambikopi/model/CafeItem.java
```

Child classes:

```text
src/main/java/interva/sambikopi/model/CafeMenuItem.java
src/main/java/interva/sambikopi/model/InventoryItem.java
```

Relationship:

```text
CafeItem
├── CafeMenuItem
└── InventoryItem
```

`CafeItem` contains common item concepts such as item name and status.

`CafeMenuItem` represents menu data, while `InventoryItem` represents stock data.

## 3. Inheritance Example

```java
public class CafeMenuItem extends CafeItem
```

```java
public class InventoryItem extends CafeItem
```

This means `CafeMenuItem` and `InventoryItem` inherit common behavior from `CafeItem`.

## 4. Overriding Example

Both child classes override methods from `CafeItem`.

Example in `CafeMenuItem`:

```java
@Override
public String getItemType() {
    return "Menu Item";
}
```

Example in `InventoryItem`:

```java
@Override
public String getItemType() {
    return "Inventory Item";
}
```

This shows method overriding because each child class gives its own implementation.

## 5. Overloading Example

Method overloading is used in:

```text
src/main/java/interva/sambikopi/model/SambiKopiDataStore.java
```

Example:

```java
public static void addMenuForReview(CafeMenuItem item)
```

and:

```java
public static void addMenuForReview(CafeMenuItem item, List<MenuIngredient> ingredients)
```

Both methods have the same name, but different parameters. This is method overloading.

## 6. Encapsulation

Encapsulation is applied in model classes.

Example:

```java
private String product;

public String getProduct() {
    return product;
}

public void setProduct(String product) {
    this.product = product;
}
```

Fields are private and accessed through getters and setters.

## 7. Abstraction

Abstraction is applied in `DatabaseManager.java`.

Controllers do not need to manually create database tables or open SQLite connections. They use database helper methods instead.

Example:

```java
DatabaseManager.initializeDatabase();
DatabaseManager.getConnection();
```

The SQL setup is hidden inside `DatabaseManager`.

## 8. Composition

Composition is used in menu ingredients.

A menu item can be composed of several stock ingredients.

Example:

```text
Matcha Latte
- Matcha Powder x1 scoop
- Fresh Milk x1 bottle
```

This relationship is stored in the `menu_ingredients` database table.

## 9. MVC Pattern

The project follows MVC:

### Model

Examples:

- `CafeItem`
- `CafeMenuItem`
- `InventoryItem`
- `OrderItem`
- `MenuIngredient`

### View

FXML files:

- `menu.fxml`
- `menu_list.fxml`
- `stock.fxml`
- `orders.fxml`
- `cashier_menu.fxml`
- `owner_report.fxml`

### Controller

Examples:

- `MainController.java`
- `MenuController.java`
- `MenuListController.java`
- `StockController.java`
- `OrdersController.java`
- `CashierMenuController.java`

Controllers connect user actions with model and database logic.

## 10. Database

The project uses SQLite.

Database file:

```text
sambi_kopi.db
```

Schema file:

```text
database_schema.sql
```

Important tables:

- `menu_items`
- `inventory_items`
- `menu_ingredients`
- `orders`
- `app_settings`

## 11. TableView

TableView is used to display database data.

Examples:

- Menu List table
- Stock table
- Orders table
- Owner Review Stock table
- Owner Review New Menu table

## 12. Chart

Owner Report uses charts to visualize report data, such as order status and payment methods.

## 13. Data Structures

The project uses:

- `ObservableList`
- `FilteredList`
- `List`
- `ArrayList`

These are used for storing and displaying menu, stock, order, and ingredient data.

## 14. Possible Questions

### Q: Where is inheritance used?

A: In the model layer. `CafeMenuItem` and `InventoryItem` inherit from `CafeItem`.

### Q: Where is overriding used?

A: In `CafeMenuItem` and `InventoryItem`, especially in `getItemType()`.

### Q: Where is overloading used?

A: In `SambiKopiDataStore.java`, where `addMenuForReview(...)` has two versions with different parameters.

### Q: Why is App extends Application not enough?

A: It is inheritance, but it comes from JavaFX framework. The project also applies inheritance in core model code through `CafeItem`.

### Q: What happens when an order is completed?

A: The order status changes to Complete and the linked stock ingredients are reduced.

### Q: Why does the Cashier menu only show approved menus?

A: New menu items must be approved by Owner before they can be sold.

### Q: Why store image path instead of image binary in the database?

A: It keeps the database smaller and easier to manage.
