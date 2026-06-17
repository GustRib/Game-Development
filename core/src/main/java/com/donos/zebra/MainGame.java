package com.donos.zebra;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.donos.zebra.entities.PlayerAnimationLoader;
import com.donos.zebra.screens.MainMenuScreen;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class MainGame extends Game {
    public SpriteBatch batch;
    private AssetManager assetManager;

    public void create() {
        batch = new SpriteBatch();

        assetManager = new AssetManager();

        assetManager.setLoader(
            TiledMap.class,
            new TmxMapLoader(new InternalFileHandleResolver())
        );

        setScreen(new MainMenuScreen(this));
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    @Override
    public void setScreen(Screen screen) {
        Screen current = getScreen();
        if (current != null) {
            current.dispose();
        }
        super.setScreen(screen);
    }

    @Override
    public void dispose() {
        Screen current = getScreen();
        if (current != null) {
            current.dispose();
        }
        super.dispose();
        PlayerAnimationLoader.dispose();
        if (assetManager != null) {
            assetManager.dispose();
            assetManager = null;
        }
        batch.dispose();
    }
}
