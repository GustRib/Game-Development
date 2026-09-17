package com.donos.zebra.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.entities.PlayerAnimationLoader;

import java.util.ArrayList;
import java.util.List;

public final class LevelLoader {

    private LevelLoader() {
    }

    public static LevelData load(String mapPath) {
        TiledMap map = new TmxMapLoader().load(mapPath);
        return load(map);
    }

    public static void queueAssets(AssetManager assetManager, String mapPath) {
        assetManager.load(mapPath, TiledMap.class);
        PlayerAnimationLoader.queueAssets(assetManager);
        
        // ADICIONE ESTA LINHA AQUI: Enfileira o Mentor junto com o mapa e o Player!
        com.donos.zebra.entities.MentorAnimationLoader.queueAssets(assetManager);
    }

    public static void queueTilesetReference(AssetManager assetManager, String mapPath) {
        assetManager.load(mapPath, TiledMap.class);
    }

    public static LevelData load(AssetManager assetManager, String mapPath) {
        TiledMap map = assetManager.get(mapPath, TiledMap.class);
        return load(map);
    }

    public static LevelData load(TiledMap map) {
        float spawnX = LevelConstants.DEFAULT_SPAWN_X;
        float spawnY = LevelConstants.DEFAULT_SPAWN_Y;

        // --- VARIÁVEIS DO MENTOR ---
        float mentorX = 0f;
        float mentorY = 0f;
        boolean hasMentor = false;
        List<Vector2> oreNodePositions = new ArrayList<>();
        float craftingStationX = 0f;
        float craftingStationY = 0f;
        boolean hasCraftingStation = false;

        MapLayer spawnLayer = map.getLayers().get(LevelConstants.SPAWN_LAYER);
        if (spawnLayer != null) {
            MapObjects spawnObjects = spawnLayer.getObjects();
            
            // 1. Lógica original do Spawn do Player
            MapObject spawn = spawnObjects.get(LevelConstants.SPAWN_OBJECT);
            if (spawn == null && spawnObjects.getCount() > 0) {
                spawn = spawnObjects.get(0);
            }

            if (spawn != null) {
                Float sx = null;
                Float sy = null;

                if (spawn instanceof RectangleMapObject) {
                    Rectangle r = ((RectangleMapObject) spawn).getRectangle();
                    sx = r.x + r.width / 2f;
                    sy = r.y + r.height / 2f;
                } else {
                    MapProperties p = spawn.getProperties();
                    sx = p.get("x", Float.class);
                    sy = p.get("y", Float.class);
                }

                if (sx != null && sy != null) {
                    spawnX = sx;
                    spawnY = sy;
                    Gdx.app.log("SPAWN", "Player posicionado em: " + spawnX + ", " + spawnY);
                } else {
                    Gdx.app.error("SPAWN", "Spawn object missing x/y properties; using default position ("
                        + LevelConstants.DEFAULT_SPAWN_X + ", " + LevelConstants.DEFAULT_SPAWN_Y + ").");
                }
            } else {
                Gdx.app.error("SPAWN", "Nenhum objeto encontrado na layer spawn; using default position ("
                    + LevelConstants.DEFAULT_SPAWN_X + ", " + LevelConstants.DEFAULT_SPAWN_Y + ").");
            }

            // 2. Mentor + ore nodes on the same spawn layer
            for (MapObject object : spawnObjects) {
                String name = object.getName();
                if (name == null) {
                    continue;
                }

                if ("Mentor".equals(name)) {
                    float[] pos = readObjectCenter(object);
                    if (pos != null) {
                        mentorX = pos[0];
                        mentorY = pos[1];
                        hasMentor = true;
                        Gdx.app.log("SPAWN", "Mentor detectado no Tiled em: " + mentorX + ", " + mentorY);
                    }
                } else if (name.startsWith(OpeningQuest.ORE_NODE_OBJECT_PREFIX)) {
                    float[] pos = readObjectCenter(object);
                    if (pos != null) {
                        oreNodePositions.add(new Vector2(pos[0], pos[1]));
                        Gdx.app.log("SPAWN", "OreNode detectado em: " + pos[0] + ", " + pos[1]);
                    }
                } else if (OpeningQuest.CRAFTING_STATION_OBJECT.equals(name)) {
                    float[] pos = readObjectCenter(object);
                    if (pos != null) {
                        craftingStationX = pos[0];
                        craftingStationY = pos[1];
                        hasCraftingStation = true;
                        Gdx.app.log("SPAWN", "CraftingStation detectado em: "
                            + craftingStationX + ", " + craftingStationY);
                    }
                }
            }
        } else {
            Gdx.app.error("SPAWN", "Camada 'spawn' não encontrada! Using default position ("
                + LevelConstants.DEFAULT_SPAWN_X + ", " + LevelConstants.DEFAULT_SPAWN_Y + ").");
        }

        // 3. Lógica de Colisões original
        Array<Rectangle> collisionRects = new Array<>();
        MapLayer collisionLayer = map.getLayers().get(LevelConstants.COLLISION_LAYER);
        if (collisionLayer != null) {
            for (MapObject object : collisionLayer.getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    collisionRects.add(rect);
                }
            }
            Gdx.app.log("COLLISION", "Carregadas " + collisionRects.size + " colisões.");
        } else {
            Gdx.app.log("COLLISION", "Layer 'colisao' não encontrada.");
        }

        Array<Polygon> collisionPolygons = buildCollisionPolygons(collisionRects);
        
        return new LevelData(map, spawnX, spawnY, collisionRects, collisionPolygons,
            mentorX, mentorY, hasMentor, oreNodePositions,
            craftingStationX, craftingStationY, hasCraftingStation);
    }

    private static float[] readObjectCenter(MapObject object) {
        if (object instanceof RectangleMapObject) {
            Rectangle r = ((RectangleMapObject) object).getRectangle();
            return new float[]{r.x + r.width / 2f, r.y + r.height / 2f};
        }
        MapProperties p = object.getProperties();
        Float x = p.get("x", Float.class);
        Float y = p.get("y", Float.class);
        if (x == null || y == null) {
            return null;
        }
        return new float[]{x, y};
    }

    private static Array<Polygon> buildCollisionPolygons(Array<Rectangle> rects) {
        Array<Polygon> polygons = new Array<>();
        for (Rectangle r : rects) {
            float[] vertices = {
                r.x, r.y,
                r.x + r.width, r.y,
                r.x + r.width, r.y + r.height,
                r.x, r.y + r.height
            };
            polygons.add(new Polygon(vertices));
        }
        return polygons;
    }
}
