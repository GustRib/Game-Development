package com.donos.zebra.items;

import com.badlogic.gdx.utils.Array;
import com.donos.zebra.entities.AnimationConstants;
import com.donos.zebra.entities.DamageText;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.Orc;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import com.donos.zebra.screens.gameplay.CombatController;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftingSliceTest {

    @Test
    void craftFailsWithInsufficientMaterialsAndConsumesNothing() {
        Inventory inventory = new Inventory(20);
        inventory.addItem(ItemRegistry.COPPER_ORE, 2);
        inventory.addItem(ItemRegistry.IRON_ORE, 1);

        assertFalse(CraftingRecipes.COPPER_LONGSWORD.craft(inventory));
        assertEquals(2, inventory.getItemCount(ItemRegistry.COPPER_ORE));
        assertEquals(1, inventory.getItemCount(ItemRegistry.IRON_ORE));
        assertEquals(0, inventory.getItemCount(ItemRegistry.COPPER_LONGSWORD));
    }

    @Test
    void craftSucceedsConsumesMaterialsAndGrantsItem() {
        Inventory inventory = new Inventory(20);
        inventory.addItem(ItemRegistry.COPPER_ORE, 5);
        inventory.addItem(ItemRegistry.IRON_ORE, 2);

        assertTrue(CraftingRecipes.COPPER_LONGSWORD.craft(inventory));
        assertEquals(0, inventory.getItemCount(ItemRegistry.COPPER_ORE));
        assertEquals(0, inventory.getItemCount(ItemRegistry.IRON_ORE));
        assertEquals(1, inventory.getItemCount(ItemRegistry.COPPER_LONGSWORD));
    }

    @Test
    void eachArmorRecipeCraftsIntoCorrectSlot() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());

        player.getInventory().addItem(ItemRegistry.COPPER_ORE, 2);
        player.getInventory().addItem(ItemRegistry.IRON_ORE, 1);
        assertTrue(CraftingRecipes.COPPER_HELMET.craft(player.getInventory()));
        player.equipCrafted(ItemRegistry.COPPER_HELMET);
        assertSame(ItemRegistry.COPPER_HELMET, player.getEquippedHelmet());
        assertEquals(2f, player.getDefense(), 0.01f);

        player.getInventory().addItem(ItemRegistry.COPPER_ORE, 3);
        player.getInventory().addItem(ItemRegistry.IRON_ORE, 3);
        assertTrue(CraftingRecipes.COPPER_CHESTPLATE.craft(player.getInventory()));
        player.equipCrafted(ItemRegistry.COPPER_CHESTPLATE);
        assertSame(ItemRegistry.COPPER_CHESTPLATE, player.getEquippedChestplate());
        assertEquals(6f, player.getDefense(), 0.01f);

        player.getInventory().addItem(ItemRegistry.COPPER_ORE, 2);
        player.getInventory().addItem(ItemRegistry.IRON_ORE, 2);
        assertTrue(CraftingRecipes.COPPER_BOOTS.craft(player.getInventory()));
        player.equipCrafted(ItemRegistry.COPPER_BOOTS);
        assertSame(ItemRegistry.COPPER_BOOTS, player.getEquippedBoots());
        assertEquals(8f, player.getDefense(), 0.01f);
    }

    @Test
    void defenseSumsAcrossZeroToThreeArmorPieces() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        assertEquals(0f, player.getDefense(), 0.01f);

        player.equipArmor(ItemRegistry.COPPER_HELMET);
        assertEquals(2f, player.getDefense(), 0.01f);

        player.equipArmor(ItemRegistry.COPPER_BOOTS);
        assertEquals(4f, player.getDefense(), 0.01f);

        player.equipArmor(ItemRegistry.COPPER_CHESTPLATE);
        assertEquals(8f, player.getDefense(), 0.01f);
    }

    @Test
    void recraftingHelmetReplacesSlotWithoutDoubleCounting() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.equipArmor(ItemRegistry.COPPER_HELMET);
        player.equipArmor(ItemRegistry.COPPER_CHESTPLATE);
        assertEquals(6f, player.getDefense(), 0.01f);

        player.equipArmor(ItemRegistry.COPPER_HELMET);
        assertSame(ItemRegistry.COPPER_HELMET, player.getEquippedHelmet());
        assertEquals(6f, player.getDefense(), 0.01f);
    }

    @Test
    void summedDefenseMitigationRespectsFloor() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.equipArmor(ItemRegistry.COPPER_HELMET);
        player.equipArmor(ItemRegistry.COPPER_CHESTPLATE);
        player.equipArmor(ItemRegistry.COPPER_BOOTS);
        assertEquals(8f, player.getDefense(), 0.01f);

        float hp = player.getCurrentHealth();
        player.takeDamage(15f); // 15 - 8 = 7
        assertEquals(hp - 7f, player.getCurrentHealth(), 0.01f);

        hp = player.getCurrentHealth();
        player.takeDamage(3f); // 3 - 8 = max(1, -5) = 1
        assertEquals(hp - 1f, player.getCurrentHealth(), 0.01f);
    }

    @Test
    void craftedSwordRaisesAttackDamageInMelee() {
        StubPlayerInput input = new StubPlayerInput();
        input.pressAttack();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.grantFirstSword();
        assertEquals(10f, player.getAttackDamage(), 0.01f);

        player.getInventory().addItem(ItemRegistry.COPPER_ORE, 5);
        player.getInventory().addItem(ItemRegistry.IRON_ORE, 2);
        assertTrue(CraftingRecipes.COPPER_LONGSWORD.craft(player.getInventory()));
        player.equipCrafted(ItemRegistry.COPPER_LONGSWORD);
        assertEquals(16f, player.getAttackDamage(), 0.01f);

        player.setPosition(100f, 100f);
        player.update(0.016f, new Array<>());
        assertEquals(AnimationConstants.ANIM_ATTACK, player.getCurrentAnimationKey());

        Orc orc = new Orc(110f, 100f, TestAnimationFactory.createOrcAnimations());
        List<Entity> entities = new ArrayList<>();
        entities.add(orc);
        List<DamageText> damageTexts = new ArrayList<>();

        float healthBefore = orc.getCurrentHealth();
        CombatController.resolvePlayerMelee(player, entities, damageTexts, true);
        assertEquals(healthBefore - 16f, orc.getCurrentHealth(), 0.01f);
    }

    @Test
    void meleeBlockedWithoutEquippedWeapon() {
        StubPlayerInput input = new StubPlayerInput();
        input.pressAttack();
        Player player = new Player(input, TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(100f, 100f);
        player.update(0.016f, new Array<>());
        assertFalse(player.hasWeaponEquipped());

        Orc orc = new Orc(110f, 100f, TestAnimationFactory.createOrcAnimations());
        List<Entity> entities = new ArrayList<>();
        entities.add(orc);
        List<DamageText> damageTexts = new ArrayList<>();
        float healthBefore = orc.getCurrentHealth();

        CombatController.resolvePlayerMelee(player, entities, damageTexts, true);
        assertEquals(healthBefore, orc.getCurrentHealth(), 0.01f);
        assertEquals(1, damageTexts.size());
    }

    @Test
    void gearIconsPointAtFinalArtPaths() {
        assertEquals("items/copper_ore.png", ItemRegistry.COPPER_ORE.getIconPath());
        assertEquals("items/iron_ore.png", ItemRegistry.IRON_ORE.getIconPath());
        assertEquals("items/copper_sword.png", ItemRegistry.COPPER_LONGSWORD.getIconPath());
        assertEquals("items/copper_sword.png", ItemRegistry.IRON_SWORD.getIconPath());
        assertEquals("items/copper_helmet.png", ItemRegistry.COPPER_HELMET.getIconPath());
        assertEquals("items/copper_chestplate.png", ItemRegistry.COPPER_CHESTPLATE.getIconPath());
        assertEquals("items/copper_boots.png", ItemRegistry.COPPER_BOOTS.getIconPath());
    }
}
