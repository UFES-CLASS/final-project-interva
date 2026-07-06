# Sambi Kopi POS

Sambi Kopi POS is a JavaFX desktop application designed to support cafe operations.  
The system provides role-based features for **Barista**, **Cashier**, and **Owner**.

## Team Members

- Daniel Widison Yeoh - 25523047
- Faizah Nabila Azzahra - 25523189
- Muhammad Malik Firdaus - 25523002
- Madina Nurbaiti - 25523224
- Xabbi Alghifari Agratama - 25523211

## Project Description

Sambi Kopi POS is a cafe management system built using JavaFX and SQLite.  
The application helps manage menu items, cashier orders, stock notifications, owner menu review, and simple business reports.

The system is divided into three main roles:

### Barista
The Barista can manage menu data, check stock, notify the owner about low stock, and process incoming orders from the cashier.

Main features:
- Add new menu items
- View menu list
- Delete menu items
- Check stock
- Notify owner about stock issues
- View and process cashier orders

### Cashier
The Cashier can create customer orders, select menu items, process payment, and send orders to the Barista page.

Main features:
- Input customer name
- Select approved menu items
- Filter menu by category
- Receive payment
- Send orders to Barista
- View cashier order history

### Owner
The Owner can review stock notifications, approve or reject new menu submissions, and view reports with charts.

Main features:
- Login security Owner tab
- Review stock notifications
- Review new menu submissions
- Approve or reject menu items
- View report charts

## Technologies Used

- Java
- JavaFX
- FXML
- Maven
- SQLite
- Scene Builder

## How to Run
Make sure Java and Maven are installed on your computer.
Open the project folder in terminal, then run:

```bash
mvn clean javafx:run
```

# Database
This project uses SQLite as the database.
The database file is created locally when the application runs.
Database-related files:

```bash
sambi_kopi.db
database_schema.sql
DatabaseManager.java
```

# Notes
This project is still in development.
Some data and features can still be improved, such as user authentication, advanced reports, receipt printing, and persistent login system.