package com.donos.zebra.skills.vfx;

import com.donos.zebra.HeadlessTestBase;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import com.donos.zebra.skills.SkillAttackTiming;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlameStrikeSlashEffectTest extends HeadlessTestBase {

    @Test
    void slashEffectLivesThroughAttackDurationAndSurvivesImpact() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        // Null textures: headless has no GL; lifetime logic does not require GPU resources.
        FlameStrikeSlashEffect effect = new FlameStrikeSlashEffect(player, 0f, null, null, null);

        assertTrue(effect.isAlive());
        effect.update(SkillAttackTiming.impactDelaySeconds());
        effect.notifyImpact();
        assertTrue(effect.isAlive());
        effect.update(SkillAttackTiming.attackDurationSeconds());
        float guard = 0f;
        while (effect.isAlive() && guard < 2f) {
            effect.update(0.05f);
            guard += 0.05f;
        }
        assertFalse(effect.isAlive());
    }
}
