package com.donos.zebra.quests;

/**
 * Optional UI/gameplay hooks for quest log events (notifications, HUD refresh).
 */
public interface QuestLogListener {

    default void onQuestStarted(QuestDefinition quest) {
    }

    default void onObjectiveProgress(QuestDefinition quest,
                                     QuestObjectiveDefinition objective,
                                     int current,
                                     int required) {
    }

    default void onObjectiveCompleted(QuestDefinition quest, QuestObjectiveDefinition objective) {
    }

    default void onQuestCompleted(QuestDefinition quest) {
    }

    default void onTrackedQuestChanged(String questId) {
    }
}
