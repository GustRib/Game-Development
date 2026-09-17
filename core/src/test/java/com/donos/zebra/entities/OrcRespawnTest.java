package com.donos.zebra.entities;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrcRespawnTest {

    @Test
    void defeatedOrcRespawnsAfterSixtySecondsWithFullHealthAndLoot() {
        Orc orc = new Orc(10f, 20f, TestAnimationFactory.createOrcAnimations(), new Random(1L));
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());

        orc.takeDamage(999f);
        assertTrue(orc.isDead());
        orc.clearLoot();
        assertTrue(orc.isLooted());

        orc.updateEnemy(player, Orc.RESPAWN_SECONDS - 1f, null);
        assertTrue(orc.isDead());

        orc.updateEnemy(player, 2f, null);
        assertFalse(orc.isDead());
        assertEquals(orc.getMaxHealth(), orc.getCurrentHealth(), 0.01f);
        assertEquals(10f, orc.getX(), 0.01f);
        assertEquals(20f, orc.getY(), 0.01f);
        assertFalse(orc.isLooted());
        assertEquals(2, orc.getLootTable().size());

        // Loot is only "available" while dead; confirm the replenished table is lootable on next death.
        orc.takeDamage(999f);
        assertTrue(orc.hasLootAvailable());
    }
}
