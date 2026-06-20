package com.donos.zebra.entities;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.donos.zebra.util.SpriteSheetLoader;

public class OrcAnimationLoader {

    // Usaremos apenas o arquivo master unificado
    public static final String ORC_MASTER_PATH = "characters/Enemy/Orc.png";
    public static final String ORC_ATTACK01_PATH = "characters/Enemy/Orc-Attack01.png";
    public static final String ORC_ATTACK02_PATH = "characters/Enemy/Orc-Attack02.png";

    private static Map<String, Animation<TextureRegion>[]> animations;
    private static Texture masterSheet;

    public static void queueAssets(AssetManager assetManager) {
        assetManager.load(ORC_MASTER_PATH, Texture.class);
        assetManager.load(ORC_ATTACK01_PATH, Texture.class);
        assetManager.load(ORC_ATTACK02_PATH, Texture.class);
    }

    public static Map<String, Animation<TextureRegion>[]> loadAnimations(AssetManager assetManager) {
        if (animations != null) {
            return animations;
        }

        animations = new HashMap<>();
        
        // Carrega as texturas usando o AssetManager ou de forma interna
        masterSheet = assetManager != null ? assetManager.get(ORC_MASTER_PATH, Texture.class) : new Texture(Gdx.files.internal(ORC_MASTER_PATH));
        Texture attack01Tex = assetManager != null ? assetManager.get(ORC_ATTACK01_PATH, Texture.class) : new Texture(Gdx.files.internal(ORC_ATTACK01_PATH));
        Texture attack02Tex = assetManager != null ? assetManager.get(ORC_ATTACK02_PATH, Texture.class) : new Texture(Gdx.files.internal(ORC_ATTACK02_PATH));

        // Fatia a folha mestre original para Idle, Run, Hurt e Death
        TextureRegion[][] grid = SpriteSheetLoader.split(masterSheet, 8, 6);

        animations.put(AnimationConstants.ANIM_IDLE, extractRow(grid, 0, 6, AnimationConstants.IDLE_FRAME_DURATION, Animation.PlayMode.LOOP));
        animations.put(AnimationConstants.ANIM_RUN, extractRow(grid, 1, 8, AnimationConstants.RUN_FRAME_DURATION, Animation.PlayMode.LOOP));
        
        // --- EXTRAÇÃO DIRETA DOS ARQUIVOS SEPARADOS (6 colunas, 1 linha) ---
        TextureRegion[][] attack01Grid = SpriteSheetLoader.split(attack01Tex, 6, 1);
        TextureRegion[][] attack02Grid = SpriteSheetLoader.split(attack02Tex, 6, 1);

        // Mapeia o Ataque 1 como o ataque padrão e o Ataque 2 como alternativo
        animations.put(AnimationConstants.ANIM_ATTACK, extractRow(attack01Grid, 0, 6, AnimationConstants.ATTACK_FRAME_DURATION, Animation.PlayMode.NORMAL));
        animations.put("attack_alt", extractRow(attack02Grid, 0, 6, AnimationConstants.ATTACK_FRAME_DURATION, Animation.PlayMode.NORMAL));
        
        animations.put("hurt", extractRow(grid, 4, 4, 0.12f, Animation.PlayMode.NORMAL));
        animations.put("death", extractRow(grid, 5, 4, 0.15f, Animation.PlayMode.NORMAL));

        return animations;
    }

    /**
     * Pega uma linha específica da nossa grade e cria um array de 4 direções com ela
     */
    private static Animation<TextureRegion>[] extractRow(TextureRegion[][] grid, int rowIndex, int frameCount, float frameDuration, Animation.PlayMode mode) {
        int targetDirections = AnimationConstants.DIRECTION_ROWS; // 4 direções
        @SuppressWarnings("unchecked")
        Animation<TextureRegion>[] anims = new Animation[targetDirections];

        // Copia apenas os frames válidos (ativos) daquela linha
        TextureRegion[] frames = new TextureRegion[frameCount];
        for (int c = 0; c < frameCount; c++) {
            frames[c] = grid[rowIndex][c];
        }

        // Replica a animação para as 4 direções para manter o padrão estrutural
        for (int d = 0; d < targetDirections; d++) {
            anims[d] = new Animation<>(frameDuration, frames);
            anims[d].setPlayMode(mode);
        }

        return anims;
    }

    public static void dispose() {
        if (masterSheet != null) {
            masterSheet.dispose();
            masterSheet = null;
        }
        animations = null;
    }
}