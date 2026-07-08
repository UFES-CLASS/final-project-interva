# Sambi Kopi POS

Sambi Kopi POS is a JavaFX desktop application for simple cafe operations. The application is separated into three role tabs: **Barista**, **Cashier**, and **Owner**.

## How to Run

```bash
mvn clean javafx:run
```

## Database

This version uses **SQLite** as the local database.

When the application runs for the first time, it will automatically create this database file in the project folder:

```text
sambi_kopi.db
```

The database stores:

- Menu items
- Menu photo paths
- Menu ingredient requirements
- Inventory / stock data
- Cashier orders
- Owner menu review data
- Owner stock notification data

Because SQLite is used locally, the data will remain saved even after the application is closed and opened again.

## Owner Login

Open the Owner tab and use this default password:

```text
owner123
```

The password can be changed in:

```text
src/main/java/interva/sambikopi/controller/MainController.java
```

## Barista Features

- Add new menu items
- Upload menu photos from local files
- Choose menu ingredients from Stock
- Set ingredient quantity requirements for each menu item
- Send saved new menu items to Owner Review
- View Menu List
- Delete selected menu item
- Delete all menu items with confirmation
- Check stock from the Stock page
- Add stock data with DatePicker expiration date
- Notify Owner about low or critical stock
- View cashier orders
- Assign, complete, or cancel orders
- Automatically reduce linked stock when an order is completed

## Cashier Features

- Create customer orders
- Input customer name
- View approved menu items with photos
- Select approved menu items
- Filter menu items by category
- Select payment method
- Receive payment
- Send confirmed orders to Barista Orders
- View cashier order history

## Owner Features

- Login-required Owner tab
- Review stock notifications from Barista
- Mark stock notifications as reviewed
- Review new menu requests from Barista
- Approve or reject new menu items
- View report dashboard with charts
- See order status chart and payment method chart

## New Updates

- Added menu image support
- Added `assets/menu` folder for menu photos
- Added image path storage in SQLite database
- Added menu photo upload in Barista Menu
- Added stock-based ingredient selection in Barista Menu
- Added `menu_ingredients` database table
- Linked menu ingredients to inventory stock data
- Completing an order now deducts stock based on the ordered menu ingredients
- Cashier Menu now displays menu photos

## Notes

The application uses a local SQLite database file. If you delete `sambi_kopi.db`, the application will create a new database and insert the default sample data again on the next run.
