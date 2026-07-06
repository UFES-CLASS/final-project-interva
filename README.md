# Sambi Kopi POS

Sambi Kopi POS is a JavaFX desktop application designed to support cafe operations.  
The system is divided into three role-based tabs: **Barista**, **Cashier**, and **Owner**.

Each role has different pages and features based on its responsibility.

---

## Tech Stack

This project is built using:

- Java
- JavaFX
- FXML
- Maven
- Scene Builder

---

## How to Run

Run the project using Maven:

mvn clean javafx:run

==============================================================================

## Application Roles

# Barista

The Barista role is used to manage menu data, stock data, and incoming customer orders.

Available pages:

- Menu
- Menu List
- Stock
- Orders

# Cashier

The Cashier role is used to create customer orders and process payments.

Available pages:

- Menu
- Orders
- Report

# Owner

The Owner role is used to review stock notifications, review new menu submissions, and view reports.

Available pages:

- Review Stock
- Review New Menu
- View Report

The Owner tab requires a password before it can be accessed.

Default owner password:

owner123

## Barista Features

# Menu Page

The Menu page is used by the barista to submit new menu items.

Features:

- Add new menu item
- Input menu name, category, price, and ingredients
- Validate empty fields
- Submit new menu data to Owner Review
- Price format uses IDR/Rupiah
- Available categories: Coffee, Non-Coffee, and Pastry

New menu items will not appear directly in the Cashier Menu.
They must be approved by the Owner first.

# Menu List Page

The Menu List page displays saved menu data.

Features:

- View menu items in a table
- Search menu items
- Refresh menu data
- Delete selected menu item
- Delete all menu items with confirmation
- Display menu status such as Approved, Pending Approval, and Rejected

# Stock Page

The Stock page is used to check and manage inventory data.

Features:

- View stock data in a table
- Search stock items
- Refresh stock data
- Check stock condition
- Notify Owner about low or critical stock

Stock notifications sent by the barista will appear in the Owner Review Stock page.

# Orders Page

The Orders page is used to handle orders sent from the cashier.

Features:

- View incoming cashier orders
- Search orders
- Assign order
- Complete order
- Cancel order
- Refresh order data
- Display customer name, product, order date, and order status
- Cashier Features

# Menu Page

The Cashier Menu page is used to create customer orders.

Features:

- Input customer name
- View approved menu items only
- Filter menu items by category
- Add selected menu items to the current order
- Select payment method
- Receive payment
- Send confirmed orders to Barista Orders

Available filters:

- All
- Coffee
- Non-Coffee
- Pastry

Only menu items approved by the Owner will appear in the Cashier Menu.

# Orders Page

The Cashier Orders page displays orders created by the cashier.

Features:

- View cashier order history
- Display customer name
- Display ordered items
- Track order status

# Report Page

The Cashier Report page is prepared for cashier-related reports.

==============================================================================

The Owner tab requires a password before accessing owner features.

Default password:

owner123

The password can be changed in:

src/main/java/interva/sambikopi/controller/MainController.java

# Review Stock Page

The Review Stock page displays stock notifications sent by the barista.

Features:

- View stock notifications
- Review low or critical stock reports
- Mark stock notification as reviewed
- Review New Menu Page

The Review New Menu page is used to review new menu submissions from the barista.

Features:

- View submitted menu items
- Approve new menu item
- Reject new menu item

If a menu item is approved, it will appear in the Cashier Menu page.
If a menu item is rejected, it will not appear in the Cashier Menu page.

# View Report Page

The View Report page displays simple business reports.

Features:

- View total orders
- View total sales
- View pending menu submissions
- View stock notifications
- Display report data using charts

## New Updates

This version includes several updates:

Added Owner tab as the third role
Added password protection for Owner access
Added Barista Stock page
Added stock notification feature from Barista to Owner
Added Owner Review Stock page
Added Owner Review New Menu page
Added Owner Report page with charts
New menu submissions from Barista now require Owner approval
Approved menus automatically appear in Cashier Menu
Rejected menus do not appear in Cashier Menu
Cashier Menu now only displays approved menu items

This version still uses dummy or in-memory data.

Because of that:

Data will reset when the application is closed
Orders are only stored while the application is running
Menu approval data is not permanently saved yet
Stock notifications are not permanently saved yet
Future Improvements

## Project Status

Sambi Kopi POS is currently in development.

The main workflows for Barista, Cashier, and Owner are already available, while database storage, advanced reports, and authentication can be improved in future versions.