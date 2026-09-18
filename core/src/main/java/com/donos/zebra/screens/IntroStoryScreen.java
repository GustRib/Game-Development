package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.donos.zebra.MainGame;
import com.donos.zebra.save.GameSession;
import com.donos.zebra.save.IntroStoryController;
import com.donos.zebra.save.IntroStoryData;

/**
 * Short narrative beat before gameplay. Input is blocked until complete/skip.
 */
public class IntroStoryScreen extends AbstractScreen {

    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 600f;

    private final MainGame game;
    private final String characterName;
    private final IntroStoryController controller;

    private OrthographicCamera camera;
    private Viewport viewport;
    private BitmapFont font;
    private BitmapFont hintFont;
    private GlyphLayout layout;
    private boolean transitioning;

    public IntroStoryScreen(MainGame game, String characterName) {
        this(game, characterName, new IntroStoryData());
    }

    public IntroStoryScreen(MainGame game, String characterName, IntroStoryData storyData) {
        super(game.batch);
        this.game = game;
        this.characterName = characterName;
        this.controller = new IntroStoryController(storyData);
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
    }

    @Override
    public void show() {
        transitioning = false;
        font = new BitmapFont();
        font.getData().setScale(1.35f);
        hintFont = new BitmapFont();
        hintFont.getData().setScale(0.9f);
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        clearScreen(0.02f, 0.02f, 0.05f, 1f);

        if (!transitioning) {
            controller.update(delta);
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                || Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)
                || Gdx.input.justTouched()) {
                controller.skip();
            }
            if (controller.isComplete()) {
                beginGameplay();
                return;
            }
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        beginBatch();

        String line = controller.getCurrentLine();
        font.setColor(1f, 0.95f, 0.85f, controller.getLineAlpha());
        layout.setText(font, line);
        float x = (VIRTUAL_WIDTH - layout.width) / 2f;
        float y = VIRTUAL_HEIGHT / 2f + 20f;
        font.draw(batch, line, x, y);

        hintFont.setColor(0.7f, 0.7f, 0.75f, 0.85f);
        String hint = "Pressione ENTER / ESPACO para pular";
        layout.setText(hintFont, hint);
        hintFont.draw(batch, hint, (VIRTUAL_WIDTH - layout.width) / 2f, 80f);

        endBatch();
    }

    private void beginGameplay() {
        if (transitioning) {
            return;
        }
        transitioning = true;
        GameSession session = GameSession.newGame(characterName);
        game.setScreen(new LoadingScreen(game, session));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (font != null) {
            font.dispose();
        }
        if (hintFont != null) {
            hintFont.dispose();
        }
        super.dispose();
    }
}
