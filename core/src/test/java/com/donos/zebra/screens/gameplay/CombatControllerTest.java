package com.donos.zebra.screens.gameplay;

import com.badlogic.gdx.utils.Array;
import com.donos.zebra.entities.AnimationConstants;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombatControllerTest {

    @Test
    void meleeHitsEnemyInRangeDuringAttack() {
        StubPlayerInput input = new StubPlayerInput();
        input.pressAttack();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(100f, 100f);
        player.grantFirstSword();
        player.update(0.016f, new Array<>());
        assertEquals(AnimationConstants.ANIM_ATTACK, player.getCurrentAnimationKey());

        Orc orc = new Orc(110f, 100f, TestAnimationFactory.createOrcAnimations());
        List<Entity> entities = new ArrayList<>();
        entities.add(player);
        entities.add(orc);
        List<DamageText> damageTexts = new ArrayList<>();

        float orcHealthBefore = orc.getCurrentHealth();
        CombatController.resolvePlayerMelee(player, entities, damageTexts, true);

        assertEquals(orcHealthBefore - CombatController.getAttackDamage(), orc.getCurrentHealth(), 0.01f);
        assertEquals(1, damageTexts.size());
        assertTrue(entities.contains(orc));
    }

    @Test
    void meleeWithoutSwordDoesNotDealDamage() {
        StubPlayerInput input = new StubPlayerInput();
        input.pressAttack();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(100f, 100f);
        player.update(0.016f, new Array<>());
        assertFalse(player.hasFirstSword());

        Orc orc = new Orc(110f, 100f, TestAnimationFactory.createOrcAnimations());
        List<Entity> entities = new ArrayList<>();
        entities.add(orc);
        List<DamageText> damageTexts = new ArrayList<>();

        float orcHealthBefore = orc.getCurrentHealth();
        CombatController.resolvePlayerMelee(player, entities, damageTexts, true);

        assertEquals(orcHealthBefore, orc.getCurrentHealth(), 0.01f);
        assertEquals(1, damageTexts.size());
        assertTrue(damageTexts.get(0).text.contains("arma"));
    }

    @Test
    void meleeDoesNotHitEnemyOutOfRange() {
        StubPlayerInput input = new StubPlayerInput();
        input.pressAttack();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(100f, 100f);
        player.grantFirstSword();
        player.update(0.016f, new Array<>());

        Orc orc = new Orc(200f, 100f, TestAnimationFactory.createOrcAnimations());
        List<Entity> entities = new ArrayList<>();
        entities.add(orc);
        List<DamageText> damageTexts = new ArrayList<>();

        float orcHealthBefore = orc.getCurrentHealth();
        CombatController.resolvePlayerMelee(player, entities, damageTexts, true);

        assertEquals(orcHealthBefore, orc.getCurrentHealth(), 0.01f);
        assertTrue(damageTexts.isEmpty());
    }

    @Test
    void lethalMeleeLeavesDeadOrcInEntityList() {
        StubPlayerInput input = new StubPlayerInput();
        input.pressAttack();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(100f, 100f);
        player.grantFirstSword();
        player.update(0.016f, new Array<>());

        Orc orc = new Orc(110f, 100f, TestAnimationFactory.createOrcAnimations());
        orc.takeDamage(35f); // leave 5 HP so one melee hit kills
        List<Entity> entities = new ArrayList<>();
        entities.add(orc);
        List<DamageText> damageTexts = new ArrayList<>();

        CombatController.resolvePlayerMelee(player, entities, damageTexts, true);

        assertTrue(orc.isDead());
        assertTrue(entities.contains(orc));
        assertFalse(entities.isEmpty());
    }
}
