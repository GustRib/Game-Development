package com.donos.zebra.screens.gameplay;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.Interaction.Interactable;
import com.donos.zebra.entities.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds a suspended overworld layer so entering the tavern does not wipe live entity state.
 */
public final class SuspendedOverworld {

    public final TiledMap map;
    public final Array<Rectangle> collisionRects;
    public final Array<Polygon> collisionPolygons;
    public final List<Entity> entities;
    public final List<Interactable> interactables;

    public SuspendedOverworld(TiledMap map,
                              Array<Rectangle> collisionRects,
                              Array<Polygon> collisionPolygons,
                              List<Entity> entities,
                              List<Interactable> interactables) {
        this.map = map;
        this.collisionRects = collisionRects;
        this.collisionPolygons = collisionPolygons;
        this.entities = new ArrayList<>(entities);
        this.interactables = new ArrayList<>(interactables);
    }
}
