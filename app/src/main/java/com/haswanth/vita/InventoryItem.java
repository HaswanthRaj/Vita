package com.haswanth.vita;

public class InventoryItem {
    private String id;
    private String name;
    private String quantity;

    public InventoryItem() { } // Default constructor for Firebase

    public InventoryItem(String id, String name, String quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getQuantity() { return quantity; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setQuantity(String quantity) { this.quantity = quantity; }
}
