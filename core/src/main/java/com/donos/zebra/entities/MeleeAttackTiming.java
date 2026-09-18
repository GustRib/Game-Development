package com.donos.zebra.entities;

/**
 * Timing for player basic melee, derived from Player1 attack sheets.
 * <ul>
 *   <li>Standing: {@code Swordsman_lvl1_attack_with_shadow.png} — 8 frames</li>
 *   <li>Walk attack: {@code Swordsman_lvl1_Walk_Attack_with_shadow.png} — 6 frames</li>
 * </ul>
 * Frame duration matches {@link AnimationConstants#ATTACK_FRAME_DURATION}.
 */
public final class MeleeAttackTiming {

    public static final int STANDING_FRAME_COUNT = 8;
    public static final int WALK_FRAME_COUNT = 6;

    /**
     * Normalized progress (0..1) when the sword impact should land (mid-late swing).
     */
    public static final float IMPACT_NORMALIZED = 0.55f;

    private MeleeAttackTiming() {
    }

    public static float standingDurationSeconds() {
        return STANDING_FRAME_COUNT * AnimationConstants.ATTACK_FRAME_DURATION;
    }

    public static float walkDurationSeconds() {
        return WALK_FRAME_COUNT * AnimationConstants.ATTACK_FRAME_DURATION;
    }

    public static float standingImpactDelaySeconds() {
        return standingDurationSeconds() * IMPACT_NORMALIZED;
    }

    public static float walkImpactDelaySeconds() {
        return walkDurationSeconds() * IMPACT_NORMALIZED;
    }
}
