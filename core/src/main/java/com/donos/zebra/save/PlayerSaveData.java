package com.donos.zebra.save;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistable player state (not a runtime Player graph).
 */
public class PlayerSaveData {

    public String characterName = "";
    public float x;
    public float y;
    public float currentHealth = 100f;
    public float maxHealth = 100f;
    public boolean hasFirstSword;
    public int totalSilver;
    public List<ItemStackSaveData> inventorySlots = new ArrayList<>();
    public String equippedWeaponId;
    public String equippedHelmetId;
    public String equippedChestplateId;
    public String equippedGlovesId;
    public String equippedBootsId;
    public ItemStackSaveData potionSlot;
    public float potionCooldownRemaining;
    public boolean inTavern;
    public float outdoorReturnX;
    public float outdoorReturnY;
}
