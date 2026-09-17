package com.donos.zebra.items;

import java.util.Random;

/**
 * Seedable rolls for orc combat loot: iron 1–3 and silver 5–9 (no copper).
 */
public final class OrcLootRolls {

    public static final int MIN_QTY = 1;
    public static final int MAX_QTY = 3;
    public static final int MIN_SILVER = 5;
    public static final int MAX_SILVER = 9;

    private OrcLootRolls() {
    }

    /** Inclusive roll in [{@link #MIN_QTY}, {@link #MAX_QTY}]. */
    public static int rollQuantity(Random random) {
        return MIN_QTY + random.nextInt(MAX_QTY - MIN_QTY + 1);
    }

    /** Inclusive roll in [{@link #MIN_SILVER}, {@link #MAX_SILVER}]. */
    public static int rollSilver(Random random) {
        return MIN_SILVER + random.nextInt(MAX_SILVER - MIN_SILVER + 1);
    }

    /**
     * Fills material loot (iron only) and returns the silver drop for this kill.
     */
    public static int fillDefaultOrcLoot(java.util.List<ItemStack> lootTable, Random random) {
        lootTable.clear();
        lootTable.add(new ItemStack(ItemRegistry.IRON_ORE, rollQuantity(random)));
        return rollSilver(random);
    }
}
