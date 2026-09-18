package com.donos.zebra.save;

import com.donos.zebra.HeadlessTestBase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveSlotTest extends HeadlessTestBase {

    private final SaveService saveService = new SaveService();

    @BeforeEach
    @AfterEach
    void clean() {
        saveService.clearAllSlots();
    }

    @Test
    void emptySlotIsDetected() {
        assertFalse(saveService.isSlotOccupied(1));
        assertFalse(saveService.hasAnySave());
        SaveSlotInfo info = saveService.getSlotInfo(2);
        assertFalse(info.occupied);
        assertEquals("VAZIO", info.summaryLine());
    }

    @Test
    void populatedSlotIsDetected() {
        SaveData data = new SaveData();
        data.player.characterName = "Gu";
        assertTrue(saveService.writeSlot(1, data));
        assertTrue(saveService.isSlotOccupied(1));
        assertTrue(saveService.hasAnySave());
        SaveSlotInfo info = saveService.getSlotInfo(1);
        assertTrue(info.occupied);
        assertEquals("Gu", info.characterName);
    }

    @Test
    void threeSlotsAreIndependent() {
        SaveData a = new SaveData();
        a.player.characterName = "One";
        SaveData b = new SaveData();
        b.player.characterName = "Two";
        assertTrue(saveService.writeSlot(1, a));
        assertTrue(saveService.writeSlot(2, b));

        assertEquals("One", saveService.readSlot(1).player.characterName);
        assertEquals("Two", saveService.readSlot(2).player.characterName);
        assertNull(saveService.readSlot(3));
        assertFalse(saveService.isSlotOccupied(3));
    }
}
