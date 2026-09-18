package com.donos.zebra.quests;

import com.donos.zebra.entities.MentorNpc;
import com.donos.zebra.entities.Player;
import com.donos.zebra.items.ItemDefinition;
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
    /** Optional player for milestone unlock side-effects. */
    private Player progressPlayer;

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

    public void setProgressPlayer(Player progressPlayer) {
        this.progressPlayer = progressPlayer;
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

    public boolean isCompleted(String questId) {
        QuestInstance qi = instances.get(questId);
        return qi != null && qi.status == QuestStatus.COMPLETED;
    }

    public boolean isShopUnlocked() {
        return isCompleted(QuestIds.QUEST_ORC_CLEANUP);
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

    public QuestObjectiveDefinition getCurrentObjective() {
        return getTrackedObjectiveDefinition();
    }

    public QuestObjectiveProgress getCurrentObjectiveProgress() {
        QuestInstance qi = getTrackedInstance();
        return qi == null ? null : qi.getCurrentProgress();
    }

    /** Next inactive mentor-chain quest whose prerequisite is met, or null. */
    public QuestDefinition findOfferableMentorQuest() {
        for (String id : QuestRegistry.MENTOR_CHAIN_AFTER_FIRST_SWORD) {
            QuestDefinition def = QuestRegistry.get(id);
            QuestInstance qi = instances.get(id);
            if (def == null || qi == null) {
                continue;
            }
            if (qi.status != QuestStatus.INACTIVE) {
                continue;
            }
            if (QuestRegistry.isPrerequisiteMet(this, def)) {
                return def;
            }
        }
        return null;
    }

    /** Active mentor-chain quest waiting on a talk turn-in with the mentor, or null. */
    public QuestDefinition findActiveMentorTurnIn() {
        for (QuestInstance qi : instances.values()) {
            if (qi.status != QuestStatus.ACTIVE) {
                continue;
            }
            QuestDefinition def = QuestRegistry.get(qi.questId);
            if (def == null) {
                continue;
            }
            QuestObjectiveDefinition obj = def.getObjective(qi.currentObjectiveIndex);
            if (obj != null
                && obj.type == QuestObjectiveType.TALK_TO_NPC
                && QuestIds.NPC_MENTOR.equals(obj.targetId)) {
                return def;
            }
        }
        return null;
    }

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
            p.firedMilestones.clear();
        }
        if (trackedQuestId == null) {
            setTrackedQuestId(questId);
        } else {
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
        applyEvent(QuestObjectiveType.COLLECT_ITEM, itemId, amount, null, null);
    }

    public void reportTalkNpc(String npcId) {
        if (npcId == null) {
            return;
        }
        applyEvent(QuestObjectiveType.TALK_TO_NPC, npcId, 1, null, null);
    }

    public void reportCraftItem(String itemId) {
        if (itemId == null) {
            return;
        }
        applyEvent(QuestObjectiveType.CRAFT_ITEM, itemId, 1, null, null);
    }

    public void reportKillEnemy(String enemyId) {
        reportKillEnemy(enemyId, null, null);
    }

    public void reportKillEnemy(String enemyId, String weaponItemId, String skillId) {
        if (enemyId == null) {
            return;
        }
        applyEvent(QuestObjectiveType.KILL_ENEMY, enemyId, 1, weaponItemId, skillId);
    }

    public void reportReachLocation(String locationId) {
        if (locationId == null) {
            return;
        }
        applyEvent(QuestObjectiveType.REACH_LOCATION, locationId, 1, null, null);
    }

    /**
     * Grants {@link QuestDefinition#reward} to the player (silver, items, unlocks).
     */
    public void grantReward(QuestDefinition def, Player player) {
        if (def == null || def.reward == null || player == null) {
            return;
        }
        QuestReward reward = def.reward;
        if (reward.silver > 0) {
            player.getWallet().addSilver(reward.silver);
        }
        for (QuestReward.ItemGrant grant : reward.items) {
            ItemDefinition item = ItemRegistry.getItem(grant.itemId);
            if (item != null) {
                player.getInventory().addItem(item, grant.quantity);
            }
        }
        for (String recipeId : reward.unlockRecipeIds) {
            player.unlockRecipe(recipeId);
        }
        for (String skillId : reward.unlockSkillIds) {
            player.getSkillBook().unlock(skillId);
        }
    }

    private void applyEvent(QuestObjectiveType type,
                            String targetId,
                            int amount,
                            String weaponItemId,
                            String skillId) {
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
            if (type == QuestObjectiveType.KILL_ENEMY) {
                if (objDef.weaponItemId != null
                    && (weaponItemId == null || !objDef.weaponItemId.equals(weaponItemId))) {
                    continue;
                }
                if (objDef.skillId != null
                    && (skillId == null || !objDef.skillId.equals(skillId))) {
                    continue;
                }
            }
            int before = progress.currentAmount;
            progress.currentAmount = Math.min(objDef.requiredAmount, progress.currentAmount + amount);
            if (progress.currentAmount != before) {
                fireMilestones(def, objDef, progress);
                if (listener != null) {
                    listener.onObjectiveProgress(def, objDef, progress.currentAmount, objDef.requiredAmount);
                }
            }
            if (progress.currentAmount >= objDef.requiredAmount) {
                completeCurrentObjective(qi, def, objDef, progress);
            }
        }
    }

    private void fireMilestones(QuestDefinition def,
                                QuestObjectiveDefinition objDef,
                                QuestObjectiveProgress progress) {
        if (objDef.progressMilestones.isEmpty()) {
            return;
        }
        for (QuestProgressMilestone milestone : objDef.progressMilestones) {
            if (progress.currentAmount < milestone.atAmount) {
                continue;
            }
            if (!progress.firedMilestones.add(milestone.atAmount)) {
                continue;
            }
            applyMilestoneEffect(milestone);
            if (listener != null) {
                listener.onProgressMilestone(def, objDef, milestone);
            }
        }
    }

    private void applyMilestoneEffect(QuestProgressMilestone milestone) {
        if (progressPlayer == null || milestone == null) {
            return;
        }
        switch (milestone.effect) {
            case UNLOCK_SKILL:
                progressPlayer.getSkillBook().unlock(milestone.targetId);
                break;
            case UNLOCK_RECIPE:
                progressPlayer.unlockRecipe(milestone.targetId);
                break;
            default:
                break;
        }
    }

    private void completeCurrentObjective(QuestInstance qi,
                                          QuestDefinition def,
                                          QuestObjectiveDefinition objDef,
                                          QuestObjectiveProgress progress) {
        progress.completed = true;
        progress.currentAmount = objDef.requiredAmount;
        // Ensure milestones at requiredAmount still fire if not already
        fireMilestones(def, objDef, progress);
        int next = qi.currentObjectiveIndex + 1;
        if (next >= def.objectives.size()) {
            qi.status = QuestStatus.COMPLETED;
            qi.currentObjectiveIndex = def.objectives.size() - 1;
            if (trackedQuestId != null && trackedQuestId.equals(qi.questId)) {
                trackedQuestId = nextActiveQuestId();
            }
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
                qi.objectives.get(i).firedMilestones.clear();
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
            qi.objectives.get(i).firedMilestones.clear();
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
            data.objectiveCompleted = new boolean[tracked.objectives.size()];
            data.objectiveFiredMilestones = new String[tracked.objectives.size()];
            for (int i = 0; i < tracked.objectives.size(); i++) {
                QuestObjectiveProgress p = tracked.objectives.get(i);
                data.objectiveProgress[i] = p.currentAmount;
                data.objectiveCompleted[i] = p.completed;
                data.objectiveFiredMilestones[i] = encodeMilestones(p);
            }
        } else {
            data.currentObjectiveIndex = 0;
            data.objectiveProgress = null;
            data.objectiveCompleted = null;
            data.objectiveFiredMilestones = null;
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
            if (qi.status != QuestStatus.COMPLETED) {
                qi.status = QuestStatus.ACTIVE;
                trackedQuestId = data.activeQuestId;
                if (data.objectiveProgress != null
                    && data.objectiveProgress.length == qi.objectives.size()) {
                    for (int i = 0; i < qi.objectives.size(); i++) {
                        if (QuestIds.QUEST_FIRST_SWORD.equals(qi.questId)) {
                            continue;
                        }
                        qi.objectives.get(i).currentAmount = Math.max(0, data.objectiveProgress[i]);
                        if (data.objectiveCompleted != null
                            && i < data.objectiveCompleted.length) {
                            qi.objectives.get(i).completed = data.objectiveCompleted[i];
                        }
                        if (data.objectiveFiredMilestones != null
                            && i < data.objectiveFiredMilestones.length) {
                            decodeMilestones(qi.objectives.get(i), data.objectiveFiredMilestones[i]);
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

    private static String encodeMilestones(QuestObjectiveProgress p) {
        if (p.firedMilestones.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Integer v : p.firedMilestones) {
            if (!first) {
                sb.append(',');
            }
            sb.append(v);
            first = false;
        }
        return sb.toString();
    }

    private static void decodeMilestones(QuestObjectiveProgress p, String encoded) {
        p.firedMilestones.clear();
        if (encoded == null || encoded.isEmpty()) {
            return;
        }
        for (String part : encoded.split(",")) {
            try {
                p.firedMilestones.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
    }
}
