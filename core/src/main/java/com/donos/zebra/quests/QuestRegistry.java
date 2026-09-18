package com.donos.zebra.quests;

import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.skills.SkillRegistry;
import com.donos.zebra.world.OpeningQuest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catalog of authored quests.
 */
public final class QuestRegistry {

    private static final Map<String, QuestDefinition> BY_ID = new LinkedHashMap<>();

    private static final QuestLocation LOC_MENTOR = new QuestLocation(
        QuestIds.AREA_VILLAGE, QuestMarkerType.NPC, QuestIds.TARGET_MENTOR, "Mentor");
    private static final QuestLocation LOC_ORCS = new QuestLocation(
        QuestIds.AREA_VILLAGE, QuestMarkerType.QUEST_OBJECTIVE, QuestIds.TARGET_ORCS, "Orcs");
    private static final QuestLocation LOC_FORGE = new QuestLocation(
        QuestIds.AREA_TAVERN, QuestMarkerType.FORGE, QuestIds.TARGET_FORGE, "Forja");

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
            LOC_MENTOR),
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
            LOC_MENTOR)
    ));

    public static final QuestDefinition ORC_CLEANUP = register(new QuestDefinition(
        QuestIds.QUEST_ORC_CLEANUP,
        "Limpeza de Orcs",
        "Elimine os orcs que ameacam a vila e reporte ao Mentor.",
        QuestIds.QUEST_FIRST_SWORD,
        QuestReward.silverOnly(50),
        QuestObjectiveDefinition.killEnemy(
            QuestIds.OBJ_KILL_ORCS_CLEANUP,
            "Derrote 5 Orcs",
            QuestIds.ENEMY_ORC,
            5,
            LOC_ORCS,
            null,
            null),
        new QuestObjectiveDefinition(
            QuestIds.OBJ_TALK_ORC_CLEANUP,
            "Fale com o Mentor",
            QuestObjectiveType.TALK_TO_NPC,
            QuestIds.NPC_MENTOR,
            1,
            LOC_MENTOR)
    ));

    public static final QuestDefinition COPPER_CHESTPLATE = register(new QuestDefinition(
        QuestIds.QUEST_COPPER_CHESTPLATE,
        "Peitoral de Cobre",
        "Forje um peitoral de cobre na forja da taverna.",
        QuestIds.QUEST_ORC_CLEANUP,
        QuestReward.silverOnly(30),
        new QuestObjectiveDefinition(
            QuestIds.OBJ_CRAFT_CHESTPLATE,
            "Crie um Peitoral de Cobre",
            QuestObjectiveType.CRAFT_ITEM,
            QuestIds.ITEM_COPPER_CHESTPLATE,
            1,
            LOC_FORGE),
        new QuestObjectiveDefinition(
            QuestIds.OBJ_TALK_CHESTPLATE,
            "Mostre o peitoral ao Mentor",
            QuestObjectiveType.TALK_TO_NPC,
            QuestIds.NPC_MENTOR,
            1,
            LOC_MENTOR)
    ));

    public static final QuestDefinition FULL_COPPER_SET = register(new QuestDefinition(
        QuestIds.QUEST_FULL_COPPER_SET,
        "Conjunto de Cobre Completo",
        "Complete o restante da armadura de cobre: capacete, luvas e botas.",
        QuestIds.QUEST_COPPER_CHESTPLATE,
        QuestReward.silverAndRecipe(60, QuestIds.RECIPE_IRON_SWORD),
        new QuestObjectiveDefinition(
            QuestIds.OBJ_CRAFT_HELMET,
            "Crie um Capacete de Cobre",
            QuestObjectiveType.CRAFT_ITEM,
            QuestIds.ITEM_COPPER_HELMET,
            1,
            LOC_FORGE),
        new QuestObjectiveDefinition(
            QuestIds.OBJ_CRAFT_GLOVES,
            "Crie Luvas de Cobre",
            QuestObjectiveType.CRAFT_ITEM,
            QuestIds.ITEM_COPPER_GLOVES,
            1,
            LOC_FORGE),
        new QuestObjectiveDefinition(
            QuestIds.OBJ_CRAFT_BOOTS,
            "Crie Botas de Cobre",
            QuestObjectiveType.CRAFT_ITEM,
            QuestIds.ITEM_COPPER_BOOTS,
            1,
            LOC_FORGE),
        new QuestObjectiveDefinition(
            QuestIds.OBJ_TALK_FULL_SET,
            "Fale com o Mentor",
            QuestObjectiveType.TALK_TO_NPC,
            QuestIds.NPC_MENTOR,
            1,
            LOC_MENTOR)
    ));

    public static final QuestDefinition IRON_BLADE_TRIAL = register(new QuestDefinition(
        QuestIds.QUEST_IRON_BLADE_TRIAL,
        "Prova da Lamina de Ferro",
        "Derrote orcs com a Espada de Ferro equipada para despertar o Redemoinho.",
        QuestIds.QUEST_FULL_COPPER_SET,
        QuestReward.silverAndSkill(80, SkillRegistry.WHIRLWIND_ID),
        QuestObjectiveDefinition.killEnemy(
            QuestIds.OBJ_KILL_IRON_BLADE,
            "Derrote 10 Orcs com a Espada de Ferro",
            QuestIds.ENEMY_ORC,
            10,
            LOC_ORCS,
            QuestIds.ITEM_IRON_SWORD,
            null),
        new QuestObjectiveDefinition(
            QuestIds.OBJ_TALK_IRON_BLADE,
            "Reporte ao Mentor",
            QuestObjectiveType.TALK_TO_NPC,
            QuestIds.NPC_MENTOR,
            1,
            LOC_MENTOR)
    ));

    public static final QuestDefinition WHIRLWIND_PROVING = register(new QuestDefinition(
        QuestIds.QUEST_WHIRLWIND_PROVING,
        "Prova do Redemoinho",
        "Prove o Redemoinho derrotando inimigos com a habilidade.",
        QuestIds.QUEST_IRON_BLADE_TRIAL,
        QuestReward.silverItemsAndSkill(
            100,
            SkillRegistry.FLAME_STRIKE_ID,
            new QuestReward.ItemGrant(ItemRegistry.POTION_MEDIUM.getId(), 10)),
        QuestObjectiveDefinition.killEnemy(
            QuestIds.OBJ_KILL_WHIRLWIND,
            "Derrote 10 inimigos com Redemoinho",
            QuestIds.ENEMY_ORC,
            10,
            LOC_ORCS,
            null,
            SkillRegistry.WHIRLWIND_ID),
        new QuestObjectiveDefinition(
            QuestIds.OBJ_TALK_WHIRLWIND,
            "Fale com o Mentor",
            QuestObjectiveType.TALK_TO_NPC,
            QuestIds.NPC_MENTOR,
            1,
            LOC_MENTOR)
    ));

    public static final QuestDefinition FLAME_STRIKE_PROVING = register(new QuestDefinition(
        QuestIds.QUEST_FLAME_STRIKE_PROVING,
        "Prova do Golpe Flamejante",
        "Prove o Golpe Flamejante derrotando inimigos com a habilidade (incluindo queimadura).",
        QuestIds.QUEST_WHIRLWIND_PROVING,
        new QuestReward(
            100,
            new QuestReward.ItemGrant(ItemRegistry.POTION_MEDIUM.getId(), 10)),
        QuestObjectiveDefinition.killEnemy(
            QuestIds.OBJ_KILL_FLAME_STRIKE,
            "Derrote 10 inimigos com Golpe Flamejante",
            QuestIds.ENEMY_ORC,
            10,
            LOC_ORCS,
            null,
            SkillRegistry.FLAME_STRIKE_ID),
        new QuestObjectiveDefinition(
            QuestIds.OBJ_TALK_FLAME_STRIKE,
            "Fale com o Mentor",
            QuestObjectiveType.TALK_TO_NPC,
            QuestIds.NPC_MENTOR,
            1,
            LOC_MENTOR)
    ));

    /** Early-game mentor chain after First Sword, in offer order. */
    public static final List<String> MENTOR_CHAIN_AFTER_FIRST_SWORD = Collections.unmodifiableList(
        ArraysList(
            QuestIds.QUEST_ORC_CLEANUP,
            QuestIds.QUEST_COPPER_CHESTPLATE,
            QuestIds.QUEST_FULL_COPPER_SET,
            QuestIds.QUEST_IRON_BLADE_TRIAL,
            QuestIds.QUEST_WHIRLWIND_PROVING,
            QuestIds.QUEST_FLAME_STRIKE_PROVING
        ));

    private QuestRegistry() {
    }

    private static List<String> ArraysList(String... ids) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, ids);
        return list;
    }

    private static QuestDefinition register(QuestDefinition def) {
        BY_ID.put(def.id, def);
        return def;
    }

    /** Test-only: register an ephemeral quest (e.g. milestone infrastructure fixtures). */
    static void registerForTest(QuestDefinition def) {
        BY_ID.put(def.id, def);
    }

    /** Test-only: remove a quest registered via {@link #registerForTest}. */
    static void unregisterForTest(String id) {
        BY_ID.remove(id);
    }

    public static QuestDefinition get(String id) {
        return BY_ID.get(id);
    }

    public static Collection<QuestDefinition> all() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }

    public static boolean isPrerequisiteMet(QuestLog log, QuestDefinition def) {
        if (def == null) {
            return false;
        }
        if (def.prerequisiteQuestId == null) {
            return true;
        }
        QuestInstance prereq = log.getInstance(def.prerequisiteQuestId);
        return prereq != null && prereq.status == QuestStatus.COMPLETED;
    }
}
