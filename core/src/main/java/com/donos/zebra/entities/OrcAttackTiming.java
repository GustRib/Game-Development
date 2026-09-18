package com.donos.zebra.entities;

/**
 * Timing for Orc melee, derived from {@code Orc-Attack01.png} (6×100px frames)
 * at {@link AnimationConstants#ATTACK_FRAME_DURATION}.
 */
public final class OrcAttackTiming {

    public static final int FRAME_COUNT = 6;
    public static final float IMPACT_NORMALIZED = 0.55f;
    public static final float DAMAGE = 15f;

    private OrcAttackTiming() {
    }

    public static float durationSeconds() {
        return FRAME_COUNT * AnimationConstants.ATTACK_FRAME_DURATION;
    }

    public static float impactDelaySeconds() {
        return durationSeconds() * IMPACT_NORMALIZED;
    }
}
