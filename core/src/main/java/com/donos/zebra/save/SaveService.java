package com.donos.zebra.save;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.SerializationException;

/**
 * Three-slot JSON persistence via {@link Gdx#files}{@ storage.
 * Writes to a temp file then replaces the slot file to avoid partial corruption.
 */
public final class SaveService {

    public static final int SLOT_COUNT = 3;
    public static final int CURRENT_VERSION = SaveData.CURRENT_VERSION;

    private static final String FILE_PREFIX = "save_slot_";
    private static final String FILE_SUFFIX = ".json";
    private static final String TEMP_SUFFIX = ".tmp";

    private final Json json;

    public SaveService() {
        this.json = createJson();
    }

    public static Json createJson() {
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.setIgnoreUnknownFields(true);
        json.setUsePrototypes(false);
        return json;
    }

    public static String slotFileName(int slotIndex) {
        requireValidSlot(slotIndex);
        return FILE_PREFIX + slotIndex + FILE_SUFFIX;
    }

    public boolean hasAnySave() {
        for (int i = 1; i <= SLOT_COUNT; i++) {
            if (isSlotOccupied(i)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSlotOccupied(int slotIndex) {
        return readSlot(slotIndex) != null;
    }

    public SaveData readSlot(int slotIndex) {
        requireValidSlot(slotIndex);
        FileHandle file = localFile(slotFileName(slotIndex));
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            String text = file.readString("UTF-8");
            return parseAndValidate(text);
        } catch (Exception e) {
            Gdx.app.error("SaveService", "Failed to read slot " + slotIndex + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Parses JSON text and validates version / required structure.
     * @return null if corrupt or unsupported
     */
    public SaveData parseAndValidate(String jsonText) {
        if (jsonText == null || jsonText.trim().isEmpty()) {
            return null;
        }
        try {
            SaveData data = json.fromJson(SaveData.class, jsonText);
            if (!isStructurallyValid(data)) {
                return null;
            }
            if (data.version != CURRENT_VERSION) {
                Gdx.app.error("SaveService", "Unsupported save version: " + data.version);
                return null;
            }
            return data;
        } catch (SerializationException e) {
            Gdx.app.error("SaveService", "Corrupt save JSON: " + e.getMessage());
            return null;
        } catch (Exception e) {
            Gdx.app.error("SaveService", "Failed to parse save: " + e.getMessage());
            return null;
        }
    }

    public static boolean isStructurallyValid(SaveData data) {
        if (data == null || data.player == null || data.world == null || data.quest == null) {
            return false;
        }
        if (data.player.characterName == null || data.player.characterName.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * @return true if the slot file was written successfully
     */
    public boolean writeSlot(int slotIndex, SaveData data) {
        requireValidSlot(slotIndex);
        if (data == null) {
            return false;
        }
        data.version = CURRENT_VERSION;
        data.slotIndex = slotIndex;
        data.savedAtEpochMs = System.currentTimeMillis();
        if (data.player == null) {
            data.player = new PlayerSaveData();
        }
        if (data.world == null) {
            data.world = new WorldSaveData();
        }
        if (data.quest == null) {
            data.quest = new QuestSaveData();
        }

        FileHandle target = localFile(slotFileName(slotIndex));
        FileHandle temp = localFile(slotFileName(slotIndex) + TEMP_SUFFIX);
        if (target == null || temp == null) {
            return false;
        }
        try {
            String text = json.prettyPrint(data);
            temp.writeString(text, false, "UTF-8");
            SaveData verify = parseAndValidate(temp.readString("UTF-8"));
            if (verify == null) {
                temp.delete();
                return false;
            }
            temp.moveTo(target);
            return true;
        } catch (Exception e) {
            Gdx.app.error("SaveService", "Failed to write slot " + slotIndex + ": " + e.getMessage());
            try {
                if (temp.exists()) {
                    temp.delete();
                }
            } catch (Exception ignored) {
            }
            return false;
        }
    }

    public SaveSlotInfo getSlotInfo(int slotIndex) {
        SaveData data = readSlot(slotIndex);
        if (data == null) {
            return SaveSlotInfo.empty(slotIndex);
        }
        return SaveSlotInfo.from(data);
    }

    public SaveSlotInfo[] getAllSlotInfos() {
        SaveSlotInfo[] infos = new SaveSlotInfo[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            infos[i] = getSlotInfo(i + 1);
        }
        return infos;
    }

    /** Test helper: delete all slot files. */
    public void clearAllSlots() {
        for (int i = 1; i <= SLOT_COUNT; i++) {
            FileHandle file = localFile(slotFileName(i));
            if (file != null && file.exists()) {
                file.delete();
            }
            FileHandle temp = localFile(slotFileName(i) + TEMP_SUFFIX);
            if (temp != null && temp.exists()) {
                temp.delete();
            }
        }
    }

    private static FileHandle localFile(String name) {
        if (Gdx.files == null) {
            return null;
        }
        return Gdx.files.local(name);
    }

    private static void requireValidSlot(int slotIndex) {
        if (slotIndex < 1 || slotIndex > SLOT_COUNT) {
            throw new IllegalArgumentException("Save slot must be 1.." + SLOT_COUNT + ", got " + slotIndex);
        }
    }
}
