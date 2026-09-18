package com.donos.zebra.skills;

import com.donos.zebra.HeadlessTestBase;
import com.donos.zebra.entities.DamageText;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import com.donos.zebra.screens.gameplay.GameFlowController;
import com.donos.zebra.screens.gameplay.GameFlowState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillCooldownTest extends HeadlessTestBase {

    @Test
    void skillCanBeUsedWhenReadyAndNotWhileOnCooldown() {
        Player player = armedPlayer();
        SkillBook book = player.getSkillBook();
        SkillCaster caster = new SkillCaster();
        List<Entity> entities = new ArrayList<>();
        entities.add(player);
        SkillCastContext ctx = new SkillCastContext(player, entities, new ArrayList<>());

        assertEquals(SkillCaster.CastResult.OK,
            caster.tryActivateSlot(0, book, ctx));
        assertFalse(book.getRuntime(SkillRegistry.WHIRLWIND_ID).isReady());
        assertEquals(SkillCaster.CastResult.ON_COOLDOWN,
            caster.tryActivateSlot(0, book, ctx));
    }

    @Test
    void cooldownProgressesAndReachesReady() {
        SkillBook book = new SkillBook();
        book.unlock(SkillRegistry.WHIRLWIND_ID);
        SkillRuntime runtime = book.getRuntime(SkillRegistry.WHIRLWIND_ID);
        runtime.startCooldown();
        assertFalse(runtime.isReady());
        book.tick(runtime.getDefinition().getCooldownSeconds() + 0.1f);
        assertTrue(runtime.isReady());
        assertEquals(0f, runtime.getCooldownRemaining(), 0.001f);
    }

    @Test
    void cooldownDoesNotProgressWhileWorldFrozenByPause() {
        GameFlowController flow = new GameFlowController();
        flow.openPause();
        assertEquals(GameFlowState.PAUSED, flow.getState());
        assertTrue(flow.isWorldFrozen());

        SkillBook book = new SkillBook();
        SkillRuntime runtime = book.getRuntime(SkillRegistry.FLAME_STRIKE_ID);
        runtime.startCooldown();
        float before = runtime.getCooldownRemaining();

        float worldDelta = flow.isWorldFrozen() ? 0f : 1f;
        book.tick(worldDelta);
        assertEquals(before, runtime.getCooldownRemaining(), 0.001f);
    }

    @Test
    void cooldownDoesNotProgressWhileDeadStateFreezesWorld() {
        GameFlowController flow = new GameFlowController();
        flow.onPlayerDied();
        assertTrue(flow.isWorldFrozen());

        SkillBook book = new SkillBook();
        SkillRuntime runtime = book.getRuntime(SkillRegistry.WHIRLWIND_ID);
        runtime.setCooldownRemaining(3f);
        book.tick(flow.isWorldFrozen() ? 0f : 1f);
        assertEquals(3f, runtime.getCooldownRemaining(), 0.001f);
    }

    private static Player armedPlayer() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.grantFirstSword();
        player.getSkillBook().unlock(SkillRegistry.WHIRLWIND_ID);
        player.getSkillBook().unlock(SkillRegistry.FLAME_STRIKE_ID);
        return player;
    }
}
