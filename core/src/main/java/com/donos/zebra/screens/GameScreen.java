package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameScreen extends AbstractScreen {

    private static final boolean DEBUG_COLLISION = false;
    private static final float UNIT_SCALE = 1f;
    private static final float CAMERA_ZOOM = 2.5f;

    private final MainGame game;
    private Player player;
    private final List<Entity> entities = new ArrayList<>();
    
    // Lista para controlar os textos de dano na tela
    private final List<DamageText> damageTexts = new ArrayList<>();
    private BitmapFont damageFont;

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
        damageTexts.clear();

        // Inicializa a fonte padrão e o ShapeRenderer
        damageFont = new BitmapFont(); 
        damageFont.getData().setScale(0.6f); // Deixa a fonte menorzinha para combinar com a escala pixel art
        shapeRenderer = new ShapeRenderer();

        OrthographicCamera camera = new OrthographicCamera();
        applyCameraViewport(camera);
        cameraController = new CameraController(camera);

        Map<String, com.badlogic.gdx.graphics.g2d.Animation<TextureRegion>[]> orcAnims = 
            com.donos.zebra.entities.OrcAnimationLoader.loadAnimations(game.getAssetManager());
            
        Orc testOrc = new Orc(levelData.spawnX + 50f, levelData.spawnY + 50f, orcAnims);
        entities.add(testOrc);
    }

@Override
    public void render(float delta) {
        clearScreen(0, 0, 0, 1);

        damageFont.getData().setScale(0.8f);

        player.update(delta, collisionPolygons);

        // --- Combate & Geração de Números de Dano do Player ---
        if (player.getCurrentAnimationKey().equals(com.donos.zebra.entities.AnimationConstants.ANIM_ATTACK) && Gdx.input.justTouched()) { 
            float attackRange = 24f;
            float attackDamage = 10f;
            
            for (int i = entities.size() - 1; i >= 0; i--) {
                Entity ent = entities.get(i);
                if (ent instanceof Enemy && !ent.isDead()) {
                    float distance = com.badlogic.gdx.math.Vector2.dst(player.getX(), player.getY(), ent.getX(), ent.getY());
                    if (distance <= attackRange) {
                        ent.takeDamage(attackDamage);
                        
                        // O dano aplicado pelo jogador continua Vermelho (Color.RED)
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

        // ====================================================================
        // AQUI ESTÁ O BLOCO ENCAIXADO! (Substituiu o loop de atualização antigo)
        // ====================================================================
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
                
                // Se o player tomou dano nesta iteração do Orc, cria o texto flutuante Laranja em cima do player!
                if (player.getCurrentHealth() < playerOldHealth) {
                    float damageTaken = playerOldHealth - player.getCurrentHealth();
                    damageTexts.add(new DamageText(player.getX(), player.getY() + 15f, "-" + (int)damageTaken, Color.ORANGE));
                    playerOldHealth = player.getCurrentHealth(); // Atualiza a referência
                }
            } else if (!(ent instanceof Player)) {
                ent.update(delta);
            }
        }
        // ====================================================================

        cameraController.follow(player.getX(), player.getY());
        mapRenderer.setView(cameraController.getCamera());
        mapRenderer.render();

        // --- RENDERIZADO DE SPRITES & TEXTOS ---
        batch.setProjectionMatrix(cameraController.getCamera().combined);
        beginBatch();
        
        // Desenha os personagens
        for (Entity entity : entities) {
            entity.render(batch);
        }
        
        // Desenha os números de dano dinâmicos (Puxando a cor de cada texto)
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

        if (DEBUG_COLLISION) {
            renderDebugCollision();
        }

        if (player.isDead()) {
        drawGameOverScreen();
    }
    }

    private void drawGameOverScreen() {
        // 1. Escurecer o fundo do jogo (efeito de fade/overlay)
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(cameraController.getCamera().combined);
        shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0f, 0f, 0f, 0.75f); // Preto com 75% de opacidade
        
        // Pega a posição atual da câmera para cobrir a tela inteira onde quer que o player tenha morrido
        float camX = cameraController.getCamera().position.x;
        float camY = cameraController.getCamera().position.y;
        float width = cameraController.getCamera().viewportWidth;
        float height = cameraController.getCamera().viewportHeight;
        
        shapeRenderer.rect(camX - width / 2, camY - height / 2, width, height);
        shapeRenderer.end();
        Gdx.gl.glDisable(com.badlogic.gdx.graphics.GL20.GL_BLEND);

        // 2. Desenhar os Textos na Tela
        batch.setProjectionMatrix(cameraController.getCamera().combined);
        beginBatch(); // Usa o seu método auxiliar de abrir o batch
        
        // Configura a fonte para o aviso principal
        damageFont.setColor(com.badlogic.gdx.graphics.Color.RED);
        damageFont.getData().setScale(1.5f); // Aumenta o tamanho do texto para o Game Over
        damageFont.draw(batch, "GAME OVER", camX - 50f, camY + 30f);
        
        // Configura a fonte para as instruções dos botões
        damageFont.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        damageFont.getData().setScale(0.7f); // Diminui o tamanho para as opções
        damageFont.draw(batch, "Pressione [ R ] para Renascer", camX - 70f, camY - 10f);
        damageFont.draw(batch, "Pressione [ ESC ] para Sair", camX - 62f, camY - 30f);
        
        endBatch(); // Fecha o batch safely

        // 3. Lógica de Entrada de Teclado (Input)
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.R)) {
            // Renasce criando uma nova instância da GameScreen
            game.setScreen(new GameScreen(game));
        } else if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            // Fecha a aplicação com segurança
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
            DungeonMap dungeonMap = DungeonGenerator.generate(DungeonGenerationConfig.defaults());
            return DungeonMapAdapter.toLevelData(dungeonMap, game.getAssetManager());
        }
        return LevelLoader.load(game.getAssetManager(), LevelConstants.MAP_PATH);
    }
}