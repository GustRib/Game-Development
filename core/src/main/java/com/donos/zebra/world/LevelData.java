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

    /** World positions of copper ore nodes parsed from the spawn layer. */
    public final List<Vector2> oreNodePositions;

    public LevelData(TiledMap map, float spawnX, float spawnY,
                     Array<Rectangle> collisionRects, Array<Polygon> collisionPolygons,
                     float mentorX, float mentorY, boolean hasMentor) {
        this(map, spawnX, spawnY, collisionRects, collisionPolygons,
            mentorX, mentorY, hasMentor, Collections.emptyList());
    }

    public LevelData(TiledMap map, float spawnX, float spawnY,
                     Array<Rectangle> collisionRects, Array<Polygon> collisionPolygons,
                     float mentorX, float mentorY, boolean hasMentor,
                     List<Vector2> oreNodePositions) {
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
    }
}
