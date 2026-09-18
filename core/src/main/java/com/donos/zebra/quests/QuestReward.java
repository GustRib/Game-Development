package com.donos.zebra.quests;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Rewards granted when a quest is completed (typically on mentor turn-in).
 */
public final class QuestReward {

    public static final class ItemGrant {
        public final String itemId;
        public final int quantity;

        public ItemGrant(String itemId, int quantity) {
            this.itemId = itemId;
            this.quantity = Math.max(1, quantity);
        }
    }

    public final int silver;
    public final List<ItemGrant> items;
    public final List<String> unlockRecipeIds;
    public final List<String> unlockSkillIds;

    public QuestReward(int silver, ItemGrant... items) {
        this(silver, items, null, null);
    }

    public QuestReward(int silver,
                       ItemGrant[] items,
                       String[] unlockRecipeIds,
                       String[] unlockSkillIds) {
        this.silver = Math.max(0, silver);
        this.items = items == null || items.length == 0
            ? Collections.emptyList()
            : Collections.unmodifiableList(Arrays.asList(items));
        this.unlockRecipeIds = unlockRecipeIds == null || unlockRecipeIds.length == 0
            ? Collections.emptyList()
            : Collections.unmodifiableList(Arrays.asList(unlockRecipeIds));
        this.unlockSkillIds = unlockSkillIds == null || unlockSkillIds.length == 0
            ? Collections.emptyList()
            : Collections.unmodifiableList(Arrays.asList(unlockSkillIds));
    }

    public static QuestReward silverOnly(int silver) {
        return new QuestReward(silver);
    }

    public static QuestReward silverAndRecipe(int silver, String recipeId) {
        return new QuestReward(silver, null, new String[]{recipeId}, null);
    }

    public static QuestReward silverAndSkill(int silver, String skillId) {
        return new QuestReward(silver, null, null, new String[]{skillId});
    }

    public static QuestReward silverItemsAndSkill(int silver, String skillId, ItemGrant... items) {
        return new QuestReward(silver, items, null, new String[]{skillId});
    }
}
