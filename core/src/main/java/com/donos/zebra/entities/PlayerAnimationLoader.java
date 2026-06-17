package com.donos.zebra.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.donos.zebra.util.SpriteSheetLoader;

import java.util.HashMap;
import java.util.Map;

public class PlayerAnimationLoader {

    private static Map<String, Animation<TextureRegion>[]> animations;
    private static Map<String, Texture> sheets;

    public static Map<String, Animation<TextureRegion>[]> loadAnimations() {
        if (animations != null) {
            return animations;
        }

        animations = new HashMap<>();
        sheets = new HashMap<>();

        animations.put(AnimationConstants.ANIM_IDLE,
            loadDirectional(AnimationConstants.IDLE_SHEET_PATH, AnimationConstants.IDLE_COLS,
                AnimationConstants.DIRECTION_ROWS, AnimationConstants.IDLE_FRAME_DURATION, Animation.PlayMode.LOOP));
        animations.put(AnimationConstants.ANIM_WALK,
            loadDirectional(AnimationConstants.WALK_SHEET_PATH, AnimationConstants.WALK_COLS,
                AnimationConstants.DIRECTION_ROWS, AnimationConstants.WALK_FRAME_DURATION, Animation.PlayMode.LOOP));
        animations.put(AnimationConstants.ANIM_ATTACK,
            loadDirectional(AnimationConstants.ATTACK_SHEET_PATH, AnimationConstants.ATTACK_COLS,
                AnimationConstants.DIRECTION_ROWS, AnimationConstants.ATTACK_FRAME_DURATION, Animation.PlayMode.NORMAL));

        return animations;
    }

    public static void queueAssets(AssetManager assetManager) {
        assetManager.load(AnimationConstants.IDLE_SHEET_PATH, Texture.class);
        assetManager.load(AnimationConstants.WALK_SHEET_PATH, Texture.class);
        assetManager.load(AnimationConstants.ATTACK_SHEET_PATH, Texture.class);
    }

    public static Map<String, Animation<TextureRegion>[]> loadAnimations(AssetManager assetManager) {
        if (animations != null) {
            return animations;
        }

        animations = new HashMap<>();

        animations.put(AnimationConstants.ANIM_IDLE,
            loadDirectional(assetManager, AnimationConstants.IDLE_SHEET_PATH, AnimationConstants.IDLE_COLS,
                AnimationConstants.DIRECTION_ROWS, AnimationConstants.IDLE_FRAME_DURATION, Animation.PlayMode.LOOP));
        animations.put(AnimationConstants.ANIM_WALK,
            loadDirectional(assetManager, AnimationConstants.WALK_SHEET_PATH, AnimationConstants.WALK_COLS,
                AnimationConstants.DIRECTION_ROWS, AnimationConstants.WALK_FRAME_DURATION, Animation.PlayMode.LOOP));
        animations.put(AnimationConstants.ANIM_ATTACK,
            loadDirectional(assetManager, AnimationConstants.ATTACK_SHEET_PATH, AnimationConstants.ATTACK_COLS,
                AnimationConstants.DIRECTION_ROWS, AnimationConstants.ATTACK_FRAME_DURATION, Animation.PlayMode.NORMAL));

        return animations;
    }

    private static Animation<TextureRegion>[] loadDirectional(String path, int cols, int rows,
                                                              float frameDuration, Animation.PlayMode mode) {
        Texture sheet = new Texture(com.badlogic.gdx.Gdx.files.internal(path));
        sheets.put(path, sheet);
        return buildDirectionalAnimations(sheet, cols, rows, frameDuration, mode);
    }

    private static Animation<TextureRegion>[] loadDirectional(AssetManager assetManager, String path, int cols, int rows,
                                                              float frameDuration, Animation.PlayMode mode) {
        Texture sheet = assetManager.get(path, Texture.class);
        return buildDirectionalAnimations(sheet, cols, rows, frameDuration, mode);
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
