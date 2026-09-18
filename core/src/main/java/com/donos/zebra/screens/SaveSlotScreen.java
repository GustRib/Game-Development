package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.donos.zebra.MainGame;
import com.donos.zebra.save.GameSession;
import com.donos.zebra.save.SaveData;
import com.donos.zebra.save.SaveService;
import com.donos.zebra.ui.InventoryUI;
import com.donos.zebra.ui.SaveSlotsPanel;

/**
 * Continue flow: pick an occupied slot and load into gameplay.
 */
public class SaveSlotScreen extends AbstractScreen {

    public enum Mode {
        CONTINUE
    }

    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 600f;

    private final MainGame game;
    private final Mode mode;
    private final SaveService saveService = new SaveService();

    private OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;
    private Skin skin;
    private BitmapFont font;
    private boolean transitioning;

    public SaveSlotScreen(MainGame game, Mode mode) {
        super(game.batch);
        this.game = game;
        this.mode = mode;
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
    }

    @Override
    public void show() {
        transitioning = false;
        font = new BitmapFont();
        skin = InventoryUI.createDefaultSkin(font);
        ensureButtonStyle(skin);

        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        root.center();

        SaveSlotsPanel panel = new SaveSlotsPanel(skin, SaveSlotsPanel.Mode.LOAD, saveService);
        panel.setListener(new SaveSlotsPanel.Listener() {
            @Override
            public void onSlotChosen(int slotIndex) {
                loadSlot(slotIndex, panel);
            }

            @Override
            public void onBack() {
                goBack();
            }
        });
        root.add(panel);
        stage.addActor(root);
    }

    private void loadSlot(int slotIndex, SaveSlotsPanel panel) {
        if (transitioning) {
            return;
        }
        SaveData data = saveService.readSlot(slotIndex);
        if (data == null) {
            panel.setStatusMessage("Save invalido ou corrompido.");
            panel.refresh();
            return;
        }
        transitioning = true;
        GameSession session = GameSession.continueFrom(data, slotIndex);
        game.setScreen(new LoadingScreen(game, session));
    }

    private void goBack() {
        if (transitioning) {
            return;
        }
        transitioning = true;
        game.setScreen(new MainMenuScreen(game));
    }

    @Override
    public void render(float delta) {
        clearScreen(0.04f, 0.04f, 0.07f, 1f);
        camera.update();
        stage.act(delta);
        stage.draw();
        if (!transitioning && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            goBack();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (stage != null) {
            stage.dispose();
        }
        if (font != null) {
            font.dispose();
        }
        super.dispose();
    }

    private static void ensureButtonStyle(Skin skin) {
        if (skin.has("pause-menu", TextButton.TextButtonStyle.class)) {
            return;
        }
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = skin.getFont("default");
        style.fontColor = Color.WHITE;
        style.disabledFontColor = Color.GRAY;
        style.up = solid(new Color(0.22f, 0.22f, 0.28f, 1f));
        style.over = solid(new Color(0.35f, 0.32f, 0.2f, 1f));
        style.down = solid(new Color(0.45f, 0.38f, 0.18f, 1f));
        style.disabled = solid(new Color(0.15f, 0.15f, 0.18f, 1f));
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
