package com.donos.zebra.entities;

import java.util.Map;
import java.util.Random;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.items.OrcLootRolls;

public class Orc extends Enemy {

    public static final float RESPAWN_SECONDS = 60f;

    private static final Random SHARED_LOOT_RANDOM = new Random();

    private final Map<String, Animation<TextureRegion>[]> animations;
    private final float spawnX;
    private final float spawnY;
    private final Random lootRandom;
    private Animation<TextureRegion>[] currentAnimation;
    private float stateTime = 0f;
    private Direction facingDirection = Direction.DOWN;
    private Polygon hitbox;

    private float hurtTimer = 0f;
    private static final float HURT_DURATION = 0.4f;

    private float attackCooldownTimer = 0f;
    private static final float ATTACK_COOLDOWN = 1.5f;

    private static final float ATTACK_RANGE = 24f;

    private boolean isAttacking = false;
    private boolean attackImpactApplied = false;
    private float attackElapsed = 0f;

    private float deathTimer;
    private float highlightPulseTime;

    public Orc(float x, float y, Map<String, Animation<TextureRegion>[]> orcAnimations) {
        this(x, y, orcAnimations, SHARED_LOOT_RANDOM);
    }

    public Orc(float x, float y, Map<String, Animation<TextureRegion>[]> orcAnimations, Random lootRandom) {
        super(x, y, 40f, 35f, 100f);
        this.spawnX = x;
        this.spawnY = y;
        this.animations = orcAnimations;
        this.lootRandom = lootRandom != null ? lootRandom : SHARED_LOOT_RANDOM;

        if (animations != null && animations.containsKey(AnimationConstants.ANIM_IDLE)) {
            this.currentAnimation = animations.get(AnimationConstants.ANIM_IDLE);
        }

        float[] vertices = { -5f, -6f, 5f, -6f, 5f, 6f, -5f, 6f };
        this.hitbox = new Polygon(vertices);
        this.hitbox.setPosition(x, y);
        setHitboxLocalVertices(vertices);

        this.silverLoot = OrcLootRolls.fillDefaultOrcLoot(this.lootTable, this.lootRandom);
    }

    public float getDeathTimer() {
        return deathTimer;
    }

    public float getSpawnX() {
        return spawnX;
    }

    public float getSpawnY() {
        return spawnY;
    }

    public void updateEnemy(Player player, float delta) {
        updateEnemy(player, delta, null);
    }

    public void updateEnemy(Player player, float delta, Array<Polygon> collisionPolygons) {
        stateTime += delta;

        if (isInteractionHighlighted() && isDead && hasLootAvailable()) {
            highlightPulseTime += delta;
        } else {
            highlightPulseTime = 0f;
        }

        if (isDead) {
            if (currentAnimation != animations.get("death")) {
                currentAnimation = animations.get("death");
                stateTime = 0f;
            }
            deathTimer += delta;
            if (deathTimer >= RESPAWN_SECONDS) {
                respawn();
            }
            return;
        }

        if (hurtTimer > 0) {
            hurtTimer -= delta;
        }

        if (attackCooldownTimer > 0) {
            attackCooldownTimer -= delta;
        }

        float oldX = this.x;
        float oldY = this.y;

        if (!player.isDead() && !isAttacking) {
            chasePlayer(player, delta, collisionPolygons);
        }

        float dx = this.x - oldX;
        float dy = this.y - oldY;
        boolean moving = (dx != 0 || dy != 0);

        float distanceToPlayer = com.badlogic.gdx.math.Vector2.dst(this.x, this.y, player.getX(), player.getY());

        if (isAttacking) {
            attackElapsed += delta;
            if (!attackImpactApplied && attackElapsed >= OrcAttackTiming.impactDelaySeconds()) {
                attackImpactApplied = true;
                float distAtImpact = com.badlogic.gdx.math.Vector2.dst(
                    this.x, this.y, player.getX(), player.getY());
                if (!player.isDead() && distAtImpact <= ATTACK_RANGE) {
                    player.takeDamage(OrcAttackTiming.DAMAGE);
                }
            }
            if (attackElapsed >= OrcAttackTiming.durationSeconds()) {
                isAttacking = false;
                attackElapsed = 0f;
                attackImpactApplied = false;
            }
        } else if (!player.isDead()
            && distanceToPlayer <= ATTACK_RANGE
            && attackCooldownTimer <= 0) {
            isAttacking = true;
            attackElapsed = 0f;
            attackImpactApplied = false;
            attackCooldownTimer = ATTACK_COOLDOWN;
            this.stateTime = 0f;
        }

        if (animations != null) {
            if (hurtTimer > 0 && animations.containsKey("hurt")) {
                this.currentAnimation = animations.get("hurt");
            } else if (isAttacking && animations.containsKey(AnimationConstants.ANIM_ATTACK)) {
                this.currentAnimation = animations.get(AnimationConstants.ANIM_ATTACK);
            } else if (moving) {
                if (Math.abs(dx) > Math.abs(dy)) {
                    facingDirection = dx > 0 ? Direction.RIGHT : Direction.LEFT;
                } else {
                    facingDirection = dy > 0 ? Direction.UP : Direction.DOWN;
                }
                this.currentAnimation = animations.get(AnimationConstants.ANIM_RUN);
            } else {
                this.currentAnimation = animations.get(AnimationConstants.ANIM_IDLE);
            }
        }
    }

    void respawn() {
        isDead = false;
        currentHealth = maxHealth;
        isLooted = false;
        deathTimer = 0f;
        x = spawnX;
        y = spawnY;
        hitbox.setPosition(x, y);
        stateTime = 0f;
        hurtTimer = 0f;
        isAttacking = false;
        attackElapsed = 0f;
        attackImpactApplied = false;
        attackCooldownTimer = 0f;
        highlightPulseTime = 0f;
        setInteractionHighlighted(false);
        if (animations != null && animations.containsKey(AnimationConstants.ANIM_IDLE)) {
            currentAnimation = animations.get(AnimationConstants.ANIM_IDLE);
        }
        silverLoot = OrcLootRolls.fillDefaultOrcLoot(lootTable, lootRandom);
        getStatusEffects().clear();
    }

    /**
     * Restores combat/loot/respawn logical state from save (timers as elapsed seconds).
     */
    public void restorePersistedState(
        float x,
        float y,
        float currentHealth,
        boolean dead,
        float deathTimer,
        boolean looted,
        int silverLoot,
        java.util.List<com.donos.zebra.items.ItemStack> lootStacks
    ) {
        this.x = x;
        this.y = y;
        this.currentHealth = Math.max(0f, Math.min(maxHealth, currentHealth));
        this.isDead = dead;
        this.deathTimer = Math.max(0f, deathTimer);
        this.isLooted = looted;
        this.silverLoot = Math.max(0, silverLoot);
        this.lootTable.clear();
        if (lootStacks != null) {
            for (com.donos.zebra.items.ItemStack stack : lootStacks) {
                if (stack != null && !stack.isEmpty()) {
                    this.lootTable.add(stack);
                }
            }
        }
        if (hitbox != null) {
            hitbox.setPosition(this.x, this.y);
        }
        stateTime = 0f;
        hurtTimer = 0f;
        isAttacking = false;
        attackElapsed = 0f;
        attackImpactApplied = false;
        attackCooldownTimer = 0f;
        highlightPulseTime = 0f;
        setInteractionHighlighted(false);
        if (isDead) {
            if (animations != null && animations.containsKey("death")) {
                currentAnimation = animations.get("death");
            }
        } else if (animations != null && animations.containsKey(AnimationConstants.ANIM_IDLE)) {
            currentAnimation = animations.get(AnimationConstants.ANIM_IDLE);
        }
    }

    @Override
    public void takeDamage(float amount) {
        boolean wasAlive = !isDead;
        super.takeDamage(amount);
        if (wasAlive && isDead) {
            deathTimer = 0f;
        }
        if (!isDead) {
            this.hurtTimer = HURT_DURATION;
            this.stateTime = 0f;
            this.isAttacking = false;
            this.attackElapsed = 0f;
            this.attackImpactApplied = false;
        }
    }

    @Override
    public void update(float delta) {
    }

    @Override
    public void render(SpriteBatch batch) {
        if (currentAnimation == null) return;

        int dirIndex = facingDirection.ordinal();
        if (dirIndex >= currentAnimation.length) {
            dirIndex = 0;
        }

        boolean looping = !isDead
            && (currentAnimation != animations.get("hurt"))
            && (currentAnimation != animations.get(AnimationConstants.ANIM_ATTACK));

        TextureRegion currentFrame = currentAnimation[dirIndex].getKeyFrame(stateTime, looping);

        if (isDead && isInteractionHighlighted() && hasLootAvailable()) {
            float pulse = 0.75f + 0.25f * (float) Math.sin(highlightPulseTime * 8f);
            batch.setColor(0.55f * pulse, 1f, 0.65f * pulse, 1f);
        } else if (!isDead && getStatusEffects().hasBurning()) {
            float pulse = 0.75f + 0.25f * (float) Math.sin(stateTime * 12f);
            batch.setColor(1f, 0.45f * pulse, 0.2f, 1f);
        }
        batch.draw(currentFrame,
            x - currentFrame.getRegionWidth() / 2f,
            y - currentFrame.getRegionHeight() / 2f,
            currentFrame.getRegionWidth(),
            currentFrame.getRegionHeight());
        batch.setColor(Color.WHITE);
    }

    @Override
    public Polygon getHitbox() {
        return hitbox;
    }

    @Override
    public void dispose() {
    }
}
