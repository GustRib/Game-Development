package com.donos.zebra.world;

public final class LevelConstants {

    public static final String MAP_PATH = "maps/Dungeon1.tmx";

    /**
     * walls_floor tileset (firstgid=377, columns=17).
     * GID 447 = row 5, column 3 — detailed dark stone/cobblestone floor.
     */
    public static final int PROCEDURAL_FLOOR_TILE_GID = 447;

    /**
     * walls_floor tileset (firstgid=377, columns=17).
     * GID 395 = row 2, column 2 — front-facing brick wall.
     */
    public static final int PROCEDURAL_WALL_TILE_GID = 395;

    public static final String SPAWN_LAYER = "spawn";
    public static final String COLLISION_LAYER = "colisao";
    public static final String SPAWN_OBJECT = "playerSpawn";

    public static final float DEFAULT_SPAWN_X = 100f;
    public static final float DEFAULT_SPAWN_Y = 100f;

    private LevelConstants() {
    }
}
