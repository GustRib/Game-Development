package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.donos.zebra.MainGame;

public class LoadingScreen extends AbstractScreen {

    private final MainGame game;
    private float timer = 0;
    private BitmapFont font;

    public LoadingScreen(MainGame game) {
        super(game.batch);
        this.game = game;
        this.font = new BitmapFont(); // fonte padrão do libGDX
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "Loading...", Gdx.graphics.getWidth() / 2f - 40, Gdx.graphics.getHeight() / 2f);
        batch.end();

        timer += delta;
        if (timer > 2) {
            game.setScreen(new GameScreen(game));
        }
    }
}
