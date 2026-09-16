package com.donos.zebra.entities;

import com.badlogic.gdx.math.Vector2;

public class StubPlayerInput extends PlayerInput {

    private final Vector2 velocity = new Vector2();
    private boolean moving;
    private Direction direction = Direction.DOWN;
    private boolean attackPressed;

    public void simulateMovement(float dx, float dy) {
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

    public void clearMovement() {
        velocity.set(0, 0);
        moving = false;
    }

    public void pressAttack() {
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
