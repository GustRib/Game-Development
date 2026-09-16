package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

public class TestAnimationFactory {

    public static Map<String, Animation<TextureRegion>[]> createDirectionalAnimations() {
        Map<String, Animation<TextureRegion>[]> animations = new HashMap<>();
        animations.put(AnimationConstants.ANIM_IDLE, createRowAnimations(4, 1, Animation.PlayMode.LOOP));
        animations.put(AnimationConstants.ANIM_WALK, createRowAnimations(4, 1, Animation.PlayMode.LOOP));
        animations.put(AnimationConstants.ANIM_RUN, createRowAnimations(4, 1, Animation.PlayMode.LOOP));
        animations.put(AnimationConstants.ANIM_ATTACK, createRowAnimations(4, 1, Animation.PlayMode.NORMAL));
        animations.put("hurt", createRowAnimations(4, 1, Animation.PlayMode.NORMAL));
        animations.put("death", createRowAnimations(4, 1, Animation.PlayMode.NORMAL));
        return animations;
    }

    public static Map<String, Animation<TextureRegion>[]> createOrcAnimations() {
        return createDirectionalAnimations();
    }

    @SuppressWarnings("unchecked")
    private static Animation<TextureRegion>[] createRowAnimations(int rows, int cols, Animation.PlayMode mode) {
        Animation<TextureRegion>[] anims = new Animation[rows];
        TextureRegion placeholder = new TextureRegion();
        for (int r = 0; r < rows; r++) {
            TextureRegion[] frames = new TextureRegion[cols];
            for (int c = 0; c < cols; c++) {
                frames[c] = placeholder;
            }
            anims[r] = new Animation<>(0.1f, frames);
            anims[r].setPlayMode(mode);
        }
        return anims;
    }
}
