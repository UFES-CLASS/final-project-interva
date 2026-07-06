Run:
mvn clean javafx:run

======================================================

## Sambi Kopi POS

Sambi Kopi POS is a JavaFX desktop application for managing cafe menu data, menu lists, inventory, and customer orders. The project uses Java, JavaFX, Maven, FXML, and Scene Builder.

## Available Features

### Menu
The Menu page is used to input and save new menu data.

Features:
- Add new menu item
- Input menu name, category, price, and ingredients
- Validate menu data before saving
- Save menu data to be displayed on the Menu List page
- Price format uses IDR/Rupiah

### Menu List
The Menu List page is used to display all saved menu items.

Features:
- View saved menu data in a table
- Search menu items
- Refresh menu data
- Display menu status such as Approved and Pending Approval

### Inventory
The Inventory page is used to manage stock data.

Features:
- View inventory items in a table
- Search inventory data
- Add stock
- Check stock condition
- Notify owner
- Display stock status such as Available, Low Stock, and Critical

### Orders
The Orders page is used to manage customer orders.

Features:
- View order data in a table
- Search orders
- Add or accept new order
- Reject incoming order
- Assign an order
- Complete an order
- Cancel an order
- Refresh order data

## New Updates

- Added a new Menu List page.
- The Menu page is now focused only on menu input.
- Saved menu data is displayed on the Menu List page.
- The Orders button now works as Add Order / Accept Order.
- The order panel is used for accepting or rejecting incoming orders.
- Complete, Assign, and Cancel buttons are now used only for existing orders.
- Package and folder structure changed to `interva.sambikopi`.

## Under Development

The Dashboard page is still under development.

## Notes

This version still uses dummy/in-memory data, so the data will reset when the application is restarted.