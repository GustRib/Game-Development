package com.donos.zebra.entities;

import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerAnimationStateTest {

    @Test
    void updateWithMovementUsesRunAnimation() {
        StubPlayerInput input = new StubPlayerInput();
        input.simulateMovement(1f, 0f);

        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.update(0.016f, new Array<>());

        assertEquals(AnimationConstants.ANIM_RUN, player.getCurrentAnimationKey());
    }

    @Test
    void updateWithoutMovementUsesIdleAnimation() {
        StubPlayerInput input = new StubPlayerInput();
        input.clearMovement();

        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.update(0.016f, new Array<>());

        assertEquals(AnimationConstants.ANIM_IDLE, player.getCurrentAnimationKey());
    }

    @Test
    void attackInputSwitchesToAttackAnimation() {
        StubPlayerInput input = new StubPlayerInput();
        input.pressAttack();

        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.grantFirstSword();
        player.update(0.016f, new Array<>());

        assertEquals(AnimationConstants.ANIM_ATTACK, player.getCurrentAnimationKey());
    }

    @Test
    void takingNonLethalDamageUsesHurtAnimation() {
        StubPlayerInput input = new StubPlayerInput();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.takeDamage(10f);
        player.update(0.016f, new Array<>());

        assertEquals("hurt", player.getCurrentAnimationKey());
        assertTrue(player.getCurrentHealth() > 0f);
    }

    @Test
    void lethalDamageUsesDeathAnimation() {
        StubPlayerInput input = new StubPlayerInput();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.takeDamage(999f);
        player.update(0.016f, new Array<>());

        assertTrue(player.isDead());
        assertEquals("death", player.getCurrentAnimationKey());
    }
}
