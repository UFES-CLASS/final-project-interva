File mapping:
- menu.fxml -> MenuController.java -> CafeMenuItem.java
- exp.fxml was renamed to inventory.fxml -> InventoryController.java -> InventoryItem.java
- App.java starts the project and allows sidebar navigation.
- pom.xml is ready for Maven + JavaFX.

How to use:
1. Run with: mvn javafx:run

Notes:
- All buttons in the two FXML files have fx:id and onAction.
- Search fields are functional at runtime.
- Data is still in-memory sample data, not database data.
- Inline FXML styles are used, no external CSS.

Fix_v2 notes:
- Sambi Kopi label is centered inside the left sidebar.
