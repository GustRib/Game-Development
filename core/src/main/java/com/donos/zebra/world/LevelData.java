package com.donos.zebra.world;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public final class LevelData {

    public final TiledMap map;
    public final float spawnX;
    public final float spawnY;
    public final Array<Rectangle> collisionRects;
    public final Array<Polygon> collisionPolygons;
    
    public final float mentorX;
    public final float mentorY;
    public final boolean hasMentor;

    public LevelData(TiledMap map, float spawnX, float spawnY,
                    Array<Rectangle> collisionRects, Array<Polygon> collisionPolygons,
                    float mentorX, float mentorY, boolean hasMentor) {
        this.map = map;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.collisionRects = collisionRects;
        this.collisionPolygons = collisionPolygons;
        this.mentorX = mentorX;
        this.mentorY = mentorY;
        this.hasMentor = hasMentor;
    }
}