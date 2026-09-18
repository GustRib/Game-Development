package com.donos.zebra.save;

import java.util.ArrayList;
import java.util.List;

/**
 * Orc combat/loot/respawn state matched on load by spawn position.
 */
public class OrcSaveData {

    public float spawnX;
    public float spawnY;
    public float x;
    public float y;
    public float currentHealth;
    public float maxHealth;
    public boolean dead;
    public float deathTimer;
    public boolean looted;
    public int silverLoot;
    public List<ItemStackSaveData> loot = new ArrayList<>();

    public OrcSaveData() {
    }
}
