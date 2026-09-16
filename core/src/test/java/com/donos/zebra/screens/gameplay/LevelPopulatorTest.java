package com.donos.zebra.screens.gameplay;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.config.DungeonGenerationConfig;
import com.donos.zebra.world.LevelData;
import com.donos.zebra.world.dungeon.DungeonGenerator;
import com.donos.zebra.world.dungeon.DungeonMap;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelPopulatorTest {

    @Test
    void mentorUsesTiledCoordinatesWhenPresent() {
        LevelData levelData = new LevelData(null, 10f, 20f, new Array<>(), new Array<>(), 55f, 66f, true);
        float[] spawn = LevelPopulator.resolveMentorSpawn(levelData);
        assertEquals(55f, spawn[0], 0.01f);
        assertEquals(66f, spawn[1], 0.01f);
    }

    @Test
    void mentorFallsBackNearPlayerWhenAbsent() {
        LevelData levelData = new LevelData(null, 10f, 20f, new Array<>(), new Array<>(), 0f, 0f, false);
        float[] spawn = LevelPopulator.resolveMentorSpawn(levelData);
        assertEquals(50f, spawn[0], 0.01f);
        assertEquals(30f, spawn[1], 0.01f);
    }

    @Test
    void tiledModeSpawnsTwoOrcsAtFixedOffsets() {
        List<Vector2> spawns = LevelPopulator.resolveOrcSpawns(false, null, 100f, 200f);
        assertEquals(2, spawns.size());
        assertEquals(160f, spawns.get(0).x, 0.01f);
        assertEquals(260f, spawns.get(0).y, 0.01f);
        assertEquals(220f, spawns.get(1).x, 0.01f);
        assertEquals(160f, spawns.get(1).y, 0.01f);
    }

    @Test
    void proceduralModeSkipsRoomsTooCloseToPlayerSpawn() {
        DungeonMap dungeonMap = DungeonGenerator.generate(DungeonGenerationConfig.defaults());
        List<Vector2> spawns = LevelPopulator.resolveOrcSpawns(
            true, dungeonMap, dungeonMap.getSpawnX(), dungeonMap.getSpawnY());

        for (Vector2 spawn : spawns) {
            float dist = Vector2.dst(dungeonMap.getSpawnX(), dungeonMap.getSpawnY(), spawn.x, spawn.y);
            assertTrue(dist > 48f);
        }
        assertTrue(spawns.size() <= dungeonMap.getRooms().size());
    }
}
