package com.donos.zebra.save;

/**
 * Serializable inventory/loot stack (stable item id + quantity).
 */
public class ItemStackSaveData {

    public String itemId;
    public int quantity;

    public ItemStackSaveData() {
    }

    public ItemStackSaveData(String itemId, int quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }
}
