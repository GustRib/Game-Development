package com.donos.zebra.quests;

/**
 * Runtime progress for one objective inside a {@link QuestInstance}.
 */
public final class QuestObjectiveProgress {

    public final String objectiveId;
    public int currentAmount;
    public boolean completed;

    public QuestObjectiveProgress(String objectiveId) {
        this.objectiveId = objectiveId;
        this.currentAmount = 0;
        this.completed = false;
    }
}
