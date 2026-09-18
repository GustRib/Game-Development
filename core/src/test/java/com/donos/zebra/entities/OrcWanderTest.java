package com.donos.zebra.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrcWanderTest {

    @Test
    void idleOrcPicksWanderTargetWithinRadiusOfSpawn() {
        Orc orc = new Orc(100f, 100f, TestAnimationFactory.createOrcAnimations(), new Random(42L));
        Player player = farAwayPlayer();

        orc.updateEnemy(player, 0.05f, new Array<>());

        assertTrue(orc.isWandering());
        assertTrue(orc.hasWanderTarget());
        float distFromSpawn = com.badlogic.gdx.math.Vector2.dst(
            orc.getSpawnX(), orc.getSpawnY(), orc.getWanderTargetX(), orc.getWanderTargetY());
        assertTrue(distFromSpawn <= Enemy.WANDER_RADIUS + 0.01f);
    }

    @Test
    void idleOrcMovesTowardWanderTargetAtHalfChaseSpeed() {
        Orc orc = new Orc(100f, 100f, TestAnimationFactory.createOrcAnimations(), new Random(7L));
        Player player = farAwayPlayer();

        orc.updateEnemy(player, 0.01f, new Array<>());
        assertTrue(orc.hasWanderTarget());
        float tx = orc.getWanderTargetX();
        float ty = orc.getWanderTargetY();
        float before = com.badlogic.gdx.math.Vector2.dst(orc.getX(), orc.getY(), tx, ty);

        float dt = 0.5f;
        orc.updateEnemy(player, dt, new Array<>());
        float after = com.badlogic.gdx.math.Vector2.dst(orc.getX(), orc.getY(), tx, ty);

        float expectedStep = orc.getSpeed() * Enemy.WANDER_SPEED_FACTOR * dt;
        assertTrue(after < before);
        assertEquals(expectedStep, before - after, 1.5f);
    }

    @Test
    void aggroImmediatelyInterruptsWanderAndChases() {
        Orc orc = new Orc(100f, 100f, TestAnimationFactory.createOrcAnimations(), new Random(3L));
        Player far = farAwayPlayer();
        orc.updateEnemy(far, 0.05f, new Array<>());
        assertTrue(orc.isWandering());

        Player near = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        near.setPosition(150f, 100f);
        float startX = orc.getX();
        orc.updateEnemy(near, 0.5f, new Array<>());

        assertFalse(orc.isWandering());
        assertTrue(orc.getX() > startX);
    }

    @Test
    void losingAggroResumesWanderAroundOriginalSpawn() {
        Orc orc = new Orc(100f, 100f, TestAnimationFactory.createOrcAnimations(), new Random(11L));
        Player near = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        near.setPosition(150f, 100f);
        orc.updateEnemy(near, 0.4f, new Array<>());
        assertFalse(orc.isWandering());

        Player far = farAwayPlayer();
        orc.updateEnemy(far, 0.05f, new Array<>());
        assertTrue(orc.isWandering());
        assertTrue(orc.hasWanderTarget());
        float distFromSpawn = com.badlogic.gdx.math.Vector2.dst(
            orc.getSpawnX(), orc.getSpawnY(), orc.getWanderTargetX(), orc.getWanderTargetY());
        assertTrue(distFromSpawn <= Enemy.WANDER_RADIUS + 0.01f);
    }

    @Test
    void deadOrcDoesNotWanderAndRespawnResetsWander() {
        Orc orc = new Orc(10f, 20f, TestAnimationFactory.createOrcAnimations(), new Random(1L));
        Player player = farAwayPlayer();
        orc.updateEnemy(player, 0.05f, new Array<>());
        assertTrue(orc.isWandering());

        orc.takeDamage(999f);
        orc.updateEnemy(player, 0.2f, new Array<>());
        assertTrue(orc.isDead());
        assertFalse(orc.isWandering());

        orc.updateEnemy(player, Orc.RESPAWN_SECONDS + 0.1f, new Array<>());
        assertFalse(orc.isDead());
        assertEquals(10f, orc.getX(), 0.01f);
        assertEquals(20f, orc.getY(), 0.01f);

        orc.updateEnemy(player, 0.05f, new Array<>());
        assertTrue(orc.isWandering());
    }

    @Test
    void arrivalStartsPauseThenPicksNewTarget() {
        // Seed chosen so first target is close enough to arrive quickly.
        Orc orc = new Orc(100f, 100f, TestAnimationFactory.createOrcAnimations(), new Random(99L));
        Player player = farAwayPlayer();
        Array<Polygon> open = new Array<>();

        boolean paused = false;
        for (int i = 0; i < 200; i++) {
            orc.updateEnemy(player, 0.25f, open);
            if (orc.getWanderPauseRemaining() > 0f) {
                paused = true;
                break;
            }
        }
        assertTrue(paused, "expected wander pause after arriving at a target");

        float pause = orc.getWanderPauseRemaining();
        assertTrue(pause >= Enemy.WANDER_PAUSE_MIN_SECONDS - 0.01f);
        assertTrue(pause <= Enemy.WANDER_PAUSE_MAX_SECONDS + 0.01f);

        orc.updateEnemy(player, pause, open);
        assertEquals(0f, orc.getWanderPauseRemaining(), 0.01f);
        orc.updateEnemy(player, 0.05f, open);
        assertTrue(orc.hasWanderTarget());
    }

    private static Player farAwayPlayer() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(500f, 500f);
        return player;
    }
}
