package com.donos.zebra.quests;

import com.donos.zebra.world.OpeningQuest;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Catalog of authored quests.
 */
public final class QuestRegistry {

    private static final Map<String, QuestDefinition> BY_ID = new LinkedHashMap<>();

    public static final QuestDefinition FIRST_SWORD = register(new QuestDefinition(
        QuestIds.QUEST_FIRST_SWORD,
        "Primeira Espada",
        "O mentor precisa de minerio de cobre para ajuda-lo a fabricar sua primeira espada.",
        new QuestObjectiveDefinition(
            QuestIds.OBJ_TALK_MENTOR_INTRO,
            "Fale com o Mentor",
            QuestObjectiveType.TALK_TO_NPC,
            QuestIds.NPC_MENTOR,
            1,
            new QuestLocation(QuestIds.AREA_VILLAGE, QuestMarkerType.NPC,
                QuestIds.TARGET_MENTOR, "Mentor")),
        new QuestObjectiveDefinition(
            QuestIds.OBJ_COLLECT_COPPER,
            "Colete " + OpeningQuest.COPPER_ORE_REQUIRED + " Minerios de Cobre",
            QuestObjectiveType.COLLECT_ITEM,
            QuestIds.ITEM_COPPER_ORE,
            OpeningQuest.COPPER_ORE_REQUIRED,
            new QuestLocation(QuestIds.AREA_MINING, QuestMarkerType.MINING_AREA,
                QuestIds.TARGET_COPPER_VEINS, "Veios de Cobre")),
        new QuestObjectiveDefinition(
            QuestIds.OBJ_TALK_MENTOR_TURNIN,
            "Entregue o cobre ao Mentor",
            QuestObjectiveType.TALK_TO_NPC,
            QuestIds.NPC_MENTOR,
            1,
            new QuestLocation(QuestIds.AREA_VILLAGE, QuestMarkerType.NPC,
                QuestIds.TARGET_MENTOR, "Mentor"))
    ));

    private QuestRegistry() {
    }

    private static QuestDefinition register(QuestDefinition def) {
        BY_ID.put(def.id, def);
        return def;
    }

    public static QuestDefinition get(String id) {
        return BY_ID.get(id);
    }

    public static Collection<QuestDefinition> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }
}
