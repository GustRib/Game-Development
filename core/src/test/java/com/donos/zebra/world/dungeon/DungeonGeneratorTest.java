package com.donos.zebra.world.dungeon;

import com.donos.zebra.config.DungeonGenerationConfig;
import org.junit.jupiter.api.Test;

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
}
