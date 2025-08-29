package com.donos.zebra.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import java.util.Map;

public class Player {

    private static final float HITBOX_SIZE = 6f;
    private static final float SPEED = 60f;

    private Map<String, Animation<TextureRegion>[]> animations;
    private Animation<TextureRegion>[] currentAnimation;
    private Animation<TextureRegion>[] previousAnimation;

    private float x, y;
    private float stateTime;
    private float scale = 1f;

    private boolean isAttacking = false;
    private int lastDirection = 2; // 0 = cima, 1 = esquerda, 2 = baixo, 3 = direita

    private Rectangle bounds;
    private float offsetX = 0f;
    private float offsetY = 0f;

    public Player() {
        animations = PlayerAnimationLoader.loadAnimations();
        currentAnimation = animations.get("idle");
        previousAnimation = currentAnimation;
        stateTime = 0f;

        x = 100f;
        y = 100f;

        bounds = new Rectangle(x - HITBOX_SIZE / 2f, y - HITBOX_SIZE / 2f, HITBOX_SIZE, HITBOX_SIZE);
    }

    public void update(float delta, Array<Rectangle> collisionRects) {

        boolean moving = false;
        float dx = 0f, dy = 0f;
        int currentDirection = lastDirection;

        if (!isAttacking) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) { dx -= SPEED * delta; moving = true; currentDirection = 1; }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) { dx += SPEED * delta; moving = true; currentDirection = 3; }
            if (Gdx.input.isKeyPressed(Input.Keys.W)) { dy += SPEED * delta; moving = true; currentDirection = 0; }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) { dy -= SPEED * delta; moving = true; currentDirection = 2; }
        }

        move(dx, dy, collisionRects);

        if (moving) lastDirection = currentDirection;

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !isAttacking) {
            isAttacking = true;
            stateTime = 0f;
            currentAnimation = animations.get("attack");
        }

        if (isAttacking) {
            if (currentAnimation[lastDirection].isAnimationFinished(stateTime)) {
                isAttacking = false;
                stateTime = 0f;
                currentAnimation = animations.get("idle");
            }
        } else if (moving) {
            currentAnimation = animations.get("walk");
        } else {
            currentAnimation = animations.get("idle");
        }

        updateAnimation(delta);
    }

    private void move(float dx, float dy, Array<Rectangle> collisionRects) {
        if ((dx == 0f && dy == 0f) || collisionRects == null) {
            bounds.setPosition(x - HITBOX_SIZE / 2f, y - HITBOX_SIZE / 2f);
            return;
        }

        if (dx != 0f) {
            Rectangle nextX = new Rectangle(bounds);
            nextX.setPosition(x + dx - HITBOX_SIZE / 2f, y - HITBOX_SIZE / 2f);
            boolean collX = false;
            for (Rectangle r : collisionRects) if (nextX.overlaps(r)) { collX = true; break; }
            if (!collX) x += dx;
        }

        if (dy != 0f) {
            Rectangle nextY = new Rectangle(bounds);
            nextY.setPosition(x - HITBOX_SIZE / 2f, y + dy - HITBOX_SIZE / 2f);
            boolean collY = false;
            for (Rectangle r : collisionRects) if (nextY.overlaps(r)) { collY = true; break; }
            if (!collY) y += dy;
        }

        bounds.setPosition(x - HITBOX_SIZE / 2f, y - HITBOX_SIZE / 2f);
    }

    private void updateAnimation(float delta) {
        if (currentAnimation != previousAnimation) {
            stateTime = 0f;
            previousAnimation = currentAnimation;
        }
        stateTime += delta; // sempre acumula
    }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = currentAnimation[lastDirection].getKeyFrame(stateTime);
        float centerX = bounds.x + bounds.width / 2f + offsetX;
        float centerY = bounds.y + bounds.height / 2f + offsetY;

        batch.draw(currentFrame,
            centerX - (currentFrame.getRegionWidth() * scale) / 2f,
            centerY - (currentFrame.getRegionHeight() * scale) / 2f,
            currentFrame.getRegionWidth() * scale,
            currentFrame.getRegionHeight() * scale);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        bounds.setPosition(x - HITBOX_SIZE / 2f, y - HITBOX_SIZE / 2f);
    }

    public Rectangle getBounds() { return bounds; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getScale() { return scale; }
    public void setScale(float newScale) { scale = newScale; }
}
