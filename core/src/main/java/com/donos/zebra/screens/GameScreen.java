package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input; 
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import com.donos.zebra.entities.MentorNpc;
import com.donos.zebra.entities.Orc;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.DamageText;
import com.donos.zebra.util.HealthBarRenderer;
import com.donos.zebra.world.CameraController;
import com.donos.zebra.world.DungeonMapAdapter;
import com.donos.zebra.world.LevelData;
import com.donos.zebra.world.LevelLoader;
import com.donos.zebra.world.LevelConstants;
import com.donos.zebra.world.dungeon.DungeonGenerator;
import com.donos.zebra.world.dungeon.DungeonMap;
import com.badlogic.gdx.audio.Music;
import com.donos.zebra.items.ItemStack;
import com.donos.zebra.ui.DialogueUI;
import com.donos.zebra.ui.InventoryUI;
import com.donos.zebra.ui.LootUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        // 1. INICIALIZA O PALCO PRIMEIRO: Sem isso, dá o erro de uiStage is null!
        uiStage = new Stage(new com.badlogic.gdx.utils.viewport.ScreenViewport());

        // 2. Cria a Skin passando a fonte válida
        uiSkin = InventoryUI.createDefaultSkin(damageFont);

        // 3. Instancia a janela de Inventário usando a Skin
        inventoryWindow = new InventoryUI(player.getInventory(), uiSkin, game.getAssetManager());
        
        // 4. Aplica o posicionamento perfeito usando topLeft
        inventoryWindow.setPosition(20, Gdx.graphics.getHeight() / 2f, com.badlogic.gdx.utils.Align.topLeft);
        uiStage.addActor(inventoryWindow);

        // 5. Configura o Multiplexer com prioridade para o palco da UI
        com.badlogic.gdx.InputMultiplexer multiplexer = new com.badlogic.gdx.InputMultiplexer();
        multiplexer.addProcessor(uiStage); 
        Gdx.input.setInputProcessor(multiplexer);

        // 6. Cria as outras janelas de interface gráfica
        lootWindow = new LootUI(uiSkin, game.getAssetManager());
        lootWindow.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f + 100, Align.center);
        uiStage.addActor(lootWindow);

        dialogueWindow = new DialogueUI(uiSkin);
        dialogueWindow.setSize(400, 80);
        dialogueWindow.setPosition(Gdx.graphics.getWidth() / 2f, 50, Align.bottom);
        uiStage.addActor(dialogueWindow);

        dialogueWindow = new DialogueUI(uiSkin);
        dialogueWindow.setSize(400, 80);
        dialogueWindow.setPosition(Gdx.graphics.getWidth() / 2f, 50, Align.bottom);
        uiStage.addActor(dialogueWindow);

        java.util.Map<String, com.badlogic.gdx.graphics.g2d.Animation<com.badlogic.gdx.graphics.g2d.TextureRegion>[]> mentorAnims = 
        com.donos.zebra.entities.MentorAnimationLoader.loadAnimations(game.getAssetManager());
        float mentorSpawnX;
        float mentorSpawnY;

        if (levelData.hasMentor) {
            // Posição exata vinda do Tiled!
            mentorSpawnX = levelData.mentorX;
            mentorSpawnY = levelData.mentorY;
        } else {
            // Fallback caso você carregue algum mapa gerado proceduralmente que não possua o objeto
            mentorSpawnX = levelData.spawnX + 40f;
            mentorSpawnY = levelData.spawnY + 10f;
        }

        MentorNpc mentor = new MentorNpc(mentorSpawnX, mentorSpawnY, dialogueWindow, mentorAnims);
        interactables.add(mentor);
        entities.add(mentor);
        
        OrthographicCamera camera = new OrthographicCamera();
        applyCameraViewport(camera);
        cameraController = new CameraController(camera);

        Map<String, com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>[]> orcAnims = 
            com.donos.zebra.entities.OrcAnimationLoader.loadAnimations(game.getAssetManager());
            
        if (proceduralMap && dungeonMap != null && dungeonMap.getRooms() != null) {
            int tileSize = dungeonMap.getTileSize();

            for (com.donos.zebra.world.dungeon.Room sala : dungeonMap.getRooms()) {
                float salaCentroX = sala.getCenterX() * tileSize + tileSize / 2f;
                float salaCentroY = sala.getCenterY() * tileSize + tileSize / 2f;

                float distanciaDoSpawn = Vector2.dst(initialSpawnX, initialSpawnY, salaCentroX, salaCentroY);
                if (distanciaDoSpawn > 48f) { 
                    entities.add(new Orc(salaCentroX, salaCentroY, orcAnims));
                }
            }
        } else {
            entities.add(new Orc(initialSpawnX + 60f, initialSpawnY + 60f, orcAnims));
            entities.add(new Orc(initialSpawnX + 120f, initialSpawnY - 40f, orcAnims));
        }

        gameplayMusic = Gdx.audio.newMusic(Gdx.files.internal("track5.wav"));
        gameplayMusic.setLooping(true);
        gameplayMusic.setVolume(0.3f);
        gameplayMusic.play();
    }

    @Override
    public void render(float delta) {
        clearScreen(0, 0, 0, 1);

        damageFont.getData().setScale(0.8f);

        // Processa as entradas das janelas de UI gráficas
        handleItemSystemInputs();

        player.update(delta, collisionPolygons);

        if (!player.isInteracting() && player.getCurrentAnimationKey().equals(com.donos.zebra.entities.AnimationConstants.ANIM_ATTACK) && Gdx.input.justTouched()) { 
            float attackRange = 24f;
            float attackDamage = 10f;
            
            for (int i = entities.size() - 1; i >= 0; i--) {
                Entity ent = entities.get(i);
                if (ent instanceof Enemy && !ent.isDead()) {
                    float distance = Vector2.dst(player.getX(), player.getY(), ent.getX(), ent.getY());
                    if (distance <= attackRange) {
                        ent.takeDamage(attackDamage);
                        damageTexts.add(new DamageText(ent.getX(), ent.getY() + 15f, "-" + (int)attackDamage, Color.RED));
                    }
                }
            }
        }

        for (int i = damageTexts.size() - 1; i >= 0; i--) {
            DamageText dt = damageTexts.get(i);
            dt.update(delta);
            if (dt.lifetime <= 0) {
                damageTexts.remove(i);
            }
        }

        float playerOldHealth = player.getCurrentHealth();

        for (int i = entities.size() - 1; i >= 0; i--) {
            Entity ent = entities.get(i);
            
            if (ent.isDead()) {
                if (ent instanceof Orc) {
                    ((Orc) ent).updateEnemy(player, delta);
                }
                continue;
            }

            if (ent instanceof Orc) {
                ((Orc) ent).updateEnemy(player, delta);
                
                if (player.getCurrentHealth() < playerOldHealth) {
                    float damageTaken = playerOldHealth - player.getCurrentHealth();
                    damageTexts.add(new DamageText(player.getX(), player.getY() + 15f, "-" + (int)damageTaken, Color.ORANGE));
                    playerOldHealth = player.getCurrentHealth();
                }
            } else if (!(ent instanceof Player)) {
                ent.update(delta);
            }
        }

        cameraController.follow(player.getX(), player.getY());
        mapRenderer.setView(cameraController.getCamera());
        mapRenderer.render();

        // --- RENDERIZADO DE SPRITES & TEXTOS (MUNDO) ---
        batch.setProjectionMatrix(cameraController.getCamera().combined);
        beginBatch();
        
        entities.sort((e1, e2) -> Float.compare(e2.getY(), e1.getY()));
        
        for (Entity entity : entities) {
            entity.render(batch);
        }
        
        for (DamageText dt : damageTexts) {
            damageFont.setColor(dt.color); 
            damageFont.draw(batch, dt.text, dt.x - 5f, dt.y);
        }
        
        endBatch();

        // --- RENDERIZADO DAS BARRAS DE VIDA (ShapeRenderer) ---
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (Entity ent : entities) {
            if (ent instanceof Orc && !ent.isDead()) {
                Orc orc = (Orc) ent;
                HealthBarRenderer.draw(shapeRenderer, orc.getX(), orc.getY() + 14f, 20f, 4f, orc.getCurrentHealth(), orc.getMaxHealth());
            } else if (ent instanceof Player) {
                Player p = (Player) ent;
                HealthBarRenderer.draw(shapeRenderer, p.getX(), p.getY() + 16f, 20f, 4f, p.getCurrentHealth(), p.getMaxHealth());
            }
        }
        shapeRenderer.end();

        // --- RENDERIZADO DE ELEMENTOS VISUAIS DO SCENE2D (FIXO NA TELA) ---
        uiStage.act(delta); 
        uiStage.draw();     

        if (DEBUG_COLLISION) {
            renderDebugCollision();
        }

        if (player.isDead()) {
            drawGameOverScreen();
        }
    }

    private void handleItemSystemInputs() {
        if (player.isDead()) return;

        // --- SE ESTIVER EM DIÁLOGO, TRAVA OUTRAS ENTRADAS ---
        if (dialogueWindow.isVisible()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                dialogueWindow.hideDialogue();
                player.setInteracting(false);
                activeInteractionTarget = null;
            }
            return; // Impede abrir inventário ou atacar enquanto fala
        }

        // 1. Tecla I: Inverte visibilidade da Janela de Inventário Gráfica
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            if (lootWindow.isVisible()) return; 
            
            boolean isVisible = !inventoryWindow.isVisible();
            inventoryWindow.setVisible(isVisible);
            if (isVisible) inventoryWindow.refresh(); 
        }

        // 2. Tecla E: Interação Geral (Corpos ou NPCs)
        if (!lootWindow.isVisible() && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            
            // Primeiro tenta interagir com NPCs/Objetos do cenário
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
                inventoryWindow.refresh(); // Sincroniza a UI imediatamente após ganhar o item!
                player.setInteracting(true); // Trava movimentação/ataque usando o boolean que você já tem
                inventoryWindow.setVisible(false);
                return;
            }
            // Se não achou NPC, procura por corpos de inimigos (seu código original de loot)
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

        // 3. Tecla SPACE: Coleta tudo da janela gráfica de Loot
        if (lootWindow.isVisible() && activeLootTarget != null && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            for (ItemStack stack : activeLootTarget.getLootTable()) {
                player.getInventory().addItem(stack.getDefinition(), stack.getQuantity());
            }

            inventoryWindow.refresh(); // Sincroniza a UI imediatamente após ganhar o item!
            activeLootTarget.clearLoot();
            lootWindow.setVisible(false);
            activeLootTarget = null;
            player.setInteracting(false);
        }
    }

    private void drawGameOverScreen() {
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.75f);
        
        float camX = cameraController.getCamera().position.x;
        float camY = cameraController.getCamera().position.y;
        float width = cameraController.getCamera().viewportWidth;
        float height = cameraController.getCamera().viewportHeight;
        
        shapeRenderer.rect(camX - width / 2, camY - height / 2, width, height);
        shapeRenderer.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        batch.setProjectionMatrix(cameraController.getCamera().combined);
        beginBatch();
        
        damageFont.getData().setScale(1.5f);
        damageFont.setColor(Color.RED);
        damageFont.draw(batch, "GAME OVER", camX - 50f, camY + 30f);
        
        damageFont.setColor(Color.WHITE);
        damageFont.getData().setScale(0.7f);
        damageFont.draw(batch, "Pressione [ R ] para Renascer", camX - 70f, camY - 10f);
        damageFont.draw(batch, "Pressione [ ESC ] para Sair", camX - 62f, camY - 30f);
        
        endBatch();

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            player.revive(initialSpawnX, initialSpawnY);
            player.update(0, collisionPolygons); 
            lootWindow.setVisible(false);
            inventoryWindow.setVisible(false);
            activeLootTarget = null;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
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
        
        // Atualiza a posição mantendo a mesma ancoragem perfeita ao redimensionar a janela
        if (inventoryWindow != null) {
            inventoryWindow.setPosition(20, height / 2f, com.badlogic.gdx.utils.Align.topLeft);
        }
        
        if (lootWindow != null) {
            lootWindow.setPosition(width / 2f, height / 2f + 100, com.badlogic.gdx.utils.Align.center);
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