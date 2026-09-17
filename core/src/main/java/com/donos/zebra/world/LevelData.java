package com.donos.zebra.world;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Collections;
import java.util.List;

public final class LevelData {

    public final TiledMap map;
    public final float spawnX;
    public final float spawnY;
    public final Array<Rectangle> collisionRects;
    public final Array<Polygon> collisionPolygons;

    public final float mentorX;
    public final float mentorY;
    public final boolean hasMentor;

    public final List<Vector2> oreNodePositions;

    public final float craftingStationX;
    public final float craftingStationY;
    public final boolean hasCraftingStation;

    public final float doorHouseX;
    public final float doorHouseY;
    public final boolean hasDoorHouse;

    public final float doorExitX;
    public final float doorExitY;
    public final boolean hasDoorExit;

    public LevelData(TiledMap map, float spawnX, float spawnY,
                     Array<Rectangle> collisionRects, Array<Polygon> collisionPolygons,
                     float mentorX, float mentorY, boolean hasMentor) {
        this(map, spawnX, spawnY, collisionRects, collisionPolygons,
            mentorX, mentorY, hasMentor, Collections.emptyList(),
            0f, 0f, false, 0f, 0f, false, 0f, 0f, false);
    }

    public LevelData(TiledMap map, float spawnX, float spawnY,
                     Array<Rectangle> collisionRects, Array<Polygon> collisionPolygons,
                     float mentorX, float mentorY, boolean hasMentor,
                     List<Vector2> oreNodePositions) {
        this(map, spawnX, spawnY, collisionRects, collisionPolygons,
            mentorX, mentorY, hasMentor, oreNodePositions,
            0f, 0f, false, 0f, 0f, false, 0f, 0f, false);
    }

    public LevelData(TiledMap map, float spawnX, float spawnY,
                     Array<Rectangle> collisionRects, Array<Polygon> collisionPolygons,
                     float mentorX, float mentorY, boolean hasMentor,
                     List<Vector2> oreNodePositions,
                     float craftingStationX, float craftingStationY, boolean hasCraftingStation) {
        this(map, spawnX, spawnY, collisionRects, collisionPolygons,
            mentorX, mentorY, hasMentor, oreNodePositions,
            craftingStationX, craftingStationY, hasCraftingStation,
            0f, 0f, false, 0f, 0f, false);
    }

    public LevelData(TiledMap map, float spawnX, float spawnY,
                     Array<Rectangle> collisionRects, Array<Polygon> collisionPolygons,
                     float mentorX, float mentorY, boolean hasMentor,
                     List<Vector2> oreNodePositions,
                     float craftingStationX, float craftingStationY, boolean hasCraftingStation,
                     float doorHouseX, float doorHouseY, boolean hasDoorHouse,
                     float doorExitX, float doorExitY, boolean hasDoorExit) {
        this.map = map;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.collisionRects = collisionRects;
        this.collisionPolygons = collisionPolygons;
        this.mentorX = mentorX;
        this.mentorY = mentorY;
        this.hasMentor = hasMentor;
        this.oreNodePositions = oreNodePositions == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(oreNodePositions);
        this.craftingStationX = craftingStationX;
        this.craftingStationY = craftingStationY;
        this.hasCraftingStation = hasCraftingStation;
        this.doorHouseX = doorHouseX;
        this.doorHouseY = doorHouseY;
        this.hasDoorHouse = hasDoorHouse;
        this.doorExitX = doorExitX;
        this.doorExitY = doorExitY;
        this.hasDoorExit = hasDoorExit;
    }
}
