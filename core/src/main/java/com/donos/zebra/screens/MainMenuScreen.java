package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.donos.zebra.MainGame;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;


public class MainMenuScreen extends AbstractScreen {

    private final MainGame game;
    private BitmapFont font;

    public MainMenuScreen(MainGame game) {
        super(game.batch);
        this.game = game;
    }

    @Override
    public void show() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("Fonts/DungeonFont.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 48;
        font = generator.generateFont(parameter);
        generator.dispose();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.3f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "Riders of Hive", 200, 400);
        font.draw(batch, "Press ENTER to Start", 200, 300);
        batch.end();

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            game.setScreen(new LoadingScreen(game));
        }
    }

    @Override
    public void dispose() {
        font.dispose();
    }
}

