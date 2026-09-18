package com.donos.zebra.save;

/**
 * Ore vein state matched on load by spawn position.
 */
public class OreNodeSaveData {

    public float x;
    public float y;
    public int hitsRemaining;
    public float depletedTimer;

    public OreNodeSaveData() {
    }

    public OreNodeSaveData(float x, float y, int hitsRemaining, float depletedTimer) {
        this.x = x;
        this.y = y;
        this.hitsRemaining = hitsRemaining;
        this.depletedTimer = depletedTimer;
    }
}
