package com.donos.zebra.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.donos.zebra.Interaction.Interactable;
import com.donos.zebra.items.ItemRegistry;
import com.donos.zebra.ui.DialogueUI;

public class MentorNpc implements Entity, Interactable {

    private final float x, y;
    private final float radius = 26f;
    private boolean gavePickaxe = false;
    private final DialogueUI dialogueUI;
    
    private final java.util.Map<String, Animation<TextureRegion>[]> animations;
    private float stateTime = 0f;

    // Um polígono vazio ou estático para a hitbox, já que ele não sofre colisões físicas dinâmicas de empurrão
    private final Polygon dummyHitbox;

    public MentorNpc(float x, float y, DialogueUI dialogueUI, java.util.Map<String, Animation<TextureRegion>[]> animations) {        this.x = x;
        this.y = y;
        this.dialogueUI = dialogueUI;
        this.animations = animations;
        
        // Inicializa uma hitbox básica de 16x16 ao redor do NPC apenas para evitar NullPointerException caso o motor tente ler
        this.dummyHitbox = new Polygon(new float[]{0, 0, 16, 0, 16, 16, 0, 16});
        this.dummyHitbox.setPosition(x - 8f, y);
    }

    // --- MÉTODOS DA INTERFACE ENTITY ---

    @Override
    public void update(float delta) {
        stateTime += delta;
    }

    @Override
    public void render(SpriteBatch batch) {
        Animation<TextureRegion>[] idle = animations.get(AnimationConstants.ANIM_IDLE);
        if (idle != null && idle.length > 0 && idle[0] != null) {
            // O stateTime passa o tempo corrido e o LibGDX troca o frame automaticamente!
            TextureRegion currentFrame = idle[0].getKeyFrame(stateTime, true); 
            
            float targetHeight = 48f; 
            float targetWidth = targetHeight; 
            
            batch.draw(
                currentFrame, 
                x - targetWidth / 2f, 
                y,                    
                targetWidth, 
                targetHeight
            );
        }
    }

    @Override
    public void takeDamage(float amount) {
        // Mentor é imortal e pacífico, ignora dano
    }

    @Override
    public boolean isDead() {
        return false; // Nunca morre
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public Polygon getHitbox() {
        return dummyHitbox;
    }

    @Override
    public float getCurrentHealth() {
        return 100f; // Valor simbólico para a barra de vida não bugar caso tente renderizar
    }

    @Override
    public void dispose() {
        // Recursos pesados de textura são limpos pelo MentorAnimationLoader / AssetManager
    }

    // --- MÉTODOS DA INTERFACE INTERACTABLE ---

    @Override
    public void onInteract(Player player) {
        if (!gavePickaxe) {
            dialogueUI.showText("Mentor: Pegue esta picareta de pedra.\nEla serve para extrair cobre!");
            
            // Adiciona o item ao inventário usando o ItemRegistry
            player.getInventory().addItem(ItemRegistry.STONE_PICKAXE, 1);
            player.getInventory().printInventory();
            
            gavePickaxe = true;
        } else {
            dialogueUI.showText("Mentor: Va ate a mina de cobre ao Norte!");
        }
    }

    @Override
    public float getInteractionRadius() {
        return radius;
    }

    @Override
    public String getPromptText() {
        return "[E] Falar com o Mentor";
    }
}