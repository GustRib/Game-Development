package com.donos.zebra.quests;

import java.util.HashSet;
import java.util.Set;

/**
 * Runtime progress for one objective inside a {@link QuestInstance}.
 */
public final class QuestObjectiveProgress {

    public final String objectiveId;
    public int currentAmount;
    public boolean completed;
    /** Threshold amounts that have already fired their milestone effects. */
    public final Set<Integer> firedMilestones = new HashSet<>();

    public QuestObjectiveProgress(String objectiveId) {
        this.objectiveId = objectiveId;
        this.currentAmount = 0;
        this.completed = false;
    }
}
