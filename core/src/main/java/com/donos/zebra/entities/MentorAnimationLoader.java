// No pacote com.donos.zebra.entities, arquivo MentorAnimationLoader.java

package com.donos.zebra.entities;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.donos.zebra.MainGame;

public class MentorAnimationLoader {

    private static final String TEXTURE_PATH = "characters/Npcs/npc_village.png"; 
    private static Animation<TextureRegion>[] idleAnimations;

    public static void queueAssets(AssetManager assetManager) {
        assetManager.load(TEXTURE_PATH, Texture.class);
    }


    @SuppressWarnings("unchecked")
    public static java.util.Map<String, Animation<TextureRegion>[]> loadAnimations(AssetManager assetManager) {
        java.util.Map<String, Animation<TextureRegion>[]> animationsMap = new java.util.HashMap<>();

        if (!assetManager.isLoaded(TEXTURE_PATH)) {
            System.err.println("Erro: Spritesheet do NPC Mentor nao carregado no AssetManager!");
            return animationsMap;
        }

        Texture texture = assetManager.get(TEXTURE_PATH, Texture.class);
        TextureRegion[][] tmp = TextureRegion.split(texture, 64, 64);

        // Array com 1 espaço (para a nossa única animação de "Olhar para baixo/Idle")
        idleAnimations = new Animation[1];
        
        // --- ATUALIZAÇÃO AQUI: Pegamos os 4 frames da primeira linha ---
        TextureRegion[] idleFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            idleFrames[i] = tmp[0][i];
        }

        // Criamos a animação. 0.25f significa 4 frames por segundo (um pouco mais lento e natural)
        idleAnimations[0] = new Animation<>(0.25f, idleFrames);
        idleAnimations[0].setPlayMode(Animation.PlayMode.LOOP);

        animationsMap.put(AnimationConstants.ANIM_IDLE, idleAnimations);
        return animationsMap;
    }

    public static void dispose() {
        // Não é necessário dar dispose na textura aqui, o AssetManager cuida disso.
        // Apenas limpamos as referências.
        idleAnimations = null;
    }
}