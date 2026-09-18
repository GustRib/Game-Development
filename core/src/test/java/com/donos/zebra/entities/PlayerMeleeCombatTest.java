package com.donos.zebra.entities;

import com.badlogic.gdx.utils.Array;
import com.donos.zebra.skills.SkillAttackTiming;
import com.donos.zebra.skills.SkillCastContext;
import com.donos.zebra.skills.SkillCaster;
import com.donos.zebra.skills.SkillRegistry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerMeleeCombatTest {

    @Test
    void standingAttackUsesStandingAnimationAndSingleImpact() {
        StubPlayerInput input = new StubPlayerInput();
        input.pressAttack();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.grantFirstSword();
        player.setPosition(100f, 100f);
        player.update(0.016f, new Array<>());

        assertTrue(player.isAttacking());
        assertFalse(player.isWalkAttack());
        assertEquals(AnimationConstants.ANIM_ATTACK, player.getCurrentAnimationKey());
        assertFalse(player.consumeMeleeImpact());

        player.update(MeleeAttackTiming.standingImpactDelaySeconds(), new Array<>());
        assertTrue(player.consumeMeleeImpact());
        assertFalse(player.consumeMeleeImpact());
    }

    @Test
    void movingAttackUsesWalkAttackAnimation() {
        StubPlayerInput input = new StubPlayerInput();
        input.simulateMovement(1f, 0f);
        input.pressAttack();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.grantFirstSword();
        player.setPosition(100f, 100f);
        float xBefore = player.getX();
        player.update(0.016f, new Array<>());

        assertTrue(player.isWalkAttack());
        assertEquals(AnimationConstants.ANIM_WALK_ATTACK, player.getCurrentAnimationKey());
        assertTrue(player.getX() > xBefore);
    }

    @Test
    void playerKeepsMovingDuringStandingAttack() {
        StubPlayerInput input = new StubPlayerInput();
        input.pressAttack();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.grantFirstSword();
        player.setPosition(100f, 100f);
        player.update(0.016f, new Array<>());
        assertEquals(AnimationConstants.ANIM_ATTACK, player.getCurrentAnimationKey());

        input.simulateMovement(1f, 0f);
        float xBefore = player.getX();
        player.update(0.05f, new Array<>());
        assertTrue(player.getX() > xBefore);
        assertTrue(player.isAttacking());
    }

    @Test
    void skillCastDoesNotEmitMeleeImpactAndAllowsMovement() {
        StubPlayerInput input = new StubPlayerInput();
        input.simulateMovement(1f, 0f);
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.grantFirstSword();
        player.getSkillBook().unlock(SkillRegistry.FLAME_STRIKE_ID);
        player.setPosition(100f, 100f);

        SkillCaster caster = new SkillCaster();
        SkillCastContext ctx = new SkillCastContext(player, new ArrayList<>(), new ArrayList<>());
        assertEquals(SkillCaster.CastResult.OK,
            caster.tryCast(SkillRegistry.FLAME_STRIKE_ID, player.getSkillBook(), ctx));
        assertTrue(player.isSkillCastAttack());
        assertTrue(player.isWalkAttack());
        assertEquals(AnimationConstants.ANIM_WALK_ATTACK, player.getCurrentAnimationKey());
        assertFalse(player.consumeMeleeImpact());

        float xBefore = player.getX();
        player.update(0.05f, new Array<>());
        assertTrue(player.getX() > xBefore);
        assertFalse(player.consumeMeleeImpact());

        player.update(SkillAttackTiming.impactDelaySeconds(), new Array<>());
        assertFalse(player.consumeMeleeImpact());
    }

    @Test
    void standingSkillCastUsesStandingAttackAnimation() {
        StubPlayerInput input = new StubPlayerInput();
        input.clearMovement();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.grantFirstSword();
        player.getSkillBook().unlock(SkillRegistry.WHIRLWIND_ID);

        SkillCaster caster = new SkillCaster();
        SkillCastContext ctx = new SkillCastContext(player, new ArrayList<>(), new ArrayList<>());
        assertEquals(SkillCaster.CastResult.OK,
            caster.tryCast(SkillRegistry.WHIRLWIND_ID, player.getSkillBook(), ctx));
        assertTrue(player.isSkillCastAttack());
        assertFalse(player.isWalkAttack());
        assertEquals(AnimationConstants.ANIM_ATTACK, player.getCurrentAnimationKey());
    }

    @Test
    void bufferedAttackStartsAfterCurrentSwing() {
        StubPlayerInput input = new StubPlayerInput();
        input.pressAttack();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.grantFirstSword();
        player.update(0.016f, new Array<>());

        input.pressAttack();
        player.update(0.02f, new Array<>());
        assertTrue(player.isAttacking());

        // Finish first swing; buffer should start second
        float remaining = MeleeAttackTiming.standingDurationSeconds();
        player.update(remaining, new Array<>());
        assertTrue(player.isAttacking());
        assertEquals(AnimationConstants.ANIM_ATTACK, player.getCurrentAnimationKey());
    }
}
