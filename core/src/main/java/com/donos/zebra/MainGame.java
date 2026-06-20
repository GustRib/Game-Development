package com.donos.zebra;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.donos.zebra.entities.PlayerAnimationLoader;
import com.donos.zebra.entities.OrcAnimationLoader;
import com.donos.zebra.screens.MainMenuScreen;

public class MainGame extends Game {
    public SpriteBatch batch;
    private AssetManager assetManager;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();

        assetManager.setLoader(
            TiledMap.class,
            new TmxMapLoader(new InternalFileHandleResolver())
        );

        // Enfileira os assets corretos
        PlayerAnimationLoader.queueAssets(assetManager);
        OrcAnimationLoader.queueAssets(assetManager); // Agora vai enfileirar apenas o Orc.png
        
        assetManager.finishLoading(); 

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
        
        // Descarte e limpeza de memória estática dos gerenciadores de animação
        PlayerAnimationLoader.dispose();
        OrcAnimationLoader.dispose();
        
        if (assetManager != null) {
            assetManager.dispose();
            assetManager = null;
        }
        batch.dispose();
    }
}