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
import com.donos.zebra.audio.MusicSettings;
import com.donos.zebra.config.DungeonGenerationConfig;
import com.donos.zebra.config.GameConfig;
import com.donos.zebra.entities.Enemy;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.MapDoor;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.CraftingStation;
import com.donos.zebra.entities.DamageText;
import com.donos.zebra.entities.MentorNpc;
import com.donos.zebra.entities.OreNode;
import com.donos.zebra.items.CraftingController;
import com.donos.zebra.items.ItemStack;
import com.donos.zebra.items.PotionRules;
import com.donos.zebra.save.GameSession;
import com.donos.zebra.save.SaveData;
import com.donos.zebra.save.SaveService;
import com.donos.zebra.save.SaveStateMapper;
import com.donos.zebra.screens.gameplay.CombatController;
import com.donos.zebra.screens.gameplay.DeathScreenController;
import com.donos.zebra.screens.gameplay.GameFlowController;
import com.donos.zebra.screens.gameplay.GameFlowState;
import com.donos.zebra.screens.gameplay.LevelPopulator;
import com.donos.zebra.screens.gameplay.PlayerDeathHandler;
import com.donos.zebra.screens.gameplay.SuspendedOverworld;
import com.donos.zebra.ui.CraftingUI;
import com.donos.zebra.ui.DialogueUI;
import com.donos.zebra.ui.EquipmentUI;
import com.donos.zebra.ui.InventoryUI;
import com.donos.zebra.ui.ItemTooltipPanel;
import com.donos.zebra.ui.LootUI;
import com.donos.zebra.ui.PauseMenuUI;
import com.donos.zebra.ui.SaveSlotsPanel;
import com.donos.zebra.ui.ShopUI;
import com.donos.zebra.ui.SkillBarUI;
import com.donos.zebra.ui.SkillMenuUI;
import com.donos.zebra.ui.SkillTooltipPanel;
import com.donos.zebra.ui.StatusToast;
import com.donos.zebra.ui.QuestJournalUI;
import com.donos.zebra.ui.QuestNotificationUI;
import com.donos.zebra.ui.QuestTrackerHUD;
import com.donos.zebra.ui.UiPanelStack;
import com.donos.zebra.ui.WorldMapUI;
import com.donos.zebra.quests.QuestDefinition;
import com.donos.zebra.quests.QuestIds;
import com.donos.zebra.quests.QuestLocation;
import com.donos.zebra.quests.QuestLog;
import com.donos.zebra.quests.QuestLogListener;
import com.donos.zebra.quests.QuestMapMarker;
import com.donos.zebra.quests.QuestMapModel;
import com.donos.zebra.quests.QuestObjectiveDefinition;
import com.donos.zebra.skills.SkillCastContext;
import com.donos.zebra.skills.SkillCaster;
import com.donos.zebra.skills.vfx.SkillEffectWorld;
import com.donos.zebra.skills.vfx.SkillVfxFactory;
import com.donos.zebra.util.HealthBarRenderer;
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
    private GameSession session;
    private final SaveService saveService = new SaveService();
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
    private BitmapFont nameFont;
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
    private ShopUI shopWindow;
    private PauseMenuUI pauseMenu;
    private SaveSlotsPanel saveSlotsOverlay;
    private SkillBarUI skillBarUI;
    private SkillMenuUI skillMenuUI;
    private SkillTooltipPanel skillTooltipPanel;
    private final SkillCaster skillCaster = new SkillCaster();
    private final SkillEffectWorld skillEffects = new SkillEffectWorld();
    private SkillVfxFactory skillVfxFactory;
    private final CraftingController craftingController = new CraftingController();
    private final GameFlowController flow = new GameFlowController();
    private final DeathScreenController deathScreen = new DeathScreenController();
    private StatusToast statusToast;
    private final QuestLog questLog = new QuestLog();
    private QuestTrackerHUD questTrackerHUD;
    private QuestJournalUI questJournalUI;
    private QuestNotificationUI questNotificationUI;
    private WorldMapUI worldMapUI;
    /** True when map was opened via M without the journal (close map → leave QUESTS). */
    private boolean worldMapStandalone;
    private final List<Interactable> interactables = new ArrayList<>();
    private Interactable activeInteractionTarget = null;

    public GameScreen(MainGame game) {
        this(game, GameSession.newGame("Hero"));
    }

    public GameScreen(MainGame game, GameSession session) {
        super(game.batch);
        this.game = game;
        this.session = session != null ? session : GameSession.newGame("Hero");
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
        nameFont = new BitmapFont();
        nameFont.getData().setScale(0.55f);
        shapeRenderer = new ShapeRenderer();

        uiStage = new Stage(new ScreenViewport());
        uiSkin = InventoryUI.createDefaultSkin(uiFont);

        player = new Player(game.getAssetManager());
        if (session.getCharacterName() != null && !session.getCharacterName().isEmpty()) {
            player.setCharacterName(session.getCharacterName());
        }
        questLog.setProgressPlayer(player);
        skillVfxFactory = new SkillVfxFactory(game.getAssetManager());
        skillVfxFactory.ensureLoaded();

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

        questNotificationUI = new QuestNotificationUI(uiSkin);
        questNotificationUI.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 90f, Align.top);
        uiStage.addActor(questNotificationUI);

        questTrackerHUD = new QuestTrackerHUD(uiSkin);
        questTrackerHUD.setPosition(Gdx.graphics.getWidth() - 16f, Gdx.graphics.getHeight() - 16f, Align.topRight);
        questTrackerHUD.setOnClicked(this::openQuestJournalFromTracker);
        uiStage.addActor(questTrackerHUD);

        questJournalUI = new QuestJournalUI(uiSkin);
        questJournalUI.setListener(new QuestJournalUI.Listener() {
            @Override
            public void onClose() {
                closeQuestJournal();
            }

            @Override
            public void onLocate(QuestDefinition quest, QuestObjectiveDefinition objective) {
                openQuestMap(objective);
            }
        });
        uiStage.addActor(questJournalUI);

        worldMapUI = new WorldMapUI(uiSkin);
        worldMapUI.setOnClosed(this::onWorldMapClosed);
        uiStage.addActor(worldMapUI);

        questLog.setListener(new QuestLogListener() {
            @Override
            public void onQuestStarted(QuestDefinition quest) {
                questNotificationUI.show("NOVA QUEST", quest.title);
                questTrackerHUD.refresh(questLog);
            }

            @Override
            public void onObjectiveProgress(QuestDefinition quest,
                                            QuestObjectiveDefinition objective,
                                            int current,
                                            int required) {
                questNotificationUI.show("OBJETIVO ATUALIZADO",
                    shortQuestLabel(objective) + ": " + current + "/" + required);
                questTrackerHUD.refresh(questLog);
                questJournalUI.refresh();
            }

            @Override
            public void onObjectiveCompleted(QuestDefinition quest, QuestObjectiveDefinition objective) {
                questNotificationUI.show("OBJETIVO CONCLUIDO", "✓ " + objective.description);
                questTrackerHUD.refresh(questLog);
                questJournalUI.refresh();
            }

            @Override
            public void onQuestCompleted(QuestDefinition quest) {
                questNotificationUI.show("QUEST CONCLUIDA", quest.title);
                questTrackerHUD.refresh(questLog);
                questJournalUI.refresh();
            }

            @Override
            public void onTrackedQuestChanged(String questId) {
                questTrackerHUD.refresh(questLog);
            }
        });

        skillTooltipPanel = new SkillTooltipPanel(uiSkin);
        skillTooltipPanel.setAssetManager(game.getAssetManager());
        uiStage.addActor(skillTooltipPanel);

        skillBarUI = new SkillBarUI(player.getSkillBook(), uiSkin, game.getAssetManager());
        skillBarUI.setTooltipPanel(skillTooltipPanel);
        skillBarUI.placeBottomCenter(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiStage.addActor(skillBarUI);

        skillMenuUI = new SkillMenuUI(player.getSkillBook(), uiSkin, game.getAssetManager());
        skillMenuUI.setTooltipPanel(skillTooltipPanel);
        skillMenuUI.bindSkillBar(skillBarUI);
        skillMenuUI.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, Align.center);
        skillMenuUI.setOnClosed(() -> {
            panelStack.remove(skillMenuUI);
            syncUiInteractionLock();
        });
        uiStage.addActor(skillMenuUI);

        inventoryWindow.setOnEquipmentChanged(equipmentWindow::refresh);
        equipmentWindow.setOnInventoryChanged(inventoryWindow::refresh);
        java.util.function.Consumer<String> toast = this::showStatusToast;
        inventoryWindow.setEquipToast(toast);
        equipmentWindow.setEquipToast(toast);
        inventoryWindow.setStatusToast(toast);

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

        shopWindow = new ShopUI(uiSkin, game.getAssetManager());
        shopWindow.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, Align.center);
        shopWindow.setOnOpened(() -> panelStack.push(shopWindow));
        shopWindow.setOnClosed(() -> {
            panelStack.remove(shopWindow);
            player.setInteracting(false);
            activeInteractionTarget = null;
        });
        shopWindow.setOnInventoryChanged(() -> {
            inventoryWindow.refresh();
            equipmentWindow.refresh();
        });
        shopWindow.setToast(toast);
        uiStage.addActor(shopWindow);

        pauseMenu = new PauseMenuUI(uiSkin);
        pauseMenu.setListener(new PauseMenuUI.Listener() {
            @Override
            public void onResume() {
                hideSaveOverlay();
                resumeGameplay();
            }

            @Override
            public void onSaveGame() {
                openSaveOverlay();
            }

            @Override
            public void onRestart() {
                restartGameplay();
            }

            @Override
            public void onExit() {
                Gdx.app.exit();
            }

            @Override
            public void onOpenOptions() {
                hideSaveOverlay();
                flow.openOptions();
            }

            @Override
            public void onBackFromOptions() {
                flow.backFromOptions();
            }
        });
        uiStage.addActor(pauseMenu);

        saveSlotsOverlay = new SaveSlotsPanel(uiSkin, SaveSlotsPanel.Mode.SAVE, saveService);
        saveSlotsOverlay.setVisible(false);
        saveSlotsOverlay.setFillParent(true);
        saveSlotsOverlay.setListener(new SaveSlotsPanel.Listener() {
            @Override
            public void onSlotChosen(int slotIndex) {
                performSave(slotIndex);
            }

            @Override
            public void onBack() {
                hideSaveOverlay();
                pauseMenu.openPause();
            }
        });
        uiStage.addActor(saveSlotsOverlay);

        if (game.getAssetManager().isLoaded(LevelConstants.TAVERN_BACKGROUND)) {
            tavernBackground = game.getAssetManager().get(LevelConstants.TAVERN_BACKGROUND, Texture.class);
        }

        OrthographicCamera camera = new OrthographicCamera();
        applyCameraViewport(camera);
        cameraController = new CameraController(camera);

        bootstrapOverworld();
        applySessionAfterBootstrap();

        gameplayMusic = Gdx.audio.newMusic(Gdx.files.internal("track5.wav"));
        gameplayMusic.setLooping(true);
        MusicSettings.bind(gameplayMusic);
        gameplayMusic.play();
    }

    private void applySessionAfterBootstrap() {
        if (session.shouldApplySave()) {
            SaveData save = session.getLoadedSave();
            List<Entity> overworldEntities = new ArrayList<>(entities);
            SaveStateMapper.applyWorld(save.world, save.quest, overworldEntities);
            SaveStateMapper.applyPlayer(save.player, player);
            SaveStateMapper.applyCrafting(save.crafting, craftingController, player.getInventory());
            if (save.player != null && save.player.inTavern) {
                outdoorReturnX = save.player.outdoorReturnX;
                outdoorReturnY = save.player.outdoorReturnY;
                float tavernX = save.player.x;
                float tavernY = save.player.y;
                player.setPosition(outdoorReturnX, outdoorReturnY);
                enterTavern();
                player.setPosition(tavernX, tavernY);
            }
            if (equipmentWindow != null) {
                equipmentWindow.refresh();
            }
            if (inventoryWindow != null) {
                inventoryWindow.refresh();
            }
            if (skillBarUI != null) {
                skillBarUI.refresh();
            }
            MentorNpc mentor = findMentor(entities);
            if (mentor == null && suspendedOverworld != null) {
                mentor = findMentor(suspendedOverworld.entities);
            }
            questLog.readFromSave(save.quest, player, mentor);
            bindQuestLogToWorldEntities();
            questTrackerHUD.refresh(questLog);
        } else if (session.getCharacterName() != null) {
            player.setCharacterName(session.getCharacterName());
        }
    }

    private void bindQuestLogToWorldEntities() {
        List<Entity> source = inTavern && suspendedOverworld != null
            ? suspendedOverworld.entities
            : entities;
        for (Entity e : source) {
            if (e instanceof MentorNpc) {
                ((MentorNpc) e).setQuestLog(questLog);
            } else if (e instanceof OreNode) {
                ((OreNode) e).setQuestLog(questLog);
            } else if (e instanceof com.donos.zebra.entities.Orc) {
                ((com.donos.zebra.entities.Orc) e).setQuestLog(questLog);
            }
        }
        for (Entity e : entities) {
            if (e instanceof OreNode) {
                ((OreNode) e).setQuestLog(questLog);
            } else if (e instanceof MentorNpc) {
                ((MentorNpc) e).setQuestLog(questLog);
            } else if (e instanceof com.donos.zebra.entities.Orc) {
                ((com.donos.zebra.entities.Orc) e).setQuestLog(questLog);
            }
        }
    }

    private static MentorNpc findMentor(List<Entity> list) {
        if (list == null) {
            return null;
        }
        for (Entity e : list) {
            if (e instanceof MentorNpc) {
                return (MentorNpc) e;
            }
        }
        return null;
    }

    private static String shortQuestLabel(QuestObjectiveDefinition objective) {
        if (objective == null) {
            return "Objetivo";
        }
        if (QuestIds.ITEM_COPPER_ORE.equals(objective.targetId)) {
            return "Cobre";
        }
        return objective.description;
    }

    private void openQuestJournalFromTracker() {
        openQuestJournal(questLog.getTrackedQuestId());
    }

    private void openQuestJournal(String selectQuestId) {
        if (player.isDead() || flow.getState() == GameFlowState.DYING || flow.getState() == GameFlowState.DEAD) {
            return;
        }
        if (flow.getState() == GameFlowState.QUESTS) {
            closeQuestJournal();
            return;
        }
        if (!flow.canOpenQuests()) {
            return;
        }
        if (lootWindow.isVisible() || craftingWindow.isVisible() || shopWindow.isVisible()) {
            return;
        }
        panelStack.closeAll();
        if (dialogueWindow.isVisible()) {
            dialogueWindow.hideDialogue();
        }
        if (skillMenuUI.isVisible()) {
            skillMenuUI.closePanel();
        }
        flow.openQuests();
        questJournalUI.open(questLog, selectQuestId);
        player.setInteracting(true);
        syncUiInteractionLock();
    }

    private void closeQuestJournal() {
        if (worldMapUI != null && worldMapUI.isOpen()) {
            worldMapStandalone = false;
            worldMapUI.close();
        }
        if (questJournalUI != null && questJournalUI.isVisible()) {
            questJournalUI.setVisible(false);
        }
        worldMapStandalone = false;
        flow.closeQuests();
        syncUiInteractionLock();
    }

    private void openQuestMap(QuestObjectiveDefinition objective) {
        if (objective == null || objective.location == null) {
            return;
        }
        TiledMap overworld = resolveOverworldMap();
        if (overworld == null) {
            return;
        }
        worldMapStandalone = false;
        List<QuestMapMarker> markers = buildWorldMapMarkers(objective.location);
        worldMapUI.openQuestFocus(overworld, markers, objective.location);
    }

    private void toggleWorldMap() {
        if (worldMapUI != null && worldMapUI.isOpen()) {
            closeWorldMapOnly();
            return;
        }
        if (player.isDead() || flow.getState() == GameFlowState.DYING || flow.getState() == GameFlowState.DEAD) {
            return;
        }
        if (flow.getState() == GameFlowState.QUESTS) {
            // Journal open: M opens full map over journal
            openFullWorldMap(false);
            return;
        }
        if (!flow.isPlaying()) {
            return;
        }
        if (lootWindow.isVisible() || craftingWindow.isVisible() || shopWindow.isVisible()) {
            return;
        }
        panelStack.closeAll();
        if (dialogueWindow.isVisible()) {
            dialogueWindow.hideDialogue();
        }
        if (skillMenuUI.isVisible()) {
            skillMenuUI.closePanel();
        }
        openFullWorldMap(true);
    }

    private void openFullWorldMap(boolean standalone) {
        TiledMap overworld = resolveOverworldMap();
        if (overworld == null) {
            return;
        }
        if (standalone) {
            flow.openQuests();
            worldMapStandalone = true;
            if (questJournalUI != null) {
                questJournalUI.setVisible(false);
            }
        } else {
            worldMapStandalone = false;
        }
        worldMapUI.openFull(overworld, buildWorldMapMarkers(null));
        player.setInteracting(true);
        syncUiInteractionLock();
    }

    private void closeWorldMapOnly() {
        if (worldMapUI == null || !worldMapUI.isOpen()) {
            return;
        }
        boolean leaveQuests = worldMapStandalone;
        worldMapUI.close();
        if (leaveQuests) {
            worldMapStandalone = false;
            flow.closeQuests();
            syncUiInteractionLock();
        }
    }

    private void onWorldMapClosed() {
        if (worldMapStandalone) {
            worldMapStandalone = false;
            if (flow.getState() == GameFlowState.QUESTS
                && (questJournalUI == null || !questJournalUI.isVisible())) {
                flow.closeQuests();
                syncUiInteractionLock();
            }
        }
    }

    private TiledMap resolveOverworldMap() {
        if (inTavern && suspendedOverworld != null && suspendedOverworld.map != null) {
            return suspendedOverworld.map;
        }
        if (!inTavern && map != null) {
            return map;
        }
        return map;
    }

    private List<QuestMapMarker> buildWorldMapMarkers(QuestLocation focus) {
        List<Entity> source = entitiesForQuestWorld();
        float px = inTavern ? outdoorReturnX : player.getX();
        float py = inTavern ? outdoorReturnY : player.getY();
        return QuestMapModel.worldMarkers(player, px, py, source, focus);
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

        MentorNpc mentor = LevelPopulator.addMentor(
            levelData, dialogueWindow, game.getAssetManager(), interactables, entities);
        if (mentor != null) {
            mentor.setOpenShop(() -> {
                shopWindow.open(player);
                player.setInteracting(true);
            });
            mentor.setQuestLog(questLog);
        }
        LevelPopulator.addOreNodes(levelData, dialogueWindow, game.getAssetManager(), interactables, entities);
        bindQuestLogToWorldEntities();
        if (!session.shouldApplySave()) {
            questLog.startQuest(QuestIds.QUEST_FIRST_SWORD);
            questLog.syncFromWorld(player, mentor);
        }
        questTrackerHUD.refresh(questLog);
        wireOreNodeFonts();
        LevelPopulator.addDoorHouse(levelData, interactables, entities, p -> enterTavern());
        LevelPopulator.addOrcs(
            proceduralMap, dungeonMap, initialSpawnX, initialSpawnY, game.getAssetManager(), entities);
        bindQuestLogToWorldEntities();
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
        if (shopWindow != null) shopWindow.closePanel();
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
        if (shopWindow != null) shopWindow.closePanel();

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

        if (player.isDead()
            && flow.getState() != GameFlowState.DYING
            && flow.getState() != GameFlowState.DEAD) {
            closeGameplayPanelsForOverlay();
            pauseMenu.closeAll();
            flow.onPlayerDied();
            deathScreen.begin();
        }

        handleItemSystemInputs();
        syncUiInteractionLock();

        float uiDelta = delta;
        float worldDelta = flow.isWorldFrozen() ? 0f : delta;
        float playerDelta = flow.isPlayerSimulationFrozen() ? 0f : delta;

        updateInteractionTargetFeedback(worldDelta);

        boolean craftWasBusy = craftingController.isBusy();
        craftingController.update(worldDelta);
        if (craftWasBusy && craftingController.hasCompletedJob()) {
            inventoryWindow.refresh();
            equipmentWindow.refresh();
            if (craftingWindow.isVisible()) {
                craftingWindow.rebuild();
            }
            statusToast.show("Forja concluida: "
                + craftingController.getActiveJob().getRecipe().getResult().getName());
            statusToast.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 36f, Align.top);
            if (questLog != null && craftingController.getActiveJob() != null) {
                questLog.reportCraftItem(craftingController.getActiveJob().getRecipe().getResult().getId());
            }
        }
        if (craftingWindow.isVisible()) {
            craftingWindow.refreshProgressUi();
        }
        statusToast.update(uiDelta);
        if (questNotificationUI != null) {
            questNotificationUI.update(uiDelta);
        }
        if (questTrackerHUD != null) {
            questTrackerHUD.setPosition(
                Gdx.graphics.getWidth() - 16f, Gdx.graphics.getHeight() - 16f, Align.topRight);
        }

        player.update(playerDelta, collisionPolygons);

        // Skill cooldowns follow world time (frozen on pause / dying / dead)
        if (worldDelta > 0f) {
            player.getSkillBook().tick(worldDelta);
            SkillCastContext skillCtx = new SkillCastContext(
                player, entities, damageTexts, skillEffects, skillVfxFactory, cameraController);
            skillCaster.update(worldDelta, skillCtx);
            skillEffects.update(worldDelta);
            cameraController.update(worldDelta);
        }

        if (!inTavern) {
            if (worldDelta > 0f) {
                CombatController.resolvePlayerMelee(player, entities, damageTexts);
            }
            CombatController.updateDamageTexts(damageTexts, worldDelta);
            CombatController.updateEntities(player, entities, damageTexts, worldDelta, collisionPolygons);
        } else {
            CombatController.updateDamageTexts(damageTexts, worldDelta);
            if (worldDelta > 0f) {
                for (Entity ent : entities) {
                    if (!(ent instanceof Player) && !(ent instanceof Enemy)) {
                        ent.update(worldDelta);
                    }
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
        } else {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            HealthBarRenderer.draw(
                shapeRenderer, player.getX(), player.getY() + 16f, 20f, 4f,
                player.getCurrentHealth(), player.getMaxHealth());
            shapeRenderer.end();
        }

        // Name above HP bar (world space, after bar so it stays readable)
        beginBatch();
        CombatController.renderPlayerName(batch, nameFont, player);
        renderInteractionPrompts();
        endBatch();

        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        batch.setProjectionMatrix(cameraController.getCamera().combined);
        skillEffects.render(batch, shapeRenderer);

        if (flow.isOverlayPause()) {
            drawPauseDim();
        }

        uiStage.act(uiDelta);
        uiStage.draw();

        if (DEBUG_COLLISION) {
            renderDebugCollision();
        }

        if (player.isDead()) {
            PlayerDeathHandler.Result deathResult = PlayerDeathHandler.updateDrawAndHandle(
                deathScreen,
                player,
                initialSpawnX,
                initialSpawnY,
                collisionPolygons,
                cameraController.getCamera(),
                shapeRenderer,
                batch,
                damageFont,
                lootWindow,
                inventoryWindow,
                uiDelta
            );
            if (deathScreen.isInteractive()) {
                flow.onDeathUiReady();
            }
            if (deathResult == PlayerDeathHandler.Result.REVIVED) {
                activeLootTarget = null;
                panelStack.clear();
                flow.onRevived();
            } else if (deathResult == PlayerDeathHandler.Result.EXIT) {
                Gdx.app.exit();
            }
        }
    }

    private void drawPauseDim() {
        OrthographicCamera camera = cameraController.getCamera();
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.45f);
        shapeRenderer.rect(
            camera.position.x - camera.viewportWidth / 2f,
            camera.position.y - camera.viewportHeight / 2f,
            camera.viewportWidth,
            camera.viewportHeight);
        shapeRenderer.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
    }

    private void resumeGameplay() {
        hideSaveOverlay();
        pauseMenu.closeAll();
        flow.resumeFromPause();
        syncUiInteractionLock();
    }

    private void openSaveOverlay() {
        if (saveSlotsOverlay == null) {
            return;
        }
        pauseMenu.closeAll();
        saveSlotsOverlay.refresh();
        saveSlotsOverlay.setStatusMessage("");
        saveSlotsOverlay.setVisible(true);
        saveSlotsOverlay.toFront();
        // Remain in PAUSED — saving must not unpause unexpectedly
        if (!flow.isOverlayPause()) {
            flow.openPause();
        }
    }

    private void hideSaveOverlay() {
        if (saveSlotsOverlay != null) {
            saveSlotsOverlay.setVisible(false);
        }
    }

    private void performSave(int slotIndex) {
        try {
            if (player != null && player.isDead()) {
                saveSlotsOverlay.setStatusMessage("Nao e possivel salvar enquanto morto.");
                return;
            }
            List<Entity> overworldEntities = collectOverworldEntitiesForSave();
            SaveData data = SaveStateMapper.capture(
                player,
                overworldEntities,
                inTavern,
                outdoorReturnX,
                outdoorReturnY,
                craftingController,
                questLog
            );
            boolean ok = saveService.writeSlot(slotIndex, data);
            if (ok) {
                session = session.withActiveSlot(slotIndex);
                saveSlotsOverlay.setStatusMessage("Jogo salvo no Slot " + slotIndex + ".");
                showStatusToast("Jogo salvo!");
            } else {
                saveSlotsOverlay.setStatusMessage("Falha ao salvar. Tente outro slot.");
            }
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Save failed: " + e.getMessage());
            if (saveSlotsOverlay != null) {
                saveSlotsOverlay.setStatusMessage("Falha ao salvar.");
            }
        }
    }

    private List<Entity> collectOverworldEntitiesForSave() {
        if (inTavern && suspendedOverworld != null) {
            return new ArrayList<>(suspendedOverworld.entities);
        }
        List<Entity> list = new ArrayList<>();
        for (Entity e : entities) {
            if (e != player) {
                list.add(e);
            }
        }
        return list;
    }

    private List<Entity> entitiesForQuestWorld() {
        if (inTavern && suspendedOverworld != null) {
            return suspendedOverworld.entities;
        }
        return entities;
    }

    private void restartGameplay() {
        MusicSettings.unbind(gameplayMusic);
        // Fresh session for this character/slot binding — does NOT delete save files
        game.setScreen(new GameScreen(game, session.forRestart()));
    }

    private void openPauseMenu() {
        closeGameplayPanelsForOverlay();
        flow.openPause();
        pauseMenu.openPause();
        player.setInteracting(true);
    }

    private void closeGameplayPanelsForOverlay() {
        if (inventoryWindow.isVisible()) {
            inventoryWindow.closePanel();
        }
        if (equipmentWindow.isVisible()) {
            equipmentWindow.closePanel();
        }
        if (craftingWindow.isVisible()) {
            craftingWindow.close();
        }
        if (shopWindow.isVisible()) {
            shopWindow.closePanel();
        }
        if (skillMenuUI != null && skillMenuUI.isVisible()) {
            skillMenuUI.closePanel();
        }
        if (dialogueWindow.isVisible()) {
            dialogueWindow.hideDialogue();
        }
        if (lootWindow.isVisible()) {
            lootWindow.setVisible(false);
            activeLootTarget = null;
        }
        inventoryWindow.hideContextMenu();
        panelStack.clear();
        activeInteractionTarget = null;
    }

    private void showStatusToast(String msg) {
        statusToast.show(msg);
        statusToast.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() - 36f, Align.top);
    }

    private void toggleSkillMenu() {
        if (skillMenuUI == null) {
            return;
        }
        if (lootWindow.isVisible() || craftingWindow.isVisible() || shopWindow.isVisible()) {
            return;
        }
        if (skillMenuUI.isVisible()) {
            skillMenuUI.closePanel();
            syncUiInteractionLock();
            return;
        }
        if (inventoryWindow.isVisible()) {
            inventoryWindow.closePanel();
        }
        if (equipmentWindow.isVisible()) {
            equipmentWindow.closePanel();
        }
        skillMenuUI.open();
        panelStack.push(skillMenuUI);
        syncUiInteractionLock();
    }

    private void tryActivateSkillSlot(int slotIndex) {
        if (!flow.isPlaying() || inTavern) {
            return;
        }
        if (lootWindow.isVisible() || craftingWindow.isVisible() || shopWindow.isVisible()
            || dialogueWindow.isVisible()
            || (skillMenuUI != null && skillMenuUI.isVisible())) {
            return;
        }
        SkillCastContext ctx = new SkillCastContext(
            player, entities, damageTexts, skillEffects, skillVfxFactory, cameraController);
        SkillCaster.CastResult result = skillCaster.tryActivateSlot(slotIndex, player.getSkillBook(), ctx);
        if (result == SkillCaster.CastResult.NEEDS_WEAPON) {
            damageTexts.add(new DamageText(
                player.getX(), player.getY() + 18f, "Precisa de uma arma!",
                com.badlogic.gdx.graphics.Color.YELLOW));
        } else if (result == SkillCaster.CastResult.ON_COOLDOWN) {
            showStatusToast("Habilidade em recarga");
        }
        if (skillBarUI != null) {
            skillBarUI.refresh();
        }
    }

    private void syncUiInteractionLock() {
        if (flow.isOverlayPause()
            || inventoryWindow.isVisible()
            || equipmentWindow.isVisible()
            || lootWindow.isVisible()
            || craftingWindow.isVisible()
            || shopWindow.isVisible()
            || dialogueWindow.isVisible()
            || (skillMenuUI != null && skillMenuUI.isVisible())
            || flow.getState() == GameFlowState.QUESTS
            || (questJournalUI != null && questJournalUI.isVisible())
            || (worldMapUI != null && worldMapUI.isOpen())) {
            player.setInteracting(true);
        } else {
            player.setInteracting(false);
        }
    }

    private void handleItemSystemInputs() {
        if (player.isDead()
            || flow.getState() == GameFlowState.DYING
            || flow.getState() == GameFlowState.DEAD) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (flow.getState() == GameFlowState.QUESTS) {
                if (worldMapUI != null && worldMapUI.isOpen()) {
                    closeWorldMapOnly();
                    return;
                }
                closeQuestJournal();
                return;
            }
            if (flow.getState() == GameFlowState.OPTIONS) {
                pauseMenu.openPause();
                flow.backFromOptions();
                return;
            }
            if (flow.getState() == GameFlowState.PAUSED) {
                if (saveSlotsOverlay != null && saveSlotsOverlay.isVisible()) {
                    hideSaveOverlay();
                    pauseMenu.openPause();
                    return;
                }
                resumeGameplay();
                return;
            }
            if (panelStack.closeTop()) {
                syncUiInteractionLock();
                return;
            }
            if (dialogueWindow.isVisible()) {
                dialogueWindow.hideDialogue();
                syncUiInteractionLock();
                return;
            }
            if (lootWindow.isVisible()) {
                lootWindow.setVisible(false);
                activeLootTarget = null;
                syncUiInteractionLock();
                return;
            }
            openPauseMenu();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            if (flow.getState() == GameFlowState.QUESTS || flow.isPlaying()) {
                toggleWorldMap();
                return;
            }
        }

        if (flow.isOverlayPause()) {
            return;
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

        if (shopWindow.isVisible()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                shopWindow.closePanel();
            }
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            openQuestJournal(questLog.getTrackedQuestId());
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
            toggleSkillMenu();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_1)) {
            tryActivateSkillSlot(0);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_2)) {
            tryActivateSkillSlot(1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_3)) {
            tryActivateSkillSlot(2);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)
            || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_4)) {
            tryActivateSkillSlot(3);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            handlePotionHotkey();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            if (lootWindow.isVisible()) return;
            inventoryWindow.hideContextMenu();
            if (inventoryWindow.isVisible()) {
                inventoryWindow.closePanel();
            } else {
                inventoryWindow.openPanel();
                panelStack.push(inventoryWindow);
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            if (lootWindow.isVisible() || craftingWindow.isVisible() || shopWindow.isVisible()) return;
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
            if (inventoryWindow.isVisible()) {
                inventoryWindow.refresh();
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
                if (!isDoor && (dialogueWindow.isVisible() || craftingWindow.isVisible() || shopWindow.isVisible())) {
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

        if (!lootWindow.isVisible()
            && !shopWindow.isVisible()
            && !craftingWindow.isVisible()
            && !dialogueWindow.isVisible()
            && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            Interactable closest = findNearestInteractable();
            if (closest != null && closest.hasSecondaryInteract()) {
                closest.onSecondaryInteract(player);
                inventoryWindow.refresh();
                equipmentWindow.refresh();
                if (shopWindow.isVisible()) {
                    player.setInteracting(true);
                }
                return;
            }
        }

        if (lootWindow.isVisible() && activeLootTarget != null && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            for (ItemStack stack : activeLootTarget.getLootTable()) {
                player.getInventory().addItem(stack.getDefinition(), stack.getQuantity());
            }
            int silver = activeLootTarget.getSilverLoot();
            if (silver > 0) {
                player.getWallet().addSilver(silver);
                showStatusToast("+" + silver + " Prata");
            }
            inventoryWindow.refresh();
            equipmentWindow.refresh();
            activeLootTarget.clearLoot();
            lootWindow.setVisible(false);
            activeLootTarget = null;
            player.setInteracting(false);
        }
    }

    private void handlePotionHotkey() {
        if (lootWindow.isVisible() || craftingWindow.isVisible() || shopWindow.isVisible() || dialogueWindow.isVisible()) {
            return;
        }
        int healPreview = player.getPotionSlot() != null
            ? player.getPotionSlot().getDefinition().getHealAmount()
            : 0;
        PotionRules.Result result = player.tryUsePotionFromSlot();
        inventoryWindow.refresh();
        equipmentWindow.refresh();
        if (result == PotionRules.Result.OK) {
            showStatusToast("+" + healPreview + " HP");
        } else {
            showStatusToast(PotionRules.feedback(result, player.getPotionCooldownRemaining()));
        }
    }

    private void renderInteractionPrompts() {
        if (player == null || player.isDead() || flow.isOverlayPause()) {
            return;
        }
        if (inventoryWindow.isVisible() || equipmentWindow.isVisible()
            || craftingWindow.isVisible() || shopWindow.isVisible()
            || dialogueWindow.isVisible() || lootWindow.isVisible()) {
            return;
        }
        Interactable nearest = findNearestInteractable();
        if (nearest == null || nameFont == null) {
            return;
        }
        float y = nearest.getY() + 42f;
        nameFont.setColor(1f, 0.95f, 0.7f, 1f);
        String primary = nearest.getPromptText();
        if (primary != null && !primary.isEmpty()) {
            nameFont.draw(batch, primary, nearest.getX() - 40f, y);
            y += 12f;
        }
        if (nearest.hasSecondaryInteract()) {
            String secondary = nearest.getSecondaryPromptText();
            if (secondary != null && !secondary.isEmpty()) {
                nameFont.setColor(0.7f, 0.9f, 1f, 1f);
                nameFont.draw(batch, secondary, nearest.getX() - 40f, y);
            }
        }
        nameFont.setColor(1f, 1f, 1f, 1f);
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
            } else if (e instanceof CraftingStation) {
                ((CraftingStation) e).setInteractionTargeted(false);
            } else if (e instanceof Enemy) {
                ((Enemy) e).setInteractionHighlighted(false);
            }
        }
        highlightedLootCorpse = null;

        boolean uiBlocks = player.isDead()
            || inventoryWindow.isVisible()
            || equipmentWindow.isVisible()
            || craftingWindow.isVisible()
            || shopWindow.isVisible()
            || dialogueWindow.isVisible()
            || lootWindow.isVisible();

        if (!uiBlocks) {
            Interactable nearest = findNearestInteractable();
            if (nearest instanceof OreNode) {
                ((OreNode) nearest).setInteractionTargeted(true);
            } else if (nearest instanceof CraftingStation) {
                ((CraftingStation) nearest).setInteractionTargeted(true);
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
            if (pauseMenu != null) {
                pauseMenu.onResize(width, height);
            }
            if (skillBarUI != null) {
                skillBarUI.placeBottomCenter(width, height);
            }
            if (skillMenuUI != null && skillMenuUI.isVisible()) {
                skillMenuUI.clampToStage(width, height);
            }
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
            MusicSettings.unbind(gameplayMusic);
            gameplayMusic.dispose();
            gameplayMusic = null;
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
        if (nameFont != null) nameFont.dispose();
        if (skillVfxFactory != null) {
            skillVfxFactory.dispose();
            skillVfxFactory = null;
        }
        skillEffects.clear();
        if (proceduralMap) {
            DungeonMapAdapter.disposeProceduralResources(map);
        } else if (inTavern && map != null) {
            map.dispose();
        }
        if (mapRenderer != null) mapRenderer.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (worldMapUI != null) worldMapUI.dispose();
    }

    private LevelData loadOverworldLevelData() {
        if (GameConfig.USE_PROCEDURAL_DUNGEON) {
            this.dungeonMap = DungeonGenerator.generate(DungeonGenerationConfig.defaults());
            return DungeonMapAdapter.toLevelData(this.dungeonMap, game.getAssetManager());
        }
        return LevelLoader.load(game.getAssetManager(), LevelConstants.MAP_PATH);
    }
}
