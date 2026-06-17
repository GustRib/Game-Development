package com.donos.zebra.world;

import com.donos.zebra.HeadlessTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SpawnParsingTest extends HeadlessTestBase {

    @Test
    void dungeonMapProvidesValidSpawnPosition() {
        LevelData levelData = LevelLoader.load(LevelConstants.MAP_PATH);

        assertNotNull(levelData);
        assertNotEquals(0f, levelData.spawnX, 0.01f);
        assertNotEquals(0f, levelData.spawnY, 0.01f);
    }
}
