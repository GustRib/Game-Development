package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.donos.zebra.MainGame;
import com.donos.zebra.config.GameConfig;


public class MainMenuScreen extends AbstractScreen {

    private final MainGame game;
    private BitmapFont font;

    public MainMenuScreen(MainGame game) {
        super(game.batch);
        this.game = game;
    }

    @Override
    public void show() {
        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        clearScreen(0.2f, 0.2f, 0.3f, 1);

        beginBatch();
        font.draw(batch, GameConfig.APP_TITLE, 200, 400);
        font.draw(batch, "Press ENTER to Start", 200, 300);
        endBatch();

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            game.setScreen(new LoadingScreen(game));
        }
    }

    @Override
    public void dispose() {
        if (font != null) {
            font.dispose();
            font = null;
        }
        super.dispose();
    }
}
