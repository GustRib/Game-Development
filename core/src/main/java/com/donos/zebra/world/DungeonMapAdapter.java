package com.donos.zebra.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.world.dungeon.DungeonMap;
import com.donos.zebra.world.dungeon.TileType;

public final class DungeonMapAdapter {

    private static final String WALLS_FLOOR_TILESET = "walls_floor";

    private static boolean tilesetCatalogLogged;

    private DungeonMapAdapter() {
    }

    public static LevelData toLevelData(DungeonMap dungeonMap) {
        TiledMap sourceMap = new TmxMapLoader().load(LevelConstants.MAP_PATH);
        return toLevelData(dungeonMap, sourceMap);
    }

    public static LevelData toLevelData(DungeonMap dungeonMap, AssetManager assetManager) {
        TiledMap sourceMap = assetManager.get(LevelConstants.MAP_PATH, TiledMap.class);
        return toLevelData(dungeonMap, sourceMap);
    }

    private static LevelData toLevelData(DungeonMap dungeonMap, TiledMap sourceMap) {
        TiledMap tiledMap = buildTiledMap(dungeonMap, sourceMap);
        Array<Rectangle> collisionRects = buildCollisionRects(dungeonMap);
        Array<Polygon> collisionPolygons = buildCollisionPolygons(collisionRects);
        return new LevelData(
            tiledMap,
            dungeonMap.getSpawnX(),
            dungeonMap.getSpawnY(),
            collisionRects,
            collisionPolygons
        );
    }

    public static void disposeProceduralResources(TiledMap tiledMap) {
        // Tilesets are shared with the AssetManager-loaded reference map; do not dispose them.
    }

    private static TiledMap buildTiledMap(DungeonMap dungeonMap, TiledMap sourceMap) {
        TiledMapTileSet tileSet = sourceMap.getTileSets().getTileSet(WALLS_FLOOR_TILESET);
        if (tileSet == null) {
            Gdx.app.error("DungeonMapAdapter",
                "Tileset '" + WALLS_FLOOR_TILESET + "' not found in " + LevelConstants.MAP_PATH);
            return new TiledMap();
        }

        logTilesetCatalog(tileSet);

        // GID 447: cobblestone floor for playable paths (TileType.FLOOR).
        TiledMapTile floorTile = tileSet.getTile(LevelConstants.PROCEDURAL_FLOOR_TILE_GID);
        // GID 395: brick wall for blocking boundaries (TileType.WALL).
        TiledMapTile wallTile = tileSet.getTile(LevelConstants.PROCEDURAL_WALL_TILE_GID);

        if (!isRenderable(floorTile)) {
            Gdx.app.error("DungeonMapAdapter",
                "Floor tile missing for gid=" + LevelConstants.PROCEDURAL_FLOOR_TILE_GID);
        }
        if (!isRenderable(wallTile)) {
            Gdx.app.error("DungeonMapAdapter",
                "Wall tile missing for gid=" + LevelConstants.PROCEDURAL_WALL_TILE_GID);
        }
        if (isRenderable(floorTile) && isRenderable(wallTile)) {
            Gdx.app.log("DungeonMapAdapter",
                "Using floor gid=" + floorTile.getId() + ", wall gid=" + wallTile.getId());
        }

        TiledMap tiledMap = new TiledMap();
        for (TiledMapTileSet sourceTileSet : sourceMap.getTileSets()) {
            tiledMap.getTileSets().addTileSet(sourceTileSet);
        }

        int tileSize = dungeonMap.getTileSize();
        TiledMapTileLayer layer = new TiledMapTileLayer(
            dungeonMap.getWidth(),
            dungeonMap.getHeight(),
            tileSize,
            tileSize
        );
        layer.setName("ground");

        int placedCells = 0;
        for (int y = 0; y < dungeonMap.getHeight(); y++) {
            for (int x = 0; x < dungeonMap.getWidth(); x++) {
                TileType cellType = dungeonMap.getTile(x, y);
                // FLOOR cells → GID 447 (stone). WALL cells → GID 395 (brick).
                TiledMapTile tile = cellType == TileType.FLOOR ? floorTile : wallTile;
                if (!isRenderable(tile)) {
                    continue;
                }

                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(tile);
                layer.setCell(x, y, cell);
                placedCells++;
            }
        }

        tiledMap.getLayers().add(layer);
        Gdx.app.log("DungeonMapAdapter",
            "Built procedural layer " + layer.getWidth() + "x" + layer.getHeight()
                + " with " + placedCells + " cells.");

        return tiledMap;
    }

    private static void logTilesetCatalog(TiledMapTileSet tileSet) {
        if (tilesetCatalogLogged) {
            return;
        }
        tilesetCatalogLogged = true;

        Integer firstGid = tileSet.getProperties().get("firstgid", null, Integer.class);
        StringBuilder sample = new StringBuilder();
        int logged = 0;
        for (TiledMapTile tile : tileSet) {
            if (!isRenderable(tile)) {
                continue;
            }
            int localId = firstGid != null ? tile.getId() - firstGid : -1;
            if (logged > 0) {
                sample.append(", ");
            }
            sample.append(tile.getId()).append("(local ").append(localId).append(')');
            logged++;
            if (logged >= 40) {
                sample.append(", ...");
                break;
            }
        }

        Gdx.app.log("DungeonMapAdapter",
            "walls_floor firstgid=" + firstGid + " sample gids: " + sample);
        Gdx.app.log("DungeonMapAdapter",
            "Configured floor gid=" + LevelConstants.PROCEDURAL_FLOOR_TILE_GID
                + " wall gid=" + LevelConstants.PROCEDURAL_WALL_TILE_GID);
    }

    private static boolean isRenderable(TiledMapTile tile) {
        return tile != null
            && tile.getTextureRegion() != null
            && tile.getTextureRegion().getTexture() != null;
    }

    public static Array<Rectangle> buildCollisionRects(DungeonMap dungeonMap) {
        Array<Rectangle> collisionRects = new Array<>();
        int tileSize = dungeonMap.getTileSize();

        for (int y = 0; y < dungeonMap.getHeight(); y++) {
            for (int x = 0; x < dungeonMap.getWidth(); x++) {
                if (dungeonMap.getTile(x, y) == TileType.WALL) {
                    collisionRects.add(new Rectangle(x * tileSize, y * tileSize, tileSize, tileSize));
                }
            }
        }

        return collisionRects;
    }

    private static Array<Polygon> buildCollisionPolygons(Array<Rectangle> rects) {
        Array<Polygon> polygons = new Array<>();
        for (Rectangle rect : rects) {
            float[] vertices = {
                rect.x, rect.y,
                rect.x + rect.width, rect.y,
                rect.x + rect.width, rect.y + rect.height,
                rect.x, rect.y + rect.height
            };
            polygons.add(new Polygon(vertices));
        }
        return polygons;
    }
}
