package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.donos.zebra.MainGame;
import com.donos.zebra.audio.MusicSettings;
import com.donos.zebra.config.GameConfig;
import com.donos.zebra.save.SaveService;
import com.donos.zebra.ui.InventoryUI;

/**
 * Game entry point: Start / Continue / Exit.
 */
public class MainMenuScreen extends AbstractScreen {

    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 600f;

    private final MainGame game;
    private final SaveService saveService = new SaveService();

    private OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;
    private Skin skin;
    private BitmapFont titleFont;
    private BitmapFont uiFont;
    private Texture backgroundTexture;
    private Music menuMusic;
    private Label statusLabel;
    private boolean transitioning;

    public MainMenuScreen(MainGame game) {
        super(game.batch);
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
    }

    @Override
    public void show() {
        transitioning = false;
        backgroundTexture = new Texture(Gdx.files.internal("MainMenuBackground.png"));

        menuMusic = Gdx.audio.newMusic(Gdx.files.internal("Track3.mp3"));
        menuMusic.setLooping(true);
        MusicSettings.bind(menuMusic);
        menuMusic.play();

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
        uiFont = generator.generateFont(parameter);
        generator.dispose();

        skin = InventoryUI.createDefaultSkin(uiFont);
        ensureMenuButtonStyle(skin);

        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        root.top().padTop(80);

        Label title = new Label(GameConfig.APP_TITLE, new Label.LabelStyle(titleFont, titleFont.getColor()));
        title.setAlignment(Align.center);
        root.add(title).padBottom(48).row();

        root.add(menuButton("INICIAR JOGO", this::startGame)).width(260).height(40).padBottom(10).row();
        root.add(menuButton("CONTINUAR", this::continueGame)).width(260).height(40).padBottom(10).row();
        root.add(menuButton("SAIR", this::exitGame)).width(260).height(40).padBottom(16).row();

        statusLabel = new Label("", skin);
        statusLabel.setColor(Color.LIGHT_GRAY);
        statusLabel.setAlignment(Align.center);
        root.add(statusLabel).width(400).row();

        stage.addActor(root);
    }

    private TextButton menuButton(String text, Runnable action) {
        TextButton btn = new TextButton(text, skin, "pause-menu");
        btn.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (!transitioning) {
                    action.run();
                }
            }
        });
        return btn;
    }

    private void startGame() {
        if (transitioning) {
            return;
        }
        transitioning = true;
        stopMusic();
        game.setScreen(new CharacterCreationScreen(game));
    }

    private void continueGame() {
        if (transitioning) {
            return;
        }
        if (!saveService.hasAnySave()) {
            statusLabel.setText("Nenhum save encontrado.");
            return;
        }
        transitioning = true;
        stopMusic();
        game.setScreen(new SaveSlotScreen(game, SaveSlotScreen.Mode.CONTINUE));
    }

    private void exitGame() {
        if (transitioning) {
            return;
        }
        transitioning = true;
        stopMusic();
        Gdx.app.exit();
    }

    private void stopMusic() {
        if (menuMusic != null) {
            menuMusic.stop();
        }
    }

    @Override
    public void render(float delta) {
        clearScreen(0, 0, 0, 1);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        beginBatch();
        batch.draw(backgroundTexture, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        endBatch();

        stage.act(delta);
        stage.draw();

        if (!transitioning && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            exitGame();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override
    public void dispose() {
        if (menuMusic != null) {
            MusicSettings.unbind(menuMusic);
            menuMusic.dispose();
            menuMusic = null;
        }
        if (backgroundTexture != null) {
            backgroundTexture.dispose();
        }
        if (titleFont != null) {
            titleFont.dispose();
        }
        if (uiFont != null) {
            uiFont.dispose();
        }
        if (stage != null) {
            stage.dispose();
        }
        super.dispose();
    }

    private static void ensureMenuButtonStyle(Skin skin) {
        if (skin.has("pause-menu", TextButton.TextButtonStyle.class)) {
            return;
        }
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = skin.getFont("default");
        style.fontColor = Color.WHITE;
        style.up = solid(new Color(0.22f, 0.22f, 0.28f, 1f));
        style.over = solid(new Color(0.35f, 0.32f, 0.2f, 1f));
        style.down = solid(new Color(0.45f, 0.38f, 0.18f, 1f));
        style.checked = solid(new Color(0.4f, 0.35f, 0.18f, 1f));
        skin.add("pause-menu", style);
    }

    private static Drawable solid(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }
}
