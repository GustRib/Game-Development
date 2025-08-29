package com.donos.zebra;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.donos.zebra.screens.MainMenuScreen;

public class MainGame extends Game {
    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        // Define a primeira tela do jogo
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
    }
}
