package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.donos.zebra.MainGame;

public class LoadingScreen extends AbstractScreen {

    private final MainGame game;
    private float timer = 0;

    public LoadingScreen(MainGame game) {
        super(game.batch);
        this.game = game;
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        // desenhar "Loading..."
        batch.end();

        timer += delta;
        if (timer > 2) { // adiciona um carregamento de 2s na tela
            game.setScreen(new GameScreen(game));
        }
    }
}
