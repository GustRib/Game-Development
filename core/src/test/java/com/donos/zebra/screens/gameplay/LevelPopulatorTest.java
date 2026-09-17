package com.donos.zebra.screens.gameplay;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.Interaction.Interactable;
import com.donos.zebra.config.DungeonGenerationConfig;
import com.donos.zebra.entities.OreNode;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.ui.DialogueUI;
import com.donos.zebra.world.LevelData;
import com.donos.zebra.world.dungeon.DungeonGenerator;
import com.donos.zebra.world.dungeon.DungeonMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void tiledModeSpawnsFiveOrcsIncludingSoutheast() {
        List<Vector2> spawns = LevelPopulator.resolveOrcSpawns(false, null, 100f, 200f);
        assertEquals(5, spawns.size());
        assertEquals(160f, spawns.get(0).x, 0.01f);
        assertEquals(260f, spawns.get(0).y, 0.01f);
        assertEquals(220f, spawns.get(1).x, 0.01f);
        assertEquals(160f, spawns.get(1).y, 0.01f);
        assertEquals(650f, spawns.get(2).x, 0.01f);
        assertEquals(150f, spawns.get(2).y, 0.01f);
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

    @Test
    void addOreNodesPutsNodesInEntitiesAndInteractables() {
        List<Vector2> orePositions = Arrays.asList(new Vector2(176f, 544f), new Vector2(208f, 520f));
        LevelData levelData = new LevelData(
            null, 10f, 20f, new Array<>(), new Array<>(), 55f, 66f, true, orePositions);

        AssetManager assetManager = mock(AssetManager.class);
        when(assetManager.get(ItemRegistry.COPPER_ORE.getIconPath(), Texture.class))
            .thenReturn(mock(Texture.class));

        List<Interactable> interactables = new ArrayList<>();
        List<Object> entities = new ArrayList<>();

        LevelPopulator.addOreNodes(levelData, mock(DialogueUI.class), assetManager, interactables, entities);

        assertEquals(2, entities.size());
        assertEquals(2, interactables.size());
        assertTrue(entities.get(0) instanceof OreNode);
        assertTrue(entities.get(1) instanceof OreNode);
        assertEquals(176f, ((OreNode) entities.get(0)).getX(), 0.01f);
        assertEquals(544f, ((OreNode) entities.get(0)).getY(), 0.01f);
    }
}
