package com.donos.zebra.world;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Polygon;
import com.donos.zebra.HeadlessTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Replaces the old file-based Spawn/Collision TMX tests with in-memory TiledMap fixtures
 * so headless runs do not depend on loading prototipo.tmx from disk.
 */
class LevelLoaderParsingTest extends HeadlessTestBase {

    @Test
    void parsesPlayerSpawnAndCollisionRectsFromInMemoryMap() {
        TiledMap map = new TiledMap();

        MapLayer spawnLayer = new MapLayer();
        spawnLayer.setName(LevelConstants.SPAWN_LAYER);
        RectangleMapObject playerSpawn = new RectangleMapObject(40f, 60f, 20f, 20f);
        playerSpawn.setName(LevelConstants.SPAWN_OBJECT);
        spawnLayer.getObjects().add(playerSpawn);

        RectangleMapObject mentor = new RectangleMapObject(140f, 80f, 16f, 16f);
        mentor.setName("Mentor");
        spawnLayer.getObjects().add(mentor);

        RectangleMapObject ore1 = new RectangleMapObject(168f, 248f, 16f, 16f);
        ore1.setName("OreNode1");
        spawnLayer.getObjects().add(ore1);
        RectangleMapObject ore2 = new RectangleMapObject(200f, 272f, 16f, 16f);
        ore2.setName("OreNode2");
        spawnLayer.getObjects().add(ore2);
        map.getLayers().add(spawnLayer);

        MapLayer collisionLayer = new MapLayer();
        collisionLayer.setName(LevelConstants.COLLISION_LAYER);
        collisionLayer.getObjects().add(new RectangleMapObject(0f, 0f, 32f, 32f));
        collisionLayer.getObjects().add(new RectangleMapObject(64f, 0f, 16f, 48f));
        map.getLayers().add(collisionLayer);

        LevelData levelData = LevelLoader.load(map);

        assertEquals(50f, levelData.spawnX, 0.01f);
        assertEquals(70f, levelData.spawnY, 0.01f);
        assertTrue(levelData.hasMentor);
        assertEquals(148f, levelData.mentorX, 0.01f);
        assertEquals(88f, levelData.mentorY, 0.01f);
        assertEquals(2, levelData.oreNodePositions.size());
        assertEquals(176f, levelData.oreNodePositions.get(0).x, 0.01f);
        assertEquals(256f, levelData.oreNodePositions.get(0).y, 0.01f);
        assertEquals(2, levelData.collisionRects.size);
        assertFalse(levelData.collisionPolygons.isEmpty());
        for (Polygon polygon : levelData.collisionPolygons) {
            assertEquals(8, polygon.getVertices().length);
        }
    }

    @Test
    void missingSpawnLayerFallsBackToDefaults() {
        TiledMap map = new TiledMap();
        LevelData levelData = LevelLoader.load(map);

        assertEquals(LevelConstants.DEFAULT_SPAWN_X, levelData.spawnX, 0.01f);
        assertEquals(LevelConstants.DEFAULT_SPAWN_Y, levelData.spawnY, 0.01f);
        assertFalse(levelData.hasMentor);
        assertTrue(levelData.oreNodePositions.isEmpty());
        assertTrue(levelData.collisionPolygons.isEmpty());
    }
}
