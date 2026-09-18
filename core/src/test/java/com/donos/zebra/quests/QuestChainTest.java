package com.donos.zebra.quests;

import com.donos.zebra.entities.MentorNpc;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import com.donos.zebra.items.CraftingRecipes;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.save.SaveData;
import com.donos.zebra.save.SaveService;
import com.donos.zebra.save.SaveStateMapper;
import com.donos.zebra.skills.SkillRegistry;
import com.donos.zebra.skills.SkillRuntime;
import com.donos.zebra.ui.DialogueUI;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class QuestChainTest {

    @Test
    void ironSwordRecipeLockedUntilFullCopperSetReward() {
        Player player = newPlayer();
        assertFalse(player.isRecipeUnlocked(CraftingRecipes.COPPER_LONGSWORD));
        player.unlockRecipe(QuestIds.RECIPE_IRON_SWORD);
        assertTrue(player.isRecipeUnlocked(CraftingRecipes.COPPER_LONGSWORD));
    }

    @Test
    void lockedIronSwordRecipeRemainsVisibleInCatalog() {
        assertTrue(CraftingRecipes.all().contains(CraftingRecipes.COPPER_LONGSWORD));
        assertTrue(CraftingRecipes.requiresUnlock(CraftingRecipes.COPPER_LONGSWORD.getId()));
        Player player = newPlayer();
        assertFalse(CraftingRecipes.isCraftable(
            CraftingRecipes.COPPER_LONGSWORD, player.getUnlockedRecipeIds()));
        player.unlockRecipe(QuestIds.RECIPE_IRON_SWORD);
        assertTrue(CraftingRecipes.isCraftable(
            CraftingRecipes.COPPER_LONGSWORD, player.getUnlockedRecipeIds()));
    }

    @Test
    void lockedSkillsRemainVisibleInSkillBookCatalog() {
        Player player = newPlayer();
        int catalogSize = SkillRegistry.all().size();
        assertEquals(catalogSize, player.getSkillBook().allRuntimes().size());
        for (SkillRuntime runtime : player.getSkillBook().allRuntimes()) {
            assertFalse(runtime.isUnlocked());
        }
        assertEquals(0, player.getSkillBook().unlockedDefinitions().size());
    }

    @Test
    void ironBladeTrialUnlocksWhirlwindOnTurnInRewardOnly() {
        Player player = newPlayer();
        QuestLog log = new QuestLog();
        log.setProgressPlayer(player);
        completeThrough(log, player, QuestIds.QUEST_FULL_COPPER_SET);

        log.startQuest(QuestIds.QUEST_IRON_BLADE_TRIAL);
        assertFalse(player.getSkillBook().getRuntime(SkillRegistry.WHIRLWIND_ID).isUnlocked());
        assertFalse(player.getSkillBook().getRuntime(SkillRegistry.FLAME_STRIKE_ID).isUnlocked());

        for (int i = 0; i < 10; i++) {
            log.reportKillEnemy(QuestIds.ENEMY_ORC, QuestIds.ITEM_IRON_SWORD, null);
        }
        assertFalse(player.getSkillBook().getRuntime(SkillRegistry.WHIRLWIND_ID).isUnlocked());
        assertTrue(log.getInstance(QuestIds.QUEST_IRON_BLADE_TRIAL).objectives.get(0).completed);
        assertEquals(QuestObjectiveType.TALK_TO_NPC, log.getCurrentObjective().type);

        log.reportTalkNpc(QuestIds.NPC_MENTOR);
        log.grantReward(QuestRegistry.IRON_BLADE_TRIAL, player);
        assertTrue(player.getSkillBook().getRuntime(SkillRegistry.WHIRLWIND_ID).isUnlocked());
        assertFalse(player.getSkillBook().getRuntime(SkillRegistry.FLAME_STRIKE_ID).isUnlocked());
        assertTrue(QuestRegistry.IRON_BLADE_TRIAL.reward.unlockSkillIds
            .contains(SkillRegistry.WHIRLWIND_ID));
    }

    @Test
    void progressMilestonesFireOnceOnly_infrastructureStillWorks() {
        String fixtureId = "test_milestone_fixture";
        QuestDefinition fixture = new QuestDefinition(
            fixtureId,
            "Milestone Fixture",
            "Infrastructure-only quest used by tests.",
            null,
            QuestReward.silverOnly(0),
            QuestObjectiveDefinition.killEnemy(
                "kill_fixture",
                "Derrote 25 Orcs",
                QuestIds.ENEMY_ORC,
                25,
                null,
                null,
                null,
                new QuestProgressMilestone(
                    10, QuestProgressMilestone.Effect.UNLOCK_SKILL, SkillRegistry.WHIRLWIND_ID),
                new QuestProgressMilestone(
                    25, QuestProgressMilestone.Effect.UNLOCK_SKILL, SkillRegistry.FLAME_STRIKE_ID)
            )
        );
        QuestRegistry.registerForTest(fixture);
        try {
            Player player = newPlayer();
            QuestLog log = new QuestLog();
            log.setProgressPlayer(player);
            AtomicInteger milestoneCount = new AtomicInteger();
            log.setListener(new QuestLogListener() {
                @Override
                public void onProgressMilestone(QuestDefinition quest,
                                                QuestObjectiveDefinition objective,
                                                QuestProgressMilestone milestone) {
                    milestoneCount.incrementAndGet();
                }
            });
            log.startQuest(fixtureId);
            for (int i = 0; i < 12; i++) {
                log.reportKillEnemy(QuestIds.ENEMY_ORC);
            }
            assertEquals(1, milestoneCount.get());
            assertTrue(player.getSkillBook().getRuntime(SkillRegistry.WHIRLWIND_ID).isUnlocked());
            for (int i = 0; i < 13; i++) {
                log.reportKillEnemy(QuestIds.ENEMY_ORC);
            }
            assertEquals(2, milestoneCount.get());
            assertTrue(player.getSkillBook().getRuntime(SkillRegistry.FLAME_STRIKE_ID).isUnlocked());
        } finally {
            QuestRegistry.unregisterForTest(fixtureId);
        }
    }

    @Test
    void weaponFilterIgnoresNonIronSwordKills() {
        Player player = newPlayer();
        QuestLog log = new QuestLog();
        log.setProgressPlayer(player);
        completeThrough(log, player, QuestIds.QUEST_FULL_COPPER_SET);
        log.startQuest(QuestIds.QUEST_IRON_BLADE_TRIAL);

        log.reportKillEnemy(QuestIds.ENEMY_ORC, "copper_sword", null);
        log.reportKillEnemy(QuestIds.ENEMY_ORC, null, null);
        assertEquals(0, log.getCurrentObjectiveProgress().currentAmount);

        log.reportKillEnemy(QuestIds.ENEMY_ORC, QuestIds.ITEM_IRON_SWORD, null);
        assertEquals(1, log.getCurrentObjectiveProgress().currentAmount);
    }

    @Test
    void skillFilterCountsWhirlwindAndFlameStrikeIncludingBurnId() {
        Player player = newPlayer();
        QuestLog log = new QuestLog();
        log.setProgressPlayer(player);
        completeThrough(log, player, QuestIds.QUEST_IRON_BLADE_TRIAL);

        log.startQuest(QuestIds.QUEST_WHIRLWIND_PROVING);
        log.reportKillEnemy(QuestIds.ENEMY_ORC, null, null);
        assertEquals(0, log.getCurrentObjectiveProgress().currentAmount);
        log.reportKillEnemy(QuestIds.ENEMY_ORC, null, SkillRegistry.WHIRLWIND_ID);
        assertEquals(1, log.getCurrentObjectiveProgress().currentAmount);

        for (int i = 0; i < 9; i++) {
            log.reportKillEnemy(QuestIds.ENEMY_ORC, null, SkillRegistry.WHIRLWIND_ID);
        }
        log.reportTalkNpc(QuestIds.NPC_MENTOR);
        log.grantReward(QuestRegistry.WHIRLWIND_PROVING, player);

        log.startQuest(QuestIds.QUEST_FLAME_STRIKE_PROVING);
        log.reportKillEnemy(QuestIds.ENEMY_ORC, null, SkillRegistry.FLAME_STRIKE_ID);
        assertEquals(1, log.getCurrentObjectiveProgress().currentAmount);
    }

    @Test
    void orcCleanupUnlocksShopAndFullCopperUnlocksRecipeOnReward() {
        Player player = newPlayer();
        QuestLog log = new QuestLog();
        log.setProgressPlayer(player);
        assertFalse(log.isShopUnlocked());

        completeThrough(log, player, QuestIds.QUEST_ORC_CLEANUP);
        assertTrue(log.isShopUnlocked());
        assertEquals(50, player.getWallet().getTotalSilver());

        completeThrough(log, player, QuestIds.QUEST_FULL_COPPER_SET);
        assertTrue(player.isRecipeUnlocked(CraftingRecipes.COPPER_LONGSWORD));
        assertEquals(50 + 30 + 60, player.getWallet().getTotalSilver());
    }

    @Test
    void mentorOffersOrcCleanupAfterFirstSwordAndBlocksShopUntilTurnIn() {
        DialogueUI dialogue = mock(DialogueUI.class);
        MentorNpc mentor = new MentorNpc(0, 0, dialogue, new HashMap<>());
        Player player = newPlayer();
        QuestLog log = new QuestLog();
        log.setProgressPlayer(player);
        mentor.setQuestLog(log);
        final boolean[] shopOpened = {false};
        mentor.setOpenShop(() -> shopOpened[0] = true);

        log.startQuest(QuestIds.QUEST_FIRST_SWORD);
        mentor.onInteract(player); // pickaxe
        player.getInventory().addItem(ItemRegistry.COPPER_ORE, 3);
        mentor.onInteract(player); // turn in first sword
        assertTrue(player.hasFirstSword());
        assertTrue(log.isCompleted(QuestIds.QUEST_FIRST_SWORD));

        shopOpened[0] = false;
        mentor.onInteract(player); // should offer orc cleanup, not shop
        assertEquals(QuestStatus.ACTIVE, log.getInstance(QuestIds.QUEST_ORC_CLEANUP).status);
        assertFalse(shopOpened[0]);
        assertFalse(mentor.hasSecondaryInteract());
        assertFalse(log.isShopUnlocked());

        for (int i = 0; i < 5; i++) {
            log.reportKillEnemy(QuestIds.ENEMY_ORC);
        }
        mentor.onInteract(player); // turn in cleanup
        assertTrue(log.isShopUnlocked());
        assertTrue(mentor.hasSecondaryInteract());
        assertEquals(50, player.getWallet().getTotalSilver());
        assertTrue(log.isCompleted(QuestIds.QUEST_ORC_CLEANUP));
    }

    @Test
    void mentorShopIsSeparateSecondaryInteractionAvailableDuringLaterQuests() {
        DialogueUI dialogue = mock(DialogueUI.class);
        MentorNpc mentor = new MentorNpc(0, 0, dialogue, new HashMap<>());
        Player player = newPlayer();
        QuestLog log = new QuestLog();
        log.setProgressPlayer(player);
        mentor.setQuestLog(log);
        final boolean[] shopOpened = {false};
        mentor.setOpenShop(() -> shopOpened[0] = true);

        completeThrough(log, player, QuestIds.QUEST_ORC_CLEANUP);
        assertTrue(mentor.hasSecondaryInteract());
        assertEquals("[R] Abrir loja", mentor.getSecondaryPromptText());

        log.startQuest(QuestIds.QUEST_COPPER_CHESTPLATE);
        shopOpened[0] = false;
        mentor.onInteract(player); // progress reminder — must not open shop
        assertFalse(shopOpened[0]);

        mentor.onSecondaryInteract(player);
        assertTrue(shopOpened[0]);

        shopOpened[0] = false;
        completeThrough(log, player, QuestIds.QUEST_FULL_COPPER_SET);
        log.startQuest(QuestIds.QUEST_IRON_BLADE_TRIAL);
        mentor.onInteract(player);
        assertFalse(shopOpened[0]);
        mentor.onSecondaryInteract(player);
        assertTrue(shopOpened[0]);
    }

    @Test
    void whirlwindProvingUnlocksFlameStrikeOnReward() {
        Player player = newPlayer();
        QuestLog log = new QuestLog();
        log.setProgressPlayer(player);
        completeThrough(log, player, QuestIds.QUEST_IRON_BLADE_TRIAL);
        assertTrue(player.getSkillBook().getRuntime(SkillRegistry.WHIRLWIND_ID).isUnlocked());
        assertFalse(player.getSkillBook().getRuntime(SkillRegistry.FLAME_STRIKE_ID).isUnlocked());

        log.startQuest(QuestIds.QUEST_WHIRLWIND_PROVING);
        for (int i = 0; i < 10; i++) {
            log.reportKillEnemy(QuestIds.ENEMY_ORC, null, SkillRegistry.WHIRLWIND_ID);
        }
        int silverBefore = player.getWallet().getTotalSilver();
        log.reportTalkNpc(QuestIds.NPC_MENTOR);
        log.grantReward(QuestRegistry.WHIRLWIND_PROVING, player);

        assertEquals(silverBefore + 100, player.getWallet().getTotalSilver());
        assertEquals(10, player.getInventory().getItemCount(ItemRegistry.POTION_MEDIUM));
        assertTrue(player.getSkillBook().getRuntime(SkillRegistry.FLAME_STRIKE_ID).isUnlocked());
        assertTrue(QuestRegistry.WHIRLWIND_PROVING.reward.unlockSkillIds
            .contains(SkillRegistry.FLAME_STRIKE_ID));
    }

    @Test
    void provingRewardsGrantGoldAndMediumPotions() {
        Player player = newPlayer();
        QuestLog log = new QuestLog();
        log.setProgressPlayer(player);
        completeThrough(log, player, QuestIds.QUEST_WHIRLWIND_PROVING);

        log.startQuest(QuestIds.QUEST_FLAME_STRIKE_PROVING);
        for (int i = 0; i < 10; i++) {
            log.reportKillEnemy(QuestIds.ENEMY_ORC, null, SkillRegistry.FLAME_STRIKE_ID);
        }
        int silverBefore = player.getWallet().getTotalSilver();
        int potionsBefore = player.getInventory().getItemCount(ItemRegistry.POTION_MEDIUM);
        log.reportTalkNpc(QuestIds.NPC_MENTOR);
        log.grantReward(QuestRegistry.FLAME_STRIKE_PROVING, player);
        assertEquals(silverBefore + 100, player.getWallet().getTotalSilver());
        assertEquals(potionsBefore + 10, player.getInventory().getItemCount(ItemRegistry.POTION_MEDIUM));
    }

    @Test
    void saveLoadPersistsRecipeSkillUnlocksAndKillProgress() {
        Player player = newPlayer();
        QuestLog log = new QuestLog();
        log.setProgressPlayer(player);
        completeThrough(log, player, QuestIds.QUEST_FULL_COPPER_SET);
        log.startQuest(QuestIds.QUEST_IRON_BLADE_TRIAL);
        for (int i = 0; i < 10; i++) {
            log.reportKillEnemy(QuestIds.ENEMY_ORC, QuestIds.ITEM_IRON_SWORD, null);
        }

        SaveData data = SaveStateMapper.capture(player, null, false, 0, 0, null, log);
        data.version = SaveData.CURRENT_VERSION;

        Player loadedPlayer = newPlayer();
        QuestLog loadedLog = new QuestLog();
        loadedLog.setProgressPlayer(loadedPlayer);
        SaveStateMapper.applyPlayer(data.player, loadedPlayer);
        loadedLog.readFromSave(data.quest, loadedPlayer, null);

        assertTrue(loadedPlayer.isRecipeUnlocked(CraftingRecipes.COPPER_LONGSWORD));
        assertFalse(loadedPlayer.getSkillBook().getRuntime(SkillRegistry.WHIRLWIND_ID).isUnlocked());
        assertFalse(loadedPlayer.getSkillBook().getRuntime(SkillRegistry.FLAME_STRIKE_ID).isUnlocked());
        assertEquals(QuestIds.QUEST_IRON_BLADE_TRIAL, loadedLog.getTrackedQuestId());
        assertEquals(QuestObjectiveType.TALK_TO_NPC, loadedLog.getCurrentObjective().type);
        assertNotNull(loadedLog.getInstance(QuestIds.QUEST_IRON_BLADE_TRIAL));
    }

    @Test
    void v1SaveMigrationKeepsSkillsAndIronRecipeIfHadFirstSword() {
        SaveData v1 = new SaveData();
        v1.version = 1;
        v1.player.characterName = "Hero";
        v1.player.hasFirstSword = true;
        v1.quest = new com.donos.zebra.save.QuestSaveData();
        v1.world = new com.donos.zebra.save.WorldSaveData();

        SaveService.migrateV1ToV2(v1);
        assertEquals(2, v1.version);
        assertTrue(v1.player.unlockedRecipeIds.contains(QuestIds.RECIPE_IRON_SWORD));
        assertTrue(v1.player.unlockedSkillIds.contains(SkillRegistry.WHIRLWIND_ID));
        assertTrue(v1.player.unlockedSkillIds.contains(SkillRegistry.FLAME_STRIKE_ID));
    }

    @Test
    void v1SaveMigrationKeepsLocksIfNoFirstSword() {
        SaveData v1 = new SaveData();
        v1.version = 1;
        v1.player.characterName = "Newbie";
        v1.player.hasFirstSword = false;
        v1.quest = new com.donos.zebra.save.QuestSaveData();
        v1.world = new com.donos.zebra.save.WorldSaveData();

        SaveService.migrateV1ToV2(v1);
        assertTrue(v1.player.unlockedRecipeIds.isEmpty());
        assertTrue(v1.player.unlockedSkillIds.isEmpty());
    }

    private static Player newPlayer() {
        return new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
    }

    /** Marks quests up to and including {@code lastQuestId} completed with rewards applied. */
    private static void completeThrough(QuestLog log, Player player, String lastQuestId) {
        log.startQuest(QuestIds.QUEST_FIRST_SWORD);
        log.reportTalkNpc(QuestIds.NPC_MENTOR);
        log.reportCollectItem(QuestIds.ITEM_COPPER_ORE, 3);
        log.reportTalkNpc(QuestIds.NPC_MENTOR);
        player.grantFirstSword();

        String[] chain = {
            QuestIds.QUEST_ORC_CLEANUP,
            QuestIds.QUEST_COPPER_CHESTPLATE,
            QuestIds.QUEST_FULL_COPPER_SET,
            QuestIds.QUEST_IRON_BLADE_TRIAL,
            QuestIds.QUEST_WHIRLWIND_PROVING,
            QuestIds.QUEST_FLAME_STRIKE_PROVING
        };
        for (String id : chain) {
            if (log.isCompleted(id)) {
                if (id.equals(lastQuestId)) {
                    return;
                }
                continue;
            }
            log.startQuest(id);
            QuestDefinition def = QuestRegistry.get(id);
            for (int i = 0; i < def.objectives.size(); i++) {
                QuestObjectiveDefinition obj = def.getObjective(i);
                if (obj.type == QuestObjectiveType.KILL_ENEMY) {
                    for (int k = 0; k < obj.requiredAmount; k++) {
                        log.reportKillEnemy(obj.targetId, obj.weaponItemId, obj.skillId);
                    }
                } else if (obj.type == QuestObjectiveType.CRAFT_ITEM) {
                    log.reportCraftItem(obj.targetId);
                } else if (obj.type == QuestObjectiveType.TALK_TO_NPC) {
                    log.reportTalkNpc(obj.targetId);
                }
            }
            log.grantReward(def, player);
            if (id.equals(lastQuestId)) {
                return;
            }
        }
    }
}
