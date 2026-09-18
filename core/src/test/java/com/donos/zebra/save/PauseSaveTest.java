package com.donos.zebra.save;

import com.donos.zebra.HeadlessTestBase;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.StubPlayerInput;
import com.donos.zebra.entities.TestAnimationFactory;
import com.donos.zebra.screens.gameplay.GameFlowController;
import com.donos.zebra.screens.gameplay.GameFlowState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PauseSaveTest extends HeadlessTestBase {

    private final SaveService saveService = new SaveService();

    @BeforeEach
    @AfterEach
    void clean() {
        saveService.clearAllSlots();
    }

    @Test
    void saveGameWritesCurrentStateWithoutLeavingPause() {
        GameFlowController flow = new GameFlowController();
        flow.openPause();
        assertEquals(GameFlowState.PAUSED, flow.getState());

        Player player = new Player(new StubPlayerInput(), TestAnimationFactory.createDirectionalAnimations());
        player.setCharacterName("Gu");
        player.setPosition(88f, 99f);
        player.getWallet().addSilver(15);

        SaveData data = SaveStateMapper.capture(
            player, new ArrayList<>(), false, 0f, 0f, null);
        assertTrue(saveService.writeSlot(2, data));

        // Saving must not mutate pause state
        assertEquals(GameFlowState.PAUSED, flow.getState());
        assertTrue(flow.isOverlayPause());

        SaveData loaded = saveService.readSlot(2);
        assertNotNull(loaded);
        assertEquals("Gu", loaded.player.characterName);
        assertEquals(88f, loaded.player.x, 0.01f);
        assertEquals(15, loaded.player.totalSilver);
    }
}
