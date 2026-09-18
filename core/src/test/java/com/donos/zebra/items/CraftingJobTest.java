package com.donos.zebra.items;

import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftingJobTest {

    @Test
    void craftConsumesImmediatelyGrantsAfterDurationAndBlocksSecondStart() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        Inventory inv = player.getInventory();
        inv.addItem(ItemRegistry.COPPER_ORE, 10);
        inv.addItem(ItemRegistry.IRON_ORE, 20);

        CraftingController controller = new CraftingController();
        assertTrue(controller.tryStart(
            CraftingRecipes.COPPER_LONGSWORD, inv, java.util.Set.of(CraftingRecipes.COPPER_LONGSWORD.getId())));
        assertEquals(0, inv.getItemCount(ItemRegistry.COPPER_ORE));
        assertEquals(0, inv.getItemCount(ItemRegistry.IRON_ORE));
        assertEquals(0, inv.getItemCount(ItemRegistry.COPPER_LONGSWORD));
        assertTrue(controller.isBusy());

        inv.addItem(ItemRegistry.COPPER_ORE, 5);
        inv.addItem(ItemRegistry.IRON_ORE, 2);
        assertFalse(controller.tryStart(CraftingRecipes.COPPER_HELMET, inv));
        assertEquals(5, inv.getItemCount(ItemRegistry.COPPER_ORE));

        controller.update(CraftingJob.DURATION_SECONDS - 0.5f);
        assertEquals(0, inv.getItemCount(ItemRegistry.COPPER_LONGSWORD));
        assertTrue(controller.isBusy());

        controller.update(1f);
        assertTrue(controller.hasCompletedJob());
        assertEquals(1, inv.getItemCount(ItemRegistry.COPPER_LONGSWORD));
        assertNull(player.getEquippedWeapon());
        assertFalse(controller.isBusy());
    }
}
