# Sambi Kopi POS

Sambi Kopi POS is a JavaFX desktop application for simple cafe operations. The application is separated into three compact role tabs: **Barista**, **Cashier**, and **Owner**.

## Team member

- Daniel Widison Yeoh - 25523047
- Faizah Nabila Azzahra - 25523189
- Muhammad Malik Firdaus - 25523002
- Madina Nurbaiti - 25523224
- Xabbi Alghifari Agratama - 25523211

## How to Run

```bash
mvn javafx:run
```

## Database

This project uses **SQLite** as the local database. When the application runs for the first time, it will create this database file in the project folder:

```text
sambi_kopi.db
```

The database stores menu data, menu photos, ingredient links, stock data, cashier orders, owner menu reviews, and stock notifications. If `sambi_kopi.db` is deleted, the application will create a new database and insert default sample data again.

## Owner Login

Default Owner password:

```text
owner123
```

The password can be changed in:

```text
src/main/java/interva/sambikopi/controller/MainController.java
```

If the wrong password is entered, the application displays:

```text
Wrong password, Please try again
```

## Main Features

### Barista

- Add new menu items and upload menu photos
- Select menu ingredients from Stock
- Add ingredient quantity and unit, such as `1 portion`, `100 gram`, or `250 ml`
- Send new menu items to Owner Review
- View, search, edit, and delete Menu List data
- Delete all menu items with confirmation
- Check, add, and edit Stock data
- Select stock expiration date using DatePicker
- Notify Owner about low or critical stock
- Manage cashier orders
- Assign, complete, or cancel orders
- Automatically reduce linked stock ingredients when an order is completed
- Prevent completing an order if the required stock is not enough

### Screenshot
![images](https://i.postimg.cc/FRNfxs1m/Screenshot-2026-07-08-083226.png)

### Cashier

- Input customer name
- View approved menu items with menu photos
- Filter menu by category
- Select approved menu items
- Select payment method
- Receive payment
- Show order confirmation / receipt popup
- Send confirmed orders to Barista Orders
- View cashier order history

### Screenshot
![images](https://i.postimg.cc/PrPCy7f2/Screenshot-2026-07-08-083656.png)

### Owner

- Login-protected Owner tab
- Review stock notifications from Barista
- Mark stock notifications as reviewed
- Review new menu submissions
- Approve or reject new menu items
- View report dashboard with summary cards and charts
- See order status chart and payment method chart

### Screenshot
![images](https://i.postimg.cc/QMkCVGmR/Screenshot-2026-07-08-083801.png)

## Data Structure Usage

This project uses JavaFX data structures such as `ObservableList` and `FilteredList` to store, display, and filter menu, stock, order, and review data in the UI.

## New Updates

- Added menu photo support for Cashier Menu
- Added image path storage in the database
- Added `assets/menu/` folder for menu photos
- Added photo upload in Barista Menu
- Added ingredient-to-stock linking system
- Added ingredient unit support
- Added automatic stock deduction when Barista completes an order
- Added stock validation before completing an order
- Added Edit Menu feature
- Added Edit Stock feature
- Added more Owner Report summary cards
- Added `.gitignore` to exclude compiled files, IDE settings, and local database files
- Updated menu card image display to stretch and fill the image area
- Updated IDR price format, for example `Rp 10.000`
- Updated flexible screen
- Added core inheritance using `CafeItem`, `CafeMenuItem`, and `InventoryItem`
- Updated Edit Stock rename function
- Updated Queue in Barista -> Order
- Updated images sources when upload photo will not visible

## Notes

The application uses a local SQLite database. The `.gitignore` excludes `.db` files, so GitHub should store the source code and `database_schema.sql`, not the local database file.