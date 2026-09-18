package com.donos.zebra.skills;

/**
 * In-flight delayed skill cast (Flame Strike windup → impact → recovery).
 */
public final class PendingSkillCast {

    public enum Phase {
        WINDUP,
        IMPACT,
        RECOVERY,
        DONE
    }

    public final String skillId;
    public final float impactDelay;
    public final float duration;
    public final float facingDegrees;
    public float elapsed;
    public boolean impactApplied;

    public PendingSkillCast(String skillId, float impactDelay, float duration, float facingDegrees) {
        this.skillId = skillId;
        this.impactDelay = impactDelay;
        this.duration = duration;
        this.facingDegrees = facingDegrees;
    }

    public Phase getPhase() {
        if (elapsed >= duration) {
            return Phase.DONE;
        }
        if (impactApplied) {
            return Phase.RECOVERY;
        }
        if (elapsed >= impactDelay) {
            return Phase.IMPACT;
        }
        return Phase.WINDUP;
    }

    public boolean isActive() {
        return elapsed < duration;
    }
}
