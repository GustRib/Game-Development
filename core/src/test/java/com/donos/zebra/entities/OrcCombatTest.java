package com.donos.zebra.entities;

import com.badlogic.gdx.utils.Array;
import com.donos.zebra.items.ItemRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrcCombatTest {

    @Test
    void orcOutsideAggroRangeDoesNotMove() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(500f, 500f);

        Orc orc = new Orc(100f, 100f, TestAnimationFactory.createOrcAnimations());
        float startX = orc.getX();
        float startY = orc.getY();

        orc.updateEnemy(player, 0.5f, new Array<>());

        assertEquals(startX, orc.getX(), 0.01f);
        assertEquals(startY, orc.getY(), 0.01f);
    }

    @Test
    void orcInAttackRangeDamagesPlayer() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(100f, 100f);
        float healthBefore = player.getCurrentHealth();

        Orc orc = new Orc(110f, 100f, TestAnimationFactory.createOrcAnimations());
        orc.updateEnemy(player, 0.016f, new Array<>());

        assertEquals(healthBefore - 15f, player.getCurrentHealth(), 0.01f);
    }

    @Test
    void orcDiesAtZeroHealthAndOffersCopperLoot() {
        Orc orc = new Orc(100f, 100f, TestAnimationFactory.createOrcAnimations());
        orc.takeDamage(40f);

        assertTrue(orc.isDead());
        assertTrue(orc.hasLootAvailable());
        assertEquals(1, orc.getLootTable().size());
        assertEquals(ItemRegistry.COPPER_ORE.getId(), orc.getLootTable().get(0).getDefinition().getId());
        assertEquals(3, orc.getLootTable().get(0).getQuantity());

        orc.clearLoot();
        assertFalse(orc.hasLootAvailable());
        assertTrue(orc.isLooted());
    }
}
