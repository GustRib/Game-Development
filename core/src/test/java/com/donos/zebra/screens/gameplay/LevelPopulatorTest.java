package com.donos.zebra.screens.gameplay;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.Interaction.Interactable;
import com.donos.zebra.config.DungeonGenerationConfig;
import com.donos.zebra.entities.OreNode;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.ui.DialogueUI;
import com.donos.zebra.util.CollisionMovement;
import com.donos.zebra.world.LevelData;
import com.donos.zebra.world.dungeon.DungeonGenerator;
import com.donos.zebra.world.dungeon.DungeonMap;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LevelPopulatorTest {

    private static final float MENTOR_X = 152.667f;
    private static final float MENTOR_Y = 104.667f;
    /** Centers matching prototipo.tmx OreNode rects after relocation around mentor. */
    private static final Vector2[] MENTOR_ORE_CENTERS = {
        new Vector2(128f, 128f),
        new Vector2(180f, 88f),
        new Vector2(152f, 140f),
        new Vector2(188f, 128f)
    };

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
    void tiledModeSpawnsSixSoutheastOrcsAndNoneNearPlayerSpawn() {
        float playerX = 55.3333f;
        float playerY = 70f;
        List<Vector2> spawns = LevelPopulator.resolveOrcSpawns(false, null, playerX, playerY);
        assertEquals(6, spawns.size());

        for (Vector2 spawn : spawns) {
            float dist = Vector2.dst(playerX, playerY, spawn.x, spawn.y);
            assertTrue(dist > 200f, "orc too close to player spawn: " + spawn);
            assertTrue(spawn.x >= 580f && spawn.x <= 750f, "expected SE x: " + spawn);
            assertTrue(spawn.y >= 90f && spawn.y <= 230f, "expected SE y: " + spawn);
        }

        assertEquals(650f, spawns.get(0).x, 0.01f);
        assertEquals(150f, spawns.get(0).y, 0.01f);
        assertEquals(700f, spawns.get(1).x, 0.01f);
        assertEquals(200f, spawns.get(1).y, 0.01f);
    }

    @Test
    void villageOrcSpawnsDoNotOverlapSampleCollisionRects() {
        Array<Polygon> walls = villageCollisionSample();
        List<Vector2> spawns = LevelPopulator.resolveOrcSpawns(false, null, 55f, 70f);
        List<Vector2> walkable = LevelPopulator.filterWalkableSpawns(
            spawns, walls, LevelPopulator.VILLAGE_ORC_HITBOX);
        assertEquals(spawns.size(), walkable.size());
        for (Vector2 spawn : spawns) {
            assertFalse(CollisionMovement.overlapsWalls(
                spawn.x, spawn.y, LevelPopulator.VILLAGE_ORC_HITBOX, walls));
        }
    }

    @Test
    void mentorOreNodeCentersAreNearMentorAndClearOfCollisionSample() {
        Array<Polygon> walls = villageCollisionSample();
        float[] oreHitbox = {0f, 0f, 16f, 0f, 16f, 16f, 0f, 16f};
        for (Vector2 center : MENTOR_ORE_CENTERS) {
            float dist = Vector2.dst(MENTOR_X, MENTOR_Y, center.x, center.y);
            assertTrue(dist >= 30f && dist <= 55f, "ore not near mentor: " + center + " dist=" + dist);
            float blX = center.x - 8f;
            float blY = center.y - 8f;
            assertFalse(CollisionMovement.overlapsWalls(blX, blY, oreHitbox, walls),
                "ore overlaps collision: " + center);
        }
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

    /** Subset of prototipo.tmx colisao rects near mentor / SE orc area. */
    private static Array<Polygon> villageCollisionSample() {
        Array<Polygon> walls = new Array<>();
        addRect(walls, 116f, 43.3333f, 28f, 26.6667f);
        addRect(walls, 122.667f, 164f, 12f, 6f);
        addRect(walls, 72f, 117.333f, 12f, 6f);
        addRect(walls, 214.667f, 118.667f, 14.6667f, 5.33333f);
        addRect(walls, 610f, 83.3333f, 43.3333f, 9.33333f);
        addRect(walls, 581.333f, 114.667f, 9.33333f, 7.33333f);
        addRect(walls, 656.667f, 223.333f, 14f, 6f);
        addRect(walls, 714f, 258.667f, 27.3333f, 20f);
        addRect(walls, 727.333f, 216.667f, 32f, 10f);
        addRect(walls, 599.333f, 248f, 17.3333f, 14f);
        return walls;
    }

    private static void addRect(Array<Polygon> walls, float x, float y, float w, float h) {
        walls.add(new Polygon(new float[]{
            x, y,
            x + w, y,
            x + w, y + h,
            x, y + h
        }));
    }
}
