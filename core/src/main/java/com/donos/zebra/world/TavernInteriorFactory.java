package com.donos.zebra.world;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;

/**
 * Builds an in-memory tavern interior sized to the game's 16px tile world scale.
 * {@code Tavern_01.png} is 1280 native; we render it at {@link #SCALE} so furniture
 * reads closer to exterior prop scale beside the ~48px player.
 * <p>
 * Art layout (native, Y up from bottom when drawn): kitchen stove top-left,
 * main-hall exit door near top-center, guest rooms along the bottom.
 */
public final class TavernInteriorFactory {

    /** Native art resolution. */
    public static final float NATIVE = 1280f;
    /**
     * 0.4 → 512 world units: keeps player readable vs interior props without
     * introducing a separate camera zoom for the tavern.
     */
    public static final float SCALE = 0.4f;
    public static final float WIDTH = NATIVE * SCALE;
    public static final float HEIGHT = NATIVE * SCALE;
    public static final float WALL = 20f;

    private static final float HEIGHT_NATIVE = 1280f;

    private TavernInteriorFactory() {
    }

    public static TiledMap buildMap() {
        TiledMap map = new TiledMap();

        MapLayer collision = new MapLayer();
        collision.setName(LevelConstants.COLLISION_LAYER);
        // Floor perimeter — gap at top-center aligns with the painted exit door
        collision.getObjects().add(rect(0, 0, WIDTH, WALL));
        collision.getObjects().add(rect(0, WALL, WALL, HEIGHT - 2 * WALL));
        collision.getObjects().add(rect(WIDTH - WALL, WALL, WALL, HEIGHT - 2 * WALL));
        collision.getObjects().add(rect(0, HEIGHT - WALL, WIDTH / 2f - 28f, WALL));
        collision.getObjects().add(rect(WIDTH / 2f + 28f, HEIGHT - WALL, WIDTH / 2f - 28f, WALL));
        // Soft furniture blocks (kitchen counter / hall tables), scaled
        collision.getObjects().add(rect(s(80), s(HEIGHT_NATIVE - 280), s(220), s(40)));
        collision.getObjects().add(rect(s(400), s(HEIGHT_NATIVE - 520), s(200), s(36)));
        map.getLayers().add(collision);

        MapLayer spawn = new MapLayer();
        spawn.setName(LevelConstants.SPAWN_LAYER);

        // Just inside the top-center door
        RectangleMapObject playerSpawn = new RectangleMapObject(
            WIDTH / 2f - 8f, s(HEIGHT_NATIVE - 200), 16f, 16f);
        playerSpawn.setName(LevelConstants.SPAWN_OBJECT);
        spawn.getObjects().add(playerSpawn);

        // Kitchen stove area (upper-left of tavern art) — forge interactable
        RectangleMapObject forge = new RectangleMapObject(s(200), s(HEIGHT_NATIVE - 340), 20f, 20f);
        forge.setName(OpeningQuest.CRAFTING_STATION_OBJECT);
        spawn.getObjects().add(forge);

        RectangleMapObject exit = new RectangleMapObject(WIDTH / 2f - 16f, s(HEIGHT_NATIVE - 120), 32f, 22f);
        exit.setName(OpeningQuest.DOOR_EXIT_OBJECT);
        spawn.getObjects().add(exit);

        map.getLayers().add(spawn);
        return map;
    }

    private static float s(float nativePx) {
        return nativePx * SCALE;
    }

    public static Vector2 defaultSpawn() {
        return new Vector2(WIDTH / 2f, s(HEIGHT_NATIVE - 190));
    }

    private static RectangleMapObject rect(float x, float y, float w, float h) {
        return new RectangleMapObject(x, y, w, h);
    }
}
