package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.donos.zebra.MainGame;
import com.donos.zebra.config.GameConfig;
import com.donos.zebra.entities.MentorAnimationLoader;
import com.donos.zebra.entities.OrcAnimationLoader;
import com.donos.zebra.entities.PlayerAnimationLoader;
import com.donos.zebra.world.LevelConstants;
import com.donos.zebra.world.LevelLoader;

public class LoadingScreen extends AbstractScreen {

    private final MainGame game;
    private boolean assetsQueued = false;
    private boolean transitionStarted = false;
    private BitmapFont font;

    public LoadingScreen(MainGame game) {
        super(game.batch);
        this.game = game;
        this.font = new BitmapFont();
    }

    @Override
    public void show() {
        if (!assetsQueued) {
            queueGameplayAssets();
            assetsQueued = true;
        }
    }

    private void queueGameplayAssets() {
        queueGameplayAssets(game.getAssetManager(), GameConfig.USE_PROCEDURAL_DUNGEON);
    }

    /**
     * Single owner of gameplay AssetManager queues. Used by LoadingScreen and tests.
     */
    public static void queueGameplayAssets(com.badlogic.gdx.assets.AssetManager assetManager,
                                           boolean proceduralDungeon) {
        assetManager.load("items/copper_ore.png", Texture.class);
        assetManager.load("items/stone_pickaxe.png", Texture.class);

        MentorAnimationLoader.queueAssets(assetManager);
        PlayerAnimationLoader.queueAssets(assetManager);
        OrcAnimationLoader.queueAssets(assetManager);

        if (proceduralDungeon) {
            LevelLoader.queueTilesetReference(assetManager, LevelConstants.MAP_PATH);
        } else {
            assetManager.load(LevelConstants.MAP_PATH, com.badlogic.gdx.maps.tiled.TiledMap.class);
        }
    }

    @Override
    public void render(float delta) {
        clearScreen(0, 0, 0, 1);

        beginBatch();
        font.draw(batch, "Loading...", Gdx.graphics.getWidth() / 2f - 40, Gdx.graphics.getHeight() / 2f);
        endBatch();

        if (!game.getAssetManager().isFinished()) {
            game.getAssetManager().update();
            return;
        }

        if (!transitionStarted) {
            transitionStarted = true;
            game.setScreen(new GameScreen(game));
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
