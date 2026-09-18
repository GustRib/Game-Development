package com.donos.zebra.save;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * UI-facing summary of a save slot (empty or occupied).
 */
public final class SaveSlotInfo {

    public final int slotIndex;
    public final boolean occupied;
    public final String characterName;
    public final long savedAtEpochMs;
    public final String worldId;
    public final boolean inTavern;

    private SaveSlotInfo(int slotIndex, boolean occupied, String characterName,
                         long savedAtEpochMs, String worldId, boolean inTavern) {
        this.slotIndex = slotIndex;
        this.occupied = occupied;
        this.characterName = characterName;
        this.savedAtEpochMs = savedAtEpochMs;
        this.worldId = worldId;
        this.inTavern = inTavern;
    }

    public static SaveSlotInfo empty(int slotIndex) {
        return new SaveSlotInfo(slotIndex, false, null, 0L, null, false);
    }

    public static SaveSlotInfo from(SaveData data) {
        String world = data.world != null ? data.world.worldId : "overworld";
        boolean tavern = data.player != null && data.player.inTavern;
        return new SaveSlotInfo(
            data.slotIndex > 0 ? data.slotIndex : 0,
            true,
            data.player.characterName,
            data.savedAtEpochMs,
            world,
            tavern
        );
    }

    public String formatTimestamp() {
        if (savedAtEpochMs <= 0L) {
            return "";
        }
        return new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            .format(new Date(savedAtEpochMs));
    }

    public String summaryLine() {
        if (!occupied) {
            return "VAZIO";
        }
        String loc = inTavern ? "Taverna" : (worldId != null ? worldId : "Mundo");
        String time = formatTimestamp();
        if (time.isEmpty()) {
            return characterName + " — " + loc;
        }
        return characterName + " — " + loc + " — " + time;
    }
}
