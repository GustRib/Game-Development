package com.donos.zebra.world.dungeon;

import com.donos.zebra.config.DungeonGenerationConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DungeonGeneratorTest {

    @Test
    void sameSeedProducesIdenticalLayout() {
        DungeonGenerationConfig config = new DungeonGenerationConfig(
            40, 30, 6, 999L, 4, 8, 16
        );

        DungeonMap first = DungeonGenerator.generate(config);
        DungeonMap second = DungeonGenerator.generate(config);

        assertEquals(first.getSpawnX(), second.getSpawnX(), 0.001f);
        assertEquals(first.getSpawnY(), second.getSpawnY(), 0.001f);
        assertEquals(first.getRooms().size(), second.getRooms().size());

        for (int y = 0; y < first.getHeight(); y++) {
            for (int x = 0; x < first.getWidth(); x++) {
                assertEquals(first.getTile(x, y), second.getTile(x, y));
            }
        }
    }

    @Test
    void generatedDungeonHasWalkableStartAndWalls() {
        DungeonMap dungeonMap = DungeonGenerator.generate(DungeonGenerationConfig.defaults());

        assertFalse(dungeonMap.getRooms().isEmpty());

        int spawnTileX = (int) (dungeonMap.getSpawnX() / dungeonMap.getTileSize());
        int spawnTileY = (int) (dungeonMap.getSpawnY() / dungeonMap.getTileSize());
        assertTrue(dungeonMap.isWalkable(spawnTileX, spawnTileY));

        boolean hasWall = false;
        for (int y = 0; y < dungeonMap.getHeight(); y++) {
            for (int x = 0; x < dungeonMap.getWidth(); x++) {
                if (dungeonMap.getTile(x, y) == TileType.WALL) {
                    hasWall = true;
                    break;
                }
            }
        }
        assertTrue(hasWall);
    }

    @Test
    void differentSeedsCanProduceDifferentLayouts() {
        DungeonMap first = DungeonGenerator.generate(new DungeonGenerationConfig(
            40, 30, 6, 1L, 4, 8, 16
        ));
        DungeonMap second = DungeonGenerator.generate(new DungeonGenerationConfig(
            40, 30, 6, 2L, 4, 8, 16
        ));

        assertNotEquals(first.getSpawnX(), second.getSpawnX(), 0.001f);
    }

    @Test
    void roomsDoNotOverlap() {
        DungeonMap map = DungeonGenerator.generate(DungeonGenerationConfig.defaults());
        List<Room> rooms = map.getRooms();
        for (int i = 0; i < rooms.size(); i++) {
            for (int j = i + 1; j < rooms.size(); j++) {
                assertFalse(rooms.get(i).overlaps(rooms.get(j), 0),
                    "Rooms " + i + " and " + j + " should not overlap");
            }
        }
    }

    @Test
    void spawnReachabilityCoversMajorityOfFloorTiles() {
        DungeonMap map = DungeonGenerator.generate(DungeonGenerationConfig.defaults());
        int spawnTileX = (int) (map.getSpawnX() / map.getTileSize());
        int spawnTileY = (int) (map.getSpawnY() / map.getTileSize());

        int floorCount = 0;
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.isWalkable(x, y)) {
                    floorCount++;
                }
            }
        }

        int reachable = countReachableFloor(map, spawnTileX, spawnTileY);
        assertTrue(reachable > 0);
        // Sequential L-corridors connect rooms; expect most floor reachable from spawn
        assertTrue(reachable >= floorCount * 0.8f,
            "Expected most floor tiles reachable; reachable=" + reachable + " floor=" + floorCount);
    }

    private static int countReachableFloor(DungeonMap map, int startX, int startY) {
        Queue<long[]> queue = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();
        long startKey = (((long) startX) << 32) | (startY & 0xffffffffL);
        queue.add(new long[]{startX, startY});
        visited.add(startKey);

        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            long[] cur = queue.poll();
            int cx = (int) cur[0];
            int cy = (int) cur[1];
            for (int[] d : dirs) {
                int nx = cx + d[0];
                int ny = cy + d[1];
                if (!map.isWalkable(nx, ny)) {
                    continue;
                }
                long key = (((long) nx) << 32) | (ny & 0xffffffffL);
                if (visited.add(key)) {
                    queue.add(new long[]{nx, ny});
                }
            }
        }
        return visited.size();
    }
}
