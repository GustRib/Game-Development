package com.donos.zebra.entities;

import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void orcInAttackRangeDamagesPlayerOnlyAtImpactFrame() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(100f, 100f);
        float healthBefore = player.getCurrentHealth();

        Orc orc = new Orc(110f, 100f, TestAnimationFactory.createOrcAnimations());
        orc.updateEnemy(player, 0.016f, new Array<>());
        assertEquals(healthBefore, player.getCurrentHealth(), 0.01f);

        orc.updateEnemy(player, OrcAttackTiming.impactDelaySeconds(), new Array<>());
        assertEquals(healthBefore - OrcAttackTiming.DAMAGE, player.getCurrentHealth(), 0.01f);
    }

    @Test
    void orcAttackDoesNotDamageTwiceInSameSwing() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(100f, 100f);
        float healthBefore = player.getCurrentHealth();

        Orc orc = new Orc(110f, 100f, TestAnimationFactory.createOrcAnimations());
        orc.updateEnemy(player, 0.016f, new Array<>());
        orc.updateEnemy(player, OrcAttackTiming.impactDelaySeconds(), new Array<>());
        float afterImpact = player.getCurrentHealth();
        orc.updateEnemy(player, 0.1f, new Array<>());

        assertEquals(healthBefore - OrcAttackTiming.DAMAGE, afterImpact, 0.01f);
        assertEquals(afterImpact, player.getCurrentHealth(), 0.01f);
    }

    @Test
    void orcDiesAtZeroHealthAndOffersMixedOreLoot() {
        Orc orc = new Orc(100f, 100f, TestAnimationFactory.createOrcAnimations(), new java.util.Random(42L));
        orc.takeDamage(40f);

        assertTrue(orc.isDead());
        assertTrue(orc.hasLootAvailable());
        assertEquals(1, orc.getLootTable().size());
        assertEquals(com.donos.zebra.items.ItemRegistry.IRON_ORE.getId(),
            orc.getLootTable().get(0).getDefinition().getId());
        int ironQty = orc.getLootTable().get(0).getQuantity();
        assertTrue(ironQty >= com.donos.zebra.items.OrcLootRolls.MIN_QTY
            && ironQty <= com.donos.zebra.items.OrcLootRolls.MAX_QTY);
        assertTrue(orc.getSilverLoot() >= com.donos.zebra.items.OrcLootRolls.MIN_SILVER
            && orc.getSilverLoot() <= com.donos.zebra.items.OrcLootRolls.MAX_SILVER);

        orc.clearLoot();
        assertTrue(orc.isLooted());
        assertTrue(!orc.hasLootAvailable());
    }
}
