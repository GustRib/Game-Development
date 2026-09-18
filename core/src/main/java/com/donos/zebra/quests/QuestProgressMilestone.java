package com.donos.zebra.quests;

/**
 * One-time side effect when objective progress reaches {@link #atAmount}.
 * Does not complete the objective by itself (unless {@code atAmount} equals requiredAmount
 * and normal completion also runs).
 */
public final class QuestProgressMilestone {

    public enum Effect {
        UNLOCK_SKILL,
        UNLOCK_RECIPE
    }

    public final int atAmount;
    public final Effect effect;
    public final String targetId;

    public QuestProgressMilestone(int atAmount, Effect effect, String targetId) {
        this.atAmount = Math.max(1, atAmount);
        this.effect = effect;
        this.targetId = targetId;
    }
}
