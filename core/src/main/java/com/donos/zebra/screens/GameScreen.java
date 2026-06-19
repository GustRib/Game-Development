package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.MainGame;
import com.donos.zebra.config.DungeonGenerationConfig;
import com.donos.zebra.config.GameConfig;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.Player;
import com.donos.zebra.world.CameraController;
import com.donos.zebra.world.DungeonMapAdapter;
import com.donos.zebra.world.LevelConstants;
import com.donos.zebra.world.LevelData;
import com.donos.zebra.world.LevelLoader;
import com.donos.zebra.world.dungeon.DungeonGenerator;
import com.donos.zebra.world.dungeon.DungeonMap;

import java.util.ArrayList;
import java.util.List;

public class GameScreen extends AbstractScreen {

    private static final boolean DEBUG_COLLISION = false;
    private static final float UNIT_SCALE = 1f;
    private static final float CAMERA_ZOOM = 2.5f;

    private final MainGame game;
    private Player player;
    private final List<Entity> entities = new ArrayList<>();

    private TiledMap map;
    private boolean proceduralMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private CameraController cameraController;
    private Array<Rectangle> collisionRects;
    private Array<Polygon> collisionPolygons;

    private ShapeRenderer shapeRenderer;

    public GameScreen(MainGame game) {
        super(game.batch);
        this.game = game;
    }

    @Override
    public void show() {
        LevelData levelData = loadLevelData();
        proceduralMap = GameConfig.USE_PROCEDURAL_DUNGEON;
        map = levelData.map;
        collisionRects = levelData.collisionRects;
        collisionPolygons = levelData.collisionPolygons;

        mapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);
        player = new Player(game.getAssetManager());
        player.setPosition(levelData.spawnX, levelData.spawnY);

        entities.clear();
        entities.add(player);

        OrthographicCamera camera = new OrthographicCamera();
        applyCameraViewport(camera);
        cameraController = new CameraController(camera);

        if (DEBUG_COLLISION) {
            shapeRenderer = new ShapeRenderer();
        }
    }

    @Override
    public void render(float delta) {
        clearScreen(0, 0, 0, 1);

        player.update(delta, collisionPolygons);

        cameraController.follow(player.getX(), player.getY());

        mapRenderer.setView(cameraController.getCamera());
        mapRenderer.render();

        batch.setProjectionMatrix(cameraController.getCamera().combined);
        beginBatch();
        for (Entity entity : entities) {
            entity.render(batch);
        }
        endBatch();

        if (DEBUG_COLLISION) {
            renderDebugCollision();
        }
    }

    private void renderDebugCollision() {
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(1, 0, 0, 1);
        for (Rectangle r : collisionRects) {
            shapeRenderer.rect(r.x, r.y, r.width, r.height);
        }

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
    public void resize(int width, int height) {
        if (cameraController != null) {
            applyCameraViewport(cameraController.getCamera(), width, height);
        }
    }

    private void applyCameraViewport(OrthographicCamera camera) {
        applyCameraViewport(camera, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void applyCameraViewport(OrthographicCamera camera, int width, int height) {
        camera.setToOrtho(false, width / CAMERA_ZOOM, height / CAMERA_ZOOM);
        camera.update();
    }

    @Override
    public void dispose() {
        super.dispose();

        for (Entity entity : entities) {
            entity.dispose();
        }
        entities.clear();

        if (proceduralMap) {
            DungeonMapAdapter.disposeProceduralResources(map);
        }
        if (mapRenderer != null) mapRenderer.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }

    private LevelData loadLevelData() {
        if (GameConfig.USE_PROCEDURAL_DUNGEON) {
            DungeonMap dungeonMap = DungeonGenerator.generate(DungeonGenerationConfig.defaults());
            return DungeonMapAdapter.toLevelData(dungeonMap, game.getAssetManager());
        }
        return LevelLoader.load(game.getAssetManager(), LevelConstants.MAP_PATH);
    }
}
