package interva.sambikopi.model;
public abstract class CafeItem {

    private String itemName;
    private String itemStatus;

    protected CafeItem(String itemName, String itemStatus) {
        this.itemName = normalize(itemName);
        this.itemStatus = normalize(itemStatus);
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = normalize(itemName);
    }

    public String getStatus() {
        return itemStatus;
    }

    public void setStatus(String itemStatus) {
        this.itemStatus = normalize(itemStatus);
    }

    public boolean hasStatus(String expectedStatus) {
        return getStatus().equalsIgnoreCase(normalize(expectedStatus));
    }

    public abstract String getItemType();

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
