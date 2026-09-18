package com.donos.zebra.skills;

import com.donos.zebra.HeadlessTestBase;
import com.donos.zebra.entities.DamageText;
import com.donos.zebra.entities.Direction;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.Orc;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlameStrikeSkillTest extends HeadlessTestBase {

    @Test
    void impactIsDelayedUntilAttackTimingThenAppliesBurning() {
        StubPlayerInput input = new StubPlayerInput();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.grantFirstSword();
        player.getSkillBook().unlock(SkillRegistry.FLAME_STRIKE_ID);
        player.setPosition(0f, 0f);
        input.simulateMovement(1f, 0f);
        player.update(0.016f, new com.badlogic.gdx.utils.Array<>());
        assertEquals(Direction.RIGHT, player.getFacingDirection());

        Orc orc = new Orc(20f, 0f, TestAnimationFactory.createOrcAnimations());
        float hpBefore = orc.getCurrentHealth();

        List<Entity> entities = new ArrayList<>();
        entities.add(player);
        entities.add(orc);
        List<DamageText> texts = new ArrayList<>();
        SkillCaster caster = new SkillCaster();
        SkillCastContext ctx = new SkillCastContext(player, entities, texts);

        assertEquals(SkillCaster.CastResult.OK,
            caster.tryCast(SkillRegistry.FLAME_STRIKE_ID, player.getSkillBook(), ctx));
        assertTrue(caster.hasActiveCast());
        assertFalse(caster.hasAppliedImpact());
        assertEquals(hpBefore, orc.getCurrentHealth(), 0.01f);
        assertFalse(orc.getStatusEffects().hasBurning());

        // Advance just before impact
        float almost = SkillAttackTiming.impactDelaySeconds() - 0.02f;
        caster.update(almost, ctx);
        assertFalse(caster.hasAppliedImpact());
        assertEquals(hpBefore, orc.getCurrentHealth(), 0.01f);

        // Cross impact threshold
        caster.update(0.05f, ctx);
        assertTrue(caster.hasAppliedImpact());
        assertTrue(orc.getCurrentHealth() < hpBefore);
        assertTrue(orc.getStatusEffects().hasBurning());

        float afterHit = orc.getCurrentHealth();
        orc.updateStatusEffects(0.5f, texts);
        orc.updateStatusEffects(0.5f, texts);
        assertTrue(orc.getCurrentHealth() < afterHit);

        orc.updateStatusEffects(BurningStatus.DEFAULT_DURATION + 0.1f, texts);
        assertFalse(orc.getStatusEffects().hasBurning());
        float afterExpire = orc.getCurrentHealth();
        orc.updateStatusEffects(1f, texts);
        assertEquals(afterExpire, orc.getCurrentHealth(), 0.01f);

        // Cast finishes with attack animation duration
        caster.update(SkillAttackTiming.attackDurationSeconds(), ctx);
        assertFalse(caster.hasActiveCast());
    }
}
