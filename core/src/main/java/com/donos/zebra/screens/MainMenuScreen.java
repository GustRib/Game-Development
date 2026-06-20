package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.donos.zebra.MainGame;
import com.donos.zebra.config.GameConfig;
import com.badlogic.gdx.audio.Music;

public class MainMenuScreen extends AbstractScreen {

    private final MainGame game;
    
    private BitmapFont titleFont;
    private BitmapFont subtitleFont;
    private Texture backgroundTexture;
    private GlyphLayout layout;
    private float stateTime = 0f;

    // --- ADICIONADOS PARA GERENCIAR A TELA CHEIA E REDIMENSIONAMENTO ---
    private OrthographicCamera camera;
    private Viewport viewport;
    
    // Defina a resolução de projeto que você quer como base (virtual)
    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 600f;

    private Music menuMusic;

    public MainMenuScreen(MainGame game) {
        super(game.batch);
        this.game = game;
        
        // Inicializa a câmera e o Viewport com a resolução virtual estável
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        
        // Garante que a câmera comece bem no centro do mundo virtual
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
    }

    @Override
    public void show() {
        layout = new GlyphLayout();
        backgroundTexture = new Texture(Gdx.files.internal("MainMenuBackground.png"));

        // --- INICIALIZAÇÃO DA MÚSICA DE FUNDO ---
        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("Track3.mp3"));
        menuMusic.setLooping(true); // Toca continuamente
        menuMusic.setVolume(0.4f);  // Define o volume (40%)
        menuMusic.play();           // Inicia a música

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Fonts/DungeonFont.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        
        parameter.size = 48; 
        parameter.color = new Color(0.9f, 0.75f, 0.2f, 1f); 
        parameter.borderWidth = 3.5f; 
        parameter.borderColor = Color.BLACK;
        titleFont = generator.generateFont(parameter);

        parameter.size = 18;
        parameter.color = Color.WHITE;
        parameter.borderWidth = 1.5f;
        parameter.borderColor = Color.BLACK;
        subtitleFont = generator.generateFont(parameter);

        generator.dispose(); 
    }

    @Override
    public void render(float delta) {
        clearScreen(0, 0, 0, 1);
        stateTime += delta;

        // 1. Atualiza a matriz da câmera e aplica no SpriteBatch antes de desenhar
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        beginBatch();

        // 2. Agora desenhamos usando SEMPRE os limites da resolução VIRTUAL fixa
        batch.draw(backgroundTexture, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        // 3. Desenha o Título centralizado na resolução estável
        layout.setText(titleFont, GameConfig.APP_TITLE);
        float titleX = (VIRTUAL_WIDTH - layout.width) / 2f;
        float titleY = (VIRTUAL_HEIGHT / 2f) + 100f; 
        titleFont.draw(batch, GameConfig.APP_TITLE, titleX, titleY);

        // 4. Efeito pulsante no texto auxiliar
        float alpha = 0.5f + (float) Math.sin(stateTime * 4.5f) * 0.5f;
        subtitleFont.setColor(1f, 1f, 1f, alpha);

        // 5. Desenha o Subtítulo centralizado na resolução estável
        String promptText = "Pressione [ ENTER ] para Iniciar";
        layout.setText(subtitleFont, promptText);
        float promptX = (VIRTUAL_WIDTH - layout.width) / 2f;
        float promptY = (VIRTUAL_HEIGHT / 2f) - 60f; 
        subtitleFont.draw(batch, promptText, promptX, promptY);

        endBatch();

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            // Interrompe a música antes de transicionar para a próxima tela
            if (menuMusic != null) {
                menuMusic.stop();
            }
            game.setScreen(new LoadingScreen(game));
        }
    }

    // --- ESSENCIAL: DIZ AO VIEWPORT COMO RECALCULAR QUANDO A JANELA ESTICAR ---
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        // Libera a música da memória para evitar memory leaks
        if (menuMusic != null) {
            menuMusic.dispose();
        }
        if (backgroundTexture != null) backgroundTexture.dispose();
        if (titleFont != null) titleFont.dispose();
        if (subtitleFont != null) subtitleFont.dispose();
        super.dispose();
    }
}