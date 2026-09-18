package com.donos.zebra.quests;

/**
 * Gameplay-facing objective kinds. UI must not special-case these beyond display.
 */
public enum QuestObjectiveType {
    COLLECT_ITEM,
    TALK_TO_NPC,
    CRAFT_ITEM,
    KILL_ENEMY,
    REACH_LOCATION
}
