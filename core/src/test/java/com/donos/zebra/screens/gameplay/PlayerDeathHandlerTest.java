package com.donos.zebra.screens.gameplay;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import com.donos.zebra.ui.InventoryUI;
import com.donos.zebra.ui.LootUI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PlayerDeathHandlerTest {

    @Test
    void applyReviveRestoresHealthPositionAndHidesLootUi() {
        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.setPosition(10f, 20f);
        player.takeDamage(999f);
        assertTrue(player.isDead());

        InventoryUI inventoryUI = mock(InventoryUI.class);
        LootUI lootUI = mock(LootUI.class);

        PlayerDeathHandler.applyRevive(player, 100f, 200f, new Array<Polygon>(), lootUI, inventoryUI);

        assertFalse(player.isDead());
        assertEquals(player.getMaxHealth(), player.getCurrentHealth(), 0.01f);
        assertEquals(100f, player.getX(), 0.01f);
        assertEquals(200f, player.getY(), 0.01f);
        verify(lootUI).setVisible(false);
        verify(inventoryUI).setVisible(false);
    }
}
