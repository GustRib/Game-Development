package com.donos.zebra.entities;

import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.ui.DialogueUI;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MentorInteractionTest {

    @Test
    void firstInteractGrantsPickaxeAndShowsDialogue() {
        DialogueUI dialogueUI = mock(DialogueUI.class);
        MentorNpc mentor = new MentorNpc(0f, 0f, dialogueUI, new HashMap<>());

        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        assertEquals(0, player.getInventory().getItemCount(ItemRegistry.STONE_PICKAXE));

        mentor.onInteract(player);
        player.setInteracting(true);

        verify(dialogueUI).showText("Mentor: Pegue esta picareta de pedra.\nEla serve para extrair cobre!");
        assertTrue(player.isInteracting());
        assertEquals(1, player.getInventory().getItemCount(ItemRegistry.STONE_PICKAXE));

        player.setInteracting(false);
        assertFalse(player.isInteracting());

        mentor.onInteract(player);
        verify(dialogueUI).showText("Mentor: Va ate a mina de cobre ao Norte!");
        assertEquals(1, player.getInventory().getItemCount(ItemRegistry.STONE_PICKAXE));
    }
}
