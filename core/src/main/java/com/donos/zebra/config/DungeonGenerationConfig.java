package com.donos.zebra.config;

public final class DungeonGenerationConfig {

    public static final int DEFAULT_MAP_WIDTH = 60;
    public static final int DEFAULT_MAP_HEIGHT = 45;
    public static final int DEFAULT_ROOM_COUNT = 8;
    public static final long DEFAULT_SEED = 12345L;
    public static final int DEFAULT_MIN_ROOM_SIZE = 5;
    public static final int DEFAULT_MAX_ROOM_SIZE = 10;
    public static final int DEFAULT_TILE_SIZE = 16;

    private final int mapWidth;
    private final int mapHeight;
    private final int roomCount;
    private final long seed;
    private final int minRoomSize;
    private final int maxRoomSize;
    private final int tileSize;

    public DungeonGenerationConfig(int mapWidth, int mapHeight, int roomCount, long seed,
                                   int minRoomSize, int maxRoomSize, int tileSize) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.roomCount = roomCount;
        this.seed = seed;
        this.minRoomSize = minRoomSize;
        this.maxRoomSize = maxRoomSize;
        this.tileSize = tileSize;
    }

    public static DungeonGenerationConfig defaults() {
        return new DungeonGenerationConfig(
            DEFAULT_MAP_WIDTH,
            DEFAULT_MAP_HEIGHT,
            DEFAULT_ROOM_COUNT,
            DEFAULT_SEED,
            DEFAULT_MIN_ROOM_SIZE,
            DEFAULT_MAX_ROOM_SIZE,
            DEFAULT_TILE_SIZE
        );
    }

    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int getRoomCount() {
        return roomCount;
    }

    public long getSeed() {
        return seed;
    }

    public int getMinRoomSize() {
        return minRoomSize;
    }

    public int getMaxRoomSize() {
        return maxRoomSize;
    }

    public int getTileSize() {
        return tileSize;
    }
}
