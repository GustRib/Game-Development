package com.donos.zebra.items;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrcLootRollsTest {

    @Test
    void ironAndSilverRollInRangeWithNoCopperAcrossManyTrials() {
        Random random = new Random(42L);
        Set<Integer> seenIron = new HashSet<>();
        Set<Integer> seenSilver = new HashSet<>();

        for (int i = 0; i < 200; i++) {
            java.util.ArrayList<ItemStack> loot = new java.util.ArrayList<>();
            int silver = OrcLootRolls.fillDefaultOrcLoot(loot, random);
            assertEquals(1, loot.size());
            assertEquals(ItemRegistry.IRON_ORE, loot.get(0).getDefinition());
            int iron = loot.get(0).getQuantity();
            assertTrue(iron >= 1 && iron <= 3);
            assertTrue(silver >= OrcLootRolls.MIN_SILVER && silver <= OrcLootRolls.MAX_SILVER);
            seenIron.add(iron);
            seenSilver.add(silver);
        }
        assertTrue(seenIron.size() >= 2, "iron rolls should vary");
        assertTrue(seenSilver.size() >= 2, "silver rolls should vary");
    }
}
