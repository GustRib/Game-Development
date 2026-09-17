package com.donos.zebra.screens.gameplay;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.Interaction.Interactable;
import com.donos.zebra.entities.CraftingStation;
import com.donos.zebra.entities.MapDoor;
import com.donos.zebra.entities.MentorAnimationLoader;
import com.donos.zebra.entities.MentorNpc;
import com.donos.zebra.entities.Orc;
import com.donos.zebra.entities.OrcAnimationLoader;
import com.donos.zebra.entities.OreNode;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.ui.CraftingUI;
import com.donos.zebra.ui.DialogueUI;
import com.donos.zebra.world.LevelData;
import com.donos.zebra.world.dungeon.DungeonMap;
import com.donos.zebra.world.dungeon.Room;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spawns mentors and enemies into a level based on LevelData / DungeonMap.
 */
public final class LevelPopulator {

    private static final float PROCEDURAL_ORC_MIN_SPAWN_DISTANCE = 48f;

    private LevelPopulator() {
    }

    /**
     * Pure spawn-position logic for mentors (no AssetManager required).
     */
    public static float[] resolveMentorSpawn(LevelData levelData) {
        if (levelData.hasMentor) {
            return new float[]{levelData.mentorX, levelData.mentorY};
        }
        return new float[]{levelData.spawnX + 40f, levelData.spawnY + 10f};
    }

    /**
     * Pure spawn-position logic for orcs (no AssetManager / animation loading).
     */
    public static List<Vector2> resolveOrcSpawns(boolean proceduralMap,
                                                 DungeonMap dungeonMap,
                                                 float playerSpawnX,
                                                 float playerSpawnY) {
        List<Vector2> spawns = new ArrayList<>();
        if (proceduralMap && dungeonMap != null && dungeonMap.getRooms() != null) {
            int tileSize = dungeonMap.getTileSize();
            for (Room room : dungeonMap.getRooms()) {
                float roomCenterX = room.getCenterX() * tileSize + tileSize / 2f;
                float roomCenterY = room.getCenterY() * tileSize + tileSize / 2f;
                float distanceFromSpawn = Vector2.dst(playerSpawnX, playerSpawnY, roomCenterX, roomCenterY);
                if (distanceFromSpawn > PROCEDURAL_ORC_MIN_SPAWN_DISTANCE) {
                    spawns.add(new Vector2(roomCenterX, roomCenterY));
                }
            }
        } else {
            // Village map: two near spawn + three in the southeast for farming loops.
            spawns.add(new Vector2(playerSpawnX + 60f, playerSpawnY + 60f));
            spawns.add(new Vector2(playerSpawnX + 120f, playerSpawnY - 40f));
            spawns.add(new Vector2(650f, 150f));
            spawns.add(new Vector2(700f, 200f));
            spawns.add(new Vector2(620f, 80f));
        }
        return spawns;
    }

    public static MentorNpc addMentor(LevelData levelData,
                                        DialogueUI dialogueWindow,
                                        AssetManager assetManager,
                                        List<Interactable> interactables,
                                        List<? super MentorNpc> entities) {
        Map<String, Animation<TextureRegion>[]> mentorAnims =
            MentorAnimationLoader.loadAnimations(assetManager);

        float[] spawn = resolveMentorSpawn(levelData);
        MentorNpc mentor = new MentorNpc(spawn[0], spawn[1], dialogueWindow, mentorAnims);
        interactables.add(mentor);
        entities.add(mentor);
        return mentor;
    }

    public static void addOrcs(boolean proceduralMap,
                               DungeonMap dungeonMap,
                               float playerSpawnX,
                               float playerSpawnY,
                               AssetManager assetManager,
                               List<? super Orc> entities) {
        Map<String, Animation<TextureRegion>[]> orcAnims =
            OrcAnimationLoader.loadAnimations(assetManager);

        for (Vector2 spawn : resolveOrcSpawns(proceduralMap, dungeonMap, playerSpawnX, playerSpawnY)) {
            entities.add(new Orc(spawn.x, spawn.y, orcAnims));
        }
    }

    public static void addOreNodes(LevelData levelData,
                                   DialogueUI dialogueWindow,
                                   AssetManager assetManager,
                                   List<Interactable> interactables,
                                   List<? super OreNode> entities) {
        if (levelData.oreNodePositions.isEmpty()) {
            return;
        }

        Texture oreTexture = assetManager.get(ItemRegistry.COPPER_ORE.getIconPath(), Texture.class);
        for (Vector2 pos : levelData.oreNodePositions) {
            OreNode node = new OreNode(pos.x, pos.y, oreTexture, dialogueWindow);
            interactables.add(node);
            entities.add(node);
        }
    }

    public static CraftingStation addCraftingStation(LevelData levelData,
                                                     CraftingUI craftingUI,
                                                     AssetManager assetManager,
                                                     List<Interactable> interactables,
                                                     List<? super CraftingStation> entities,
                                                     Array<Polygon> collisionPolygons) {
        if (!levelData.hasCraftingStation) {
            return null;
        }

        CraftingStation station = new CraftingStation(
            levelData.craftingStationX,
            levelData.craftingStationY,
            assetManager.isLoaded(CraftingStation.FORGE_TEXTURE_PATH)
                ? assetManager.get(CraftingStation.FORGE_TEXTURE_PATH, Texture.class)
                : null,
            craftingUI);
        interactables.add(station);
        entities.add(station);
        if (collisionPolygons != null) {
            collisionPolygons.add(station.getCollisionPolygon());
        }
        return station;
    }

    public static MapDoor addDoorHouse(LevelData levelData,
                                       List<Interactable> interactables,
                                       List<? super MapDoor> entities,
                                       MapDoor.Transition transition) {
        if (!levelData.hasDoorHouse) {
            return null;
        }
        // Match TMX DoorHouse rect (12x16 door plate on village house facade)
        MapDoor door = new MapDoor(
            levelData.doorHouseX, levelData.doorHouseY, 12f, 16f,
            "[E] Entrar", transition);
        interactables.add(door);
        entities.add(door);
        return door;
    }

    public static MapDoor addDoorExit(LevelData levelData,
                                      List<Interactable> interactables,
                                      List<? super MapDoor> entities,
                                      MapDoor.Transition transition) {
        if (!levelData.hasDoorExit) {
            return null;
        }
        MapDoor door = new MapDoor(
            levelData.doorExitX, levelData.doorExitY, 32f, 18f,
            "[E] Sair", transition);
        interactables.add(door);
        entities.add(door);
        return door;
    }
}
