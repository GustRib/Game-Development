package com.donos.zebra.world.dungeon;

import com.donos.zebra.config.DungeonGenerationConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class DungeonGenerator {

    private static final int ROOM_PLACEMENT_PADDING = 1;
    private static final int MAX_PLACEMENT_ATTEMPTS_MULTIPLIER = 50;

    private DungeonGenerator() {
    }

    public static DungeonMap generate(DungeonGenerationConfig config) {
        Random random = new Random(config.getSeed());
        TileType[][] tiles = createWallGrid(config.getMapWidth(), config.getMapHeight());
        List<Room> rooms = placeRooms(config, random, tiles);

        for (int i = 0; i < rooms.size() - 1; i++) {
            Room from = rooms.get(i);
            Room to = rooms.get(i + 1);
            carveCorridor(tiles, from.getCenterX(), from.getCenterY(), to.getCenterX(), to.getCenterY());
        }

        float spawnX;
        float spawnY;
        if (rooms.isEmpty()) {
            spawnX = config.getMapWidth() * config.getTileSize() / 2f;
            spawnY = config.getMapHeight() * config.getTileSize() / 2f;
        } else {
            Room startRoom = rooms.get(0);
            spawnX = tileCenterToWorld(startRoom.getCenterX(), config.getTileSize());
            spawnY = tileCenterToWorld(startRoom.getCenterY(), config.getTileSize());
        }

        return new DungeonMap(
            config.getMapWidth(),
            config.getMapHeight(),
            config.getTileSize(),
            config.getSeed(),
            tiles,
            rooms,
            spawnX,
            spawnY
        );
    }

    private static TileType[][] createWallGrid(int width, int height) {
        TileType[][] tiles = new TileType[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                tiles[y][x] = TileType.WALL;
            }
        }
        return tiles;
    }

    private static List<Room> placeRooms(DungeonGenerationConfig config, Random random, TileType[][] tiles) {
        List<Room> rooms = new ArrayList<>();
        int maxAttempts = config.getRoomCount() * MAX_PLACEMENT_ATTEMPTS_MULTIPLIER;
        int attempts = 0;

        while (rooms.size() < config.getRoomCount() && attempts < maxAttempts) {
            attempts++;

            int width = randomRoomSize(config, random);
            int height = randomRoomSize(config, random);
            int maxX = config.getMapWidth() - width - 1;
            int maxY = config.getMapHeight() - height - 1;
            if (maxX <= 1 || maxY <= 1) {
                break;
            }

            int x = 1 + random.nextInt(maxX - 1);
            int y = 1 + random.nextInt(maxY - 1);
            Room candidate = new Room(x, y, width, height);

            if (overlapsExisting(candidate, rooms)) {
                continue;
            }

            rooms.add(candidate);
            carveRoom(tiles, candidate);
        }

        return rooms;
    }

    private static int randomRoomSize(DungeonGenerationConfig config, Random random) {
        int min = config.getMinRoomSize();
        int max = config.getMaxRoomSize();
        if (min >= max) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
    }

    private static boolean overlapsExisting(Room candidate, List<Room> rooms) {
        for (Room existing : rooms) {
            if (candidate.overlaps(existing, ROOM_PLACEMENT_PADDING)) {
                return true;
            }
        }
        return false;
    }

    private static void carveRoom(TileType[][] tiles, Room room) {
        for (int y = room.getY(); y < room.getY() + room.getHeight(); y++) {
            for (int x = room.getX(); x < room.getX() + room.getWidth(); x++) {
                tiles[y][x] = TileType.FLOOR;
            }
        }
    }

    private static void carveCorridor(TileType[][] tiles, int fromX, int fromY, int toX, int toY) {
        int x = fromX;
        int y = fromY;

        while (x != toX) {
            tiles[y][x] = TileType.FLOOR;
            x += x < toX ? 1 : -1;
        }

        while (y != toY) {
            tiles[y][x] = TileType.FLOOR;
            y += y < toY ? 1 : -1;
        }

        tiles[y][x] = TileType.FLOOR;
    }

    private static float tileCenterToWorld(int tileCoordinate, int tileSize) {
        return tileCoordinate * tileSize + tileSize / 2f;
    }
}
