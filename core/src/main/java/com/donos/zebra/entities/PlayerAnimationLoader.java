package com.donos.zebra.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.donos.zebra.util.SpriteSheetLoader;

import java.util.HashMap;
import java.util.Map;

public class PlayerAnimationLoader {

    private static final int PLACEHOLDER_FRAME_SIZE = 64;

    private static Map<String, Animation<TextureRegion>[]> animations;
    private static Map<String, Texture> sheets;

    public static Map<String, Animation<TextureRegion>[]> loadAnimations() {
        if (animations != null) {
            return animations;
        }

        animations = new HashMap<>();
        sheets = new HashMap<>();
        loadAllAnimations(null);
        return animations;
    }

    public static void queueAssets(AssetManager assetManager) {
        assetManager.load(AnimationConstants.IDLE_SHEET_PATH, Texture.class);
        assetManager.load(AnimationConstants.WALK_SHEET_PATH, Texture.class);
        assetManager.load(AnimationConstants.RUN_SHEET_PATH, Texture.class);
        assetManager.load(AnimationConstants.ATTACK_SHEET_PATH, Texture.class);
    }

    public static Map<String, Animation<TextureRegion>[]> loadAnimations(AssetManager assetManager) {
        if (animations != null) {
            return animations;
        }

        animations = new HashMap<>();
        sheets = new HashMap<>();
        loadAllAnimations(assetManager);
        return animations;
    }

    private static void loadAllAnimations(AssetManager assetManager) {
        animations.put(AnimationConstants.ANIM_IDLE,
            loadDirectional(assetManager, AnimationConstants.IDLE_SHEET_PATH,
                AnimationConstants.IDLE_FRAME_DURATION, Animation.PlayMode.LOOP));
        animations.put(AnimationConstants.ANIM_WALK,
            loadDirectional(assetManager, AnimationConstants.WALK_SHEET_PATH,
                AnimationConstants.WALK_FRAME_DURATION, Animation.PlayMode.LOOP));
        animations.put(AnimationConstants.ANIM_RUN,
            loadDirectional(assetManager, AnimationConstants.RUN_SHEET_PATH,
                AnimationConstants.RUN_FRAME_DURATION, Animation.PlayMode.LOOP));
        animations.put(AnimationConstants.ANIM_ATTACK,
            loadDirectional(assetManager, AnimationConstants.ATTACK_SHEET_PATH,
                AnimationConstants.ATTACK_FRAME_DURATION, Animation.PlayMode.NORMAL));
    }

    private static Animation<TextureRegion>[] loadDirectional(AssetManager assetManager, String path,
                                                              float frameDuration, Animation.PlayMode mode) {
        Texture sheet = loadSheet(path, assetManager);
        int rows = AnimationConstants.DIRECTION_ROWS;
        int cols = detectFrameColumns(sheet, rows);
        return buildDirectionalAnimations(sheet, cols, rows, frameDuration, mode);
    }

    private static Texture loadSheet(String path, AssetManager assetManager) {
        if (Gdx.files.internal(path).exists()) {
            Texture sheet = assetManager != null
                ? assetManager.get(path, Texture.class)
                : new Texture(Gdx.files.internal(path));
            sheets.put(path, sheet);
            return sheet;
        }

        Gdx.app.error("PlayerAnimationLoader", "Missing sprite sheet: " + path + ". Using placeholder.");
        int cols = 6;
        Texture sheet = createPlaceholderSheet(cols, AnimationConstants.DIRECTION_ROWS);
        sheets.put(path, sheet);
        return sheet;
    }

    private static int detectFrameColumns(Texture sheet, int rows) {
        int frameHeight = sheet.getHeight() / rows;
        int frameWidth = frameHeight;
        return sheet.getWidth() / frameWidth;
    }

    private static Texture createPlaceholderSheet(int cols, int rows) {
        int width = cols * PLACEHOLDER_FRAME_SIZE;
        int height = rows * PLACEHOLDER_FRAME_SIZE;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                float shade = 0.35f + (row * 0.1f) + (col * 0.02f);
                pixmap.setColor(shade, 0.55f, 0.95f, 1f);
                pixmap.fillRectangle(
                    col * PLACEHOLDER_FRAME_SIZE,
                    row * PLACEHOLDER_FRAME_SIZE,
                    PLACEHOLDER_FRAME_SIZE,
                    PLACEHOLDER_FRAME_SIZE
                );
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    private static Animation<TextureRegion>[] buildDirectionalAnimations(Texture sheet, int cols, int rows,
                                                                         float frameDuration, Animation.PlayMode mode) {
        TextureRegion[][] tmp = SpriteSheetLoader.split(sheet, cols, rows);

        @SuppressWarnings("unchecked")
        Animation<TextureRegion>[] anims = new Animation[rows];

        for (int r = 0; r < rows; r++) {
            TextureRegion[] frames = new TextureRegion[cols];
            for (int c = 0; c < cols; c++) {
                frames[c] = tmp[r][c];
            }
            anims[r] = new Animation<>(frameDuration, frames);
            anims[r].setPlayMode(mode);
        }

        return anims;
    }

    public static void dispose() {
        if (sheets != null) {
            for (Texture t : sheets.values()) {
                t.dispose();
            }
            sheets = null;
        }
        animations = null;
    }
}
