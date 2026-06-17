package com.donos.zebra.world;

import com.donos.zebra.HeadlessTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class CollisionExtractionTest extends HeadlessTestBase {

    @Test
    void dungeonMapProvidesCollisionPolygons() {
        LevelData levelData = LevelLoader.load(LevelConstants.MAP_PATH);

        assertFalse(levelData.collisionPolygons.isEmpty());
    }
}
