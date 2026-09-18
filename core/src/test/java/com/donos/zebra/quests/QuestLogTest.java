package com.donos.zebra.quests;

import com.donos.zebra.entities.MentorNpc;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.save.QuestSaveData;
import com.donos.zebra.world.OpeningQuest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestLogTest {

    @Test
    void firstSwordQuestStartsAndAdvancesThroughObjectives() {
        QuestLog log = new QuestLog();
        AtomicInteger starts = new AtomicInteger();
        AtomicInteger completes = new AtomicInteger();
        log.setListener(new QuestLogListener() {
            @Override
            public void onQuestStarted(QuestDefinition quest) {
                starts.incrementAndGet();
            }

            @Override
            public void onQuestCompleted(QuestDefinition quest) {
                completes.incrementAndGet();
            }
        });
        log.startQuest(QuestIds.QUEST_FIRST_SWORD);
        assertEquals(1, starts.get());
        QuestInstance qi = log.getTrackedInstance();
        assertNotNull(qi);
        assertEquals(0, qi.currentObjectiveIndex);
        assertEquals(0, qi.getCurrentProgress().currentAmount);

        log.reportTalkNpc(QuestIds.NPC_MENTOR);
        assertTrue(qi.objectives.get(0).completed);
        assertEquals(1, qi.currentObjectiveIndex);

        log.reportCollectItem(QuestIds.ITEM_COPPER_ORE, 1);
        assertEquals(1, qi.objectives.get(1).currentAmount);
        log.reportCollectItem(QuestIds.ITEM_COPPER_ORE, 1);
        assertEquals(2, qi.objectives.get(1).currentAmount);
        log.reportCollectItem(QuestIds.ITEM_COPPER_ORE, 1);
        assertTrue(qi.objectives.get(1).completed);
        assertEquals(OpeningQuest.COPPER_ORE_REQUIRED, qi.objectives.get(1).currentAmount);
        assertEquals(2, qi.currentObjectiveIndex);

        log.reportTalkNpc(QuestIds.NPC_MENTOR);
        assertEquals(QuestStatus.COMPLETED, qi.status);
        assertEquals(1, completes.get());
    }

    @Test
    void collectCannotExceedRequiredAmount() {
        QuestLog log = new QuestLog();
        log.startQuest(QuestIds.QUEST_FIRST_SWORD);
        log.reportTalkNpc(QuestIds.NPC_MENTOR);
        log.reportCollectItem(QuestIds.ITEM_COPPER_ORE, 10);
        QuestInstance qi = log.getInstance(QuestIds.QUEST_FIRST_SWORD);
        assertEquals(OpeningQuest.COPPER_ORE_REQUIRED, qi.objectives.get(1).currentAmount);
        assertTrue(qi.objectives.get(1).completed);
    }

    @Test
    void killAndCraftEventsMatchObjectiveTypes() {
        QuestLog log = new QuestLog();
        // Synthetic quest progress via registry FIRST_SWORD only has talk/collect —
        // verify report methods are no-ops when type mismatches and still safe.
        log.startQuest(QuestIds.QUEST_FIRST_SWORD);
        log.reportKillEnemy(QuestIds.ENEMY_ORC);
        log.reportCraftItem(ItemRegistry.IRON_SWORD.getId());
        assertEquals(0, log.getTrackedInstance().currentObjectiveIndex);
    }

    @Test
    void syncFromWorldMarksCompletedWhenPlayerHasSword() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.grantFirstSword();
        MentorNpc mentor = new MentorNpc(0, 0, null,
            TestAnimationFactory.createDirectionalAnimations());
        mentor.setGavePickaxe(true);

        QuestLog log = new QuestLog();
        log.syncFromWorld(player, mentor);
        assertEquals(QuestStatus.COMPLETED, log.getInstance(QuestIds.QUEST_FIRST_SWORD).status);
    }

    @Test
    void syncFromWorldRestoresCollectProgressFromInventory() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.getInventory().addItem(ItemRegistry.STONE_PICKAXE, 1);
        player.getInventory().addItem(ItemRegistry.COPPER_ORE, 2);
        MentorNpc mentor = new MentorNpc(0, 0, null,
            TestAnimationFactory.createDirectionalAnimations());
        mentor.setGavePickaxe(true);

        QuestLog log = new QuestLog();
        log.syncFromWorld(player, mentor);
        QuestInstance qi = log.getInstance(QuestIds.QUEST_FIRST_SWORD);
        assertEquals(QuestStatus.ACTIVE, qi.status);
        assertEquals(1, qi.currentObjectiveIndex);
        assertEquals(2, qi.objectives.get(1).currentAmount);
        assertFalse(qi.objectives.get(1).completed);
    }

    @Test
    void saveAndLoadRestoresActiveQuestProgress() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        MentorNpc mentor = new MentorNpc(0, 0, null,
            TestAnimationFactory.createDirectionalAnimations());

        QuestLog log = new QuestLog();
        log.startQuest(QuestIds.QUEST_FIRST_SWORD);
        log.reportTalkNpc(QuestIds.NPC_MENTOR);
        log.reportCollectItem(QuestIds.ITEM_COPPER_ORE, 2);
        mentor.setGavePickaxe(true);
        player.getInventory().addItem(ItemRegistry.STONE_PICKAXE, 1);
        player.getInventory().addItem(ItemRegistry.COPPER_ORE, 2);

        QuestSaveData data = new QuestSaveData();
        data.mentorGavePickaxe = true;
        log.writeToSave(data);

        QuestLog loaded = new QuestLog();
        loaded.readFromSave(data, player, mentor);
        QuestInstance qi = loaded.getInstance(QuestIds.QUEST_FIRST_SWORD);
        assertEquals(QuestStatus.ACTIVE, qi.status);
        assertEquals(1, qi.currentObjectiveIndex);
        assertEquals(2, qi.objectives.get(1).currentAmount);
    }

    @Test
    void currentObjectiveAdvancesAfterTalkToMentor() {
        QuestLog log = new QuestLog();
        log.startQuest(QuestIds.QUEST_FIRST_SWORD);
        QuestObjectiveDefinition before = log.getCurrentObjective();
        assertNotNull(before);
        assertEquals(QuestObjectiveType.TALK_TO_NPC, before.type);
        assertEquals(QuestIds.NPC_MENTOR, before.targetId);

        log.reportTalkNpc(QuestIds.NPC_MENTOR);

        QuestObjectiveDefinition after = log.getCurrentObjective();
        assertNotNull(after);
        assertEquals(QuestObjectiveType.COLLECT_ITEM, after.type);
        assertEquals(QuestIds.ITEM_COPPER_ORE, after.targetId);
        assertEquals(OpeningQuest.COPPER_ORE_REQUIRED, after.requiredAmount);
        assertEquals(0, log.getCurrentObjectiveProgress().currentAmount);
        assertFalse(log.getCurrentObjectiveProgress().completed);
    }

    @Test
    void currentObjectiveTracksCopperCollectionProgress() {
        QuestLog log = new QuestLog();
        log.startQuest(QuestIds.QUEST_FIRST_SWORD);
        log.reportTalkNpc(QuestIds.NPC_MENTOR);

        log.reportCollectItem(QuestIds.ITEM_COPPER_ORE, 1);
        assertEquals(1, log.getCurrentObjectiveProgress().currentAmount);
        assertEquals(QuestIds.ITEM_COPPER_ORE, log.getCurrentObjective().targetId);

        log.reportCollectItem(QuestIds.ITEM_COPPER_ORE, 1);
        assertEquals(2, log.getCurrentObjectiveProgress().currentAmount);

        log.reportCollectItem(QuestIds.ITEM_COPPER_ORE, 1);
        assertTrue(log.getInstance(QuestIds.QUEST_FIRST_SWORD).objectives.get(1).completed);
        QuestObjectiveDefinition delivery = log.getCurrentObjective();
        assertNotNull(delivery);
        assertEquals(QuestObjectiveType.TALK_TO_NPC, delivery.type);
        assertEquals(QuestIds.NPC_MENTOR, delivery.targetId);
        assertFalse(log.getCurrentObjectiveProgress().completed);
    }

    @Test
    void currentObjectiveIsDeliveryUntilQuestCompletes() {
        QuestLog log = new QuestLog();
        log.startQuest(QuestIds.QUEST_FIRST_SWORD);
        log.reportTalkNpc(QuestIds.NPC_MENTOR);
        log.reportCollectItem(QuestIds.ITEM_COPPER_ORE, OpeningQuest.COPPER_ORE_REQUIRED);
        assertEquals(2, log.getTrackedInstance().currentObjectiveIndex);
        assertEquals("Entregue o cobre ao Mentor", log.getCurrentObjective().description);

        log.reportTalkNpc(QuestIds.NPC_MENTOR);
        assertEquals(QuestStatus.COMPLETED, log.getInstance(QuestIds.QUEST_FIRST_SWORD).status);
        assertNull(log.getTrackedInstance());
        assertNull(log.getCurrentObjective());
    }

    @Test
    void listenerSeesAdvancedObjectiveIndexAfterCompletion() {
        QuestLog log = new QuestLog();
        AtomicInteger observedIndex = new AtomicInteger(-1);
        log.setListener(new QuestLogListener() {
            @Override
            public void onObjectiveCompleted(QuestDefinition quest, QuestObjectiveDefinition objective) {
                QuestInstance qi = log.getInstance(quest.id);
                observedIndex.set(qi.currentObjectiveIndex);
            }
        });
        log.startQuest(QuestIds.QUEST_FIRST_SWORD);
        log.reportTalkNpc(QuestIds.NPC_MENTOR);
        assertEquals(1, observedIndex.get());
        assertEquals(QuestObjectiveType.COLLECT_ITEM, log.getCurrentObjective().type);
    }

    @Test
    void currentObjectiveLocationPointsToMiningForCopper() {
        QuestLog log = new QuestLog();
        log.startQuest(QuestIds.QUEST_FIRST_SWORD);
        log.reportTalkNpc(QuestIds.NPC_MENTOR);
        QuestLocation loc = QuestMapModel.currentObjectiveLocation(log);
        assertNotNull(loc);
        assertEquals(QuestIds.AREA_MINING, loc.areaId);
        assertEquals(QuestMarkerType.MINING_AREA, loc.markerType);
        assertEquals(QuestIds.TARGET_COPPER_VEINS, loc.targetId);
        List<QuestMapMarker> markers = QuestMapModel.worldMarkers(null, 10f, 20f, List.of(), loc);
        assertTrue(markers.isEmpty());
    }

    @Test
    void forgeLocationIsDistinctFromTavernTarget() {
        QuestLocation forge = new QuestLocation(
            QuestIds.AREA_TAVERN, QuestMarkerType.FORGE, QuestIds.TARGET_FORGE, "Forja");
        assertEquals(QuestMarkerType.FORGE, forge.markerType);
        assertEquals(QuestIds.TARGET_FORGE, forge.targetId);
        assertFalse(QuestIds.TARGET_TAVERN.equals(forge.targetId));
    }
}
