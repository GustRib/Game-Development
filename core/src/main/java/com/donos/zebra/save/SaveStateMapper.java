package com.donos.zebra.save;

import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.MentorNpc;
import com.donos.zebra.entities.OreNode;
import com.donos.zebra.entities.Orc;
import com.donos.zebra.entities.Player;
import com.donos.zebra.items.CraftingController;
import com.donos.zebra.items.CraftingRecipe;
import com.donos.zebra.items.CraftingRecipes;
import com.donos.zebra.items.Inventory;
import com.donos.zebra.items.ItemDefinition;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.items.ItemStack;

import java.util.List;

/**
 * Extracts / restores game state DTOs without touching LibGDX runtime graphs.
 */
public final class SaveStateMapper {

    private static final float POS_EPS = 1.5f;

    private SaveStateMapper() {
    }

    public static SaveData capture(
        Player player,
        List<Entity> overworldEntities,
        boolean inTavern,
        float outdoorReturnX,
        float outdoorReturnY,
        CraftingController craftingController
    ) {
        SaveData data = new SaveData();
        data.player = capturePlayer(player, inTavern, outdoorReturnX, outdoorReturnY);
        data.quest = new QuestSaveData();
        data.world = new WorldSaveData();
        data.world.worldId = "overworld";

        if (overworldEntities != null) {
            for (Entity e : overworldEntities) {
                if (e instanceof MentorNpc) {
                    data.quest.mentorGavePickaxe = ((MentorNpc) e).hasGavePickaxe();
                } else if (e instanceof OreNode) {
                    OreNode ore = (OreNode) e;
                    data.world.oreNodes.add(new OreNodeSaveData(
                        ore.getX(), ore.getY(), ore.getHitsRemaining(), ore.getDepletedTimer()));
                } else if (e instanceof Orc) {
                    data.world.orcs.add(captureOrc((Orc) e));
                }
            }
        }

        if (craftingController != null && craftingController.isBusy()
            && craftingController.getActiveJob() != null) {
            CraftingSaveData craft = new CraftingSaveData();
            craft.recipeId = craftingController.getActiveJob().getRecipe().getId();
            craft.elapsedSeconds = craftingController.getActiveJob().getElapsed();
            data.crafting = craft;
        }
        return data;
    }

    public static PlayerSaveData capturePlayer(
        Player player, boolean inTavern, float outdoorReturnX, float outdoorReturnY
    ) {
        PlayerSaveData p = new PlayerSaveData();
        p.characterName = player.getCharacterName() != null ? player.getCharacterName() : "";
        p.x = player.getX();
        p.y = player.getY();
        p.currentHealth = player.getCurrentHealth();
        p.maxHealth = player.getMaxHealth();
        p.hasFirstSword = player.hasFirstSword();
        p.totalSilver = player.getWallet().getTotalSilver();
        p.inTavern = inTavern;
        p.outdoorReturnX = outdoorReturnX;
        p.outdoorReturnY = outdoorReturnY;
        p.potionCooldownRemaining = player.getPotionCooldownRemaining();

        Inventory inv = player.getInventory();
        ItemStack[] slots = inv.getSlots();
        for (int i = 0; i < slots.length; i++) {
            p.inventorySlots.add(toStackSave(slots[i]));
        }

        p.equippedWeaponId = idOf(player.getEquippedWeapon());
        p.equippedHelmetId = idOf(player.getEquippedHelmet());
        p.equippedChestplateId = idOf(player.getEquippedChestplate());
        p.equippedGlovesId = idOf(player.getEquippedGloves());
        p.equippedBootsId = idOf(player.getEquippedBoots());
        p.potionSlot = toStackSave(player.getPotionSlot());
        String[] barIds = player.getSkillBook().getBar().snapshotIds();
        p.skillBarSlotIds = new java.util.ArrayList<>();
        for (String id : barIds) {
            p.skillBarSlotIds.add(id);
        }
        return p;
    }

    public static void applyPlayer(PlayerSaveData data, Player player) {
        if (data == null || player == null) {
            return;
        }
        player.setCharacterName(data.characterName);
        player.setCurrentHealth(data.currentHealth);
        player.setHasFirstSword(data.hasFirstSword);
        player.getWallet().setTotalSilver(data.totalSilver);
        player.setPotionCooldownRemaining(data.potionCooldownRemaining);

        Inventory inv = player.getInventory();
        inv.clearAll();
        if (data.inventorySlots != null) {
            for (int i = 0; i < data.inventorySlots.size(); i++) {
                ItemStack stack = fromStackSave(data.inventorySlots.get(i));
                if (stack != null) {
                    inv.setStackAt(i, stack);
                }
            }
        }

        player.restoreEquipment(
            item(data.equippedWeaponId),
            item(data.equippedHelmetId),
            item(data.equippedChestplateId),
            item(data.equippedGlovesId),
            item(data.equippedBootsId)
        );
        player.restorePotionSlot(fromStackSave(data.potionSlot));
        if (data.skillBarSlotIds != null && !data.skillBarSlotIds.isEmpty()) {
            player.getSkillBook().getBar().restoreFromIds(
                data.skillBarSlotIds.toArray(new String[0]));
        }
        player.setPosition(data.x, data.y);
    }

    public static void applyWorld(WorldSaveData world, QuestSaveData quest, List<Entity> entities) {
        if (entities == null) {
            return;
        }
        for (Entity e : entities) {
            if (e instanceof MentorNpc && quest != null) {
                ((MentorNpc) e).setGavePickaxe(quest.mentorGavePickaxe);
            } else if (e instanceof OreNode && world != null) {
                OreNode ore = (OreNode) e;
                OreNodeSaveData match = findOre(world, ore.getX(), ore.getY());
                if (match != null) {
                    ore.restoreState(match.hitsRemaining, match.depletedTimer);
                }
            } else if (e instanceof Orc && world != null) {
                Orc orc = (Orc) e;
                OrcSaveData match = findOrc(world, orc.getSpawnX(), orc.getSpawnY());
                if (match != null) {
                    applyOrc(match, orc);
                }
            }
        }
    }

    public static void applyCrafting(CraftingSaveData craft, CraftingController controller, Inventory inventory) {
        if (craft == null || controller == null || inventory == null) {
            return;
        }
        if (craft.recipeId == null || craft.recipeId.isEmpty()) {
            return;
        }
        CraftingRecipe recipe = CraftingRecipes.findById(craft.recipeId);
        if (recipe == null) {
            return;
        }
        controller.restoreJob(recipe, inventory, craft.elapsedSeconds);
    }

    private static OrcSaveData captureOrc(Orc orc) {
        OrcSaveData d = new OrcSaveData();
        d.spawnX = orc.getSpawnX();
        d.spawnY = orc.getSpawnY();
        d.x = orc.getX();
        d.y = orc.getY();
        d.currentHealth = orc.getCurrentHealth();
        d.maxHealth = orc.getMaxHealth();
        d.dead = orc.isDead();
        d.deathTimer = orc.getDeathTimer();
        d.looted = orc.isLooted();
        d.silverLoot = orc.getSilverLoot();
        for (ItemStack stack : orc.getLootTable()) {
            ItemStackSaveData s = toStackSave(stack);
            if (s != null) {
                d.loot.add(s);
            }
        }
        return d;
    }

    private static void applyOrc(OrcSaveData data, Orc orc) {
        java.util.ArrayList<ItemStack> loot = new java.util.ArrayList<>();
        if (data.loot != null) {
            for (ItemStackSaveData s : data.loot) {
                ItemStack stack = fromStackSave(s);
                if (stack != null) {
                    loot.add(stack);
                }
            }
        }
        orc.restorePersistedState(
            data.x,
            data.y,
            data.currentHealth,
            data.dead,
            data.deathTimer,
            data.looted,
            data.silverLoot,
            loot
        );
    }

    private static OreNodeSaveData findOre(WorldSaveData world, float x, float y) {
        for (OreNodeSaveData o : world.oreNodes) {
            if (near(o.x, o.y, x, y)) {
                return o;
            }
        }
        return null;
    }

    private static OrcSaveData findOrc(WorldSaveData world, float spawnX, float spawnY) {
        for (OrcSaveData o : world.orcs) {
            if (near(o.spawnX, o.spawnY, spawnX, spawnY)) {
                return o;
            }
        }
        return null;
    }

    private static boolean near(float ax, float ay, float bx, float by) {
        return Math.abs(ax - bx) <= POS_EPS && Math.abs(ay - by) <= POS_EPS;
    }

    private static ItemStackSaveData toStackSave(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.getDefinition() == null) {
            return null;
        }
        return new ItemStackSaveData(stack.getDefinition().getId(), stack.getQuantity());
    }

    private static ItemStack fromStackSave(ItemStackSaveData data) {
        if (data == null || data.itemId == null || data.itemId.isEmpty() || data.quantity <= 0) {
            return null;
        }
        ItemDefinition def = ItemRegistry.getItem(data.itemId);
        if (def == null) {
            return null;
        }
        return new ItemStack(def, data.quantity);
    }

    private static String idOf(ItemDefinition def) {
        return def != null ? def.getId() : null;
    }

    private static ItemDefinition item(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return ItemRegistry.getItem(id);
    }
}
