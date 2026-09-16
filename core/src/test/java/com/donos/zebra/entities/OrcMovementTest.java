package com.donos.zebra.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrcMovementTest {

    @Test
    void orcChasesPlayerInOpenSpace() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(200f, 100f);

        Orc orc = new Orc(100f, 100f, TestAnimationFactory.createOrcAnimations());
        float startX = orc.getX();

        orc.updateEnemy(player, 0.5f, new Array<>());

        assertTrue(orc.getX() > startX);
    }

    @Test
    void orcCannotMoveThroughWallTowardPlayer() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(200f, 100f);

        Orc orc = new Orc(100f, 100f, TestAnimationFactory.createOrcAnimations());

        Array<Polygon> walls = new Array<>();
        walls.add(new Polygon(new float[]{
            110f, 80f,
            130f, 80f,
            130f, 120f,
            110f, 120f
        }));

        float startX = orc.getX();
        orc.updateEnemy(player, 0.5f, walls);

        assertEquals(startX, orc.getX(), 0.5f);
    }

    @Test
    void orcSlidesAlongWallWhenDiagonalChaseBlockedOnOneAxis() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        // Within aggro (100) and northeast of the orc
        player.setPosition(150f, 160f);

        Orc orc = new Orc(100f, 100f, TestAnimationFactory.createOrcAnimations());

        // Wall just east of the orc; northward movement remains free
        Array<Polygon> walls = new Array<>();
        walls.add(new Polygon(new float[]{
            108f, 50f,
            180f, 50f,
            180f, 250f,
            108f, 250f
        }));

        float startX = orc.getX();
        float startY = orc.getY();
        orc.updateEnemy(player, 0.5f, walls);

        assertEquals(startX, orc.getX(), 0.5f);
        assertTrue(orc.getY() > startY);
    }
}
