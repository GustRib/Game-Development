package com.donos.zebra.skills;

import com.donos.zebra.HeadlessTestBase;
import com.donos.zebra.entities.DamageText;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.Orc;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WhirlwindSkillTest extends HeadlessTestBase {

    @Test
    void damagesEnemiesInsideRadiusOnly() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.grantFirstSword();
        player.setPosition(100f, 100f);

        Orc near = new Orc(100f + WhirlwindBehavior.RADIUS - 2f, 100f,
            TestAnimationFactory.createOrcAnimations());
        Orc far = new Orc(100f + WhirlwindBehavior.RADIUS + 10f, 100f,
            TestAnimationFactory.createOrcAnimations());
        float nearHp = near.getCurrentHealth();
        float farHp = far.getCurrentHealth();

        List<Entity> entities = new ArrayList<>();
        entities.add(player);
        entities.add(near);
        entities.add(far);
        List<DamageText> texts = new ArrayList<>();

        SkillCastContext ctx = new SkillCastContext(player, entities, texts);
        assertEquals(SkillCaster.CastResult.OK,
            new SkillCaster().tryCast(SkillRegistry.WHIRLWIND_ID, player.getSkillBook(), ctx));

        assertTrue(near.getCurrentHealth() < nearHp);
        assertEquals(farHp, far.getCurrentHealth(), 0.01f);
        assertTrue(near.getCurrentHealth() > 0f || near.isDead());
    }
}
