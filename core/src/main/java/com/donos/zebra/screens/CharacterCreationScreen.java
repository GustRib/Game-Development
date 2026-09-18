package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.donos.zebra.MainGame;
import com.donos.zebra.entities.AnimationConstants;
import com.donos.zebra.entities.PlayerAnimationLoader;
import com.donos.zebra.save.CharacterNameValidator;
import com.donos.zebra.ui.InventoryUI;

import java.util.Map;

/**
 * Name entry + live player idle preview before intro.
 */
public class CharacterCreationScreen extends AbstractScreen {

    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 600f;

    private final MainGame game;

    private OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;
    private Skin skin;
    private BitmapFont font;
    private TextField nameField;
    private Label statusLabel;
    private boolean transitioning;

    private Map<String, Animation<TextureRegion>[]> animations;
    private float stateTime;
    private float previewX;
    private float previewY;

    public CharacterCreationScreen(MainGame game) {
        super(game.batch);
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
    }

    @Override
    public void show() {
        transitioning = false;
        stateTime = 0f;
        previewX = VIRTUAL_WIDTH / 2f;
        previewY = VIRTUAL_HEIGHT / 2f + 40f;

        animations = PlayerAnimationLoader.loadAnimations();

        font = new BitmapFont();
        font.getData().setScale(1.2f);
        skin = InventoryUI.createDefaultSkin(font);
        ensureStyles(skin);

        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        Table root = new Table();
        root.setFillParent(true);
        root.center().padTop(160);

        Label title = new Label("CRIAR PERSONAGEM", skin);
        title.setColor(new Color(0.95f, 0.85f, 0.4f, 1f));
        title.setAlignment(Align.center);
        root.add(title).padBottom(12).row();

        Label hint = new Label("Digite o nome do heroi", skin);
        hint.setColor(Color.LIGHT_GRAY);
        root.add(hint).padBottom(8).row();

        nameField = new TextField("", skin, "char-name");
        nameField.setMaxLength(CharacterNameValidator.MAX_LENGTH);
        nameField.setMessageText("Nome...");
        nameField.setAlignment(Align.center);
        root.add(nameField).width(280).height(36).padBottom(10).row();

        statusLabel = new Label("", skin);
        statusLabel.setColor(new Color(1f, 0.45f, 0.4f, 1f));
        statusLabel.setAlignment(Align.center);
        root.add(statusLabel).width(360).padBottom(12).row();

        Table buttons = new Table();
        buttons.add(actionButton("CRIAR PERSONAGEM", this::createCharacter)).width(200).height(36).padRight(12);
        buttons.add(actionButton("VOLTAR", this::goBack)).width(140).height(36);
        root.add(buttons).row();

        stage.addActor(root);
        stage.setKeyboardFocus(nameField);
    }

    private TextButton actionButton(String text, Runnable action) {
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

    private void createCharacter() {
        String normalized = CharacterNameValidator.validateAndNormalize(nameField.getText());
        if (normalized == null) {
            statusLabel.setText("Nome invalido (1-" + CharacterNameValidator.MAX_LENGTH
                + " letras/numeros).");
            return;
        }
        transitioning = true;
        game.setScreen(new IntroStoryScreen(game, normalized));
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
        clearScreen(0.05f, 0.05f, 0.08f, 1f);
        stateTime += delta;
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        beginBatch();
        drawPlayerPreview();
        endBatch();

        stage.act(delta);
        stage.draw();

        if (!transitioning && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            goBack();
        } else if (!transitioning && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            createCharacter();
        }
    }

    private void drawPlayerPreview() {
        if (animations == null) {
            return;
        }
        Animation<TextureRegion>[] idle = animations.get(AnimationConstants.ANIM_IDLE);
        if (idle == null || idle.length == 0 || idle[0] == null) {
            return;
        }
        TextureRegion frame = idle[0].getKeyFrame(stateTime, true);
        float w = frame.getRegionWidth() * 1.5f;
        float h = frame.getRegionHeight() * 1.5f;
        batch.draw(frame, previewX - w / 2f, previewY - h / 2f, w, h);
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

    private static void ensureStyles(Skin skin) {
        if (!skin.has("pause-menu", TextButton.TextButtonStyle.class)) {
            TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
            style.font = skin.getFont("default");
            style.fontColor = Color.WHITE;
            style.up = solid(new Color(0.22f, 0.22f, 0.28f, 1f));
            style.over = solid(new Color(0.35f, 0.32f, 0.2f, 1f));
            style.down = solid(new Color(0.45f, 0.38f, 0.18f, 1f));
            skin.add("pause-menu", style);
        }
        if (!skin.has("char-name", TextField.TextFieldStyle.class)) {
            TextField.TextFieldStyle style = new TextField.TextFieldStyle();
            style.font = skin.getFont("default");
            style.fontColor = Color.WHITE;
            style.background = solid(new Color(0.12f, 0.12f, 0.16f, 1f));
            style.cursor = solid(new Color(0.9f, 0.8f, 0.35f, 1f));
            style.selection = solid(new Color(0.35f, 0.32f, 0.2f, 0.7f));
            skin.add("char-name", style);
        }
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
