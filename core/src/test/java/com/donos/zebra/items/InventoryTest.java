package com.donos.zebra.items;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryTest {

    @Test
    void addItemStacksCopperOre() {
        Inventory inventory = new Inventory(5);
        assertTrue(inventory.addItem(ItemRegistry.COPPER_ORE, 3));
        assertTrue(inventory.addItem(ItemRegistry.COPPER_ORE, 2));
        assertEquals(5, inventory.getItemCount(ItemRegistry.COPPER_ORE));
        assertNotNull(inventory.getStackAt(0));
        assertEquals(5, inventory.getStackAt(0).getQuantity());
    }

    @Test
    void removeItemConsumesStacks() {
        Inventory inventory = new Inventory(5);
        inventory.addItem(ItemRegistry.COPPER_ORE, 5);
        assertTrue(inventory.removeItem(ItemRegistry.COPPER_ORE, 3));
        assertEquals(2, inventory.getItemCount(ItemRegistry.COPPER_ORE));
        assertFalse(inventory.removeItem(ItemRegistry.COPPER_ORE, 10));
    }

    @Test
    void nonStackableWeaponUsesOwnSlot() {
        Inventory inventory = new Inventory(5);
        assertTrue(inventory.addItem(ItemRegistry.IRON_SWORD, 1));
        assertTrue(inventory.addItem(ItemRegistry.IRON_SWORD, 1));
        assertEquals(2, inventory.getItemCount(ItemRegistry.IRON_SWORD));
        assertNotNull(inventory.getStackAt(0));
        assertNotNull(inventory.getStackAt(1));
    }

    @Test
    void itemRegistryReturnsRegisteredDefinitions() {
        assertNotNull(ItemRegistry.getItem("copper_ore"));
        assertEquals(ItemRegistry.COPPER_ORE, ItemRegistry.getItem("copper_ore"));
        assertEquals(ItemType.RESOURCE, ItemRegistry.COPPER_ORE.getType());
    }
}
