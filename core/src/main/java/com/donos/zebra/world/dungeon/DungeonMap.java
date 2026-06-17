package com.donos.zebra.world.dungeon;

import java.util.Collections;
import java.util.List;

public final class DungeonMap {

    private final int width;
    private final int height;
    private final int tileSize;
    private final long seed;
    private final TileType[][] tiles;
    private final List<Room> rooms;
    private final float spawnX;
    private final float spawnY;

    public DungeonMap(int width, int height, int tileSize, long seed, TileType[][] tiles,
                      List<Room> rooms, float spawnX, float spawnY) {
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        this.seed = seed;
        this.tiles = tiles;
        this.rooms = Collections.unmodifiableList(rooms);
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTileSize() {
        return tileSize;
    }

    public long getSeed() {
        return seed;
    }

    public TileType getTile(int x, int y) {
        return tiles[y][x];
    }

    public TileType[][] getTiles() {
        return tiles;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public float getSpawnX() {
        return spawnX;
    }

    public float getSpawnY() {
        return spawnY;
    }

    public boolean isWalkable(int tileX, int tileY) {
        if (tileX < 0 || tileY < 0 || tileX >= width || tileY >= height) {
            return false;
        }
        return tiles[tileY][tileX] == TileType.FLOOR;
    }
}
