package com.donos.zebra.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.world.LevelConstants;

import java.util.Map;

public class Player implements Entity {

    static final float HITBOX_HALF_WIDTH = 5f;
    static final float HITBOX_HALF_HEIGHT = 6f;
    static final float HITBOX_OFFSET_X = 0f;
    static final float HITBOX_OFFSET_Y = -4f;

    private float currentHealth = 100f;
    private boolean isDead = false;

    private final PlayerInput input;
    private Map<String, Animation<TextureRegion>[]> animations;
    private Animation<TextureRegion>[] currentAnimation;
    private Animation<TextureRegion>[] previousAnimation;
    private String currentAnimationKey = AnimationConstants.ANIM_IDLE;

    private float x, y;
    private float stateTime;
    private float scale = 1f;

    private float offsetX = 0f;
    private float offsetY = 0f;

    private boolean isAttacking = false;
    private Direction lastDirection = Direction.DOWN;

    private Polygon hitbox;
    private float[] hitboxLocalVertices;
    private final Polygon scratchX = new Polygon();
    private final Polygon scratchY = new Polygon();

    public Player() {
        this(new PlayerInput());
    }

    public Player(AssetManager assetManager) {
        this(new PlayerInput(), assetManager);
    }

    Player(PlayerInput input) {
        this(input, PlayerAnimationLoader.loadAnimations());
    }

    Player(PlayerInput input, Map<String, Animation<TextureRegion>[]> animations) {
        this.input = input;
        this.animations = animations;
        initState();
    }

    Player(PlayerInput input, AssetManager assetManager) {
        this.input = input;
        animations = PlayerAnimationLoader.loadAnimations(assetManager);
        initState();
    }

    private void initState() {
        currentAnimation = animations.get(AnimationConstants.ANIM_IDLE);
        previousAnimation = currentAnimation;
        stateTime = 0f;

        x = LevelConstants.DEFAULT_SPAWN_X;
        y = LevelConstants.DEFAULT_SPAWN_Y;

        float[] vertices = {
            -HITBOX_HALF_WIDTH, -HITBOX_HALF_HEIGHT,
            HITBOX_HALF_WIDTH, -HITBOX_HALF_HEIGHT,
            HITBOX_HALF_WIDTH, HITBOX_HALF_HEIGHT,
            -HITBOX_HALF_WIDTH, HITBOX_HALF_HEIGHT
        };
        hitbox = new Polygon(vertices);
        hitboxLocalVertices = vertices.clone();
        syncHitboxPosition();
    }

    private void syncHitboxPosition() {
        hitbox.setPosition(x + HITBOX_OFFSET_X, y + HITBOX_OFFSET_Y);
    }

    void setAnimations(Map<String, Animation<TextureRegion>[]> animations) {
        this.animations = animations;
        currentAnimation = animations.get(AnimationConstants.ANIM_IDLE);
        previousAnimation = currentAnimation;
        currentAnimationKey = AnimationConstants.ANIM_IDLE;
    }

    @Override
    public void update(float delta) {
        // Collision-aware update is invoked via update(delta, collisionPolygons).
    }

    public void update(float delta, Array<Polygon> collisionPolygons) {
        boolean moving = false;
        Direction currentDirection = lastDirection;

        if (!isAttacking) {
            Vector2 velocity = input.getIntendedVelocity(delta);
            move(velocity.x, velocity.y, collisionPolygons);
            moving = input.isMoving();
            currentDirection = input.getIntendedDirection();

            if (input.isAttacking()) {
                isAttacking = true;
                stateTime = 0f;
                setCurrentAnimation(AnimationConstants.ANIM_ATTACK);
            }
        }

        if (moving) {
            lastDirection = currentDirection;
        }

        updateAnimationState(moving);
        updateAnimation(delta);
    }

    private void updateAnimationState(boolean moving) {
        if (isAttacking) {
            setCurrentAnimation(AnimationConstants.ANIM_ATTACK);
            if (currentAnimation[lastDirection.ordinal()].isAnimationFinished(stateTime)) {
                isAttacking = false;
                stateTime = 0f;
                setCurrentAnimation(AnimationConstants.ANIM_IDLE);
            }
        } else if (moving) {
            setCurrentAnimation(AnimationConstants.ANIM_RUN);
        } else {
            setCurrentAnimation(AnimationConstants.ANIM_IDLE);
        }
    }

    private void setCurrentAnimation(String key) {
        currentAnimationKey = key;
        currentAnimation = animations.get(key);
    }

    void move(float dx, float dy, Array<Polygon> collisionPolygons) {
        if ((dx == 0f && dy == 0f) || collisionPolygons == null) return;

        float hitboxX = x + HITBOX_OFFSET_X;
        float hitboxY = y + HITBOX_OFFSET_Y;

        if (dx != 0f) {
            scratchX.setVertices(hitboxLocalVertices);
            scratchX.setPosition(hitboxX + dx, hitboxY);
            boolean collX = false;
            for (Polygon p : collisionPolygons) {
                if (Intersector.overlapConvexPolygons(scratchX, p)) {
                    collX = true;
                    break;
                }
            }
            if (!collX) x += dx;
        }

        if (dy != 0f) {
            scratchY.setVertices(hitboxLocalVertices);
            scratchY.setPosition(x + HITBOX_OFFSET_X, hitboxY + dy);
            boolean collY = false;
            for (Polygon p : collisionPolygons) {
                if (Intersector.overlapConvexPolygons(scratchY, p)) {
                    collY = true;
                    break;
                }
            }
            if (!collY) y += dy;
        }

        syncHitboxPosition();
    }

    private void updateAnimation(float delta) {
        if (currentAnimation != previousAnimation) {
            stateTime = 0f;
            previousAnimation = currentAnimation;
        }
        stateTime += delta;
    }

    @Override
    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = currentAnimation[lastDirection.ordinal()].getKeyFrame(stateTime);

        batch.draw(currentFrame,
            x + offsetX - (currentFrame.getRegionWidth() * scale) / 2f,
            y + offsetY - (currentFrame.getRegionHeight() * scale) / 2f,
            currentFrame.getRegionWidth() * scale,
            currentFrame.getRegionHeight() * scale);
    }

    @Override
    public void dispose() {
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        syncHitboxPosition();
    }

    public Polygon getHitbox() {
        return hitbox;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public String getCurrentAnimationKey() {
        return currentAnimationKey;
    }

    public void setOffsets(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    @Override
    public void takeDamage(float amount) {
        if (isDead) return;
        currentHealth -= amount;
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
        }
    }

    @Override
    public boolean isDead() {
        return isDead;
    }

    @Override
    public float getCurrentHealth() {
        return currentHealth;
    }

    public float getMaxHealth(){
        return 100f;
    }
}
