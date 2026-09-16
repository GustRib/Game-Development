package com.donos.zebra.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerMovementTest {

    @Test
    void playerCanMoveInOpenSpace() {
        StubPlayerInput input = new StubPlayerInput();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        Array<Polygon> openSpace = new Array<>();

        float startX = player.getX();
        player.move(10f, 0f, openSpace);

        assertTrue(player.getX() > startX);
    }

    @Test
    void playerCannotMoveIntoWall() {
        StubPlayerInput input = new StubPlayerInput();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(100f, 100f);

        Array<Polygon> walls = new Array<>();
        walls.add(new Polygon(new float[]{
            105f, 80f,
            115f, 80f,
            115f, 120f,
            105f, 120f
        }));

        player.move(10f, 0f, walls);

        assertEquals(100f, player.getX(), 0.01f);
    }

    @Test
    void playerSlidesAlongWallOnDiagonalMove() {
        StubPlayerInput input = new StubPlayerInput();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(100f, 100f);

        // Wall just east of the player hitbox so +X is blocked but +Y remains free
        Array<Polygon> walls = new Array<>();
        walls.add(new Polygon(new float[]{
            110f, 50f,
            160f, 50f,
            160f, 150f,
            110f, 150f
        }));

        player.move(10f, 10f, walls);

        assertEquals(100f, player.getX(), 0.01f);
        assertEquals(110f, player.getY(), 0.01f);
    }

    @Test
    void playerBlockedOnBothAxesAtCorner() {
        StubPlayerInput input = new StubPlayerInput();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(100f, 100f);

        Array<Polygon> walls = new Array<>();
        walls.add(new Polygon(new float[]{
            105f, 80f,
            130f, 80f,
            130f, 130f,
            105f, 130f
        }));
        walls.add(new Polygon(new float[]{
            80f, 105f,
            130f, 105f,
            130f, 130f,
            80f, 130f
        }));

        player.move(10f, 10f, walls);

        assertEquals(100f, player.getX(), 0.01f);
        assertEquals(100f, player.getY(), 0.01f);
    }
}
