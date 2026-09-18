package com.donos.zebra.quests;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    /**
     * When non-null on a {@link QuestObjectiveType#KILL_ENEMY} objective,
     * only kills with this equipped weapon item id count.
     */
    public final String weaponItemId;
    /**
     * When non-null on a {@link QuestObjectiveType#KILL_ENEMY} objective,
     * only kills attributed to this skill id count.
     */
    public final String skillId;
    /** One-shot progress thresholds (reusable milestone infrastructure). */
    public final List<QuestProgressMilestone> progressMilestones;

    public QuestObjectiveDefinition(String id,
                                    String description,
                                    QuestObjectiveType type,
                                    String targetId,
                                    int requiredAmount,
                                    QuestLocation location) {
        this(id, description, type, targetId, requiredAmount, location, null, null, null);
    }

    public QuestObjectiveDefinition(String id,
                                    String description,
                                    QuestObjectiveType type,
                                    String targetId,
                                    int requiredAmount,
                                    QuestLocation location,
                                    String weaponItemId,
                                    String skillId,
                                    List<QuestProgressMilestone> progressMilestones) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.targetId = targetId;
        this.requiredAmount = Math.max(1, requiredAmount);
        this.location = location;
        this.weaponItemId = weaponItemId;
        this.skillId = skillId;
        this.progressMilestones = progressMilestones == null || progressMilestones.isEmpty()
            ? Collections.emptyList()
            : Collections.unmodifiableList(progressMilestones);
    }

    public static QuestObjectiveDefinition killEnemy(String id,
                                                     String description,
                                                     String enemyId,
                                                     int requiredAmount,
                                                     QuestLocation location,
                                                     String weaponItemId,
                                                     String skillId,
                                                     QuestProgressMilestone... milestones) {
        List<QuestProgressMilestone> list = milestones == null || milestones.length == 0
            ? null
            : Arrays.asList(milestones);
        return new QuestObjectiveDefinition(
            id, description, QuestObjectiveType.KILL_ENEMY, enemyId, requiredAmount,
            location, weaponItemId, skillId, list);
    }
}
