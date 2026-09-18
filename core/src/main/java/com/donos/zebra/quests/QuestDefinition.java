package com.donos.zebra.quests;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Immutable quest template.
 */
public final class QuestDefinition {

    public final String id;
    public final String title;
    public final String description;
    public final List<QuestObjectiveDefinition> objectives;
    public final QuestReward reward;
    /** Prior quest that must be completed before this can be offered; null = none. */
    public final String prerequisiteQuestId;

    public QuestDefinition(String id,
                           String title,
                           String description,
                           QuestObjectiveDefinition... objectives) {
        this(id, title, description, null, null, objectives);
    }

    public QuestDefinition(String id,
                           String title,
                           String description,
                           String prerequisiteQuestId,
                           QuestReward reward,
                           QuestObjectiveDefinition... objectives) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.prerequisiteQuestId = prerequisiteQuestId;
        this.reward = reward;
        this.objectives = Collections.unmodifiableList(Arrays.asList(objectives));
    }

    public QuestObjectiveDefinition getObjective(int index) {
        if (index < 0 || index >= objectives.size()) {
            return null;
        }
        return objectives.get(index);
    }
}
