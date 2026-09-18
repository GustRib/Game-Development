package com.donos.zebra.entities;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.items.ItemStack;
import com.donos.zebra.util.CollisionMovement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Enemy implements Entity {
    /** Territory radius for idle wander around {@link #spawnX}/{@link #spawnY}. */
    public static final float WANDER_RADIUS = 80f;
    /** Idle wander uses half of chase {@link #speed}. */
    public static final float WANDER_SPEED_FACTOR = 0.5f;
    public static final float WANDER_PAUSE_MIN_SECONDS = 2f;
    public static final float WANDER_PAUSE_MAX_SECONDS = 5f;
    public static final float WANDER_ARRIVAL_DISTANCE = 4f;
    /** Stand-off distance while chasing (do not walk into the player). */
    public static final float CHASE_STOP_DISTANCE = 8f;

    protected float x, y;
    protected float spawnX, spawnY;
    protected float maxHealth;
    protected float currentHealth;
    protected boolean isDead = false;

    //Controle de Loot e Estado de Saque
    protected boolean isLooted = false;
    protected final List<ItemStack> lootTable = new ArrayList<>();
    /** Silver (Prata) dropped with this corpse — not an inventory ItemStack. */
    protected int silverLoot;
    /** Nearest lootable corpse highlight (set by GameScreen each frame). */
    private boolean interactionHighlighted;
    private final com.donos.zebra.skills.StatusEffectController statusEffects =
        new com.donos.zebra.skills.StatusEffectController();
    private String killWeaponItemId;
    private String killSkillId;

    protected float speed;
    protected float aggroRange;

    private final Polygon scratchX = new Polygon();
    private final Polygon scratchY = new Polygon();
    private final float[] moveScratch = new float[2];
    private float[] hitboxLocalVertices;

    private Random wanderRandom = new Random();
    private float wanderTargetX;
    private float wanderTargetY;
    private float wanderPauseRemaining;
    private boolean hasWanderTarget;
    private boolean wandering;

    public Enemy(float x, float y, float maxHealth, float speed, float aggroRange) {
        this.x = x;
        this.y = y;
        this.spawnX = x;
        this.spawnY = y;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
        this.speed = speed;
        this.aggroRange = aggroRange;
    }

    protected void setHitboxLocalVertices(float[] localVertices) {
        this.hitboxLocalVertices = localVertices.clone();
    }

    protected void setWanderRandom(Random random) {
        if (random != null) {
            this.wanderRandom = random;
        }
    }

    /**
     * Chase when the player is in aggro range; otherwise idle-wander around spawn.
     * Losing aggro resumes wander from the current position with targets still
     * chosen relative to the original spawn (territory), which is the simplest
     * fit for the existing spawn/respawn anchor.
     */
    protected void updateLocomotion(Player player, float delta, Array<Polygon> collisionPolygons) {
        if (isDead) {
            wandering = false;
            return;
        }
        if (player != null && !player.isDead()) {
            float distance = Vector2.dst(this.x, this.y, player.getX(), player.getY());
            if (distance <= aggroRange) {
                interruptWander();
                chasePlayer(player, delta, collisionPolygons);
                return;
            }
        }
        updateWander(delta, collisionPolygons);
    }

    // IA Simples para seguir o jogador na sua direção
    protected void chasePlayer(Player player, float delta, Array<Polygon> collisionPolygons) {
        if (isDead || player == null || player.isDead()) return;

        float distance = Vector2.dst(this.x, this.y, player.getX(), player.getY());

        // Persegue se estiver no alcance de detecção, mas não colado demais
        if (distance <= aggroRange && distance > CHASE_STOP_DISTANCE) {
            moveToward(player.getX(), player.getY(), speed, delta, collisionPolygons);
        }
    }

    private void updateWander(float delta, Array<Polygon> collisionPolygons) {
        wandering = true;
        if (wanderPauseRemaining > 0f) {
            wanderPauseRemaining -= delta;
            if (wanderPauseRemaining > 0f) {
                return;
            }
            // Pause just ended — pick a new leg on the next tick.
            wanderPauseRemaining = 0f;
            hasWanderTarget = false;
            return;
        }

        if (!hasWanderTarget) {
            pickWanderTarget();
        }

        float dist = Vector2.dst(x, y, wanderTargetX, wanderTargetY);
        if (dist <= WANDER_ARRIVAL_DISTANCE) {
            hasWanderTarget = false;
            wanderPauseRemaining = WANDER_PAUSE_MIN_SECONDS
                + wanderRandom.nextFloat()
                * (WANDER_PAUSE_MAX_SECONDS - WANDER_PAUSE_MIN_SECONDS);
            return;
        }

        float wanderSpeed = speed * WANDER_SPEED_FACTOR;
        float beforeX = x;
        float beforeY = y;
        moveToward(wanderTargetX, wanderTargetY, wanderSpeed, delta, collisionPolygons);
        float moved = Vector2.dst(beforeX, beforeY, x, y);
        float after = Vector2.dst(x, y, wanderTargetX, wanderTargetY);
        float intended = wanderSpeed * delta;
        if (isStuckOnWander(dist, after, intended) && moved < 0.01f) {
            hasWanderTarget = false;
            wanderPauseRemaining = 0.35f;
        }
    }

    private void pickWanderTarget() {
        float angle = wanderRandom.nextFloat() * (float) (Math.PI * 2.0);
        // Keep targets outside arrival epsilon so the orc actually walks each leg.
        float minRadius = Math.max(WANDER_ARRIVAL_DISTANCE + 2f, WANDER_RADIUS * 0.25f);
        float radius = minRadius + wanderRandom.nextFloat() * (WANDER_RADIUS - minRadius);
        wanderTargetX = spawnX + (float) Math.cos(angle) * radius;
        wanderTargetY = spawnY + (float) Math.sin(angle) * radius;
        hasWanderTarget = true;
    }

    private void interruptWander() {
        wandering = false;
        hasWanderTarget = false;
        wanderPauseRemaining = 0f;
    }

    /** Clears wander timers so a respawned enemy starts picking a new wander point. */
    protected void resetWanderState() {
        interruptWander();
    }

    private void moveToward(float targetX, float targetY, float moveSpeed,
                            float delta, Array<Polygon> collisionPolygons) {
        float dirX = targetX - this.x;
        float dirY = targetY - this.y;

        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (length <= 0.0001f) {
            return;
        }
        dirX /= length;
        dirY /= length;

        float dx = dirX * moveSpeed * delta;
        float dy = dirY * moveSpeed * delta;
        float intended = (float) Math.sqrt(dx * dx + dy * dy);
        if (intended > length) {
            dx = dirX * length;
            dy = dirY * length;
        }

        if (hitboxLocalVertices != null) {
            moveScratch[0] = x;
            moveScratch[1] = y;
            CollisionMovement.tryMove(
                moveScratch,
                0f,
                0f,
                hitboxLocalVertices,
                dx,
                dy,
                collisionPolygons,
                scratchX,
                scratchY
            );
            this.x = moveScratch[0];
            this.y = moveScratch[1];
        } else {
            this.x += dx;
            this.y += dy;
        }

        if (getHitbox() != null) {
            getHitbox().setPosition(this.x, this.y);
        }
    }

    /** True when progress toward the current wander target is effectively blocked. */
    private boolean isStuckOnWander(float distBefore, float distAfter, float intendedStep) {
        return intendedStep > 0.05f && distAfter >= distBefore - 0.01f;
    }

    public boolean isWandering() {
        return wandering && !isDead;
    }

    public boolean hasWanderTarget() {
        return hasWanderTarget;
    }

    public float getWanderTargetX() {
        return wanderTargetX;
    }

    public float getWanderTargetY() {
        return wanderTargetY;
    }

    public float getWanderPauseRemaining() {
        return wanderPauseRemaining;
    }

    public float getSpawnX() {
        return spawnX;
    }

    public float getSpawnY() {
        return spawnY;
    }

    public float getAggroRange() {
        return aggroRange;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public void takeDamage(float amount) {
        if (isDead) return;
        currentHealth -= amount;
        System.out.println("Monstro tomou dano! Vida atual: " + currentHealth + "/" + maxHealth);
        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
            interruptWander();
        }
    }

    /**
     * Damage with kill attribution for quest tracking.
     */
    public void takeDamage(float amount, String weaponItemId, String skillId) {
        this.killWeaponItemId = weaponItemId;
        this.killSkillId = skillId;
        takeDamage(amount);
    }

    public String getKillWeaponItemId() {
        return killWeaponItemId;
    }

    public String getKillSkillId() {
        return killSkillId;
    }

    public void setKillAttribution(String weaponItemId, String skillId) {
        this.killWeaponItemId = weaponItemId;
        this.killSkillId = skillId;
    }

    /**
     * Verifica se o corpo do inimigo está elegível para ser looteado.
     * Um inimigo pode ser saqueado apenas se estiver morto, não tiver sido looteado ainda
     * e possuir itens em seu inventário interno.
     */
    public boolean hasLootAvailable() {
        return isDead && !isLooted && (!lootTable.isEmpty() || silverLoot > 0);
    }

    /**
     * Retorna a lista de itens contida no corpo do inimigo.
     */
    public List<ItemStack> getLootTable() {
        return lootTable;
    }

    public int getSilverLoot() {
        return silverLoot;
    }

    public void setSilverLoot(int silverLoot) {
        this.silverLoot = Math.max(0, silverLoot);
    }

    /**
     * Marca o corpo do inimigo como looteado e limpa seus itens internos.
     * Garante que o loot exista apenas uma única vez no mundo.
     */
    public void clearLoot() {
        this.lootTable.clear();
        this.silverLoot = 0;
        this.isLooted = true;
        System.out.println("O corpo do inimigo foi completamente saqueado e esvaziado.");
    }

    /**
     * Verifica se o inimigo já foi completamente limpo pelo jogador.
     */
    public boolean isLooted() {
        return isLooted;
    }

    public void setInteractionHighlighted(boolean highlighted) {
        this.interactionHighlighted = highlighted;
    }

    public boolean isInteractionHighlighted() {
        return interactionHighlighted;
    }

    @Override
    public boolean isDead() { return isDead; }

    @Override
    public float getX() { return x; }

    @Override
    public float getY() { return y; }

    @Override
    public float getCurrentHealth() { return currentHealth; }

    public float getMaxHealth() {
        return maxHealth;
    }

    public com.donos.zebra.skills.StatusEffectController getStatusEffects() {
        return statusEffects;
    }

    public void updateStatusEffects(float delta, java.util.List<com.donos.zebra.entities.DamageText> damageTexts) {
        if (isDead) {
            statusEffects.clear();
            return;
        }
        statusEffects.update(this, delta, damageTexts);
    }
}
