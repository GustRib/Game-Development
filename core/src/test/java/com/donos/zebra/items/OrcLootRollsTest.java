package com.donos.zebra.items;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrcLootRollsTest {

    @Test
    void rollsStayWithinOneToThreeAndVaryAcrossManyTrials() {
        Random random = new Random(42L);
        Set<Integer> seenCopper = new HashSet<>();
        Set<Integer> seenIron = new HashSet<>();

        for (int i = 0; i < 200; i++) {
            java.util.ArrayList<ItemStack> loot = new java.util.ArrayList<>();
            OrcLootRolls.fillDefaultOrcLoot(loot, random);
            assertEquals(2, loot.size());
            int copper = loot.get(0).getQuantity();
            int iron = loot.get(1).getQuantity();
            assertTrue(copper >= 1 && copper <= 3);
            assertTrue(iron >= 1 && iron <= 3);
            assertEquals(ItemRegistry.COPPER_ORE, loot.get(0).getDefinition());
            assertEquals(ItemRegistry.IRON_ORE, loot.get(1).getDefinition());
            seenCopper.add(copper);
            seenIron.add(iron);
        }
        assertTrue(seenCopper.size() >= 2, "copper rolls should vary");
        assertTrue(seenIron.size() >= 2, "iron rolls should vary");
    }
}
