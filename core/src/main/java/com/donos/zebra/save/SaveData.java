package com.donos.zebra.save;

/**
 * Versioned top-level save document. Runtime objects are never serialized directly.
 */
public class SaveData {

    public static final int CURRENT_VERSION = 2;

    public int version = CURRENT_VERSION;
    public long savedAtEpochMs;
    public int slotIndex;
    public PlayerSaveData player = new PlayerSaveData();
    public WorldSaveData world = new WorldSaveData();
    public QuestSaveData quest = new QuestSaveData();
    public CraftingSaveData crafting;
}
