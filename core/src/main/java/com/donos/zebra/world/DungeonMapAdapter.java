package com.donos.zebra.world;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.world.dungeon.DungeonMap;
import com.donos.zebra.world.dungeon.TileType;

public final class DungeonMapAdapter {

    public static final String PROCEDURAL_TEXTURE_KEY = "proceduralDungeonTexture";

    private static final int WALL_TILE_ID = 1;
    private static final int FLOOR_TILE_ID = 2;

    private DungeonMapAdapter() {
    }

    public static LevelData toLevelData(DungeonMap dungeonMap) {
        TiledMap tiledMap = buildTiledMap(dungeonMap);
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
        if (tiledMap == null) {
            return;
        }
        Object texture = tiledMap.getProperties().get(PROCEDURAL_TEXTURE_KEY);
        if (texture instanceof Texture) {
            ((Texture) texture).dispose();
            tiledMap.getProperties().remove(PROCEDURAL_TEXTURE_KEY);
        }
    }

    private static TiledMap buildTiledMap(DungeonMap dungeonMap) {
        int tileSize = dungeonMap.getTileSize();
        Texture texture = createTileTexture(tileSize);
        TextureRegion[][] split = TextureRegion.split(texture, tileSize, tileSize);

        TiledMapTile wallTile = new StaticTiledMapTile(split[0][0]);
        wallTile.setId(WALL_TILE_ID);
        TiledMapTile floorTile = new StaticTiledMapTile(split[0][1]);
        floorTile.setId(FLOOR_TILE_ID);

        TiledMapTileSet tileSet = new TiledMapTileSet();
        tileSet.setName("procedural");
        tileSet.addTile(wallTile);
        tileSet.addTile(floorTile);

        TiledMap tiledMap = new TiledMap();
        tiledMap.getProperties().put(PROCEDURAL_TEXTURE_KEY, texture);
        tiledMap.getTileSets().addTileSet(tileSet);

        TiledMapTileLayer layer = new TiledMapTileLayer(
            dungeonMap.getWidth(),
            dungeonMap.getHeight(),
            tileSize,
            tileSize
        );
        layer.setName("ground");

        for (int y = 0; y < dungeonMap.getHeight(); y++) {
            for (int x = 0; x < dungeonMap.getWidth(); x++) {
                TiledMapTile tile = dungeonMap.getTile(x, y) == TileType.FLOOR ? floorTile : wallTile;
                TiledMapTileLayer.Cell cell = new TiledMapTileLayer.Cell();
                cell.setTile(tile);
                layer.setCell(x, y, cell);
            }
        }

        tiledMap.getLayers().add(layer);
        return tiledMap;
    }

    private static Texture createTileTexture(int tileSize) {
        Pixmap pixmap = new Pixmap(tileSize * 2, tileSize, Pixmap.Format.RGB888);
        pixmap.setColor(0.18f, 0.18f, 0.22f, 1f);
        pixmap.fillRectangle(0, 0, tileSize, tileSize);
        pixmap.setColor(0.34f, 0.29f, 0.24f, 1f);
        pixmap.fillRectangle(tileSize, 0, tileSize, tileSize);
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
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
