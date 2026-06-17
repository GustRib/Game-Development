package com.donos.zebra.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;

public class PlayerInput {

    private static final float SPEED = 60f;

    private final Vector2 velocity = new Vector2();
    private Direction intendedDirection = Direction.DOWN;
    private boolean moving;

    public Vector2 getIntendedVelocity(float delta) {
        velocity.set(0, 0);
        moving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            velocity.x -= SPEED * delta;
            moving = true;
            intendedDirection = Direction.LEFT;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            velocity.x += SPEED * delta;
            moving = true;
            intendedDirection = Direction.RIGHT;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            velocity.y += SPEED * delta;
            moving = true;
            intendedDirection = Direction.UP;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            velocity.y -= SPEED * delta;
            moving = true;
            intendedDirection = Direction.DOWN;
        }

        return velocity;
    }

    public boolean isAttacking() {
        return Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
    }

    public Direction getIntendedDirection() {
        return intendedDirection;
    }

    public boolean isMoving() {
        return moving;
    }
}
