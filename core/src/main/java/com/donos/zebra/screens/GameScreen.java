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

    // debug renderer (opcional)
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

        MapLayer spawnLayer = map.getLayers().get("spawn");
        if (spawnLayer == null) {
            Gdx.app.error("SPAWN", "Camada 'spawn' não encontrada!");
        } else {
            MapObjects spawnObjects = spawnLayer.getObjects();
            MapObject spawn = spawnObjects.get("playerSpawn");
            if (spawn == null && spawnObjects.getCount() > 0) {
                spawn = spawnObjects.get(0);
            }

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
        }

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

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        animatedObjects = new Array<>();

        // --- ShapeRenderer para debug ---
        shapeRenderer = new ShapeRenderer();
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        player.update(delta, collisionRects);

        camera.position.set(player.getX(), player.getY(), 0);
        camera.update();

        mapRenderer.setView(camera);
        mapRenderer.render();

        for (AnimatedObject obj : animatedObjects) {
            obj.update(delta);
        }


        // Desenho principal do player e objetos
        batch.begin();
        player.render(batch);
        for (AnimatedObject obj : animatedObjects) {
            obj.render(batch);
        }
        batch.end();

        // --- DEBUG: desenha retângulos de colisão ---
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(1, 0, 0, 1); // vermelho
        for (Rectangle r : collisionRects) {
            shapeRenderer.rect(r.x, r.y, r.width, r.height);
        }

        shapeRenderer.setColor(0, 1, 0, 1); // verde
        Rectangle p = player.getBounds();
        shapeRenderer.rect(p.x, p.y, p.width, p.height);

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
