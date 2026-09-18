package com.donos.zebra.skills.vfx;

import com.donos.zebra.HeadlessTestBase;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import com.donos.zebra.skills.SkillAttackTiming;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WhirlwindRingEffectTest extends HeadlessTestBase {

    @Test
    void whirlwindEffectSpinsThenExpires() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        WhirlwindRingEffect effect = new WhirlwindRingEffect(player, 40f, null, null, null, null);

        assertTrue(effect.isAlive());
        effect.update(SkillAttackTiming.attackDurationSeconds() * 0.5f);
        assertTrue(effect.isAlive());
        float guard = 0f;
        while (effect.isAlive() && guard < 3f) {
            effect.update(0.05f);
            guard += 0.05f;
        }
        assertFalse(effect.isAlive());
    }
}
