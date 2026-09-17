package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
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
import com.donos.zebra.entities.MapDoor;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.DamageText;
import com.donos.zebra.items.CraftingController;
import com.donos.zebra.items.ItemStack;
import com.donos.zebra.screens.gameplay.CombatController;
import com.donos.zebra.screens.gameplay.LevelPopulator;
import com.donos.zebra.screens.gameplay.PlayerDeathHandler;
import com.donos.zebra.screens.gameplay.SuspendedOverworld;
import com.donos.zebra.ui.CraftingUI;
import com.donos.zebra.ui.DialogueUI;
import com.donos.zebra.ui.EquipmentUI;
import com.donos.zebra.ui.InventoryUI;
import com.donos.zebra.ui.ItemTooltipPanel;
import com.donos.zebra.ui.LootUI;
import com.donos.zebra.ui.StatusToast;
import com.donos.zebra.ui.UiPanelStack;
import com.donos.zebra.entities.OreNode;
import com.donos.zebra.world.CameraController;
import com.donos.zebra.world.DungeonMapAdapter;
import com.donos.zebra.world.LevelConstants;
import com.donos.zebra.world.LevelData;
import com.donos.zebra.world.LevelLoader;
import com.donos.zebra.world.TavernInteriorFactory;
import com.donos.zebra.world.dungeon.DungeonGenerator;
import com.donos.zebra.world.dungeon.DungeonMap;

import java.util.ArrayList;
import java.util.Iterator;
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
    private float outdoorReturnX;
    private float outdoorReturnY;

    private DungeonMap dungeonMap;
    private BitmapFont damageFont;
    private BitmapFont uiFont;
    private TiledMap map;
    private boolean proceduralMap;
    private boolean inTavern;
    private Texture tavernBackground;
    private OrthogonalTiledMapRenderer mapRenderer;
    private CameraController cameraController;
    private Array<Rectangle> collisionRects;
    private Array<Polygon> collisionPolygons;

    /** Live overworld parked while inside the tavern (not reloaded on exit). */
    private SuspendedOverworld suspendedOverworld;

    private Music gameplayMusic;
    private ShapeRenderer shapeRenderer;

    private Enemy activeLootTarget = null;
    private Enemy highlightedLootCorpse = null;

    private Stage uiStage;
    private InventoryUI inventoryWindow;
    private EquipmentUI equipmentWindow;
    private ItemTooltipPanel tooltipPanel;
    private LootUI lootWindow;
    private Skin uiSkin;
    private final UiPanelStack panelStack = new UiPanelStack();

    private DialogueUI dialogueWindow;
    private CraftingUI craftingWindow;
    private final CraftingController craftingController = new CraftingController();
    private StatusToast statusToast;
    private final List<Interactable> interactables = new ArrayList<>();
    private Interactable activeInteractionTarget = null;

    public GameScreen(MainGame game) {
        super(game.batch);
        this.game = game;
    }

    @Override
    public void show() {
        proceduralMap = GameConfig.USE_PROCEDURAL_DUNGEON;
        inTavern = false;
        suspendedOverworld = null;

        damageFont = new BitmapFont();
        damageFont.getData().setScale(0.85f);
        uiFont = new BitmapFont();
        uiFont.getData().setScale(1.4f);
        shapeRenderer = new ShapeRenderer();

        uiStage = new Stage(new ScreenViewport());
        uiSkin = InventoryUI.createDefaultSkin(uiFont);

        player = new Player(game.getAssetManager());

        inventoryWindow = new InventoryUI(player.getInventory(), uiSkin, game.getAssetManager(), player);
        inventoryWindow.setPosition(24, Gdx.graphics.getHeight() / 2f + 40f, Align.topLeft);
        inventoryWindow.setOnClosed(() -> panelStack.remove(inventoryWindow));
        uiStage.addActor(inventoryWindow);

        tooltipPanel = new ItemTooltipPanel(uiSkin);
        uiStage.addActor(tooltipPanel);
        inventoryWindow.setTooltipPanel(tooltipPanel);

        equipmentWindow = new EquipmentUI(uiSkin, game.getAssetManager(), tooltipPanel);
        equipmentWindow.bind(player);
        equipmentWindow.setPosition(Gdx.graphics.getWidth() - 24f, Gdx.graphics.getHeight() / 2f + 40f, Align.topRight);
        equipmentWindow.setOnClosed(() -> panelStack.remove(equipmentWindow));
        uiStage.addActor(equipmentWindow);

        statusToast = new StatusToast(uiSkin);
        statusToast.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 36f, Align.top);
        uiStage.addActor(statusToast);

        inventoryWindow.setOnEquipmentChanged(equipmentWindow::refresh);
        equipmentWindow.setOnInventoryChanged(inventoryWindow::refresh);
        inventoryWindow.setEquipToast(msg -> {
            statusToast.show(msg);
            statusToast.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 36f, Align.top);
        });
        equipmentWindow.setEquipToast(msg -> {
            statusToast.show(msg);
            statusToast.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 36f, Align.top);
        });

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);
        Gdx.input.setInputProcessor(multiplexer);

        lootWindow = new LootUI(uiSkin, game.getAssetManager());
        lootWindow.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f + 100, Align.center);
        uiStage.addActor(lootWindow);

        dialogueWindow = new DialogueUI(uiSkin);
        dialogueWindow.setSize(560, 140);
        dialogueWindow.setPosition(Gdx.graphics.getWidth() / 2f, 60, Align.bottom);
        uiStage.addActor(dialogueWindow);

        craftingWindow = new CraftingUI(uiSkin, game.getAssetManager(), craftingController);
        craftingWindow.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, Align.center);
        craftingWindow.setOnOpened(() -> panelStack.push(craftingWindow));
        craftingWindow.setOnClosed(() -> {
            panelStack.remove(craftingWindow);
            player.setInteracting(false);
            activeInteractionTarget = null;
        });
        craftingWindow.setOnInventoryChanged(() -> {
            inventoryWindow.refresh();
            equipmentWindow.refresh();
        });
        uiStage.addActor(craftingWindow);

        if (game.getAssetManager().isLoaded(LevelConstants.TAVERN_BACKGROUND)) {
            tavernBackground = game.getAssetManager().get(LevelConstants.TAVERN_BACKGROUND, Texture.class);
        }

        OrthographicCamera camera = new OrthographicCamera();
        applyCameraViewport(camera);
        cameraController = new CameraController(camera);

        bootstrapOverworld();

        gameplayMusic = Gdx.audio.newMusic(Gdx.files.internal("track5.wav"));
        gameplayMusic.setLooping(true);
        gameplayMusic.setVolume(0.3f);
        gameplayMusic.play();
    }

    /** First load only — populates exterior from LevelLoader/Populator. */
    private void bootstrapOverworld() {
        inTavern = false;
        LevelData levelData = loadOverworldLevelData();
        map = levelData.map;
        collisionRects = levelData.collisionRects;
        collisionPolygons = levelData.collisionPolygons;
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        mapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);

        initialSpawnX = levelData.spawnX;
        initialSpawnY = levelData.spawnY;
        outdoorReturnX = levelData.hasDoorHouse ? levelData.doorHouseX : levelData.spawnX;
        outdoorReturnY = levelData.hasDoorHouse ? levelData.doorHouseY - 18f : levelData.spawnY;
        player.setPosition(initialSpawnX, initialSpawnY);

        entities.clear();
        interactables.clear();
        entities.add(player);
        damageTexts.clear();

        LevelPopulator.addMentor(levelData, dialogueWindow, game.getAssetManager(), interactables, entities);
        LevelPopulator.addOreNodes(levelData, dialogueWindow, game.getAssetManager(), interactables, entities);
        wireOreNodeFonts();
        LevelPopulator.addDoorHouse(levelData, interactables, entities, p -> enterTavern());
        LevelPopulator.addOrcs(
            proceduralMap, dungeonMap, initialSpawnX, initialSpawnY, game.getAssetManager(), entities);
    }

    private void wireOreNodeFonts() {
        for (Entity e : entities) {
            if (e instanceof OreNode) {
                ((OreNode) e).setProgressFont(damageFont);
            }
        }
    }

    private void enterTavern() {
        outdoorReturnX = player.getX();
        outdoorReturnY = player.getY();
        player.setInteracting(false);
        activeInteractionTarget = null;
        if (craftingWindow != null) craftingWindow.close();
        if (dialogueWindow != null) dialogueWindow.hideDialogue();

        // Park live overworld — do NOT dispose orcs/ore/mentor
        List<Entity> parkedEntities = new ArrayList<>();
        for (Entity e : entities) {
            if (e != player) {
                parkedEntities.add(e);
            }
        }
        suspendedOverworld = new SuspendedOverworld(
            map, collisionRects, collisionPolygons, parkedEntities, new ArrayList<>(interactables));

        entities.removeIf(e -> e != player);
        interactables.clear();
        damageTexts.clear();

        if (mapRenderer != null) {
            mapRenderer.dispose();
            mapRenderer = null;
        }

        inTavern = true;
        TiledMap tavernMap = TavernInteriorFactory.buildMap();
        LevelData levelData = LevelLoader.load(tavernMap);
        map = levelData.map;
        collisionRects = levelData.collisionRects;
        collisionPolygons = levelData.collisionPolygons;
        mapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);

        player.setPosition(levelData.spawnX, levelData.spawnY);
        LevelPopulator.addCraftingStation(
            levelData, craftingWindow, game.getAssetManager(), interactables, entities, collisionPolygons);
        LevelPopulator.addDoorExit(levelData, interactables, entities, p -> exitTavern());
    }

    private void exitTavern() {
        float returnX = outdoorReturnX;
        float returnY = outdoorReturnY;
        player.setInteracting(false);
        activeInteractionTarget = null;
        if (craftingWindow != null) craftingWindow.close();

        // Dispose only tavern-local entities/map
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            Entity e = it.next();
            if (e != player) {
                e.dispose();
                it.remove();
            }
        }
        interactables.clear();
        if (map != null) {
            map.dispose();
        }
        if (mapRenderer != null) {
            mapRenderer.dispose();
            mapRenderer = null;
        }

        if (suspendedOverworld == null) {
            bootstrapOverworld();
            player.setPosition(returnX, returnY);
            return;
        }

        // Restore exact overworld runtime state
        inTavern = false;
        map = suspendedOverworld.map;
        collisionRects = suspendedOverworld.collisionRects;
        collisionPolygons = suspendedOverworld.collisionPolygons;
        mapRenderer = new OrthogonalTiledMapRenderer(map, UNIT_SCALE);

        entities.clear();
        entities.add(player);
        entities.addAll(suspendedOverworld.entities);
        interactables.clear();
        interactables.addAll(suspendedOverworld.interactables);
        suspendedOverworld = null;

        player.setPosition(returnX, returnY);
    }

    @Override
    public void render(float delta) {
        clearScreen(0, 0, 0, 1);

        handleItemSystemInputs();
        syncUiInteractionLock();
        updateInteractionTargetFeedback(delta);

        // Craft timer is player-owned (tavern forge) — ticks even with UI closed / in tavern.
        boolean craftWasBusy = craftingController.isBusy();
        craftingController.update(delta);
        if (craftWasBusy && craftingController.hasCompletedJob()) {
            inventoryWindow.refresh();
            equipmentWindow.refresh();
            if (craftingWindow.isVisible()) {
                craftingWindow.rebuild();
            }
            statusToast.show("Forja concluida: "
                + craftingController.getActiveJob().getRecipe().getResult().getName());
            statusToast.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 36f, Align.top);
        }
        if (craftingWindow.isVisible()) {
            craftingWindow.refreshProgressUi();
        }
        statusToast.update(delta);

        player.update(delta, collisionPolygons);

        if (!inTavern) {
            CombatController.resolvePlayerMelee(player, entities, damageTexts);
            CombatController.updateDamageTexts(damageTexts, delta);
            CombatController.updateEntities(player, entities, damageTexts, delta, collisionPolygons);
        } else {
            CombatController.updateDamageTexts(damageTexts, delta);
            for (Entity ent : entities) {
                if (!(ent instanceof Player) && !(ent instanceof Enemy)) {
                    ent.update(delta);
                }
            }
        }

        cameraController.follow(player.getX(), player.getY());
        mapRenderer.setView(cameraController.getCamera());
        mapRenderer.render();

        batch.setProjectionMatrix(cameraController.getCamera().combined);
        beginBatch();

        if (inTavern && tavernBackground != null) {
            batch.draw(tavernBackground, 0, 0, TavernInteriorFactory.WIDTH, TavernInteriorFactory.HEIGHT);
        }

        entities.sort((e1, e2) -> Float.compare(e2.getY(), e1.getY()));
        for (Entity entity : entities) {
            entity.render(batch);
        }
        CombatController.renderDamageTexts(batch, damageFont, damageTexts);
        endBatch();

        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (Entity entity : entities) {
            if (entity instanceof MapDoor) {
                ((MapDoor) entity).renderMarker(shapeRenderer);
            }
        }
        shapeRenderer.end();

        if (!inTavern) {
            CombatController.renderHealthBars(shapeRenderer, entities);
        }

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
                panelStack.clear();
            }
        }
    }

    private void syncUiInteractionLock() {
        if (inventoryWindow.isVisible()
            || equipmentWindow.isVisible()
            || lootWindow.isVisible()
            || craftingWindow.isVisible()
            || dialogueWindow.isVisible()) {
            player.setInteracting(true);
        } else {
            player.setInteracting(false);
        }
    }

    private void handleItemSystemInputs() {
        if (player.isDead()) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (panelStack.closeTop()) {
                syncUiInteractionLock();
                return;
            }
            // No open windows — leave ESC alone (death screen handles exit).
        }

        if (dialogueWindow.isVisible()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                dialogueWindow.hideDialogue();
                player.setInteracting(false);
                activeInteractionTarget = null;
            }
            return;
        }

        if (craftingWindow.isVisible()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                craftingWindow.close();
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            if (lootWindow.isVisible()) return;
            if (inventoryWindow.isVisible()) {
                inventoryWindow.closePanel();
            } else {
                inventoryWindow.openPanel();
                panelStack.push(inventoryWindow);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (lootWindow.isVisible() || craftingWindow.isVisible()) return;
            if (equipmentWindow.isVisible()) {
                equipmentWindow.closePanel();
            } else {
                equipmentWindow.openPanel();
                panelStack.push(equipmentWindow);
            }
        }

        if (inventoryWindow.isVisible() || equipmentWindow.isVisible()) {
            if (equipmentWindow.isVisible()) {
                equipmentWindow.refresh();
            }
            return;
        }

        if (!lootWindow.isVisible() && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            Interactable closestInteractable = findNearestInteractable();

            if (closestInteractable != null) {
                activeInteractionTarget = closestInteractable;
                boolean isDoor = closestInteractable instanceof MapDoor;
                closestInteractable.onInteract(player);
                inventoryWindow.refresh();
                equipmentWindow.refresh();
                if (!isDoor && (dialogueWindow.isVisible() || craftingWindow.isVisible())) {
                    player.setInteracting(true);
                }
                return;
            }

            if (inTavern) {
                return;
            }

            Enemy closestCorpse = findNearestLootableCorpse();
            if (closestCorpse != null) {
                activeLootTarget = closestCorpse;
                lootWindow.updateLoot(activeLootTarget);
                lootWindow.setVisible(true);
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

    private Interactable findNearestInteractable() {
        Interactable closest = null;
        float minDist = INTERACTION_RANGE;
        for (Interactable inter : interactables) {
            float dist = Vector2.dst(player.getX(), player.getY(), inter.getX(), inter.getY());
            if (dist <= minDist) {
                minDist = dist;
                closest = inter;
            }
        }
        return closest;
    }

    private Enemy findNearestLootableCorpse() {
        Enemy closest = null;
        float minDist = INTERACTION_RANGE;
        for (Entity ent : entities) {
            if (ent instanceof Enemy) {
                Enemy enemy = (Enemy) ent;
                if (enemy.hasLootAvailable()) {
                    float dist = Vector2.dst(player.getX(), player.getY(), enemy.getX(), enemy.getY());
                    if (dist <= minDist) {
                        minDist = dist;
                        closest = enemy;
                    }
                }
            }
        }
        return closest;
    }

    /** Highlights the same nearest ore/corpse that E would target. */
    private void updateInteractionTargetFeedback(float delta) {
        for (Entity e : entities) {
            if (e instanceof OreNode) {
                ((OreNode) e).setInteractionTargeted(false);
            } else if (e instanceof Enemy) {
                ((Enemy) e).setInteractionHighlighted(false);
            }
        }
        highlightedLootCorpse = null;

        boolean uiBlocks = player.isDead()
            || inventoryWindow.isVisible()
            || equipmentWindow.isVisible()
            || craftingWindow.isVisible()
            || dialogueWindow.isVisible()
            || lootWindow.isVisible();

        if (!uiBlocks) {
            Interactable nearest = findNearestInteractable();
            if (nearest instanceof OreNode) {
                ((OreNode) nearest).setInteractionTargeted(true);
            } else if (!inTavern && nearest == null) {
                Enemy corpse = findNearestLootableCorpse();
                if (corpse != null) {
                    corpse.setInteractionHighlighted(true);
                    highlightedLootCorpse = corpse;
                }
            }
        }
        // OreNode.update (incl. respawn timer + pulse) runs via CombatController.updateEntities
        // while in the overworld — do not double-tick here or respawn fires at half duration.
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
                inventoryWindow.clampToStage(width, height);
            }
            if (equipmentWindow != null) {
                equipmentWindow.clampToStage(width, height);
            }
            if (craftingWindow != null && craftingWindow.isVisible()) {
                craftingWindow.clampToStage(width, height);
            }
            if (lootWindow != null) {
                lootWindow.setPosition(width / 2f, height / 2f + 100, Align.center);
            }
        }
        if (dialogueWindow != null) {
            dialogueWindow.setPosition(width / 2f, 60, Align.bottom);
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
        if (suspendedOverworld != null) {
            for (Entity entity : suspendedOverworld.entities) {
                entity.dispose();
            }
        }
        if (damageFont != null) damageFont.dispose();
        if (uiFont != null) uiFont.dispose();
        if (proceduralMap) {
            DungeonMapAdapter.disposeProceduralResources(map);
        } else if (inTavern && map != null) {
            map.dispose();
        }
        if (mapRenderer != null) mapRenderer.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }

    private LevelData loadOverworldLevelData() {
        if (GameConfig.USE_PROCEDURAL_DUNGEON) {
            this.dungeonMap = DungeonGenerator.generate(DungeonGenerationConfig.defaults());
            return DungeonMapAdapter.toLevelData(this.dungeonMap, game.getAssetManager());
        }
        return LevelLoader.load(game.getAssetManager(), LevelConstants.MAP_PATH);
    }
}
