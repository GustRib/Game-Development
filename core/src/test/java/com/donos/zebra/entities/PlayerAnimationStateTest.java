package com.donos.zebra.entities;

import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerAnimationStateTest {

    @Test
    void updateWithMovementUsesWalkAnimation() {
        StubPlayerInput input = new StubPlayerInput();
        input.simulateMovement(1f, 0f);

        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.update(0.016f, new Array<>());

        assertEquals(AnimationConstants.ANIM_WALK, player.getCurrentAnimationKey());
    }

    @Test
    void updateWithoutMovementUsesIdleAnimation() {
        StubPlayerInput input = new StubPlayerInput();
        input.clearMovement();

        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.update(0.016f, new Array<>());

        assertEquals(AnimationConstants.ANIM_IDLE, player.getCurrentAnimationKey());
    }
}
