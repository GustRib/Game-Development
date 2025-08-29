package com.donos.zebra.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class AbstractScreen implements Screen {

    protected final SpriteBatch batch;

    public AbstractScreen(SpriteBatch batch) {
        this.batch = batch;
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
    }
}
