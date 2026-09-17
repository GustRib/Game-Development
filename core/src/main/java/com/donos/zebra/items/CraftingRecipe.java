package com.donos.zebra.items;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One crafting recipe: result item plus exact material costs.
 */
public final class CraftingRecipe {

    private final String id;
    private final String displayName;
    private final ItemDefinition result;
    private final Map<ItemDefinition, Integer> costs;

    public CraftingRecipe(String id, String displayName, ItemDefinition result,
                          Map<ItemDefinition, Integer> costs) {
        this.id = id;
        this.displayName = displayName;
        this.result = result;
        this.costs = Collections.unmodifiableMap(new LinkedHashMap<>(costs));
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ItemDefinition getResult() {
        return result;
    }

    public Map<ItemDefinition, Integer> getCosts() {
        return costs;
    }

    public boolean canAfford(Inventory inventory) {
        for (Map.Entry<ItemDefinition, Integer> entry : costs.entrySet()) {
            if (!inventory.hasItemQuantity(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Consumes materials only (no result). Used when a delayed craft starts.
     */
    public boolean consumeMaterials(Inventory inventory) {
        if (!canAfford(inventory)) {
            return false;
        }
        for (Map.Entry<ItemDefinition, Integer> entry : costs.entrySet()) {
            inventory.removeItem(entry.getKey(), entry.getValue());
        }
        return true;
    }

    /**
     * Consumes materials and adds the result immediately.
     */
    public boolean craft(Inventory inventory) {
        if (!consumeMaterials(inventory)) {
            return false;
        }
        return inventory.addItem(result, 1);
    }
}
