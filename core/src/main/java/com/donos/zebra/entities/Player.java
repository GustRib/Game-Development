package com.donos.zebra.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;

import java.util.Map;

public class Player {

    private static final float SPEED = 60f;

    private Map<String, Animation<TextureRegion>[]> animations;
    private Animation<TextureRegion>[] currentAnimation;
    private Animation<TextureRegion>[] previousAnimation;

    private float x, y;
    private float stateTime;
    private float scale = 1f;

    private boolean isAttacking = false;
    private int lastDirection = 0;
    // 0=baixo - 1=esquerda - 2=direita - 3=cima

    private Polygon hitbox;

    public Player() {
        animations = PlayerAnimationLoader.loadAnimations();
        currentAnimation = animations.get("idle");
        previousAnimation = currentAnimation;
        stateTime = 0f;

        x = 100f;
        y = 100f;

        float[] vertices = {
            -6, -10,  // canto inferior esquerdo
            6, -10,   // canto inferior direito
            6, 10,    // canto superior direito
            -6, 10    // canto superior esquerdo
        };
        hitbox = new Polygon(vertices);
        hitbox.setPosition(x, y);
    }

    public void update(float delta, Array<Polygon> collisionPolygons) {

        boolean moving = false;
        float dx = 0f, dy = 0f;
        int currentDirection = lastDirection;

        if (!isAttacking) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                dx -= SPEED * delta;
                moving = true;
                currentDirection = 1;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                dx += SPEED * delta;
                moving = true;
                currentDirection = 2;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.W)) {
                dy += SPEED * delta;
                moving = true;
                currentDirection = 3;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                dy -= SPEED * delta;
                moving = true;
                currentDirection = 0;
            }
        }

        move(dx, dy, collisionPolygons);

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

    private void move(float dx, float dy, Array<Polygon> collisionPolygons) {
        if ((dx == 0f && dy == 0f) || collisionPolygons == null) return;

        if (dx != 0f) {
            Polygon nextX = new Polygon(hitbox.getVertices());
            nextX.setPosition(x + dx, y);
            boolean collX = false;
            for (Polygon p : collisionPolygons) {
                if (Intersector.overlapConvexPolygons(nextX, p)) { collX = true; break; }
            }
            if (!collX) x += dx;
        }

        if (dy != 0f) {
            Polygon nextY = new Polygon(hitbox.getVertices());
            nextY.setPosition(x, y + dy);
            boolean collY = false;
            for (Polygon p : collisionPolygons) {
                if (Intersector.overlapConvexPolygons(nextY, p)) { collY = true; break; }
            }
            if (!collY) y += dy;
        }

        hitbox.setPosition(x, y);
    }

    private void updateAnimation(float delta) {
        if (currentAnimation != previousAnimation) {
            stateTime = 0f;
            previousAnimation = currentAnimation;
        }
        stateTime += delta;
    }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = currentAnimation[lastDirection].getKeyFrame(stateTime);

        float centerX = hitbox.getX();
        float centerY = hitbox.getY();

        batch.draw(currentFrame,
            centerX - (currentFrame.getRegionWidth() * scale) / 2f,
            centerY - (currentFrame.getRegionHeight() * scale) / 2f,
            currentFrame.getRegionWidth() * scale,
            currentFrame.getRegionHeight() * scale);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        hitbox.setPosition(x, y);
    }

    public Polygon getHitbox() { return hitbox; }
    public float getX() { return x; }
    public float getY() { return y; }
}
