package com.donos.zebra.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.entities.PlayerAnimationLoader;

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
    }

    public static LevelData load(AssetManager assetManager, String mapPath) {
        TiledMap map = assetManager.get(mapPath, TiledMap.class);
        return load(map);
    }

    public static LevelData load(TiledMap map) {
        float spawnX = LevelConstants.DEFAULT_SPAWN_X;
        float spawnY = LevelConstants.DEFAULT_SPAWN_Y;

        MapLayer spawnLayer = map.getLayers().get(LevelConstants.SPAWN_LAYER);
        if (spawnLayer != null) {
            MapObjects spawnObjects = spawnLayer.getObjects();
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
        } else {
            Gdx.app.error("SPAWN", "Camada 'spawn' não encontrada! Using default position ("
                + LevelConstants.DEFAULT_SPAWN_X + ", " + LevelConstants.DEFAULT_SPAWN_Y + ").");
        }

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
        return new LevelData(map, spawnX, spawnY, collisionRects, collisionPolygons);
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
