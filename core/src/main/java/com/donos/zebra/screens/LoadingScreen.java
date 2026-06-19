package com.donos.zebra.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.donos.zebra.MainGame;
import com.donos.zebra.config.GameConfig;
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
            if (GameConfig.USE_PROCEDURAL_DUNGEON) {
                PlayerAnimationLoader.queueAssets(game.getAssetManager());
                LevelLoader.queueTilesetReference(game.getAssetManager(), LevelConstants.MAP_PATH);
            } else {
                LevelLoader.queueAssets(game.getAssetManager(), LevelConstants.MAP_PATH);
            }
            assetsQueued = true;
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
