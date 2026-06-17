package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

class StubPlayerInput extends PlayerInput {

    private final Vector2 velocity = new Vector2();
    private boolean moving;
    private Direction direction = Direction.DOWN;
    private boolean attackPressed;

    void simulateMovement(float dx, float dy) {
        velocity.set(dx, dy);
        moving = dx != 0f || dy != 0f;
        if (dx < 0f) {
            direction = Direction.LEFT;
        } else if (dx > 0f) {
            direction = Direction.RIGHT;
        } else if (dy > 0f) {
            direction = Direction.UP;
        } else if (dy < 0f) {
            direction = Direction.DOWN;
        }
    }

    void clearMovement() {
        velocity.set(0, 0);
        moving = false;
    }

    void pressAttack() {
        attackPressed = true;
    }

    @Override
    public Vector2 getIntendedVelocity(float delta) {
        return velocity;
    }

    @Override
    public boolean isMoving() {
        return moving;
    }

    @Override
    public Direction getIntendedDirection() {
        return direction;
    }

    @Override
    public boolean isAttacking() {
        boolean pressed = attackPressed;
        attackPressed = false;
        return pressed;
    }
}

class TestAnimationFactory {

    static Map<String, Animation<TextureRegion>[]> createDirectionalAnimations() {
        Map<String, Animation<TextureRegion>[]> animations = new HashMap<>();
        animations.put(AnimationConstants.ANIM_IDLE, createRowAnimations(4, 1, Animation.PlayMode.LOOP));
        animations.put(AnimationConstants.ANIM_WALK, createRowAnimations(4, 1, Animation.PlayMode.LOOP));
        animations.put(AnimationConstants.ANIM_ATTACK, createRowAnimations(4, 1, Animation.PlayMode.NORMAL));
        return animations;
    }

    @SuppressWarnings("unchecked")
    private static Animation<TextureRegion>[] createRowAnimations(int rows, int cols, Animation.PlayMode mode) {
        Animation<TextureRegion>[] anims = new Animation[rows];
        TextureRegion placeholder = new TextureRegion();
        for (int r = 0; r < rows; r++) {
            TextureRegion[] frames = new TextureRegion[cols];
            for (int c = 0; c < cols; c++) {
                frames[c] = placeholder;
            }
            anims[r] = new Animation<>(0.1f, frames);
            anims[r].setPlayMode(mode);
        }
        return anims;
    }
}

class PlayerMovementTest {

    @org.junit.jupiter.api.Test
    void playerCanMoveInOpenSpace() {
        StubPlayerInput input = new StubPlayerInput();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        Array<Polygon> openSpace = new Array<>();

        float startX = player.getX();
        player.move(10f, 0f, openSpace);

        org.junit.jupiter.api.Assertions.assertTrue(player.getX() > startX);
    }

    @org.junit.jupiter.api.Test
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

        org.junit.jupiter.api.Assertions.assertEquals(100f, player.getX(), 0.01f);
    }
}
