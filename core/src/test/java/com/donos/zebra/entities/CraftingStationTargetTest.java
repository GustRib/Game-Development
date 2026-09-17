package com.donos.zebra.entities;

import com.donos.zebra.ui.CraftingUI;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class CraftingStationTargetTest {

    @Test
    void interactionTargetFlagTogglesLikeOreNodePattern() {
        CraftingStation station = new CraftingStation(10f, 20f, mock(CraftingUI.class));
        assertFalse(station.isInteractionTargeted());
        station.setInteractionTargeted(true);
        assertTrue(station.isInteractionTargeted());
        station.update(0.1f);
        station.setInteractionTargeted(false);
        assertFalse(station.isInteractionTargeted());
    }
}
