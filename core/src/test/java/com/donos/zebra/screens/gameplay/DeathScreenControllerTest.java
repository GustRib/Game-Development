package com.donos.zebra.screens.gameplay;

import com.badlogic.gdx.utils.Array;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeathScreenControllerTest {

    @Test
    void deathUiNotInteractiveUntilAnimationFinishesPlusDelay() {
        DeathScreenController controller = new DeathScreenController();
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.takeDamage(999f);
        assertTrue(player.isDead());
        assertFalse(player.isDeathAnimationFinished());

        controller.begin();
        controller.update(player.isDeathAnimationFinished(), 0.05f);
        assertEquals(DeathScreenController.Phase.WAITING_ANIM, controller.getPhase());
        assertFalse(controller.isInteractive());

        // Test death anim: 1 frame @ 0.1s
        player.update(0.12f, new Array<>());
        assertTrue(player.isDeathAnimationFinished());

        controller.update(true, 0.2f);
        assertFalse(controller.isInteractive());

        controller.update(true, DeathScreenController.POST_ANIM_DELAY_SECONDS);
        assertTrue(controller.isInteractive());
        assertEquals(DeathScreenController.Phase.INTERACTIVE, controller.getPhase());
    }
}
