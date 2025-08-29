package com.donos.zebra.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.HashMap;
import java.util.Map;

public class PlayerAnimationLoader {

    private static Map<String, Animation<TextureRegion>[]> animations;
    private static Map<String, Texture> sheets; // para dar dispose depois

    public static Map<String, Animation<TextureRegion>[]> loadAnimations() {
        animations = new HashMap<>();
        sheets = new HashMap<>();

        animations.put("idle", loadDirectional("characters/Player1/Swordsman_lvl1_Idle_with_shadow.png", 10, 4, 0.18f, Animation.PlayMode.LOOP));
        animations.put("walk", loadDirectional("characters/Player1/Swordsman_lvl1_Walk_with_shadow.png", 8, 4, 0.10f, Animation.PlayMode.LOOP));
        animations.put("attack", loadDirectional("characters/Player1/Swordsman_lvl1_Attack_with_shadow.png", 6, 4, 0.07f, Animation.PlayMode.NORMAL));

        return animations;
    }

    private static Animation<TextureRegion>[] loadDirectional(String path, int cols, int rows, float frameDuration, Animation.PlayMode mode) {
        Texture sheet = new Texture(Gdx.files.internal(path));
        sheets.put(path, sheet);

        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / cols, sheet.getHeight() / rows);

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
            for (Texture t : sheets.values()) t.dispose();
        }
    }
}
