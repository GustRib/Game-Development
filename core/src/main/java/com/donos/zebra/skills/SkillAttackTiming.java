package com.donos.zebra.skills;

import com.donos.zebra.entities.AnimationConstants;

/**
 * Shared timing derived from the player's standing attack sheet
 * ({@code Swordsman_lvl1_attack_with_shadow.png}: 8 columns × {@link AnimationConstants#ATTACK_FRAME_DURATION}).
 */
public final class SkillAttackTiming {

    /** Frame columns in the wired standing attack sheet. */
    public static final int ATTACK_FRAME_COUNT = 8;

    /**
     * Normalized attack progress (0..1) when the sword impact should land.
     * Mid-late swing so VFX/damage sync with the visible cut.
     */
    public static final float IMPACT_NORMALIZED = 0.55f;

    private SkillAttackTiming() {
    }

    public static float attackDurationSeconds() {
        return ATTACK_FRAME_COUNT * AnimationConstants.ATTACK_FRAME_DURATION;
    }

    public static float impactDelaySeconds() {
        return attackDurationSeconds() * IMPACT_NORMALIZED;
    }
}
