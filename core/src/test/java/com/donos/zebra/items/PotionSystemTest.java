package com.donos.zebra.items;

import com.badlogic.gdx.utils.Array;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PotionSystemTest {

    @Test
    void potionHealsAndClampsAtMaxHp() {
        Player player = newPlayer();
        player.takeDamage(15f); // 85 HP
        assertEquals(85f, player.getCurrentHealth(), 0.01f);

        player.getInventory().addItem(ItemRegistry.POTION_LARGE, 1);
        assertEquals(PotionRules.Result.OK, player.tryUsePotionFromInventory(0));
        assertEquals(player.getMaxHealth(), player.getCurrentHealth(), 0.01f);
        assertEquals(0, player.getInventory().getItemCount(ItemRegistry.POTION_LARGE));
    }

    @Test
    void potionBlockedAtFullHealthDoesNotConsume() {
        Player player = newPlayer();
        player.getInventory().addItem(ItemRegistry.POTION_SMALL, 1);
        assertEquals(PotionRules.Result.FULL_HP, player.tryUsePotionFromInventory(0));
        assertEquals(player.getMaxHealth(), player.getCurrentHealth(), 0.01f);
        assertEquals(1, player.getInventory().getItemCount(ItemRegistry.POTION_SMALL));
    }

    @Test
    void potionsStackToNinetyNineThenNewStack() {
        Inventory inv = new Inventory(5);
        assertTrue(inv.addItem(ItemRegistry.POTION_MEDIUM, 99));
        assertEquals(99, inv.getStackAt(0).getQuantity());
        assertTrue(inv.addItem(ItemRegistry.POTION_MEDIUM, 1));
        assertEquals(99, inv.getStackAt(0).getQuantity());
        assertNotNull(inv.getStackAt(1));
        assertEquals(1, inv.getStackAt(1).getQuantity());

        assertTrue(inv.addItem(ItemRegistry.POTION_SMALL, 5));
        assertSame(ItemRegistry.POTION_SMALL, inv.getStackAt(2).getDefinition());
        assertEquals(5, inv.getStackAt(2).getQuantity());
    }

    @Test
    void hotkeyUsesSlotStartsCooldownAndBlocksUntilExpiry() {
        Player player = newPlayer();
        player.takeDamage(40f);
        player.getInventory().addItem(ItemRegistry.POTION_SMALL, 2);
        assertTrue(player.movePotionStackToSlot(0));
        assertNotNull(player.getPotionSlot());
        assertEquals(2, player.getPotionSlot().getQuantity());
        assertEquals(0, player.getInventory().getItemCount(ItemRegistry.POTION_SMALL));

        assertEquals(PotionRules.Result.OK, player.tryUsePotionFromSlot());
        assertEquals(1, player.getPotionSlot().getQuantity());
        assertTrue(player.isPotionOnCooldown());

        assertEquals(PotionRules.Result.ON_COOLDOWN, player.tryUsePotionFromSlot());
        assertEquals(1, player.getPotionSlot().getQuantity());

        Array<com.badlogic.gdx.math.Polygon> empty = new Array<>();
        player.update(PotionRules.COOLDOWN_SECONDS - 0.5f, empty);
        assertTrue(player.isPotionOnCooldown());
        player.update(1f, empty);
        assertFalse(player.isPotionOnCooldown());
        assertEquals(PotionRules.Result.OK, player.tryUsePotionFromSlot());
        assertNull(player.getPotionSlot());
    }

    @Test
    void hotkeyWithEmptySlotReturnsNoPotion() {
        Player player = newPlayer();
        player.takeDamage(10f);
        assertEquals(PotionRules.Result.NO_POTION, player.tryUsePotionFromSlot());
    }

    @Test
    void moveToSlotSwapsPreviousWithoutCooldown() {
        Player player = newPlayer();
        player.getInventory().addItem(ItemRegistry.POTION_SMALL, 3);
        player.getInventory().addItem(ItemRegistry.POTION_LARGE, 2);
        assertTrue(player.movePotionStackToSlot(0));
        assertSame(ItemRegistry.POTION_SMALL, player.getPotionSlot().getDefinition());

        int largeIdx = -1;
        for (int i = 0; i < 20; i++) {
            ItemStack s = player.getInventory().getStackAt(i);
            if (s != null && s.getDefinition() == ItemRegistry.POTION_LARGE) {
                largeIdx = i;
                break;
            }
        }
        assertTrue(largeIdx >= 0);
        assertTrue(player.movePotionStackToSlot(largeIdx));
        assertSame(ItemRegistry.POTION_LARGE, player.getPotionSlot().getDefinition());
        assertEquals(2, player.getPotionSlot().getQuantity());
        assertEquals(3, player.getInventory().getItemCount(ItemRegistry.POTION_SMALL));
        assertFalse(player.isPotionOnCooldown());
    }

    @Test
    void inventoryUseSharesCooldownWithHotkey() {
        Player player = newPlayer();
        player.takeDamage(40f);
        player.getInventory().addItem(ItemRegistry.POTION_MEDIUM, 1);
        player.getInventory().addItem(ItemRegistry.POTION_SMALL, 1);
        assertTrue(player.movePotionStackToSlot(1));

        assertEquals(PotionRules.Result.OK, player.tryUsePotionFromInventory(0));
        assertEquals(PotionRules.Result.ON_COOLDOWN, player.tryUsePotionFromSlot());
    }

    private static Player newPlayer() {
        return new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
    }
}
