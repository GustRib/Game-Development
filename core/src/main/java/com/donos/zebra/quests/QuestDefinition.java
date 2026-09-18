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

    public QuestDefinition(String id,
                           String title,
                           String description,
                           QuestObjectiveDefinition... objectives) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.objectives = Collections.unmodifiableList(Arrays.asList(objectives));
    }

    public QuestObjectiveDefinition getObjective(int index) {
        if (index < 0 || index >= objectives.size()) {
            return null;
        }
        return objectives.get(index);
    }
}
