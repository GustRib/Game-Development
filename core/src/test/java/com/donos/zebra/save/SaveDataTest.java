package com.donos.zebra.save;

import com.donos.zebra.HeadlessTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaveDataTest extends HeadlessTestBase {

    private final SaveService saveService = new SaveService();

    @Test
    void saveDataSerializesAndLoadsWithVersion() {
        SaveData original = sampleSave("Gu");
        original.version = SaveData.CURRENT_VERSION;
        String json = SaveService.createJson().prettyPrint(original);
        assertNotNull(json);
        assertTrue(json.contains("Gu"));

        SaveData loaded = saveService.parseAndValidate(json);
        assertNotNull(loaded);
        assertEquals(SaveData.CURRENT_VERSION, loaded.version);
        assertEquals("Gu", loaded.player.characterName);
        assertEquals(42, loaded.player.totalSilver);
    }

    @Test
    void versionIsPresentOnWrite() {
        SaveData data = sampleSave("Hero");
        assertTrue(saveService.writeSlot(1, data));
        SaveData loaded = saveService.readSlot(1);
        assertNotNull(loaded);
        assertEquals(1, loaded.version);
        saveService.clearAllSlots();
    }

    @Test
    void invalidCorruptDataIsHandled() {
        assertNull(saveService.parseAndValidate("{ not json"));
        assertNull(saveService.parseAndValidate(""));
        assertNull(saveService.parseAndValidate(null));

        SaveData badVersion = sampleSave("X");
        badVersion.version = 99;
        String json = SaveService.createJson().toJson(badVersion);
        assertNull(saveService.parseAndValidate(json));

        SaveData noName = sampleSave("X");
        noName.player.characterName = "";
        assertNull(saveService.parseAndValidate(SaveService.createJson().toJson(noName)));
    }

    private static SaveData sampleSave(String name) {
        SaveData data = new SaveData();
        data.version = SaveData.CURRENT_VERSION;
        data.player.characterName = name;
        data.player.x = 10f;
        data.player.y = 20f;
        data.player.totalSilver = 42;
        data.quest.mentorGavePickaxe = true;
        return data;
    }
}
