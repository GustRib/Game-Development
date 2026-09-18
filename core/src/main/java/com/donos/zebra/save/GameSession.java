package com.donos.zebra.save;

/**
 * Session handed from menu/intro into LoadingScreen / GameScreen.
 * Screen navigation owns menu/create/intro; this only carries play intent.
 */
public final class GameSession {

    private final String characterName;
    private final int activeSlot;
    private final SaveData loadedSave;
    private final boolean fromContinue;

    private GameSession(String characterName, int activeSlot, SaveData loadedSave, boolean fromContinue) {
        this.characterName = characterName;
        this.activeSlot = activeSlot;
        this.loadedSave = loadedSave;
        this.fromContinue = fromContinue;
    }

    /** Brand-new character after intro (no save applied yet). */
    public static GameSession newGame(String characterName) {
        return new GameSession(characterName, 0, null, false);
    }

    /** Continue from an occupied slot. */
    public static GameSession continueFrom(SaveData save, int slotIndex) {
        String name = save != null && save.player != null ? save.player.characterName : "";
        return new GameSession(name, slotIndex, save, true);
    }

    /** Restart mid-session: keep name/slot binding, discard loaded snapshot. */
    public GameSession forRestart() {
        return new GameSession(characterName, activeSlot, null, false);
    }

    public GameSession withActiveSlot(int slotIndex) {
        return new GameSession(characterName, slotIndex, loadedSave, fromContinue);
    }

    public String getCharacterName() {
        return characterName;
    }

    /** 1..3 once bound by save/continue; 0 if unbound. */
    public int getActiveSlot() {
        return activeSlot;
    }

    public boolean hasActiveSlot() {
        return activeSlot >= 1 && activeSlot <= SaveService.SLOT_COUNT;
    }

    public SaveData getLoadedSave() {
        return loadedSave;
    }

    public boolean isFromContinue() {
        return fromContinue;
    }

    public boolean shouldApplySave() {
        return loadedSave != null;
    }
}
