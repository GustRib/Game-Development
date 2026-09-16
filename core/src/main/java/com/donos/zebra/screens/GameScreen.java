package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.donos.zebra.MainGame;
import com.donos.zebra.Interaction.Interactable;
import com.donos.zebra.config.DungeonGenerationConfig;
import com.donos.zebra.config.GameConfig;
import com.donos.zebra.entities.Enemy;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.DamageText;
import com.donos.zebra.items.ItemStack;
import com.donos.zebra.screens.gameplay.CombatController;
import com.donos.zebra.screens.gameplay.LevelPopulator;
import com.donos.zebra.screens.gameplay.PlayerDeathHandler;
import com.donos.zebra.ui.DialogueUI;
import com.donos.zebra.ui.InventoryUI;
import com.donos.zebra.ui.LootUI;
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
    private static final float INTERACTION_RANGE = 26f;

    private final MainGame game;
    private Player player;
    private final List<Entity> entities = new ArrayList<>();
    private final List<DamageText> damageTexts = new ArrayList<>();

    private float initialSpawnX;
    private float initialSpawnY;

    private DungeonMap dungeonMap;
    private BitmapFont damageFont;
    private TiledMap map;
    private boolean proceduralMap;
    private OrthogonalTiledMapRenderer mapRenderer;
    private CameraController cameraController;
    private Array<Rectangle> collisionRects;
    private Array<Polygon> collisionPolygons;

    private Music gameplayMusic;
    private ShapeRenderer shapeRenderer;

    private Enemy activeLootTarget = null;

    private Stage uiStage;
    private InventoryUI inventoryWindow;
    private LootUI lootWindow;
    private Skin uiSkin;

    private DialogueUI dialogueWindow;
    private final List<Interactable> interactables = new ArrayList<>();
    private Interactable activeInteractionTarget = null;

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

        initialSpawnX = levelData.spawnX;
        initialSpawnY = levelData.spawnY;
        player.setPosition(initialSpawnX, initialSpawnY);

        entities.clear();
        entities.add(player);
        damageTexts.clear();

        damageFont = new BitmapFont();
        damageFont.getData().setScale(0.6f);
        shapeRenderer = new ShapeRenderer();

        uiStage = new Stage(new ScreenViewport());
        uiSkin = InventoryUI.createDefaultSkin(damageFont);

        inventoryWindow = new InventoryUI(player.getInventory(), uiSkin, game.getAssetManager());
        inventoryWindow.setPosition(20, Gdx.graphics.getHeight() / 2f, Align.topLeft);
        uiStage.addActor(inventoryWindow);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);
        Gdx.input.setInputProcessor(multiplexer);

        lootWindow = new LootUI(uiSkin, game.getAssetManager());
        lootWindow.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f + 100, Align.center);
        uiStage.addActor(lootWindow);

        dialogueWindow = new DialogueUI(uiSkin);
        dialogueWindow.setSize(400, 80);
        dialogueWindow.setPosition(Gdx.graphics.getWidth() / 2f, 50, Align.bottom);
        uiStage.addActor(dialogueWindow);

        LevelPopulator.addMentor(levelData, dialogueWindow, game.getAssetManager(), interactables, entities);
        LevelPopulator.addOrcs(
            proceduralMap, dungeonMap, initialSpawnX, initialSpawnY, game.getAssetManager(), entities);

        OrthographicCamera camera = new OrthographicCamera();
        applyCameraViewport(camera);
        cameraController = new CameraController(camera);

        gameplayMusic = Gdx.audio.newMusic(Gdx.files.internal("track5.wav"));
        gameplayMusic.setLooping(true);
        gameplayMusic.setVolume(0.3f);
        gameplayMusic.play();
    }

    @Override
    public void render(float delta) {
        clearScreen(0, 0, 0, 1);

        damageFont.getData().setScale(0.8f);

        handleItemSystemInputs();

        player.update(delta, collisionPolygons);

        CombatController.resolvePlayerMelee(player, entities, damageTexts);
        CombatController.updateDamageTexts(damageTexts, delta);
        CombatController.updateEntities(player, entities, damageTexts, delta, collisionPolygons);

        cameraController.follow(player.getX(), player.getY());
        mapRenderer.setView(cameraController.getCamera());
        mapRenderer.render();

        batch.setProjectionMatrix(cameraController.getCamera().combined);
        beginBatch();

        entities.sort((e1, e2) -> Float.compare(e2.getY(), e1.getY()));
        for (Entity entity : entities) {
            entity.render(batch);
        }
        CombatController.renderDamageTexts(batch, damageFont, damageTexts);

        endBatch();

        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        CombatController.renderHealthBars(shapeRenderer, entities);

        uiStage.act(delta);
        uiStage.draw();

        if (DEBUG_COLLISION) {
            renderDebugCollision();
        }

        if (player.isDead()) {
            boolean revived = PlayerDeathHandler.drawAndHandle(
                player,
                initialSpawnX,
                initialSpawnY,
                collisionPolygons,
                cameraController.getCamera(),
                shapeRenderer,
                batch,
                damageFont,
                lootWindow,
                inventoryWindow
            );
            if (revived) {
                activeLootTarget = null;
            }
        }
    }

    private void handleItemSystemInputs() {
        if (player.isDead()) return;

        if (dialogueWindow.isVisible()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                dialogueWindow.hideDialogue();
                player.setInteracting(false);
                activeInteractionTarget = null;
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            if (lootWindow.isVisible()) return;

            boolean isVisible = !inventoryWindow.isVisible();
            inventoryWindow.setVisible(isVisible);
            if (isVisible) inventoryWindow.refresh();
        }

        if (!lootWindow.isVisible() && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            Interactable closestInteractable = null;
            float minInteractDist = INTERACTION_RANGE;

            for (Interactable inter : interactables) {
                float dist = Vector2.dst(player.getX(), player.getY(), inter.getX(), inter.getY());
                if (dist <= minInteractDist) {
                    minInteractDist = dist;
                    closestInteractable = inter;
                }
            }

            if (closestInteractable != null) {
                activeInteractionTarget = closestInteractable;
                closestInteractable.onInteract(player);
                inventoryWindow.refresh();
                player.setInteracting(true);
                inventoryWindow.setVisible(false);
                return;
            }

            Enemy closestCorpse = null;
            float minCorpseDist = INTERACTION_RANGE;

            for (Entity ent : entities) {
                if (ent instanceof Enemy) {
                    Enemy enemy = (Enemy) ent;
                    if (enemy.hasLootAvailable()) {
                        float dist = Vector2.dst(player.getX(), player.getY(), enemy.getX(), enemy.getY());
                        if (dist <= minCorpseDist) {
                            minCorpseDist = dist;
                            closestCorpse = enemy;
                        }
                    }
                }
            }

            if (closestCorpse != null) {
                activeLootTarget = closestCorpse;
                lootWindow.updateLoot(activeLootTarget);
                lootWindow.setVisible(true);
                inventoryWindow.setVisible(false);
                player.setInteracting(true);
            }
        }

        if (lootWindow.isVisible() && activeLootTarget != null && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            for (ItemStack stack : activeLootTarget.getLootTable()) {
                player.getInventory().addItem(stack.getDefinition(), stack.getQuantity());
            }

            inventoryWindow.refresh();
            activeLootTarget.clearLoot();
            lootWindow.setVisible(false);
            activeLootTarget = null;
            player.setInteracting(false);
        }
    }

    private void renderDebugCollision() {
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
        if (uiStage != null) {
            uiStage.getViewport().update(width, height, true);

            if (inventoryWindow != null) {
                inventoryWindow.setPosition(20, height / 2f, Align.topLeft);
            }

            if (lootWindow != null) {
                lootWindow.setPosition(width / 2f, height / 2f + 100, Align.center);
            }
        }
        if (dialogueWindow != null) {
            dialogueWindow.setPosition(width / 2f, 50, Align.bottom);
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
        if (gameplayMusic != null) {
            gameplayMusic.dispose();
        }
        super.dispose();

        if (uiStage != null) uiStage.dispose();
        if (uiSkin != null) uiSkin.dispose();

        for (Entity entity : entities) {
            entity.dispose();
        }
        entities.clear();
        if (damageFont != null) damageFont.dispose();
        if (proceduralMap) {
            DungeonMapAdapter.disposeProceduralResources(map);
        }
        if (mapRenderer != null) mapRenderer.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }

    private LevelData loadLevelData() {
        if (GameConfig.USE_PROCEDURAL_DUNGEON) {
            this.dungeonMap = DungeonGenerator.generate(DungeonGenerationConfig.defaults());
            return DungeonMapAdapter.toLevelData(this.dungeonMap, game.getAssetManager());
        }
        return LevelLoader.load(game.getAssetManager(), LevelConstants.MAP_PATH);
    }
}
