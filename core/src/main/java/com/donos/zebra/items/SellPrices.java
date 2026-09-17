package com.donos.zebra.items;

/**
 * Sell prices for ores and formula-derived resale for station-crafted gear.
 * Potions and other items are not sellable.
 */
public final class SellPrices {

    public static final int IRON_ORE_UNIT = 3;
    public static final int COPPER_ORE_UNIT = 4;
    public static final int CRAFTED_FLAT_BONUS = 2;

    private SellPrices() {
    }

    /**
     * @return sell price in Prata for one unit, or {@code -1} if not sellable
     */
    public static int unitSellPrice(ItemDefinition item) {
        if (item == null) {
            return -1;
        }
        if (item == ItemRegistry.IRON_ORE) {
            return IRON_ORE_UNIT;
        }
        if (item == ItemRegistry.COPPER_ORE) {
            return COPPER_ORE_UNIT;
        }
        CraftingRecipe recipe = recipeForResult(item);
        if (recipe != null) {
            return derivedCraftedResale(recipe);
        }
        return -1;
    }

    public static boolean isSellable(ItemDefinition item) {
        return unitSellPrice(item) > 0;
    }

    /**
     * Sum of (material qty × ore unit sell price) + {@link #CRAFTED_FLAT_BONUS}.
     */
    public static int derivedCraftedResale(CraftingRecipe recipe) {
        if (recipe == null) {
            return -1;
        }
        int total = CRAFTED_FLAT_BONUS;
        for (java.util.Map.Entry<ItemDefinition, Integer> entry : recipe.getCosts().entrySet()) {
            int unit = oreUnitPrice(entry.getKey());
            if (unit < 0) {
                return -1;
            }
            total += entry.getValue() * unit;
        }
        return total;
    }

    private static int oreUnitPrice(ItemDefinition material) {
        if (material == ItemRegistry.IRON_ORE) {
            return IRON_ORE_UNIT;
        }
        if (material == ItemRegistry.COPPER_ORE) {
            return COPPER_ORE_UNIT;
        }
        return -1;
    }

    public static CraftingRecipe recipeForResult(ItemDefinition result) {
        if (result == null) {
            return null;
        }
        for (CraftingRecipe recipe : CraftingRecipes.all()) {
            if (recipe.getResult() == result) {
                return recipe;
            }
        }
        return null;
    }

    /**
     * Sells one unit from inventory into the wallet.
     * @return false if item missing or not sellable
     */
    public static boolean trySellOne(ItemDefinition item, Inventory inventory, Wallet wallet) {
        if (!isSellable(item) || inventory == null || wallet == null) {
            return false;
        }
        if (!inventory.hasItemQuantity(item, 1)) {
            return false;
        }
        int price = unitSellPrice(item);
        if (!inventory.removeItem(item, 1)) {
            return false;
        }
        wallet.addSilver(price);
        return true;
    }
}
