package com.donos.zebra.world;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shared constants for the opening "mine copper → first sword" slice.
 */
public final class OpeningQuest {

    /** Three ores: one depleted node (3 strikes) completes the turn-in without busywork. */
    public static final int COPPER_ORE_REQUIRED = 3;

    public static final int ORE_NODE_HITS = 3;

    public static final String ORE_NODE_OBJECT_PREFIX = "OreNode";

    public static final String CRAFTING_STATION_OBJECT = "CraftingStation";

    private OpeningQuest() {
    }

    public static List<Vector2> emptyOreList() {
        return Collections.emptyList();
    }

    public static List<Vector2> mutableOreList() {
        return new ArrayList<>();
    }
}
