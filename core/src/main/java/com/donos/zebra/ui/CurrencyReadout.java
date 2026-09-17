package com.donos.zebra.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.donos.zebra.items.Wallet;

/**
 * Compact wallet readout with gold/silver coin icons (~1/4 of a 48px inventory icon = 12px).
 */
public final class CurrencyReadout extends Table {

    public static final float COIN_ICON_SIZE = 12f;

    private final Label goldLabel;
    private final Label silverLabel;

    public CurrencyReadout(Skin skin, AssetManager assetManager) {
        pad(2);
        left();

        Image goldIcon = coin(assetManager, "items/gold_coin.png");
        if (goldIcon != null) {
            add(goldIcon).size(COIN_ICON_SIZE, COIN_ICON_SIZE).padRight(3);
        }
        goldLabel = new Label("0", skin);
        goldLabel.setColor(new Color(0.95f, 0.85f, 0.4f, 1f));
        add(goldLabel).padRight(10);

        Image silverIcon = coin(assetManager, "items/silver_coin.png");
        if (silverIcon != null) {
            add(silverIcon).size(COIN_ICON_SIZE, COIN_ICON_SIZE).padRight(3);
        }
        silverLabel = new Label("0", skin);
        silverLabel.setColor(new Color(0.85f, 0.88f, 0.95f, 1f));
        add(silverLabel);
    }

    public void refresh(Wallet wallet) {
        if (wallet == null) {
            goldLabel.setText("0");
            silverLabel.setText("0");
            return;
        }
        goldLabel.setText(String.valueOf(wallet.getGold()));
        silverLabel.setText(String.valueOf(wallet.getSilverRemainder()));
    }

    private static Image coin(AssetManager assetManager, String path) {
        if (assetManager == null || !assetManager.isLoaded(path)) {
            return null;
        }
        return new Image(assetManager.get(path, Texture.class));
    }
}
