package com.donos.zebra.skills;

/**
 * Four hotbar slots referencing skill IDs (null = empty).
 */
public final class SkillBar {

    public static final int SLOT_COUNT = 4;

    private final String[] slotIds = new String[SLOT_COUNT];

    public SkillBar() {
        // Default loadout: Whirlwind / Flame Strike / empty / empty
        slotIds[0] = SkillRegistry.WHIRLWIND_ID;
        slotIds[1] = SkillRegistry.FLAME_STRIKE_ID;
    }

    public String getSlotId(int index) {
        if (index < 0 || index >= SLOT_COUNT) {
            return null;
        }
        return slotIds[index];
    }

    public SkillDefinition getSlotDefinition(int index) {
        return SkillRegistry.get(getSlotId(index));
    }

    public void setSlot(int index, String skillId) {
        if (index < 0 || index >= SLOT_COUNT) {
            return;
        }
        if (skillId == null || skillId.isEmpty()) {
            slotIds[index] = null;
            return;
        }
        if (!SkillRegistry.isKnown(skillId)) {
            return;
        }
        slotIds[index] = skillId;
    }

    public void clearSlot(int index) {
        setSlot(index, null);
    }

    /** Swap two bar slots (empty allowed). */
    public void swapSlots(int a, int b) {
        if (a < 0 || a >= SLOT_COUNT || b < 0 || b >= SLOT_COUNT) {
            return;
        }
        String tmp = slotIds[a];
        slotIds[a] = slotIds[b];
        slotIds[b] = tmp;
    }

    public String[] snapshotIds() {
        return slotIds.clone();
    }

    public void restoreFromIds(String[] ids) {
        for (int i = 0; i < SLOT_COUNT; i++) {
            slotIds[i] = null;
        }
        if (ids == null) {
            return;
        }
        for (int i = 0; i < SLOT_COUNT && i < ids.length; i++) {
            setSlot(i, ids[i]);
        }
    }
}
