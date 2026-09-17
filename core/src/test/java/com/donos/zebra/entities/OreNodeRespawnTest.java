package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.Texture;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.ui.DialogueUI;
import com.donos.zebra.world.OpeningQuest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class OreNodeRespawnTest {

    @Test
    void depletedNodeRespawnsAfterNinetySeconds() {
        OreNode node = new OreNode(0f, 0f, mock(Texture.class), mock(DialogueUI.class));
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.getInventory().addItem(ItemRegistry.STONE_PICKAXE, 1);

        for (int i = 0; i < OpeningQuest.ORE_NODE_HITS; i++) {
            node.onInteract(player);
        }
        assertTrue(node.isDepleted());

        node.update(OreNode.RESPAWN_SECONDS - 1f);
        assertTrue(node.isDepleted());
        assertEquals(0, node.getHitsRemaining());

        node.update(2f);
        assertFalse(node.isDepleted());
        assertEquals(OpeningQuest.ORE_NODE_HITS, node.getHitsRemaining());
        assertEquals("0/3", node.getProgressLabel());
    }
}
