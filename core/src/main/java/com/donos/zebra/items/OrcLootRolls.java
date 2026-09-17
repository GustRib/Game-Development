package com.donos.zebra.items;

import java.util.Random;

/**
 * Seedable rolls for orc combat loot quantities (1–3 inclusive per material).
 */
public final class OrcLootRolls {

    public static final int MIN_QTY = 1;
    public static final int MAX_QTY = 3;

    private OrcLootRolls() {
    }

    /** Inclusive roll in [{@link #MIN_QTY}, {@link #MAX_QTY}]. */
    public static int rollQuantity(Random random) {
        return MIN_QTY + random.nextInt(MAX_QTY - MIN_QTY + 1);
    }

    public static void fillDefaultOrcLoot(java.util.List<ItemStack> lootTable, Random random) {
        lootTable.clear();
        lootTable.add(new ItemStack(ItemRegistry.COPPER_ORE, rollQuantity(random)));
        lootTable.add(new ItemStack(ItemRegistry.IRON_ORE, rollQuantity(random)));
    }
}
