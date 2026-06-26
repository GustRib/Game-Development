package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input; // Adicionado para gerenciar as teclas
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2; // Adicionado para cálculo de distância
import com.badlogic.gdx.utils.Array;
import com.donos.zebra.MainGame;
import com.donos.zebra.config.DungeonGenerationConfig;
import com.donos.zebra.config.GameConfig;
import com.donos.zebra.entities.Enemy;
import com.donos.zebra.entities.Entity;
import com.donos.zebra.entities.Orc;
import com.donos.zebra.entities.Player;
import com.donos.zebra.entities.DamageText;
import com.donos.zebra.util.HealthBarRenderer;
import com.donos.zebra.world.CameraController;
import com.donos.zebra.world.DungeonMapAdapter;
import com.donos.zebra.world.LevelConstants;
import com.donos.zebra.world.LevelData;
import com.donos.zebra.world.LevelLoader;
import com.donos.zebra.world.dungeon.DungeonGenerator;
import com.donos.zebra.world.dungeon.DungeonMap;
import com.badlogic.gdx.audio.Music;
import com.donos.zebra.items.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameScreen extends AbstractScreen {

    private static final boolean DEBUG_COLLISION = false;
    private static final float UNIT_SCALE = 1f;
    private static final float CAMERA_ZOOM = 2.5f;
    private static final float INTERACTION_RANGE = 26f; // Distância limite para interagir com o corpo (tecla E)

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

    // CONTROLE DO SISTEMA DE INTERAÇÃO E LOOT ---
    private boolean showLootWindow = false;
    private boolean showInventoryWindow = false;
    private Enemy activeLootTarget = null;

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

                float distanciaDoSpawn = com.badlogic.gdx.math.Vector2.dst(initialSpawnX, initialSpawnY, salaCentroX, salaCentroY);
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

        handleItemSystemInputs();

        player.update(delta, collisionPolygons);

        // --- Combate & Geração de Números de Dano do Player ---
        // Adicionada a trava !player.isInteracting() para impedir ataques acidentais abrindo/fechando menus
        if (!player.isInteracting() && player.getCurrentAnimationKey().equals(com.donos.zebra.entities.AnimationConstants.ANIM_ATTACK) && Gdx.input.justTouched()) { 
            float attackRange = 24f;
            float attackDamage = 10f;
            
            for (int i = entities.size() - 1; i >= 0; i--) {
                Entity ent = entities.get(i);
                if (ent instanceof Enemy && !ent.isDead()) {
                    float distance = com.badlogic.gdx.math.Vector2.dst(player.getX(), player.getY(), ent.getX(), ent.getY());
                    if (distance <= attackRange) {
                        ent.takeDamage(attackDamage);
                        damageTexts.add(new DamageText(ent.getX(), ent.getY() + 15f, "-" + (int)attackDamage, Color.RED));
                    }
                }
            }
        }

        // Atualiza textos de dano e remove os antigos
        for (int i = damageTexts.size() - 1; i >= 0; i--) {
            DamageText dt = damageTexts.get(i);
            dt.update(delta);
            if (dt.lifetime <= 0) {
                damageTexts.remove(i);
            }
        }

        // --- Loop de Atualização de Entidades ---
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

        // --- RENDERIZADO DE SPRITES & TEXTOS ---
        batch.setProjectionMatrix(cameraController.getCamera().combined);
        beginBatch();
        
        // NOVO: Ordena a lista temporariamente por profundidade antes de desenhar
        entities.sort((e1, e2) -> Float.compare(e2.getY(), e1.getY()));
        
        // Desenha os personagens na ordem correta de perspectiva
        for (Entity entity : entities) {
            entity.render(batch);
        }
        
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

        // --- RENDERIZACAO DAS JANELAS TEXTUAIS DO SISTEMA DE ITENS ---
        renderItemSystemWindows();

        if (DEBUG_COLLISION) {
            renderDebugCollision();
        }

        if (player.isDead()) {
            drawGameOverScreen();
        }
    }

    /**
     * Processa as entradas de teclado específicas para o fluxo de loot e visualização do inventário.
     */
    private void handleItemSystemInputs() {
        if (player.isDead()) return;

        // 1. Tecla I: Abre/Fecha a janela de visualização do Inventário (Apenas se não estiver saqueando)
        if (!showLootWindow && Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            showInventoryWindow = !showInventoryWindow;
        }

        // 2. Tecla E: Inicia a interação por proximidade com corpos de monstros caídos
        if (!showLootWindow && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            Enemy closestCorpse = null;
            float minDistance = INTERACTION_RANGE;

            for (Entity ent : entities) {
                if (ent instanceof Enemy) {
                    Enemy enemy = (Enemy) ent;
                    // Verifica se o monstro está morto e ainda possui itens para saquear
                    if (enemy.hasLootAvailable()) {
                        float dist = Vector2.dst(player.getX(), player.getY(), enemy.getX(), enemy.getY());
                        if (dist <= minDistance) {
                            minDistance = dist;
                            closestCorpse = enemy;
                        }
                    }
                }
            }

            // Se encontrou um corpo elegível no raio de alcance, abre a janela textual de loot
            if (closestCorpse != null) {
                activeLootTarget = closestCorpse;
                showLootWindow = true;
                showInventoryWindow = false;       // Fecha a listagem do inventário se aberta
                player.setInteracting(true);       // Congela os inputs de movimento/ataque do herói
            }
        }

        // 3. Tecla SPACE: Realiza a transferência total de itens do corpo para o inventário do Player
        if (showLootWindow && activeLootTarget != null && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            // Varre o loot interno do monstro e injeta na única fonte de verdade do jogador
            for (ItemStack stack : activeLootTarget.getLootTable()) {
                player.getInventory().addItem(stack.getDefinition(), stack.getQuantity());
            }

            // Limpa o corpo completamente e impede novos saques
            activeLootTarget.clearLoot();
            
            // Fecha a janela de loot automaticamente e descongela o Player
            showLootWindow = false;
            activeLootTarget = null;
            player.setInteracting(false);
        }
    }

    /**
     * Desenha as sobreposições estritamente textuais na tela para validação do fluxo do jogo.
     */
    private void renderItemSystemWindows() {
        if (!showLootWindow && !showInventoryWindow) return;

        float camX = cameraController.getCamera().position.x;
        float camY = cameraController.getCamera().position.y;

        batch.setProjectionMatrix(cameraController.getCamera().combined);
        beginBatch();

        // --- JANELA DE LOOT TEXTUAL ---
        if (showLootWindow && activeLootTarget != null) {
            damageFont.setColor(Color.YELLOW);
            damageFont.getData().setScale(0.7f);
            damageFont.draw(batch, "=== CORPO DO ORC ===", camX - 55f, camY + 40f);

            float currentY = camY + 25f;
            damageFont.setColor(Color.WHITE);
            damageFont.getData().setScale(0.6f);
            
            for (ItemStack stack : activeLootTarget.getLootTable()) {
                damageFont.draw(batch, "- " + stack.getQuantity() + "x " + stack.getDefinition().getName(), camX - 45f, currentY);
                currentY -= 12f;
            }

            damageFont.setColor(Color.GREEN);
            damageFont.getData().setScale(0.55f);
            damageFont.draw(batch, "Pressione [ SPACE ] para Coletar Tudo", camX - 65f, camY - 20f);
        }

        // --- JANELA DE INVENTÁRIO TEXTUAL ---
        if (showInventoryWindow) {
            damageFont.setColor(Color.CYAN);
            damageFont.getData().setScale(0.7f);
            damageFont.draw(batch, "=== INVENTARIO (TEXTUAL) ===", camX - 65f, camY + 50f);

            float currentY = camY + 35f;
            damageFont.setColor(Color.WHITE);
            damageFont.getData().setScale(0.6f);

            ItemStack[] slots = player.getInventory().getSlots();
            boolean hasItems = false;

            for (int i = 0; i < slots.length; i++) {
                if (slots[i] != null) {
                    damageFont.draw(batch, "Slot [" + i + "]: " + slots[i].getQuantity() + "x " + slots[i].getDefinition().getName(), camX - 55f, currentY);
                    currentY -= 12f;
                    hasItems = true;
                }
            }

            if (!hasItems) {
                damageFont.setColor(Color.GRAY);
                damageFont.draw(batch, "( O inventario esta vazio )", camX - 50f, camY + 15f);
            }

            damageFont.setColor(Color.LIGHT_GRAY);
            damageFont.getData().setScale(0.55f);
            damageFont.draw(batch, "Pressione [ I ] para fechar", camX - 45f, camY - 40f);
        }

        endBatch();
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
        damageFont.setColor(com.badlogic.gdx.graphics.Color.RED);
        damageFont.draw(batch, "GAME OVER", camX - 50f, camY + 30f);
        
        damageFont.getData().setScale(0.7f);
        damageFont.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        damageFont.draw(batch, "Pressione [ R ] para Renascer", camX - 70f, camY - 10f);
        damageFont.draw(batch, "Pressione [ ESC ] para Sair", camX - 62f, camY - 30f);
        
        endBatch();

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
            player.revive(initialSpawnX, initialSpawnY);
            player.update(0, collisionPolygons); 
            // Garante reset das janelas de item ao ressurgir
            showLootWindow = false;
            showInventoryWindow = false;
            activeLootTarget = null;
        } else if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
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