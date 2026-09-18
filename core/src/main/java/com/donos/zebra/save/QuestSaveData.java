package com.donos.zebra.save;

/**
 * Opening-quest mentor flag plus reusable quest log persistence.
 */
public class QuestSaveData {

    /** Legacy opening-slice flag (mentor gave pickaxe). */
    public boolean mentorGavePickaxe;

    /** Tracked / HUD quest id. */
    public String activeQuestId;

    public int currentObjectiveIndex;

    /** Per-objective counters for the tracked quest (optional). */
    public int[] objectiveProgress;

    public boolean[] objectiveCompleted;

    public String[] completedQuestIds;
}
