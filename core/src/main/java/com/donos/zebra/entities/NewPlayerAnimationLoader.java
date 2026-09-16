package com.donos.zebra.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.HashMap;
import java.util.Map;

public class NewPlayerAnimationLoader {


    private static final int FRAME_SIZE = 64;

    private static final String LPC_PATH =
            "characters/newplayer_allanims.png";

    private static Map<String, Animation<TextureRegion>[]> animations;
    private static Texture sheet;

    public static void queueAssets(AssetManager assetManager) {
        assetManager.load(LPC_PATH, Texture.class);
    }

    public static Map<String, Animation<TextureRegion>[]> loadAnimations(
            AssetManager assetManager
    ) {

        if (animations != null) {
            return animations;
        }

        animations = new HashMap<>();

        sheet = assetManager.get(LPC_PATH, Texture.class);

        TextureRegion[][] frames =
                TextureRegion.split(sheet, FRAME_SIZE, FRAME_SIZE);

        animations.put(
                AnimationConstants.ANIM_IDLE,
                buildDirectional(
                        frames,
                        22,
                        23,
                        24,
                        25,
                        2,
                        0.5f,
                        Animation.PlayMode.LOOP
                )
        );

        animations.put(
                AnimationConstants.ANIM_WALK,
                buildDirectional(
                        frames,
                        8,
                        9,
                        10,
                        11,
                        9,
                        0.10f,
                        Animation.PlayMode.LOOP
                )
        );

        animations.put(
                AnimationConstants.ANIM_ATTACK,
                buildDirectional(
                        frames,
                        54,
                        55,
                        56,
                        57,
                        6,
                        0.08f,
                        Animation.PlayMode.NORMAL
                )
        );

        animations.put(
                "death",
                buildSingleRow(
                        frames,
                        20,
                        6,
                        0.15f,
                        Animation.PlayMode.NORMAL
                )
        );

        return animations;
    }

    @SuppressWarnings("unchecked")
    private static Animation<TextureRegion>[] buildDirectional(
            TextureRegion[][] frames,
            int upRow,
            int leftRow,
            int downRow,
            int rightRow,
            int frameCount,
            float duration,
            Animation.PlayMode playMode
    ) {

        Animation<TextureRegion>[] result =
                new Animation[4];

        result[Direction.UP.ordinal()] =
                createAnimation(
                        frames,
                        upRow,
                        frameCount,
                        duration,
                        playMode
                );

        result[Direction.LEFT.ordinal()] =
                createAnimation(
                        frames,
                        leftRow,
                        frameCount,
                        duration,
                        playMode
                );

        result[Direction.DOWN.ordinal()] =
                createAnimation(
                        frames,
                        downRow,
                        frameCount,
                        duration,
                        playMode
                );

        result[Direction.RIGHT.ordinal()] =
                createAnimation(
                        frames,
                        rightRow,
                        frameCount,
                        duration,
                        playMode
                );

        return result;
    }

    @SuppressWarnings("unchecked")
    private static Animation<TextureRegion>[] buildSingleRow(
            TextureRegion[][] frames,
            int row,
            int frameCount,
            float duration,
            Animation.PlayMode playMode
    ) {

        Animation<TextureRegion>[] result =
                new Animation[4];

        Animation<TextureRegion> anim =
                createAnimation(
                        frames,
                        row,
                        frameCount,
                        duration,
                        playMode
                );

        for (int i = 0; i < 4; i++) {
            result[i] = anim;
        }

        return result;
    }

    private static Animation<TextureRegion> createAnimation(
            TextureRegion[][] frames,
            int row,
            int frameCount,
            float duration,
            Animation.PlayMode playMode
    ) {

        Array<TextureRegion> regions =
                new Array<>();

        for (int col = 0; col < frameCount; col++) {
            regions.add(frames[row][col]);
        }

        return new Animation<>(
                duration,
                regions,
                playMode
        );
    }

    public static void dispose() {
        animations = null;
    }
}