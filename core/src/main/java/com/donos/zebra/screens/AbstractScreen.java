package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public abstract class AbstractScreen implements Screen {

    protected final SpriteBatch batch;

    public AbstractScreen(SpriteBatch batch) {
        this.batch = batch;
    }

    protected void clearScreen(float r, float g, float b, float a) {
        Gdx.gl.glClearColor(r, g, b, a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    protected void beginBatch() {
        batch.begin();
    }

    protected void endBatch() {
        batch.end();
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
