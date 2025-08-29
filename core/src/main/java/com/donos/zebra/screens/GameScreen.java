package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.donos.zebra.MainGame;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.AnimatedObject;
import com.donos.zebra.entities.PlayerAnimationLoader;

public class GameScreen extends AbstractScreen {

    private final MainGame game;
    private Player player;

    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private OrthographicCamera camera;

    private Array<AnimatedObject> animatedObjects;
    private Array<Rectangle> collisionRects;

    private static final float UNIT_SCALE = 1f;

    private ShapeRenderer shapeRenderer;

    public GameScreen(MainGame game) {
        super(game.batch);
        this.game = game;
    }

    @Override
    public void show() {
        map = new TmxMapLoader().load("maps/Dungeon1.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);
        player = new Player();

        // --- Spawn do player ---
        MapLayer spawnLayer = map.getLayers().get("spawn");
        if (spawnLayer != null) {
            MapObjects spawnObjects = spawnLayer.getObjects();
            MapObject spawn = spawnObjects.get("playerSpawn");
            if (spawn == null && spawnObjects.getCount() > 0) spawn = spawnObjects.get(0);

            if (spawn != null) {
                float sx = 0f, sy = 0f;

                if (spawn instanceof RectangleMapObject) {
                    Rectangle r = ((RectangleMapObject) spawn).getRectangle();
                    sx = r.x + r.width / 2f;
                    sy = r.y + r.height / 2f;
                } else {
                    MapProperties p = spawn.getProperties();
                    sx = p.get("x", Float.class);
                    sy = p.get("y", Float.class);
                }

                player.setPosition(sx, sy);
                Gdx.app.log("SPAWN", "Player posicionado em: " + sx + ", " + sy);
            } else {
                Gdx.app.error("SPAWN", "Nenhum objeto encontrado na layer spawn.");
            }
        } else {
            Gdx.app.error("SPAWN", "Camada 'spawn' não encontrada!");
        }

        // --- Colisões ---
        collisionRects = new Array<>();
        MapLayer collisionLayer = map.getLayers().get("colisao");
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

        // --- Camera ---
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        animatedObjects = new Array<>();

        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Converte os retângulos do mapa em polígonos
        Array<Polygon> collisionPolygons = new Array<>();
        for (Rectangle r : collisionRects) {
            float[] vertices = {
                r.x, r.y,
                r.x + r.width, r.y,
                r.x + r.width, r.y + r.height,
                r.x, r.y + r.height
            };
            Polygon poly = new Polygon(vertices);
            collisionPolygons.add(poly);
        }

        player.update(delta, collisionPolygons);

        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();

        mapRenderer.setView(camera);
        mapRenderer.render();

        for (AnimatedObject obj : animatedObjects) obj.update(delta);

        batch.begin();
        player.render(batch);
        for (AnimatedObject obj : animatedObjects) obj.render(batch);
        batch.end();

        // --- DEBUG: colisões ---
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Retângulos de colisão
        shapeRenderer.setColor(1, 0, 0, 1);
        for (Rectangle r : collisionRects) {
            shapeRenderer.rect(r.x, r.y, r.width, r.height);
        }

        // Polígono do player
        shapeRenderer.setColor(0, 1, 0, 1);
        Polygon p = player.getHitbox();
        float[] vertices = p.getTransformedVertices();
        for (int i = 0; i < vertices.length; i += 2) {
            float x1 = vertices[i];
            float y1 = vertices[i + 1];
            float x2 = vertices[(i + 2) % vertices.length];
            float y2 = vertices[(i + 3) % vertices.length];
            shapeRenderer.line(x1, y1, x2, y2);
        }

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        super.dispose();

        PlayerAnimationLoader.dispose();

        if (map != null) map.dispose();
        if (mapRenderer != null) mapRenderer.dispose();
        if (animatedObjects != null) {
            for (AnimatedObject obj : animatedObjects) obj.dispose();
        }
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}
