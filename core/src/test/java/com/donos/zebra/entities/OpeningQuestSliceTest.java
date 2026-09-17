package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.Texture;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.ui.DialogueUI;
import com.donos.zebra.world.OpeningQuest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class OpeningQuestSliceTest {

    @Test
    void miningWithoutPickaxeDoesNotYieldOre() {
        DialogueUI dialogueUI = mock(DialogueUI.class);
        OreNode node = new OreNode(0f, 0f, mock(Texture.class), dialogueUI);
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());

        node.onInteract(player);

        assertEquals(0, player.getInventory().getItemCount(ItemRegistry.COPPER_ORE));
        assertEquals(OpeningQuest.ORE_NODE_HITS, node.getHitsRemaining());
        verify(dialogueUI).showText(contains("picareta"));
    }

    @Test
    void miningWithPickaxeYieldsOreAndDepletesNode() {
        DialogueUI dialogueUI = mock(DialogueUI.class);
        OreNode node = new OreNode(0f, 0f, mock(Texture.class), dialogueUI);
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.getInventory().addItem(ItemRegistry.STONE_PICKAXE, 1);

        assertEquals("0/3", node.getProgressLabel());
        for (int i = 0; i < OpeningQuest.ORE_NODE_HITS; i++) {
            node.onInteract(player);
            assertEquals((i + 1) + "/" + OpeningQuest.ORE_NODE_HITS, node.getProgressLabel());
        }

        assertEquals(OpeningQuest.ORE_NODE_HITS, player.getInventory().getItemCount(ItemRegistry.COPPER_ORE));
        assertTrue(node.isDepleted());
        assertEquals(0, node.getHitsRemaining());
        verify(dialogueUI, never()).showText(contains("Clang"));
        verify(dialogueUI, never()).showText(contains("esgota"));
    }

    @Test
    void mentorTurnInBelowThresholdDoesNotGrantSwordOrConsumeOre() {
        DialogueUI dialogueUI = mock(DialogueUI.class);
        MentorNpc mentor = new MentorNpc(0f, 0f, dialogueUI, new HashMap<>());
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());

        mentor.onInteract(player); // pickaxe + intro
        player.getInventory().addItem(ItemRegistry.COPPER_ORE, OpeningQuest.COPPER_ORE_REQUIRED - 1);
        mentor.onInteract(player);

        assertFalse(player.hasFirstSword());
        assertEquals(OpeningQuest.COPPER_ORE_REQUIRED - 1,
            player.getInventory().getItemCount(ItemRegistry.COPPER_ORE));
        assertEquals(0, player.getInventory().getItemCount(ItemRegistry.IRON_SWORD));
        verify(dialogueUI).showText(contains("Ainda falta cobre"));
        verify(dialogueUI, never()).showText(contains("Bom trabalho"));
    }

    @Test
    void mentorTurnInAtThresholdGrantsSwordAndConsumesOre() {
        DialogueUI dialogueUI = mock(DialogueUI.class);
        MentorNpc mentor = new MentorNpc(0f, 0f, dialogueUI, new HashMap<>());
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());

        mentor.onInteract(player);
        player.getInventory().addItem(ItemRegistry.COPPER_ORE, OpeningQuest.COPPER_ORE_REQUIRED);
        mentor.onInteract(player);

        assertTrue(player.hasFirstSword());
        assertEquals(0, player.getInventory().getItemCount(ItemRegistry.COPPER_ORE));
        assertEquals(0, player.getInventory().getItemCount(ItemRegistry.IRON_SWORD));
        assertSame(ItemRegistry.IRON_SWORD, player.getEquippedWeapon());
        verify(dialogueUI).showText(contains("Bom trabalho"));
        verify(dialogueUI).showText(contains("Eis sua primeira espada"));
    }
}
