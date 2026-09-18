package com.donos.zebra.quests;

import com.donos.zebra.entities.MentorNpc;
import com.donos.zebra.entities.Player;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.save.QuestSaveData;
import com.donos.zebra.world.OpeningQuest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns quest instances, progress events, and tracked (HUD) quest.
 * Gameplay systems report events here; UI only observes.
 */
public final class QuestLog {

    private final Map<String, QuestInstance> instances = new LinkedHashMap<>();
    private String trackedQuestId;
    private QuestLogListener listener;

    public QuestLog() {
        for (QuestDefinition def : QuestRegistry.all()) {
            instances.put(def.id, new QuestInstance(def));
        }
    }

    public void setListener(QuestLogListener listener) {
        this.listener = listener;
    }

    public QuestLogListener getListener() {
        return listener;
    }

    public String getTrackedQuestId() {
        return trackedQuestId;
    }

    public void setTrackedQuestId(String questId) {
        this.trackedQuestId = questId;
        if (listener != null) {
            listener.onTrackedQuestChanged(questId);
        }
    }

    public QuestInstance getInstance(String questId) {
        return instances.get(questId);
    }

    public Collection<QuestInstance> allInstances() {
        return Collections.unmodifiableCollection(instances.values());
    }

    public List<QuestInstance> getActiveQuests() {
        List<QuestInstance> list = new ArrayList<>();
        for (QuestInstance qi : instances.values()) {
            if (qi.status == QuestStatus.ACTIVE) {
                list.add(qi);
            }
        }
        return list;
    }

    public List<QuestInstance> getCompletedQuests() {
        List<QuestInstance> list = new ArrayList<>();
        for (QuestInstance qi : instances.values()) {
            if (qi.status == QuestStatus.COMPLETED) {
                list.add(qi);
            }
        }
        return list;
    }

    public QuestInstance getTrackedInstance() {
        if (trackedQuestId == null) {
            return null;
        }
        QuestInstance qi = instances.get(trackedQuestId);
        if (qi == null || qi.status != QuestStatus.ACTIVE) {
            return null;
        }
        return qi;
    }

    public QuestDefinition getTrackedDefinition() {
        QuestInstance qi = getTrackedInstance();
        return qi == null ? null : QuestRegistry.get(qi.questId);
    }

    public QuestObjectiveDefinition getTrackedObjectiveDefinition() {
        QuestInstance qi = getTrackedInstance();
        QuestDefinition def = getTrackedDefinition();
        if (qi == null || def == null) {
            return null;
        }
        return def.getObjective(qi.currentObjectiveIndex);
    }

    /** Current active objective of the tracked quest, or null. */
    public QuestObjectiveDefinition getCurrentObjective() {
        return getTrackedObjectiveDefinition();
    }

    /** Progress row for {@link #getCurrentObjective()}, or null. */
    public QuestObjectiveProgress getCurrentObjectiveProgress() {
        QuestInstance qi = getTrackedInstance();
        return qi == null ? null : qi.getCurrentProgress();
    }

    /** Starts a quest and tracks it if nothing else is tracked. */
    public void startQuest(String questId) {
        QuestDefinition def = QuestRegistry.get(questId);
        QuestInstance qi = instances.get(questId);
        if (def == null || qi == null || qi.status == QuestStatus.COMPLETED) {
            return;
        }
        if (qi.status == QuestStatus.ACTIVE) {
            return;
        }
        qi.status = QuestStatus.ACTIVE;
        qi.currentObjectiveIndex = 0;
        for (QuestObjectiveProgress p : qi.objectives) {
            p.currentAmount = 0;
            p.completed = false;
        }
        if (trackedQuestId == null) {
            setTrackedQuestId(questId);
        }
        if (listener != null) {
            listener.onQuestStarted(def);
        }
    }

    public void reportCollectItem(String itemId, int amount) {
        if (itemId == null || amount <= 0) {
            return;
        }
        applyEvent(QuestObjectiveType.COLLECT_ITEM, itemId, amount);
    }

    public void reportTalkNpc(String npcId) {
        if (npcId == null) {
            return;
        }
        applyEvent(QuestObjectiveType.TALK_TO_NPC, npcId, 1);
    }

    public void reportCraftItem(String itemId) {
        if (itemId == null) {
            return;
        }
        applyEvent(QuestObjectiveType.CRAFT_ITEM, itemId, 1);
    }

    public void reportKillEnemy(String enemyId) {
        if (enemyId == null) {
            return;
        }
        applyEvent(QuestObjectiveType.KILL_ENEMY, enemyId, 1);
    }

    public void reportReachLocation(String locationId) {
        if (locationId == null) {
            return;
        }
        applyEvent(QuestObjectiveType.REACH_LOCATION, locationId, 1);
    }

    private void applyEvent(QuestObjectiveType type, String targetId, int amount) {
        for (QuestInstance qi : instances.values()) {
            if (qi.status != QuestStatus.ACTIVE) {
                continue;
            }
            QuestDefinition def = QuestRegistry.get(qi.questId);
            if (def == null) {
                continue;
            }
            QuestObjectiveDefinition objDef = def.getObjective(qi.currentObjectiveIndex);
            QuestObjectiveProgress progress = qi.getCurrentProgress();
            if (objDef == null || progress == null || progress.completed) {
                continue;
            }
            if (objDef.type != type) {
                continue;
            }
            if (!targetId.equals(objDef.targetId)) {
                continue;
            }
            int before = progress.currentAmount;
            progress.currentAmount = Math.min(objDef.requiredAmount, progress.currentAmount + amount);
            if (progress.currentAmount != before && listener != null) {
                listener.onObjectiveProgress(def, objDef, progress.currentAmount, objDef.requiredAmount);
            }
            if (progress.currentAmount >= objDef.requiredAmount) {
                completeCurrentObjective(qi, def, objDef, progress);
            }
        }
    }

    private void completeCurrentObjective(QuestInstance qi,
                                          QuestDefinition def,
                                          QuestObjectiveDefinition objDef,
                                          QuestObjectiveProgress progress) {
        progress.completed = true;
        progress.currentAmount = objDef.requiredAmount;
        int next = qi.currentObjectiveIndex + 1;
        if (next >= def.objectives.size()) {
            qi.status = QuestStatus.COMPLETED;
            qi.currentObjectiveIndex = def.objectives.size() - 1;
            if (trackedQuestId != null && trackedQuestId.equals(qi.questId)) {
                trackedQuestId = nextActiveQuestId();
            }
            // Notify after domain state is fully updated so UI sees the final objective / completion.
            if (listener != null) {
                listener.onObjectiveCompleted(def, objDef);
                if (trackedQuestId == null || !trackedQuestId.equals(qi.questId)) {
                    listener.onTrackedQuestChanged(trackedQuestId);
                }
                listener.onQuestCompleted(def);
            }
        } else {
            qi.currentObjectiveIndex = next;
            if (listener != null) {
                listener.onObjectiveCompleted(def, objDef);
            }
        }
    }

    private String nextActiveQuestId() {
        for (QuestInstance qi : instances.values()) {
            if (qi.status == QuestStatus.ACTIVE) {
                return qi.questId;
            }
        }
        return null;
    }

    /**
     * Aligns quest state with mentor/sword/inventory after load or bootstrap
     * without inventing new story beats.
     */
    public void syncFromWorld(Player player, MentorNpc mentor) {
        QuestInstance qi = instances.get(QuestIds.QUEST_FIRST_SWORD);
        QuestDefinition def = QuestRegistry.FIRST_SWORD;
        if (qi == null || def == null || player == null) {
            return;
        }

        boolean gavePickaxe = mentor != null && mentor.hasGavePickaxe();
        boolean hasSword = player.hasFirstSword();
        int copper = player.getInventory().getItemCount(ItemRegistry.COPPER_ORE);

        if (hasSword) {
            qi.status = QuestStatus.COMPLETED;
            qi.currentObjectiveIndex = def.objectives.size() - 1;
            for (int i = 0; i < qi.objectives.size(); i++) {
                QuestObjectiveProgress p = qi.objectives.get(i);
                QuestObjectiveDefinition od = def.getObjective(i);
                p.completed = true;
                p.currentAmount = od != null ? od.requiredAmount : 1;
            }
            if (QuestIds.QUEST_FIRST_SWORD.equals(trackedQuestId)) {
                trackedQuestId = nextActiveQuestId();
            }
            return;
        }

        qi.status = QuestStatus.ACTIVE;
        if (trackedQuestId == null) {
            trackedQuestId = QuestIds.QUEST_FIRST_SWORD;
        }

        if (!gavePickaxe) {
            qi.currentObjectiveIndex = 0;
            resetFrom(qi, 0);
            return;
        }

        // Pickaxe given: intro talk done
        markCompleted(qi, def, 0);
        if (copper >= OpeningQuest.COPPER_ORE_REQUIRED) {
            markCompleted(qi, def, 1);
            qi.objectives.get(1).currentAmount = OpeningQuest.COPPER_ORE_REQUIRED;
            qi.currentObjectiveIndex = 2;
            qi.objectives.get(2).currentAmount = 0;
            qi.objectives.get(2).completed = false;
        } else {
            qi.currentObjectiveIndex = 1;
            qi.objectives.get(1).currentAmount = Math.min(OpeningQuest.COPPER_ORE_REQUIRED, copper);
            qi.objectives.get(1).completed = false;
            for (int i = 2; i < qi.objectives.size(); i++) {
                qi.objectives.get(i).currentAmount = 0;
                qi.objectives.get(i).completed = false;
            }
        }
    }

    private static void markCompleted(QuestInstance qi, QuestDefinition def, int index) {
        QuestObjectiveProgress p = qi.objectives.get(index);
        QuestObjectiveDefinition od = def.getObjective(index);
        p.completed = true;
        p.currentAmount = od != null ? od.requiredAmount : 1;
    }

    private static void resetFrom(QuestInstance qi, int fromIndex) {
        for (int i = fromIndex; i < qi.objectives.size(); i++) {
            qi.objectives.get(i).currentAmount = 0;
            qi.objectives.get(i).completed = false;
        }
    }

    public void writeToSave(QuestSaveData data) {
        if (data == null) {
            return;
        }
        data.activeQuestId = trackedQuestId;
        QuestInstance tracked = getTrackedInstance();
        if (tracked != null) {
            data.currentObjectiveIndex = tracked.currentObjectiveIndex;
            data.objectiveProgress = new int[tracked.objectives.size()];
            for (int i = 0; i < tracked.objectives.size(); i++) {
                data.objectiveProgress[i] = tracked.objectives.get(i).currentAmount;
            }
            data.objectiveCompleted = new boolean[tracked.objectives.size()];
            for (int i = 0; i < tracked.objectives.size(); i++) {
                data.objectiveCompleted[i] = tracked.objectives.get(i).completed;
            }
        } else {
            data.currentObjectiveIndex = 0;
            data.objectiveProgress = null;
            data.objectiveCompleted = null;
        }
        List<String> completed = new ArrayList<>();
        for (QuestInstance qi : instances.values()) {
            if (qi.status == QuestStatus.COMPLETED) {
                completed.add(qi.questId);
            }
        }
        data.completedQuestIds = completed.toArray(new String[0]);
    }

    public void readFromSave(QuestSaveData data, Player player, MentorNpc mentor) {
        // Prefer world flags for opening quest integrity, then overlay saved counters.
        syncFromWorld(player, mentor);
        if (data == null) {
            return;
        }
        if (data.completedQuestIds != null) {
            for (String id : data.completedQuestIds) {
                QuestInstance qi = instances.get(id);
                QuestDefinition def = QuestRegistry.get(id);
                if (qi == null || def == null) {
                    continue;
                }
                qi.status = QuestStatus.COMPLETED;
                qi.currentObjectiveIndex = Math.max(0, def.objectives.size() - 1);
                for (int i = 0; i < qi.objectives.size(); i++) {
                    QuestObjectiveDefinition od = def.getObjective(i);
                    qi.objectives.get(i).completed = true;
                    qi.objectives.get(i).currentAmount = od != null ? od.requiredAmount : 1;
                }
            }
        }
        if (data.activeQuestId != null && instances.containsKey(data.activeQuestId)) {
            QuestInstance qi = instances.get(data.activeQuestId);
            if (qi.status == QuestStatus.ACTIVE) {
                trackedQuestId = data.activeQuestId;
                if (data.objectiveProgress != null
                    && data.objectiveProgress.length == qi.objectives.size()) {
                    for (int i = 0; i < qi.objectives.size(); i++) {
                        // Do not regress opening-quest sync from world flags
                        if (QuestIds.QUEST_FIRST_SWORD.equals(qi.questId)) {
                            continue;
                        }
                        qi.objectives.get(i).currentAmount = Math.max(0, data.objectiveProgress[i]);
                        if (data.objectiveCompleted != null
                            && i < data.objectiveCompleted.length) {
                            qi.objectives.get(i).completed = data.objectiveCompleted[i];
                        }
                    }
                }
                if (data.currentObjectiveIndex >= 0
                    && data.currentObjectiveIndex < qi.objectives.size()
                    && !QuestIds.QUEST_FIRST_SWORD.equals(qi.questId)) {
                    qi.currentObjectiveIndex = data.currentObjectiveIndex;
                }
            }
        }
        if (trackedQuestId == null) {
            trackedQuestId = nextActiveQuestId();
        }
    }
}
