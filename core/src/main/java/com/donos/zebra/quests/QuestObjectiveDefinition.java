package com.donos.zebra.quests;

/**
 * Immutable objective template inside a {@link QuestDefinition}.
 */
public final class QuestObjectiveDefinition {

    public final String id;
    public final String description;
    public final QuestObjectiveType type;
    /** Item / NPC / enemy / location id depending on {@link #type}. */
    public final String targetId;
    public final int requiredAmount;
    public final QuestLocation location;

    public QuestObjectiveDefinition(String id,
                                    String description,
                                    QuestObjectiveType type,
                                    String targetId,
                                    int requiredAmount,
                                    QuestLocation location) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.targetId = targetId;
        this.requiredAmount = Math.max(1, requiredAmount);
        this.location = location;
    }
}
