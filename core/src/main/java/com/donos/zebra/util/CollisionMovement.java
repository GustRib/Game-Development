package com.donos.zebra.util;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;

/**
 * Shared axis-separated movement against world collision polygons.
 * Used by Player and Enemy so both slide along walls the same way.
 */
public final class CollisionMovement {

    private CollisionMovement() {
    }

    /**
     * Attempts to apply {@code dx}/{@code dy} with independent X then Y checks.
     *
     * @param position        in/out world position; index 0 = x, index 1 = y
     * @param hitboxOffsetX   offset from entity origin to hitbox origin
     * @param hitboxOffsetY   offset from entity origin to hitbox origin
     * @param localVertices   hitbox vertices in local space (copied into scratches)
     * @param dx              intended X delta
     * @param dy              intended Y delta
     * @param walls           solid world polygons; null is treated as no collision
     * @param scratchX        reusable polygon for X probe
     * @param scratchY        reusable polygon for Y probe
     */
    public static void tryMove(float[] position,
                               float hitboxOffsetX,
                               float hitboxOffsetY,
                               float[] localVertices,
                               float dx,
                               float dy,
                               Array<Polygon> walls,
                               Polygon scratchX,
                               Polygon scratchY) {
        if ((dx == 0f && dy == 0f) || walls == null || position == null || localVertices == null) {
            return;
        }

        float x = position[0];
        float y = position[1];
        float hitboxX = x + hitboxOffsetX;
        float hitboxY = y + hitboxOffsetY;

        if (dx != 0f) {
            scratchX.setVertices(localVertices);
            scratchX.setPosition(hitboxX + dx, hitboxY);
            if (!overlapsAny(scratchX, walls)) {
                x += dx;
                hitboxX = x + hitboxOffsetX;
            }
        }

        if (dy != 0f) {
            scratchY.setVertices(localVertices);
            scratchY.setPosition(hitboxX, hitboxY + dy);
            if (!overlapsAny(scratchY, walls)) {
                y += dy;
            }
        }

        position[0] = x;
        position[1] = y;
    }

    private static boolean overlapsAny(Polygon probe, Array<Polygon> walls) {
        for (Polygon wall : walls) {
            if (Intersector.overlapConvexPolygons(probe, wall)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if placing a hitbox at {@code (x, y)} would overlap any wall.
     * Used for spawn validation (orcs, ore nodes) before entities enter the world.
     */
    public static boolean overlapsWalls(float x,
                                        float y,
                                        float[] localVertices,
                                        Array<Polygon> walls) {
        if (walls == null || walls.size == 0 || localVertices == null) {
            return false;
        }
        Polygon probe = new Polygon(localVertices.clone());
        probe.setPosition(x, y);
        return overlapsAny(probe, walls);
    }
}
