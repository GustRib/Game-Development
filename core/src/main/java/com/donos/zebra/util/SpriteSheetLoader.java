package com.donos.zebra.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public final class SpriteSheetLoader {

    private SpriteSheetLoader() {
    }

    public static TextureRegion[][] split(String path, int cols, int rows) {
        Texture texture = new Texture(path);
        return split(texture, cols, rows);
    }

    public static TextureRegion[][] split(Texture texture, int cols, int rows) {
        return TextureRegion.split(texture, texture.getWidth() / cols, texture.getHeight() / rows);
    }
}
