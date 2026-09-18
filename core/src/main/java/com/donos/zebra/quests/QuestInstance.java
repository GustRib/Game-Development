package com.donos.zebra.quests;

import java.util.ArrayList;
import java.util.List;

/**
 * Runtime state for one quest owned by {@link QuestLog}.
 */
public final class QuestInstance {

    public final String questId;
    public QuestStatus status = QuestStatus.INACTIVE;
    public int currentObjectiveIndex;
    public final List<QuestObjectiveProgress> objectives = new ArrayList<>();

    public QuestInstance(QuestDefinition definition) {
        this.questId = definition.id;
        for (QuestObjectiveDefinition obj : definition.objectives) {
            objectives.add(new QuestObjectiveProgress(obj.id));
        }
    }

    public QuestObjectiveProgress getCurrentProgress() {
        if (status != QuestStatus.ACTIVE || currentObjectiveIndex < 0
            || currentObjectiveIndex >= objectives.size()) {
            return null;
        }
        return objectives.get(currentObjectiveIndex);
    }

    public QuestObjectiveProgress getProgress(int index) {
        if (index < 0 || index >= objectives.size()) {
            return null;
        }
        return objectives.get(index);
    }
}
