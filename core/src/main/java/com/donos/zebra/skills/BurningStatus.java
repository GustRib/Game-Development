package com.donos.zebra.skills;

import com.badlogic.gdx.graphics.Color;
import com.donos.zebra.entities.DamageText;
import com.donos.zebra.entities.Enemy;

import java.util.List;

/**
 * Periodic fire DoT. Designed so Poison/Bleed can mirror this pattern later.
 * When applied by Flame Strike, DoT kills inherit that skill attribution.
 */
public final class BurningStatus implements StatusEffect {

    public static final String ID = "burning";
    public static final float DEFAULT_DURATION = 2f;
    public static final float DEFAULT_TICK_INTERVAL = 0.5f;
    public static final float DEFAULT_DAMAGE_PER_TICK = 3f;

    private float remainingDuration;
    private float tickInterval;
    private float damagePerTick;
    private float tickAccumulator;
    private final String sourceSkillId;

    public BurningStatus() {
        this(DEFAULT_DURATION, DEFAULT_TICK_INTERVAL, DEFAULT_DAMAGE_PER_TICK, null);
    }

    public BurningStatus(String sourceSkillId) {
        this(DEFAULT_DURATION, DEFAULT_TICK_INTERVAL, DEFAULT_DAMAGE_PER_TICK, sourceSkillId);
    }

    public BurningStatus(float duration, float tickInterval, float damagePerTick) {
        this(duration, tickInterval, damagePerTick, null);
    }

    public BurningStatus(float duration, float tickInterval, float damagePerTick, String sourceSkillId) {
        this.remainingDuration = Math.max(0f, duration);
        this.tickInterval = Math.max(0.05f, tickInterval);
        this.damagePerTick = Math.max(0f, damagePerTick);
        this.tickAccumulator = 0f;
        this.sourceSkillId = sourceSkillId;
    }

    public String getSourceSkillId() {
        return sourceSkillId;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void update(Enemy target, float delta, List<DamageText> damageTexts) {
        if (isExpired() || target == null || target.isDead() || delta <= 0f) {
            remainingDuration = Math.max(0f, remainingDuration - Math.max(0f, delta));
            return;
        }
        remainingDuration -= delta;
        tickAccumulator += delta;
        while (tickAccumulator >= tickInterval && !target.isDead()) {
            tickAccumulator -= tickInterval;
            if (sourceSkillId != null) {
                target.takeDamage(damagePerTick, null, sourceSkillId);
            } else {
                target.takeDamage(damagePerTick);
            }
            if (damageTexts != null) {
                damageTexts.add(new DamageText(
                    target.getX(), target.getY() + 18f,
                    "-" + (int) damagePerTick, new Color(1f, 0.45f, 0.1f, 1f)));
            }
        }
        if (remainingDuration < 0f) {
            remainingDuration = 0f;
        }
    }

    @Override
    public boolean isExpired() {
        return remainingDuration <= 0f;
    }

    @Override
    public void refreshFrom(StatusEffect other) {
        if (!(other instanceof BurningStatus)) {
            return;
        }
        BurningStatus burn = (BurningStatus) other;
        this.remainingDuration = burn.remainingDuration;
        this.tickInterval = burn.tickInterval;
        this.damagePerTick = burn.damagePerTick;
        this.tickAccumulator = 0f;
        // sourceSkillId is final — keep original attribution on refresh
    }

    public float getRemainingDuration() {
        return remainingDuration;
    }

    public float getDamagePerTick() {
        return damagePerTick;
    }

    public float getTickInterval() {
        return tickInterval;
    }
}
